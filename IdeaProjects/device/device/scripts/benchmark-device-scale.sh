#!/usr/bin/env bash

set -euo pipefail

show_usage() {
    printf '%s\n' \
        'Usage: bash scripts/benchmark-device-scale.sh --confirm-isolated [options]' \
        '  --confirm-isolated  Required acknowledgement: use disposable Testcontainers MySQL only.' \
        '  --datasets LIST     Comma-separated row counts, default: 1000,10000,100000.' \
        '  --warmups N         Warm-ups per read/export operation, default: 1.' \
        '  --repetitions N     Recorded runs per operation and dataset, default: 3.' \
        '  --import-repetitions N  Recorded imports per dataset, default: 1.' \
        '  --output PATH       Markdown report, default: target/benchmarks/device-scale-baseline.md.' \
        '  --force             Replace an existing report path.' \
        '' \
        'The script unsets external datasource variables, creates synthetic CSV files in a' \
        'temporary directory, and runs only DeviceScaleBenchmarkIT against jdbc:tc:mysql.'
}

confirmed=false
datasets="1000,10000,100000"
warmups=1
repetitions=3
import_repetitions=1
output_path="target/benchmarks/device-scale-baseline.md"
force=false

while (($# > 0)); do
    case "$1" in
        --confirm-isolated)
            confirmed=true
            shift
            ;;
        --datasets)
            datasets="${2:-}"
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
        --import-repetitions)
            import_repetitions="${2:-}"
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

if [[ ! "${datasets}" =~ ^[0-9]+(,[0-9]+)*$ ]]; then
    printf '%s\n' '--datasets must be comma-separated positive integers.' >&2
    exit 2
fi

if [[ ! "${warmups}" =~ ^[1-9][0-9]*$ ]] \
        || [[ ! "${repetitions}" =~ ^[1-9][0-9]*$ ]] \
        || [[ ! "${import_repetitions}" =~ ^[1-9][0-9]*$ ]]; then
    printf '%s\n' '--warmups, --repetitions and --import-repetitions must be positive integers.' >&2
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

dataset_dir="$(mktemp -d "${TMPDIR:-/tmp}/device-benchmark.XXXXXX")"
cleanup() {
    rm -rf -- "${dataset_dir}"
}
trap cleanup EXIT

IFS=',' read -r -a dataset_values <<< "${datasets}"
for rows in "${dataset_values[@]}"; do
    if ((rows < 1 || rows > 100000)); then
        printf 'Dataset row count must be from 1 to 100000: %s\n' "${rows}" >&2
        exit 2
    fi
    bash scripts/generate-device-csv.sh \
        --rows "${rows}" \
        --output "${dataset_dir}/devices-${rows}.csv"
done

output_dir="$(dirname -- "${output_path}")"
mkdir -p -- "${output_dir}"
output_path="$(cd -- "${output_dir}" && pwd)/$(basename -- "${output_path}")"
commit="$(git rev-parse HEAD)"

printf 'Running isolated benchmark for datasets %s; report: %s\n' "${datasets}" "${output_path}"

(
    unset DB_URL DB_USERNAME DB_PASSWORD
    unset SPRING_DATASOURCE_URL SPRING_DATASOURCE_USERNAME SPRING_DATASOURCE_PASSWORD
    TEST_ADMIN_PASSWORD='benchmark-only-password' \
    TEST_JWT_SIGNER_KEY='benchmark-only-jwt-signer-key-with-at-least-thirty-two-characters' \
    bash ./mvnw \
        -Dtest=DeviceScaleBenchmarkIT \
        -DargLine='-Xms128m -Xmx512m' \
        -Ddevice.benchmark.enabled=true \
        -Ddevice.benchmark.dataset-dir="${dataset_dir}" \
        -Ddevice.benchmark.output="${output_path}" \
        -Ddevice.benchmark.datasets="${datasets}" \
        -Ddevice.benchmark.warmups="${warmups}" \
        -Ddevice.benchmark.repetitions="${repetitions}" \
        -Ddevice.benchmark.import-repetitions="${import_repetitions}" \
        -Ddevice.benchmark.commit="${commit}" \
        test
)

printf 'Benchmark completed: %s\n' "${output_path}"
