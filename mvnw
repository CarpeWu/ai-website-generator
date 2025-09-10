#!/bin/sh
# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup script (optimized version)
# ----------------------------------------------------------------------------

set -euf
[ "${MVNW_VERBOSE-}" != debug ] || set -x

# -----------------------------
# Common functions
# -----------------------------
die() { printf "[ERROR] %s\n" "$1" >&2; exit 1; }
verbose() { [ "${MVNW_VERBOSE-}" = true ] && printf "[INFO] %s\n" "$1"; }
trim() { printf "%s" "${1}" | tr -d '[:space:]'; }

# Convert path for different OS
native_path() { printf %s\\n "$1"; }
case "$(uname)" in
  CYGWIN* | MINGW*)
    [ -z "${JAVA_HOME-}" ] || JAVA_HOME="$(cygpath --unix "$JAVA_HOME")"
    native_path() { cygpath --path --windows "$1"; }
    ;;
esac

# -----------------------------
# Set Java commands
# -----------------------------
set_java_home() {
  if [ -n "${JAVA_HOME-}" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
    JAVACCMD="$JAVA_HOME/bin/javac"
    [ -x "$JAVACMD" ] && [ -x "$JAVACCMD" ] || \
      die "JAVA_HOME is set to \"$JAVA_HOME\" but java/javac not found"
  else
    JAVACMD="$(command -v java || true)"
    JAVACCMD="$(command -v javac || true)"
    [ -x "${JAVACMD-}" ] && [ -x "${JAVACCMD-}" ] || \
      die "Neither JAVA_HOME is set nor java/javac in PATH"
  fi
}

# -----------------------------
# Hash (like Java String.hashCode)
# -----------------------------
hash_string() {
  str="${1:-}" h=0
  while [ -n "$str" ]; do
    char="${str%"${str#?}"}"
    h=$(((h * 31 + $(LC_CTYPE=C printf %d "'$char")) % 4294967296))
    str="${str#?}"
  done
  printf %x\\n $h
}

# -----------------------------
# Parse wrapper properties
# -----------------------------
while IFS="=" read -r key value; do
  case "${key-}" in
    distributionUrl) distributionUrl=$(trim "${value-}") ;;
    distributionSha256Sum) distributionSha256Sum=$(trim "${value-}") ;;
  esac
done <"${0%/*}/.mvn/wrapper/maven-wrapper.properties"
[ -n "${distributionUrl-}" ] || die "distributionUrl not defined in maven-wrapper.properties"

# -----------------------------
# Maven home setup
# -----------------------------
distributionUrlName="${distributionUrl##*/}"
distributionUrlNameMain="${distributionUrlName%.*}"
distributionUrlNameMain="${distributionUrlNameMain%-bin}"
MAVEN_USER_HOME="${MAVEN_USER_HOME:-${HOME}/.m2}"
MAVEN_HOME="${MAVEN_USER_HOME}/wrapper/dists/${distributionUrlNameMain}/$(hash_string "$distributionUrl")"

exec_maven() {
  unset MVNW_VERBOSE MVNW_USERNAME MVNW_PASSWORD MVNW_REPOURL || :
  exec "$MAVEN_HOME/bin/mvn" "$@" || die "cannot exec Maven"
}

# -----------------------------
# If Maven already installed
# -----------------------------
if [ -d "$MAVEN_HOME" ]; then
  verbose "Found existing Maven at $MAVEN_HOME"
  exec_maven "$@"
fi

# -----------------------------
# Prepare download dir
# -----------------------------
TMP_DOWNLOAD_DIR="$(mktemp -d)" || die "cannot create temp dir"
clean() { rm -rf -- "$TMP_DOWNLOAD_DIR"; }
trap clean HUP INT TERM EXIT

mkdir -p -- "${MAVEN_HOME%/*}"
verbose "Downloading Maven from: $distributionUrl"

# -----------------------------
# Download Maven
# -----------------------------
download_file="$TMP_DOWNLOAD_DIR/$distributionUrlName"
if [ -z "${MVNW_USERNAME-}" ] && command -v wget >/dev/null; then
  wget -q "$distributionUrl" -O "$download_file" || die "wget failed"
elif [ -z "${MVNW_USERNAME-}" ] && command -v curl >/dev/null; then
  curl -sSL -o "$download_file" "$distributionUrl" || die "curl failed"
else
  set_java_home
  javaSource="$TMP_DOWNLOAD_DIR/Downloader.java"
  cat >"$javaSource" <<-END
	public class Downloader extends java.net.Authenticator {
	  protected java.net.PasswordAuthentication getPasswordAuthentication() {
	    return new java.net.PasswordAuthentication(
	      System.getenv("MVNW_USERNAME"),
	      System.getenv("MVNW_PASSWORD").toCharArray()
	    );
	  }
	  public static void main(String[] args) throws Exception {
	    setDefault(new Downloader());
	    java.nio.file.Files.copy(
	      java.net.URI.create(args[0]).toURL().openStream(),
	      java.nio.file.Paths.get(args[1]).toAbsolutePath().normalize()
	    );
	  }
	}
	END
  "$(native_path "$JAVACCMD")" "$(native_path "$javaSource")"
  "$(native_path "$JAVACMD")" -cp "$(native_path "$TMP_DOWNLOAD_DIR")" Downloader "$distributionUrl" "$(native_path "$download_file")"
fi

# -----------------------------
# Validate SHA256 (if provided)
# -----------------------------
if [ -n "${distributionSha256Sum-}" ]; then
  if command -v sha256sum >/dev/null; then
    echo "$distributionSha256Sum  $download_file" | sha256sum -c - || die "SHA256 validation failed"
  elif command -v shasum >/dev/null; then
    echo "$distributionSha256Sum  $download_file" | shasum -a 256 -c - || die "SHA256 validation failed"
  else
    die "SHA256 validation requested but no tool available"
  fi
fi

# -----------------------------
# Extract and install Maven
# -----------------------------
if command -v unzip >/dev/null; then
  unzip -q "$download_file" -d "$TMP_DOWNLOAD_DIR" || die "failed to unzip"
else
  tar xzf "$download_file" -C "$TMP_DOWNLOAD_DIR" || die "failed to untar"
fi

printf %s\\n "$distributionUrl" >"$TMP_DOWNLOAD_DIR/$distributionUrlNameMain/mvnw.url"
mv -- "$TMP_DOWNLOAD_DIR/$distributionUrlNameMain" "$MAVEN_HOME" || die "fail to move MAVEN_HOME"

clean || :
exec_maven "$@"
