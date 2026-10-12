#!/usr/bin/env bash

set -euo pipefail

show_usage() {
    printf '%s\n' \
        'Usage: bash scripts/benchmark-password-reset-timing.sh --confirm-isolated [options]' \
        '  --confirm-isolated  Required acknowledgement: use disposable Testcontainers MySQL only.' \
        '  --samples N         Recorded existing/missing email pairs, default: 50.' \
        '  --warmups N         Unrecorded warm-up pairs, default: 10.' \
        '  --output PATH       Markdown report, default: target/benchmarks/password-reset-timing.md.' \
        '  --force             Replace an existing report path.' \
        '' \
        'The benchmark unsets external datasource variables and runs only' \
        'PasswordResetTimingBenchmarkIT against disposable jdbc:tc:mysql.'
}

confirmed=false
samples=50
warmups=10
output_path="target/benchmarks/password-reset-timing.md"
force=false

while (($# > 0)); do
    case "$1" in
        --confirm-isolated)
            confirmed=true
            shift
            ;;
        --samples)
            samples="${2:-}"
            shift 2
            ;;
        --warmups)
            warmups="${2:-}"
            shift 2
            ;;
        --output)
            output_path="${2:-}"
            shift 2
            ;;
        --force)
            force=true
            shift
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        *)
            printf 'Unknown argument: %s\n' "$1" >&2
            show_usage >&2
            exit 2
            ;;
    esac
done

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
project_root="$(cd -- "${script_dir}/.." && pwd)"
cd "${project_root}"

if [[ "${confirmed}" != true ]]; then
    printf '%s\n' 'Refusing to run without --confirm-isolated.' >&2
    exit 2
fi

if [[ ! "${samples}" =~ ^[1-9][0-9]*$ ]] || ((samples > 200)); then
    printf '%s\n' '--samples must be an integer from 1 to 200.' >&2
    exit 2
fi

if [[ ! "${warmups}" =~ ^[0-9]+$ ]] || ((warmups > 50)); then
    printf '%s\n' '--warmups must be an integer from 0 to 50.' >&2
    exit 2
fi

if [[ -e "${output_path}" && "${force}" != true ]]; then
    printf 'Output already exists; pass --force to replace it: %s\n' "${output_path}" >&2
    exit 2
fi

if ! command -v docker >/dev/null 2>&1 || ! docker info >/dev/null 2>&1; then
    printf '%s\n' 'Docker daemon must be installed and reachable for the isolated benchmark.' >&2
    exit 1
fi

output_dir="$(dirname -- "${output_path}")"
mkdir -p -- "${output_dir}"
output_path="$(cd -- "${output_dir}" && pwd)/$(basename -- "${output_path}")"
commit="$(git rev-parse HEAD)"
working_tree="clean"
if [[ -n "$(git status --porcelain --untracked-files=normal)" ]]; then
    working_tree="dirty (uncommitted changes present)"
fi

printf 'Running isolated password-reset timing comparison; report: %s\n' "${output_path}"

(
    unset DB_URL DB_USERNAME DB_PASSWORD
    unset SPRING_DATASOURCE_URL SPRING_DATASOURCE_USERNAME SPRING_DATASOURCE_PASSWORD
    TEST_ADMIN_PASSWORD='benchmark-only-password' \
    TEST_JWT_SIGNER_KEY='benchmark-only-jwt-signer-key-with-at-least-thirty-two-characters' \
    bash ./mvnw \
        -Dtest=PasswordResetTimingBenchmarkIT \
        -DargLine='-Xms128m -Xmx512m' \
        -Dpassword-reset.timing.enabled=true \
        -Dpassword-reset.timing.output="${output_path}" \
        -Dpassword-reset.timing.samples="${samples}" \
        -Dpassword-reset.timing.warmups="${warmups}" \
        -Dpassword-reset.timing.commit="${commit}" \
        -Dpassword-reset.timing.working-tree="${working_tree}" \
        test
)

printf 'Password-reset timing comparison completed: %s\n' "${output_path}"
