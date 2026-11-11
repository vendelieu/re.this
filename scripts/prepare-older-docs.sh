#!/usr/bin/env bash
# Assembles the "older versions" directory consumed by the Dokka versioning plugin
# (`-PdokkaOlderVersionsDir`, see docs/build.gradle.kts).
#
# The published site (gh-pages) has the latest release at its root and every earlier release under
# `older/<version>/`. Before building release X we need every previously published version laid out as
# `<out>/<version>/<dokka html output>`:
#   - each `older/<version>/` is copied as-is;
#   - the current root (the latest release so far) becomes `<out>/<its version>/`, without its own `older/`.
#
# Usage: prepare-older-docs.sh <published-site-dir> <out-dir> <new-version> [fallback-previous-version]
#
# The version of the published root comes from its `version.json` (written by the versioning plugin).
# Sites published before multi-versioning have none; then `fallback-previous-version` (the previous
# release tag) is used. If neither is available the root is skipped with a warning.
# Archived versions without a `version.json` get one, since the plugin requires it.
set -euo pipefail

if [ "$#" -lt 3 ] || [ "$#" -gt 4 ]; then
  echo "usage: $0 <published-site-dir> <out-dir> <new-version> [fallback-previous-version]" >&2
  exit 2
fi

site="$1"
out="$2"
new_version="$3"
fallback="${4:-}"

if [ ! -f "$site/index.html" ]; then
  echo "::error::$site does not look like a published Dokka site (no index.html)" >&2
  exit 1
fi

rm -rf "$out"
mkdir -p "$out"

# Previously archived versions.
if [ -d "$site/older" ]; then
  for dir in "$site"/older/*/; do
    [ -d "$dir" ] || continue
    name="$(basename "$dir")"
    [ "$name" = "$new_version" ] && continue
    cp -R "$dir" "$out/$name"
  done
fi

# The current root is the latest published release.
root_version=""
if [ -f "$site/version.json" ]; then
  root_version="$(sed -n 's/.*"version"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' "$site/version.json")"
fi
root_version="${root_version:-$fallback}"

if [ -z "$root_version" ]; then
  echo "::warning::cannot determine the version of the published root; it will not be archived" >&2
elif [ "$root_version" = "$new_version" ]; then
  echo "published root is already $new_version (re-run); not archiving it as an older version"
else
  mkdir -p "$out/$root_version"
  # `older/` is excluded: nested archives would be copied again by the plugin.
  (cd "$site" && tar --exclude='./older' -cf - .) | (cd "$out/$root_version" && tar -xf -)
  echo "archived published root as $root_version"
fi

# The versioning plugin identifies an archived version by its `version.json`. Sites published before
# multi-versioning (or by older plugin releases) lack one, so write it from the directory name.
for dir in "$out"/*/; do
  [ -d "$dir" ] || continue
  name="$(basename "$dir")"
  [ -f "$dir/version.json" ] || printf '{"version":"%s"}\n' "$name" > "$dir/version.json"
done

echo "older versions prepared in $out:"
ls -1 "$out"
