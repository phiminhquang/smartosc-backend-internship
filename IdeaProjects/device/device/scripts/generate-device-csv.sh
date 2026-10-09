#!/usr/bin/env bash

set -euo pipefail

show_usage() {
    printf '%s\n' \
        'Usage: bash scripts/generate-device-csv.sh --rows N --output PATH [--force]' \
        '  --rows N      Number of synthetic rows, from 1 to 1000000.' \
        '  --output PATH CSV file to create. Parent directory must already exist.' \
        '  --force       Replace PATH if it already exists.'
}

rows=""
output_path=""
force=false

while (($# > 0)); do
    case "$1" in
        --rows)
            rows="${2:-}"
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

if [[ ! "${rows}" =~ ^[0-9]+$ ]] || ((rows < 1 || rows > 1000000)); then
    printf '%s\n' '--rows must be an integer from 1 to 1000000.' >&2
    exit 2
fi

if [[ -z "${output_path}" ]]; then
    printf '%s\n' '--output is required.' >&2
    exit 2
fi

output_dir="$(dirname -- "${output_path}")"
if [[ ! -d "${output_dir}" ]]; then
    printf 'Output directory does not exist: %s\n' "${output_dir}" >&2
    exit 2
fi

if [[ -e "${output_path}" && "${force}" != true ]]; then
    printf 'Output already exists; pass --force to replace it: %s\n' "${output_path}" >&2
    exit 2
fi

temporary_path="$(mktemp "${output_dir}/.device-scale.XXXXXX")"
cleanup() {
    rm -f -- "${temporary_path}"
}
trap cleanup EXIT

awk -v rows="${rows}" 'BEGIN {
    categories[0] = "LAPTOP"
    categories[1] = "MONITOR"
    categories[2] = "PHONE"
    print "category,name,model,description"
    for (i = 1; i <= rows; i++) {
        category = categories[(i - 1) % 3]
        printf "%s,Scale Device %07d,Model %03d,Synthetic scale-test row %07d\n", \
            category, i, ((i - 1) % 100) + 1, i
    }
}' > "${temporary_path}"

mv -- "${temporary_path}" "${output_path}"
trap - EXIT
printf 'Created %s synthetic rows at %s\n' "${rows}" "${output_path}"
