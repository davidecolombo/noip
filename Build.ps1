$env:NOIP_USERNAME = $null
$env:NOIP_PASSWORD = $null
$env:NOIP_HOSTNAME = $null
$env:NOIP_USER_AGENT = $null
$env:NOIP_ENCRYPTOR_KEY = $null

Write-Host "Environment variables cleared"
mvn dependency:tree | Out-File -FilePath dependency_tree.txt
mvn clean install
