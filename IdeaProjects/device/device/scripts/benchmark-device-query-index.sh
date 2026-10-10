#!/usr/bin/env bash

set -euo pipefail

show_usage() {
    printf '%s\n' \
        'Usage: bash scripts/benchmark-device-query-index.sh --confirm-isolated [options]' \
        '  --confirm-isolated  Required acknowledgement: use disposable Testcontainers MySQL only.' \
        '  --rows N            Synthetic device rows, default: 100000; maximum: 100000.' \
        '  --warmups N         Warm-ups per operation/variant/cycle, default: 2.' \
        '  --repetitions N     Recorded runs per operation/variant/cycle, default: 3.' \
        '  --cycles N          Alternating baseline/indexed cycles, default: 2.' \
        '  --output PATH       Markdown report, default: target/benchmarks/device-query-index-comparison.md.' \
        '  --force             Replace an existing report path.' \
        '' \
        'The script unsets external datasource variables, generates one synthetic CSV, and' \
        'runs only DeviceQueryIndexBenchmarkIT against disposable jdbc:tc:mysql.'
}

confirmed=false
rows=100000
warmups=2
repetitions=3
cycles=2
output_path="target/benchmarks/device-query-index-comparison.md"
force=false

while (($# > 0)); do
    case "$1" in
        --confirm-isolated)
            confirmed=true
            shift
            ;;
        --rows)
            rows="${2:-}"
            shift 2
            ;;
        --warmups)
            warmups="${2:-}"
            shift 2
            ;;
        --repetitions)
            repetitions="${2:-}"
            shift 2
            ;;
        --cycles)
            cycles="${2:-}"
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

if [[ ! "${rows}" =~ ^[1-9][0-9]*$ ]] || ((rows > 100000)); then
    printf '%s\n' '--rows must be an integer from 1 to 100000.' >&2
    exit 2
fi

if [[ ! "${warmups}" =~ ^[1-9][0-9]*$ ]] \
        || [[ ! "${repetitions}" =~ ^[1-9][0-9]*$ ]] \
        || [[ ! "${cycles}" =~ ^[1-9][0-9]*$ ]]; then
    printf '%s\n' '--warmups, --repetitions and --cycles must be positive integers.' >&2
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

dataset_dir="$(mktemp -d "${TMPDIR:-/tmp}/device-index-benchmark.XXXXXX")"
cleanup() {
    rm -rf -- "${dataset_dir}"
}
trap cleanup EXIT

dataset_path="${dataset_dir}/devices-${rows}.csv"
bash scripts/generate-device-csv.sh --rows "${rows}" --output "${dataset_path}"

output_dir="$(dirname -- "${output_path}")"
mkdir -p -- "${output_dir}"
output_path="$(cd -- "${output_dir}" && pwd)/$(basename -- "${output_path}")"
commit="$(git rev-parse HEAD)"
working_tree="clean"
if [[ -n "$(git status --porcelain --untracked-files=normal)" ]]; then
    working_tree="dirty (uncommitted changes present)"
fi

printf 'Running isolated index comparison for %s rows; report: %s\n' "${rows}" "${output_path}"

(
    unset DB_URL DB_USERNAME DB_PASSWORD
    unset SPRING_DATASOURCE_URL SPRING_DATASOURCE_USERNAME SPRING_DATASOURCE_PASSWORD
    TEST_ADMIN_PASSWORD='benchmark-only-password' \
    TEST_JWT_SIGNER_KEY='benchmark-only-jwt-signer-key-with-at-least-thirty-two-characters' \
    bash ./mvnw \
        -Dtest=DeviceQueryIndexBenchmarkIT \
        -DargLine='-Xms128m -Xmx512m' \
        -Ddevice.index-benchmark.enabled=true \
        -Ddevice.index-benchmark.dataset="${dataset_path}" \
        -Ddevice.index-benchmark.output="${output_path}" \
        -Ddevice.index-benchmark.rows="${rows}" \
        -Ddevice.index-benchmark.warmups="${warmups}" \
        -Ddevice.index-benchmark.repetitions="${repetitions}" \
        -Ddevice.index-benchmark.cycles="${cycles}" \
        -Ddevice.index-benchmark.commit="${commit}" \
        -Ddevice.index-benchmark.working-tree="${working_tree}" \
        test
)

printf 'Index comparison completed: %s\n' "${output_path}"
