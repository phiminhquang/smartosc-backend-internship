#!/usr/bin/env bash

set -euo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
project_root="$(cd -- "${script_dir}/.." && pwd)"
mode="${1:-safe}"

cd "${project_root}"

check_docs() {
    local required_files=(
        "PROJECT.md"
        "AGENTS.md"
        "AI-HANDOFF.md"
        "specs/README.md"
        "specs/_template/spec.md"
        "specs/_template/plan.md"
        "specs/_template/tasks.md"
        "specs/_template/verification.md"
        "docs/decisions/README.md"
        "docs/decisions/000-template.md"
    )

    local file
    for file in "${required_files[@]}"; do
        if [[ ! -s "${file}" ]]; then
            printf 'Required documentation is missing or empty: %s\n' "${file}" >&2
            return 1
        fi
    done

    git diff --check -- .
    printf 'Documentation and diff checks passed.\n'
}

compile_backend() {
    bash ./mvnw -DskipTests compile
}

run_backend_tests() {
    local agent_jar=""
    local agent_root="${HOME}/.m2/repository/net/bytebuddy/byte-buddy-agent"
    local -a maven_args=("$@")

    if [[ -d "${agent_root}" ]]; then
        agent_jar="$(find "${agent_root}" -type f -name 'byte-buddy-agent-*.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' -print | sort -V | tail -n 1)"
    fi

    if [[ -n "${agent_jar}" ]]; then
        maven_args=("-DargLine=-javaagent:${agent_jar}" "${maven_args[@]}")
    fi

    bash ./mvnw "${maven_args[@]}" test
}

test_password_reset_backend() {
    local test_classes
    test_classes="PasswordResetServiceImplTest,AuthenticationimplTest,JwtTokenVersionValidatorTest,AuthenticationControllerSecurityTest"

    run_backend_tests "-Dtest=${test_classes}"
}

test_safe_backend() {
    local test_classes
    test_classes="PasswordResetServiceImplTest,AuthenticationimplTest,JwtTokenVersionValidatorTest,AuthenticationControllerSecurityTest,DeviceFileServiceImplTest"

    run_backend_tests "-Dtest=${test_classes}"
}

test_data_generator() {
    local generator_dir
    local generator_file
    local line_count

    generator_dir="$(mktemp -d "${TMPDIR:-/tmp}/device-generator.XXXXXX")"
    generator_file="${generator_dir}/devices.csv"

    bash scripts/generate-device-csv.sh --rows 7 --output "${generator_file}" >/dev/null
    line_count="$(wc -l < "${generator_file}")"

    if [[ "${line_count}" -ne 8 ]]; then
        printf 'Data generator produced %s lines; expected 8.\n' "${line_count}" >&2
        rm -rf -- "${generator_dir}"
        return 1
    fi

    if bash scripts/generate-device-csv.sh --rows 7 --output "${generator_file}" >/dev/null 2>&1; then
        printf '%s\n' 'Data generator overwrote an existing file without --force.' >&2
        rm -rf -- "${generator_dir}"
        return 1
    fi

    rm -rf -- "${generator_dir}"
    printf '%s\n' 'Synthetic device CSV generator checks passed.'
}

test_benchmark_guard() {
    bash scripts/benchmark-device-scale.sh --help >/dev/null

    if bash scripts/benchmark-device-scale.sh >/dev/null 2>&1; then
        printf '%s\n' 'Device scale benchmark ran without --confirm-isolated.' >&2
        return 1
    fi

    printf '%s\n' 'Device scale benchmark isolation guard checks passed.'
}

check_frontend() {
    (
        cd frontend
        node --test \
            test/auth-services.test.mjs \
            test/security-referrer.test.mjs \
            test/url-token-purge.test.mjs
        npm run lint
        npm run build
    )
}

test_frontend_compose_smoke() {
    local required_urls=(
        "http://127.0.0.1:8080/v3/api-docs"
        "http://127.0.0.1:8025/api/v1/info"
        "http://127.0.0.1:5173/"
    )

    local url
    for url in "${required_urls[@]}"; do
        if ! curl --fail --silent --show-error --output /dev/null "${url}"; then
            printf 'Required Compose endpoint is not reachable: %s\n' "${url}" >&2
            return 1
        fi
    done

    (
        cd frontend
        FRONTEND_URL="http://127.0.0.1:5173" \
            BACKEND_URL="http://127.0.0.1:8080" \
            MAILPIT_URL="http://127.0.0.1:8025" \
            npm run test:e2e
    )
}

test_isolated_backend() {
    if ! command -v docker >/dev/null 2>&1 || ! docker info >/dev/null 2>&1; then
        printf 'Docker daemon must be installed and reachable by this user for isolated MySQL Testcontainers tests.\n' >&2
        return 1
    fi

    (
        unset DB_URL DB_USERNAME DB_PASSWORD
        unset SPRING_DATASOURCE_URL SPRING_DATASOURCE_USERNAME SPRING_DATASOURCE_PASSWORD
        run_backend_tests
    )
}

show_usage() {
    printf '%s\n' \
        'Usage: bash scripts/verify.sh [safe|docs|backend|frontend|frontend-e2e|password-reset|integration]' \
        '  safe           Documentation, backend compile, targeted password-reset tests, frontend lint/build.' \
        '  docs           Required documentation and git diff whitespace checks.' \
        '  backend        Backend compile only; does not run datasource-backed application tests.' \
        '  frontend       Frontend contract/security tests, lint and production build.' \
        '  frontend-e2e   Playwright Chromium login/forgot/reset against the healthy full Compose stack.' \
        '  password-reset Targeted backend tests for password reset, JWT version and endpoint security.' \
        '  integration    Full backend tests using disposable MySQL Testcontainers; requires Docker.'
}

case "${mode}" in
    safe)
        check_docs
        compile_backend
        test_safe_backend
        test_data_generator
        test_benchmark_guard
        check_frontend
        ;;
    docs)
        check_docs
        ;;
    backend)
        compile_backend
        ;;
    frontend)
        check_frontend
        ;;
    frontend-e2e)
        test_frontend_compose_smoke
        ;;
    password-reset)
        test_password_reset_backend
        ;;
    integration)
        test_isolated_backend
        ;;
    -h|--help|help)
        show_usage
        ;;
    *)
        printf 'Unknown verification mode: %s\n' "${mode}" >&2
        show_usage >&2
        exit 2
        ;;
esac
