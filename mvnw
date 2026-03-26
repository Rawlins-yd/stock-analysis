#!/usr/bin/env sh
set -e

BASE_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
WRAPPER_DIR="$BASE_DIR/.mvn"
MVN_VERSION="3.9.9"
MVN_DIR="$WRAPPER_DIR/apache-maven-$MVN_VERSION"
MVN_BIN="$MVN_DIR/bin/mvn"

if [ ! -x "$MVN_BIN" ]; then
  mkdir -p "$WRAPPER_DIR"
  ARCHIVE="$WRAPPER_DIR/apache-maven-$MVN_VERSION-bin.tar.gz"

  URLS="
https://dlcdn.apache.org/maven/maven-3/$MVN_VERSION/binaries/apache-maven-$MVN_VERSION-bin.tar.gz
https://archive.apache.org/dist/maven/maven-3/$MVN_VERSION/binaries/apache-maven-$MVN_VERSION-bin.tar.gz
https://repo1.maven.org/maven2/org/apache/maven/apache-maven/$MVN_VERSION/apache-maven-$MVN_VERSION-bin.tar.gz
"

  DOWNLOADED=""
  for URL in $URLS; do
    if command -v curl >/dev/null 2>&1; then
      if curl -fsSL "$URL" -o "$ARCHIVE"; then
        DOWNLOADED="yes"
        break
      fi
    elif command -v wget >/dev/null 2>&1; then
      if wget -q "$URL" -O "$ARCHIVE"; then
        DOWNLOADED="yes"
        break
      fi
    else
      echo "Need curl or wget to download Maven." >&2
      exit 1
    fi
  done

  if [ -z "$DOWNLOADED" ]; then
    echo "Failed to download Maven $MVN_VERSION from available mirrors." >&2
    exit 1
  fi

  tar -xzf "$ARCHIVE" -C "$WRAPPER_DIR"
fi

exec "$MVN_BIN" "$@"
