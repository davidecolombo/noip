# Encrypt a password using the No-IP updater application
# Usage: .\encrypt.ps1 <password> <key>

param(
    [Parameter(Mandatory=$true)][string]$Password,
    [Parameter(Mandatory=$true)][string]$Key
)

# Find JAR file
$jarFile = Get-ChildItem -Path target -Filter "noip-*-jar-with-dependencies.jar" | Select-Object -First 1 | ForEach-Object { $_.Name }

if (-not $jarFile) {
    Write-Host "Error: JAR file not found in target directory. Run 'mvn package' first." -ForegroundColor Red
    exit 1
}

$env:NOIP_ENCRYPTOR_KEY = $Key
java -jar "target\$jarFile" -encrypt "$Password"
