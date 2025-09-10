<# : batch portion
@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup script (optimized version)
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET __MVNW_CMD__=
@SET __MVNW_ERROR__=
@SET __MVNW_PSMODULEP_SAVE=%PSModulePath%
@SET PSModulePath=
@FOR /F "usebackq tokens=1* delims==" %%A IN (`powershell -noprofile "& {$scriptDir='%~dp0'; $script='%__MVNW_ARG0_NAME__%'; icm -ScriptBlock ([Scriptblock]::Create((Get-Content -Raw '%~f0'))) -NoNewScope}"`) DO @(
  IF "%%A"=="MVN_CMD" (set __MVNW_CMD__=%%B) ELSE IF "%%B"=="" (echo %%A) ELSE (echo %%A=%%B)
)
@SET PSModulePath=%__MVNW_PSMODULEP_SAVE%
@SET __MVNW_PSMODULEP_SAVE=
@SET __MVNW_ARG0_NAME__=
@SET MVNW_USERNAME=
@SET MVNW_PASSWORD=
@IF NOT "%__MVNW_CMD__%"=="" (%__MVNW_CMD__% %*)
@echo [ERROR] Cannot start Maven from wrapper >&2 && exit /b 1
@GOTO :EOF
: end batch / begin powershell #>

$ErrorActionPreference = "Stop"
if ($env:MVNW_VERBOSE -eq "true") {
  $VerbosePreference = "Continue"
}

# -----------------------------
# Read wrapper properties
# -----------------------------
$props = Get-Content -Raw "$scriptDir/.mvn/wrapper/maven-wrapper.properties" | ConvertFrom-StringData
$distributionUrl = $props.distributionUrl
$distributionSha256Sum = $props.distributionSha256Sum

if (-not $distributionUrl) {
  Write-Error "[ERROR] distributionUrl missing in maven-wrapper.properties"
}

# -----------------------------
# Detect mvnd or maven
# -----------------------------
switch -wildcard -casesensitive ($distributionUrl -replace '^.*/','') {
  "maven-mvnd-*" {
    $USE_MVND = $true
    $distributionUrl = $distributionUrl -replace '-bin\.[^.]*$', "-windows-amd64.zip"
    $MVN_CMD = "mvnd.cmd"
  }
  default {
    $USE_MVND = $false
    $MVN_CMD = $script -replace '^mvnw','mvn'
  }
}

# -----------------------------
# Calculate Maven home
# -----------------------------
if ($env:MVNW_REPOURL) {
  $MVNW_REPO_PATTERN = if ($USE_MVND) { "/maven/mvnd/" } else { "/org/apache/maven/" }
  $distributionUrl = "$env:MVNW_REPOURL$MVNW_REPO_PATTERN$($distributionUrl -replace '^.*'+$MVNW_REPO_PATTERN,'')"
}
$distributionUrlName = $distributionUrl -replace '^.*/',''
$distributionUrlNameMain = $distributionUrlName -replace '\.[^.]*$','' -replace '-bin$',''
if (-not $distributionUrlNameMain) {
  Write-Error "[ERROR] distributionUrl is invalid: $distributionUrl"
}

$MAVEN_HOME_PARENT = if ($env:MAVEN_USER_HOME) {
  "$env:MAVEN_USER_HOME/wrapper/dists/$distributionUrlNameMain"
} else {
  "$HOME/.m2/wrapper/dists/$distributionUrlNameMain"
}
$MAVEN_HOME_NAME = ([System.Security.Cryptography.MD5]::Create().ComputeHash([byte[]][char[]]$distributionUrl) | ForEach-Object { $_.ToString("x2") }) -join ''
$MAVEN_HOME = "$MAVEN_HOME_PARENT/$MAVEN_HOME_NAME"

if (Test-Path -Path $MAVEN_HOME -PathType Container) {
  Write-Verbose "Found existing Maven at $MAVEN_HOME"
  Write-Output "MVN_CMD=$MAVEN_HOME/bin/$MVN_CMD"
  exit 0
}

# -----------------------------
# Prepare temp dir
# -----------------------------
$TMP_DOWNLOAD_DIR = New-Item -ItemType Directory -Path ([System.IO.Path]::GetTempPath() + [System.IO.Path]::GetRandomFileName())
function Cleanup { if (Test-Path $TMP_DOWNLOAD_DIR) { Remove-Item $TMP_DOWNLOAD_DIR -Recurse -Force -ErrorAction SilentlyContinue } }
trap { Cleanup }

New-Item -ItemType Directory -Path $MAVEN_HOME_PARENT -Force | Out-Null

# -----------------------------
# Download Maven
# -----------------------------
Write-Verbose "Downloading Maven from: $distributionUrl"
$downloadFile = "$TMP_DOWNLOAD_DIR/$distributionUrlName"

$webclient = New-Object System.Net.WebClient
if ($env:MVNW_USERNAME -and $env:MVNW_PASSWORD) {
  $webclient.Credentials = New-Object System.Net.NetworkCredential($env:MVNW_USERNAME, $env:MVNW_PASSWORD)
}
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
$webclient.DownloadFile($distributionUrl, $downloadFile)

# -----------------------------
# Validate SHA256
# -----------------------------
if ($distributionSha256Sum) {
  if ($USE_MVND) {
    Write-Error "[ERROR] SHA256 validation not supported for mvnd"
  }
  if (-not (Get-Command Get-FileHash -ErrorAction SilentlyContinue)) {
    Write-Error "[ERROR] SHA256 validation requested but Get-FileHash not available"
  }
  $actualHash = (Get-FileHash $downloadFile -Algorithm SHA256).Hash.ToLower()
  if ($actualHash -ne $distributionSha256Sum.ToLower()) {
    Write-Error "[ERROR] SHA256 mismatch. File may be compromised."
  }
}

# -----------------------------
# Extract and move
# -----------------------------
Expand-Archive $downloadFile -DestinationPath $TMP_DOWNLOAD_DIR -Force
Rename-Item "$TMP_DOWNLOAD_DIR/$distributionUrlNameMain" $MAVEN_HOME_NAME
Move-Item "$TMP_DOWNLOAD_DIR/$MAVEN_HOME_NAME" $MAVEN_HOME_PARENT -Force

Cleanup
Write-Output "MVN_CMD=$MAVEN_HOME/bin/$MVN_CMD"
