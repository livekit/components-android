#!/bin/bash
set -euo pipefail

NEW_VERSION="${1:?Usage: $0 <version>}"
>&2 echo "updating livekit version to $NEW_VERSION"

# sed command works only on linux based systems as macOS version expects a backup file passed additionally
if [ "$(uname)" == "Darwin" ]; then
  ARGS=('')
else
  ARGS=()
fi

sed -i "${ARGS[@]}" -e "s/var livekitVersion = \".*\"/var livekitVersion = \"${NEW_VERSION}\"/" livekit-compose-components/build.gradle
