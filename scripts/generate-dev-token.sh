#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage: scripts/generate-dev-token.sh [-s subject] [-c "scope ..."] [-t ttl_seconds]

Prints an RS256 JWT accepted by the API, for local development only.

  -s  subject (sub claim)              default: dev-user
  -c  space-separated scopes           default: "users:read users:write"
  -t  lifetime in seconds (max 3600)   default: 900

Reads JWT_PRIVATE_KEY (base64 of the private key PEM), JWT_ISSUER and JWT_AUDIENCE
from the environment. When a .env file exists at the repository root, its values take precedence.

Example:
  TOKEN=$(scripts/generate-dev-token.sh -c users:read)
  curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/user/api/v1/users
EOF
}

fail() {
  echo "error: $*" >&2
  exit 1
}

base64url() {
  openssl base64 -A | tr '+/' '-_' | tr -d '='
}

subject="dev-user"
scope="users:read users:write"
ttl=900

while getopts ":s:c:t:h" opt; do
  case "$opt" in
    s) subject="$OPTARG" ;;
    c) scope="$OPTARG" ;;
    t) ttl="$OPTARG" ;;
    h) usage; exit 0 ;;
    *) usage >&2; exit 2 ;;
  esac
done

env_file="$(cd "$(dirname "$0")/.." && pwd)/.env"
if [[ -f "$env_file" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$env_file"
  set +a
fi

[[ -n "${JWT_PRIVATE_KEY:-}" ]] || fail "JWT_PRIVATE_KEY is not set"
[[ -n "${JWT_ISSUER:-}" ]] || fail "JWT_ISSUER is not set"
[[ -n "${JWT_AUDIENCE:-}" ]] || fail "JWT_AUDIENCE is not set"
[[ "$JWT_ISSUER$JWT_AUDIENCE" != *[\"\\]* ]] || fail "JWT_ISSUER and JWT_AUDIENCE must not contain quotes or backslashes"
[[ "$subject" =~ ^[A-Za-z0-9._@-]+$ ]] || fail "invalid subject: $subject"
[[ "$scope" =~ ^[A-Za-z0-9:._\ -]*$ ]] || fail "invalid scope: $scope"
[[ "$ttl" =~ ^[0-9]+$ ]] && (( ttl > 0 && ttl <= 3600 )) || fail "ttl must be between 1 and 3600 seconds"

private_key="$(mktemp)"
trap 'rm -f "$private_key"' EXIT
chmod 600 "$private_key"
printf '%s' "$JWT_PRIVATE_KEY" | openssl base64 -d -A > "$private_key" \
  || fail "JWT_PRIVATE_KEY is not valid base64"

now="$(date +%s)"
header='{"alg":"RS256","typ":"JWT"}'
payload=$(printf '{"iss":"%s","aud":["%s"],"sub":"%s","scope":"%s","iat":%d,"exp":%d,"jti":"%s"}' \
  "$JWT_ISSUER" "$JWT_AUDIENCE" "$subject" "$scope" "$now" "$((now + ttl))" "$(openssl rand -hex 16)")

signing_input="$(printf '%s' "$header" | base64url).$(printf '%s' "$payload" | base64url)"
signature="$(printf '%s' "$signing_input" | openssl dgst -sha256 -sign "$private_key" -binary | base64url)" \
  || fail "signing failed: is JWT_PRIVATE_KEY an RSA private key PEM?"

printf '%s.%s\n' "$signing_input" "$signature"
