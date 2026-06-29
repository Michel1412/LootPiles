#!/usr/bin/env bash
# Ensures optional compile-only libs are available for CI.
# Loot Piles resolves JADE/JEI from Maven; this script keeps the same CI layout as sibling mods.
set -euo pipefail

mkdir -p libs
if [ -z "$(find libs -maxdepth 1 -type f -name '*.jar' 2>/dev/null | head -n1)" ]; then
  echo "No local compile-only jars required for Loot Piles."
fi
