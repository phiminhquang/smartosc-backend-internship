#!/usr/bin/env bash

set -euo pipefail
set +x

show_usage() {
    printf '%s\n' \
        'Usage: RENDER_DEPLOY_HOOK_URL=... RENDER_API_TOKEN=... RENDER_SERVICE_ID=srv-... RENDER_COMMIT_SHA=<40 hex> bash scripts/render-deploy-and-wait.sh --confirm-deploy' \
        '' \
        'Optional environment:' \
        '  RENDER_SERVICE_LABEL              Non-secret label for log output.' \
        '  RENDER_DEPLOY_TIMEOUT_SECONDS     Default: 1200.' \
        '  RENDER_POLL_INTERVAL_SECONDS      Default: 10.'
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    show_usage
    exit 0
fi

if [[ "${1:-}" != "--confirm-deploy" || "$#" -ne 1 ]]; then
    printf '%s\n' 'Refusing to trigger Render without the exact --confirm-deploy flag.' >&2
    show_usage >&2
    exit 2
fi

hook_url="${RENDER_DEPLOY_HOOK_URL:-}"
api_token="${RENDER_API_TOKEN:-}"
service_id="${RENDER_SERVICE_ID:-}"
commit_sha="${RENDER_COMMIT_SHA:-}"
service_label="${RENDER_SERVICE_LABEL:-render-service}"
timeout_seconds="${RENDER_DEPLOY_TIMEOUT_SECONDS:-1200}"
poll_seconds="${RENDER_POLL_INTERVAL_SECONDS:-10}"

[[ -n "${hook_url}" ]] || { printf '%s\n' 'RENDER_DEPLOY_HOOK_URL is required.' >&2; exit 2; }
[[ -n "${api_token}" ]] || { printf '%s\n' 'RENDER_API_TOKEN is required.' >&2; exit 2; }
[[ "${api_token}" != *$'\n'* && "${api_token}" != *'"'* && "${api_token}" != *'\\'* ]] || {
    printf '%s\n' 'Render API token contains unsupported characters.' >&2
    exit 2
}
[[ "${hook_url}" =~ ^https://api\.render\.com/deploy/ ]] || {
    printf '%s\n' 'RENDER_DEPLOY_HOOK_URL must use the official Render deploy endpoint.' >&2
    exit 2
}
[[ "${hook_url}" == *"key="* ]] || { printf '%s\n' 'Render deploy hook is missing its key parameter.' >&2; exit 2; }
[[ "${hook_url}" != *$'\n'* && "${hook_url}" != *'"'* && "${hook_url}" != *'\\'* ]] || {
    printf '%s\n' 'Render deploy hook contains unsupported characters.' >&2
    exit 2
}
[[ "${service_id}" =~ ^srv-[A-Za-z0-9]+$ ]] || { printf '%s\n' 'RENDER_SERVICE_ID must start with srv-.' >&2; exit 2; }
[[ "${commit_sha}" =~ ^[0-9a-f]{40}$ ]] || { printf '%s\n' 'RENDER_COMMIT_SHA must be a full lowercase Git SHA.' >&2; exit 2; }
[[ "${timeout_seconds}" =~ ^[1-9][0-9]*$ ]] || { printf '%s\n' 'Invalid deploy timeout.' >&2; exit 2; }
[[ "${poll_seconds}" =~ ^[1-9][0-9]*$ ]] || { printf '%s\n' 'Invalid poll interval.' >&2; exit 2; }

temp_dir="$(mktemp -d "${TMPDIR:-/tmp}/device-render-deploy.XXXXXX")"
trap 'rm -rf -- "${temp_dir}"' EXIT
chmod 700 "${temp_dir}"

hook_separator='?'
[[ "${hook_url}" == *'?'* ]] && hook_separator='&'
deploy_url="${hook_url}${hook_separator}ref=${commit_sha}"

hook_config="${temp_dir}/hook.curl"
api_config="${temp_dir}/api.curl"
printf 'url = "%s"\nrequest = "POST"\n' "${deploy_url}" > "${hook_config}"
printf 'header = "Authorization: Bearer %s"\n' "${api_token}" > "${api_config}"
chmod 600 "${hook_config}" "${api_config}"

hook_response="${temp_dir}/hook-response.json"
hook_status="$(curl --config "${hook_config}" --proto '=https' --tlsv1.2 \
    --connect-timeout 15 --max-time 45 --silent --show-error \
    --output "${hook_response}" --write-out '%{http_code}')"

case "${hook_status}" in
    200|201) ;;
    202)
        printf '%s\n' "${service_label}: Render queued the deploy without a deploy ID; retry after the active deploy finishes." >&2
        exit 1
        ;;
    *)
        printf '%s\n' "${service_label}: deploy hook failed with HTTP ${hook_status}." >&2
        exit 1
        ;;
esac

deploy_id="$(python3 -c '
import json, sys
with open(sys.argv[1], encoding="utf-8") as handle:
    payload = json.load(handle)
print(payload.get("id") or payload.get("deploy", {}).get("id") or "")
' "${hook_response}")"

[[ "${deploy_id}" =~ ^dep-[A-Za-z0-9]+$ ]] || {
    printf '%s\n' "${service_label}: Render response did not contain a valid deploy ID." >&2
    exit 1
}

printf '%s\n' "${service_label}: deploy ${deploy_id} accepted for commit ${commit_sha:0:12}."
deadline=$((SECONDS + timeout_seconds))
last_status=''

while (( SECONDS < deadline )); do
    deploy_response="${temp_dir}/deploy-response.json"
    api_status="$(curl --config "${api_config}" --proto '=https' --tlsv1.2 \
        --connect-timeout 15 --max-time 45 --silent --show-error \
        --output "${deploy_response}" --write-out '%{http_code}' \
        "https://api.render.com/v1/services/${service_id}/deploys/${deploy_id}")"

    if [[ "${api_status}" != "200" ]]; then
        printf '%s\n' "${service_label}: Render deploy status API returned HTTP ${api_status}." >&2
        exit 1
    fi

    readarray -t deploy_fields < <(python3 -c '
import json, sys
with open(sys.argv[1], encoding="utf-8") as handle:
    payload = json.load(handle)
deploy = payload.get("deploy", payload)
commit = deploy.get("commit") or {}
print(deploy.get("status") or "")
if isinstance(commit, dict):
    print(commit.get("id") or deploy.get("commitId") or "")
else:
    print(commit if isinstance(commit, str) else deploy.get("commitId") or "")
' "${deploy_response}")
    deploy_status="${deploy_fields[0]:-}"
    deployed_commit="${deploy_fields[1]:-}"

    if [[ "${deploy_status}" != "${last_status}" ]]; then
        printf '%s\n' "${service_label}: deploy status ${deploy_status:-unknown}."
        last_status="${deploy_status}"
    fi

    case "${deploy_status}" in
        live)
            if [[ -n "${deployed_commit}" && "${deployed_commit}" != "${commit_sha}" ]]; then
                printf '%s\n' "${service_label}: live deploy commit does not match the requested commit." >&2
                exit 1
            fi
            printf '%s\n' "${service_label}: deploy ${deploy_id} is live."
            exit 0
            ;;
        failed|build_failed|update_failed|pre_deploy_failed|canceled|deactivated)
            printf '%s\n' "${service_label}: deploy ${deploy_id} ended with ${deploy_status}." >&2
            exit 1
            ;;
    esac

    sleep "${poll_seconds}"
done

printf '%s\n' "${service_label}: deploy ${deploy_id} did not become live within ${timeout_seconds}s." >&2
exit 1
