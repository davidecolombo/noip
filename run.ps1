# Run No-IP updater using environment variables

$missing = @()

if (-not $env:NOIP_USERNAME) { $missing += "NOIP_USERNAME" }
if (-not $env:NOIP_PASSWORD) { $missing += "NOIP_PASSWORD" }
if (-not $env:NOIP_HOSTNAME) { $missing += "NOIP_HOSTNAME" }

if ($missing.Count -gt 0) {
    Write-Host "Error: Missing required environment variable(s): $($missing -join ', ')" -ForegroundColor Red
    Write-Host ""
    Write-Host "Required:"
    Write-Host '  $env:NOIP_USERNAME = "your_username"'
    Write-Host '  $env:NOIP_PASSWORD = "your_password_or_encrypted_password"'
    Write-Host '  $env:NOIP_HOSTNAME = "yourhost.ddns.net"'
    Write-Host ""
    Write-Host "Optional:"
    Write-Host '  $env:NOIP_USER_AGENT = "YourApp/1.0 your@email.com"'
    Write-Host '  $env:NOIP_ENCRYPTOR_KEY = "your_encryption_key"  (required if password is encrypted)'
    Write-Host ""
    Write-Host "Example:"
    Write-Host '  $env:NOIP_USERNAME = "myuser"'
    Write-Host '  $env:NOIP_PASSWORD = "mypass"'
    Write-Host '  $env:NOIP_HOSTNAME = "myhost.ddns.net"'
    Write-Host '  .\run.ps1'
    exit 1
}

# Check if password is encrypted
if ($env:NOIP_PASSWORD -match "^ENC\(.*\)$" -and -not $env:NOIP_ENCRYPTOR_KEY) {
    Write-Host "Error: NOIP_ENCRYPTOR_KEY is required when NOIP_PASSWORD is encrypted" -ForegroundColor Red
    Write-Host ""
    Write-Host "Example:"
    Write-Host '  $env:NOIP_ENCRYPTOR_KEY = "your_encryption_key"'
    Write-Host '  .\run.ps1'
    exit 1
}

# Find JAR file
$jarFile = Get-ChildItem -Path target -Filter "noip-*-jar-with-dependencies.jar" | Select-Object -First 1 | ForEach-Object { $_.Name }

if (-not $jarFile) {
    Write-Host "Error: JAR file not found in target directory. Run 'mvn package' first." -ForegroundColor Red
    exit 1
}

Write-Host "Running No-IP updater with:" -ForegroundColor Cyan
Write-Host "  Username: $env:NOIP_USERNAME"
Write-Host "  Hostname: $env:NOIP_HOSTNAME"
Write-Host ""

java -jar "target\$jarFile"
