#!/usr/bin/env bash

set -euo pipefail
umask 077

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
project_root="$(cd -- "${script_dir}/.." && pwd)"
git_root="$(git -C "${project_root}" rev-parse --show-toplevel 2>/dev/null || printf '%s' "${project_root}")"

show_usage() {
    printf '%s\n' \
        'Backup:' \
        '  bash scripts/demo-db-backup-restore.sh backup --confirm-demo-readonly --config /outside/source.cnf --database device_demo --output /outside/device-demo-YYYYMMDD.sql' \
        '' \
        'Restore into an existing EMPTY isolated database:' \
        '  bash scripts/demo-db-backup-restore.sh restore --confirm-isolated-restore --config /outside/target.cnf --database device_demo_restore_YYYYMMDD --input /outside/device-demo-YYYYMMDD.sql' \
        '' \
        'The MySQL option file must be mode 600, outside Git, and contain [client], credentials, ssl-mode=VERIFY_IDENTITY and an ssl-ca path.'
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" || $# -eq 0 ]]; then
    show_usage
    exit 0
fi

action="$1"
shift
confirmation=''
config_file=''
database=''
output_file=''
input_file=''

while [[ $# -gt 0 ]]; do
    case "$1" in
        --confirm-demo-readonly|--confirm-isolated-restore)
            confirmation="$1"
            shift
            ;;
        --config)
            config_file="${2:-}"
            shift 2
            ;;
        --database)
            database="${2:-}"
            shift 2
            ;;
        --output)
            output_file="${2:-}"
            shift 2
            ;;
        --input)
            input_file="${2:-}"
            shift 2
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

case "${action}" in
    backup)
        [[ "${confirmation}" == '--confirm-demo-readonly' ]] || {
            printf '%s\n' 'Backup requires the exact --confirm-demo-readonly flag.' >&2
            exit 2
        }
        [[ "${database}" == 'device_demo' ]] || {
            printf '%s\n' 'Backup is restricted to the documented device_demo database.' >&2
            exit 2
        }
        [[ -n "${output_file}" ]] || { printf '%s\n' '--output is required for backup.' >&2; exit 2; }
        ;;
    restore)
        [[ "${confirmation}" == '--confirm-isolated-restore' ]] || {
            printf '%s\n' 'Restore requires the exact --confirm-isolated-restore flag.' >&2
            exit 2
        }
        [[ "${database}" =~ ^device_demo_restore_[A-Za-z0-9_]+$ ]] || {
            printf '%s\n' 'Restore target must match device_demo_restore_<suffix>; device_demo is never a restore target.' >&2
            exit 2
        }
        [[ -n "${input_file}" ]] || { printf '%s\n' '--input is required for restore.' >&2; exit 2; }
        ;;
    *)
        printf 'Unknown action: %s\n' "${action}" >&2
        show_usage >&2
        exit 2
        ;;
esac

[[ "${config_file}" == /* && -f "${config_file}" ]] || {
    printf '%s\n' '--config must be an existing absolute path.' >&2
    exit 2
}

config_path="$(realpath -m -- "${config_file}")"
case "${config_path}" in
    "${git_root}"|"${git_root}"/*)
        printf '%s\n' 'MySQL option files must stay outside the Git repository.' >&2
        exit 2
        ;;
esac

config_mode="$(stat -c '%a' -- "${config_path}")"
config_mode_value=$((8#${config_mode}))
if (( (config_mode_value & 077) != 0 )); then
    printf '%s\n' 'MySQL option file must not be readable or writable by group/others; use chmod 600.' >&2
    exit 2
fi

grep -Eq '^\[client\][[:space:]]*$' "${config_path}" || { printf '%s\n' 'MySQL option file needs a [client] section.' >&2; exit 2; }
grep -Eiq '^[[:space:]]*ssl-mode[[:space:]]*=[[:space:]]*VERIFY_IDENTITY[[:space:]]*$' "${config_path}" || {
    printf '%s\n' 'MySQL option file must enforce ssl-mode=VERIFY_IDENTITY.' >&2
    exit 2
}
grep -Eiq '^[[:space:]]*ssl-ca[[:space:]]*=' "${config_path}" || {
    printf '%s\n' 'MySQL option file must provide ssl-ca.' >&2
    exit 2
}
ca_path="$(awk -F '=' '
    tolower($1) ~ /^[[:space:]]*ssl-ca[[:space:]]*$/ {
        value = substr($0, index($0, "=") + 1)
        gsub(/^[[:space:]]+|[[:space:]]+$/, "", value)
        gsub(/^"|"$/, "", value)
        print value
    }
' "${config_path}" | tail -n 1)"
[[ "${ca_path}" == /* && -f "${ca_path}" ]] || {
    printf '%s\n' 'ssl-ca must point to an existing absolute certificate path.' >&2
    exit 2
}

command -v mysql >/dev/null 2>&1 || { printf '%s\n' 'mysql client is required.' >&2; exit 1; }

connected_database="$(mysql --defaults-extra-file="${config_path}" --batch --skip-column-names \
    --database="${database}" --execute='SELECT DATABASE()')"
[[ "${connected_database}" == "${database}" ]] || { printf '%s\n' 'Connected database does not match the requested database.' >&2; exit 1; }

if [[ "${action}" == 'backup' ]]; then
    command -v mysqldump >/dev/null 2>&1 || { printf '%s\n' 'mysqldump client is required.' >&2; exit 1; }
    [[ "${output_file}" == /* ]] || { printf '%s\n' 'Backup output must be an absolute path.' >&2; exit 2; }
    output_path="$(realpath -m -- "${output_file}")"
    case "${output_path}" in
        "${git_root}"|"${git_root}"/*)
            printf '%s\n' 'Backup output must stay outside the Git repository.' >&2
            exit 2
            ;;
    esac
    [[ "${output_path}" == *.sql ]] || { printf '%s\n' 'Backup output must end in .sql.' >&2; exit 2; }
    [[ -d "$(dirname -- "${output_path}")" ]] || { printf '%s\n' 'Backup output directory does not exist.' >&2; exit 2; }
    [[ ! -e "${output_path}" && ! -e "${output_path}.sha256" ]] || {
        printf '%s\n' 'Refusing to overwrite an existing backup or checksum.' >&2
        exit 2
    }

    partial_output="$(mktemp "$(dirname -- "${output_path}")/.device-demo-backup.XXXXXX.sql")"
    cleanup_partial() {
        rm -f -- "${partial_output}"
    }
    trap cleanup_partial EXIT

    mysqldump --defaults-extra-file="${config_path}" --single-transaction --quick \
        --set-gtid-purged=OFF --no-tablespaces --hex-blob --default-character-set=utf8mb4 \
        --result-file="${partial_output}" "${database}"
    chmod 600 "${partial_output}"
    [[ -s "${partial_output}" ]] && grep -Fq 'flyway_schema_history' "${partial_output}" || {
        printf '%s\n' 'Backup is empty or does not contain Flyway history.' >&2
        exit 1
    }
    if ! ln -- "${partial_output}" "${output_path}"; then
        printf '%s\n' 'Could not finalize backup without overwriting a file.' >&2
        exit 1
    fi
    rm -f -- "${partial_output}"
    trap - EXIT
    (
        cd -- "$(dirname -- "${output_path}")"
        sha256sum -- "$(basename -- "${output_path}")" > "$(basename -- "${output_path}").sha256"
    )
    chmod 600 "${output_path}.sha256"
    printf 'Backup created with SHA-256 checksum: %s\n' "${output_path}"
    printf '%s\n' 'Treat both files as sensitive because the SQL dump contains application data.'
    exit 0
fi

[[ "${input_file}" == /* && -f "${input_file}" ]] || { printf '%s\n' 'Restore input must be an existing absolute .sql path.' >&2; exit 2; }
input_path="$(realpath -m -- "${input_file}")"
case "${input_path}" in
    "${git_root}"|"${git_root}"/*)
        printf '%s\n' 'Restore input must stay outside the Git repository.' >&2
        exit 2
        ;;
esac
[[ "${input_path}" == *.sql ]] || { printf '%s\n' 'Restore input must end in .sql.' >&2; exit 2; }
[[ -f "${input_path}.sha256" ]] || { printf '%s\n' 'Restore requires the matching .sha256 file.' >&2; exit 2; }
checksum_line_count="$(wc -l < "${input_path}.sha256")"
checksum_filename="$(awk 'NR == 1 { print $2 }' "${input_path}.sha256")"
[[ "${checksum_line_count}" == '1' && "${checksum_filename}" == "$(basename -- "${input_path}")" ]] || {
    printf '%s\n' 'Checksum file must contain exactly the matching backup filename.' >&2
    exit 2
}
(
    cd -- "$(dirname -- "${input_path}")"
    sha256sum --check --status "$(basename -- "${input_path}").sha256"
) || { printf '%s\n' 'Backup checksum verification failed.' >&2; exit 1; }

if grep -Eiq '^[[:space:]]*(CREATE|DROP)[[:space:]]+DATABASE|^[[:space:]]*USE[[:space:]]+' "${input_path}"; then
    printf '%s\n' 'Dump contains database-selection or database-destruction statements; refusing restore.' >&2
    exit 1
fi

target_table_count="$(mysql --defaults-extra-file="${config_path}" --batch --skip-column-names \
    --database="${database}" --execute='SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE()')"
[[ "${target_table_count}" == '0' ]] || {
    printf '%s\n' 'Restore target is not empty. Create a fresh isolated database instead of overwriting it.' >&2
    exit 1
}

if ! mysql --defaults-extra-file="${config_path}" --database="${database}" < "${input_path}"; then
    printf '%s\n' 'Restore failed. Do not retry into the partially restored database; drop/recreate only this isolated target after review.' >&2
    exit 1
fi

verification="$(mysql --defaults-extra-file="${config_path}" --batch --skip-column-names \
    --database="${database}" --execute="SELECT CONCAT('tables=', COUNT(*)) FROM information_schema.tables WHERE table_schema = DATABASE(); SELECT CONCAT('flyway=', COUNT(*)) FROM flyway_schema_history WHERE success = 1; SELECT CONCAT('users=', COUNT(*)) FROM users; SELECT CONCAT('devices=', COUNT(*)) FROM devices;")"
printf '%s\n' 'Isolated restore completed. Non-sensitive verification counts:'
printf '%s\n' "${verification}"
