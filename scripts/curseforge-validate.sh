#!/usr/bin/env bash
set -euo pipefail

: "${CURSEFORGE_TOKEN:?CURSEFORGE_TOKEN secret is not set}"
: "${CURSEFORGE_PROJECT_ID:?CURSEFORGE_PROJECT_ID secret is not set}"

API="https://minecraft.curseforge.com/api"

echo "Validating CurseForge credentials for project ${CURSEFORGE_PROJECT_ID}..."
response="$(curl -sS -w '\n%{http_code}' \
  -H "X-Api-Token: ${CURSEFORGE_TOKEN}" \
  "${API}/projects/${CURSEFORGE_PROJECT_ID}")"

code="$(printf '%s' "$response" | tail -n1)"
body="$(printf '%s' "$response" | sed '$d')"

if [ "$code" != "200" ]; then
  echo "::error::CurseForge project lookup failed (HTTP ${code}): ${body}"
  exit 1
fi

name="$(printf '%s' "$body" | jq -r '.name // .data.name // empty')"
if [ -z "$name" ]; then
  echo "::error::CurseForge response did not include a project name."
  exit 1
fi

echo "CurseForge project OK: ${name} (${CURSEFORGE_PROJECT_ID})"

if [ -n "${GITHUB_ENV:-}" ]; then
  {
    echo "CURSEFORGE_TOKEN=${CURSEFORGE_TOKEN}"
    echo "CURSEFORGE_PROJECT_ID=${CURSEFORGE_PROJECT_ID}"
  } >> "$GITHUB_ENV"
fi
