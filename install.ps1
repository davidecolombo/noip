# No-IP Java Updater - Interactive Install Script (Windows)
# Usage: powershell -Command "Invoke-RestMethod -Uri 'https://raw.githubusercontent.com/davidecolombo/noip/master/install.ps1' | Invoke-Expression"

$version = "1.0.4"
$jarUrl = "https://github.com/davidecolombo/noip/releases/download/v${version}/noip-${version}-jar-with-dependencies.jar"

Write-Host "No-IP Java Updater - Installation" -ForegroundColor Cyan
Write-Host "=================================="
Write-Host ""

$userName = Read-Host "Enter your NoIP.com userName"

$password = Read-Host -AsSecureString "Enter your NoIP.com password"
$bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($password)
$password = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($bstr)
[System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)

if ($password -match "^ENC\(") {
    $encryptorKey = Read-Host -AsSecureString "Enter your encryption key (required for encrypted password)"
    $bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($encryptorKey)
    $encryptorKey = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($bstr)
    [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
    
    if ([string]::IsNullOrEmpty($encryptorKey)) {
        Write-Host "Error: encryption key is required when password is encrypted" -ForegroundColor Red
        exit 1
    }
}

$hostName = Read-Host "Enter your NoIP.com hostName"

$userAgent = "NoIP-Java/1.0 no-reply@noip.local"
$userInput = Read-Host "Enter your userAgent ($userAgent)"
if ($userInput) { $userAgent = $userInput }

$ipProtocol = "dual"
$userInput = Read-Host "Enter IP protocol (ipv4, ipv6, or dual) [$ipProtocol]"
if ($userInput) { $ipProtocol = $userInput }

Write-Host ""
Write-Host "Downloading noip.jar..." -ForegroundColor Yellow

$noipDir = "$env:USERPROFILE\noip"
if (-not (Test-Path $noipDir)) { New-Item -ItemType Directory -Path $noipDir | Out-Null }

Invoke-WebRequest -Uri $jarUrl -OutFile "$noipDir\noip.jar" -UseBasicParsing

$settingsJson = @{
    userName = $userName
    password = $password
    hostName = $hostName
    userAgent = $userAgent
    ipProtocol = $ipProtocol
} | ConvertTo-Json

Set-Content -Path "$noipDir\settings.json" -Value $settingsJson

Write-Host "Configuration saved to $noipDir\settings.json" -ForegroundColor Green
Write-Host ""
Write-Host "Running No-IP update..." -ForegroundColor Yellow

$jarPath = "$noipDir\noip.jar"

if ($encryptorKey) {
    $env:NOIP_ENCRYPTOR_KEY = $encryptorKey
    java -jar $jarPath -settings "$noipDir\settings.json"
} else {
    java -jar $jarPath -settings "$noipDir\settings.json"
}
