#!/usr/bin/env bash

set -euo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
project_root="$(cd -- "${script_dir}/.." && pwd)"
mode="${1:-up}"
compose_args=(-f compose.yaml -f compose.ide.yaml)

cd "${project_root}"

check_local_env() {
    if [[ ! -f .env ]]; then
        printf 'Missing .env. Copy .env.example to .env and add local-only values first.\n' >&2
        return 1
    fi

    local key
    for key in LOCAL_DB_PASSWORD LOCAL_ADMIN_PASSWORD LOCAL_JWT_SIGNER_KEY; do
        if ! grep -Eq "^${key}=.+" .env; then
            printf 'Missing or empty local setting in .env: %s\n' "${key}" >&2
            return 1
        fi
    done
}

show_usage() {
    printf '%s\n' \
        'Usage: bash scripts/dev-ide.sh [up|status|down|full]' \
        '  up      Stop containerized app services; start MySQL and Mailpit for IntelliJ backend.' \
        '  status  Show the current Compose service state.' \
        '  down    Stop local MySQL and Mailpit without deleting the database volume.' \
        '  full    Restore the full four-service Compose stack.'
}

case "${mode}" in
    up)
        check_local_env
        docker compose stop backend frontend
        docker compose "${compose_args[@]}" up -d --wait mysql mailpit
        printf '%s\n' \
            'IDE dependencies are ready.' \
            'Select "Device Backend (IDE)" in IntelliJ and run or debug it.' \
            'Start the frontend separately with: cd frontend && npm run dev'
        ;;
    status)
        docker compose "${compose_args[@]}" ps
        ;;
    down)
        docker compose "${compose_args[@]}" stop mysql mailpit
        ;;
    full)
        check_local_env
        docker compose up --build -d --wait
        ;;
    -h|--help|help)
        show_usage
        ;;
    *)
        printf 'Unknown mode: %s\n' "${mode}" >&2
        show_usage >&2
        exit 2
        ;;
esac
