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

test_password_reset_backend() {
    local test_classes
    test_classes="PasswordResetServiceImplTest,AuthenticationimplTest,JwtTokenVersionValidatorTest,AuthenticationControllerSecurityTest"

    local agent_jar=""
    local agent_root="${HOME}/.m2/repository/net/bytebuddy/byte-buddy-agent"

    if [[ -d "${agent_root}" ]]; then
        agent_jar="$(find "${agent_root}" -type f -name 'byte-buddy-agent-*.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' -print | sort -V | tail -n 1)"
    fi

    if [[ -n "${agent_jar}" ]]; then
        bash ./mvnw "-DargLine=-javaagent:${agent_jar}" "-Dtest=${test_classes}" test
    else
        bash ./mvnw "-Dtest=${test_classes}" test
    fi
}

check_frontend() {
    (
        cd frontend
        npm run lint
        npm run build
    )
}

show_usage() {
    printf '%s\n' \
        'Usage: bash scripts/verify.sh [safe|docs|backend|frontend|password-reset]' \
        '  safe           Documentation, backend compile, targeted password-reset tests, frontend lint/build.' \
        '  docs           Required documentation and git diff whitespace checks.' \
        '  backend        Backend compile only; does not run datasource-backed application tests.' \
        '  frontend       Frontend lint and production build.' \
        '  password-reset Targeted backend tests for password reset, JWT version and endpoint security.'
}

case "${mode}" in
    safe)
        check_docs
        compile_backend
        test_password_reset_backend
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
    password-reset)
        test_password_reset_backend
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
