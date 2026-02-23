#!/bin/bash
set -euo pipefail

# No-IP Java Updater - Interactive Install Script
# Usage: bash <(curl -s https://raw.githubusercontent.com/davidecolombo/noip/master/install.sh)

readonly version="1.0.3"
readonly jar_url="https://github.com/davidecolombo/noip/releases/download/v${version}/noip-${version}-jar-with-dependencies.jar"

echo "No-IP Java Updater - Installation"
echo "=================================="
echo ""

# Read the userName
read -p "Enter your NoIP.com userName: " userName

# Read the password
read -s -p "Enter your NoIP.com password: " password
echo ""

# Check if password is encrypted (starts with ENC()
if [[ "$password" == ENC\(* ]]; then
    read -s -p "Enter your encryption key (required for encrypted password): " encryptorKey
    echo ""
    
    if [[ -z "$encryptorKey" ]]; then
        echo "Error: encryption key is required when password is encrypted"
        exit 1
    fi
fi

# Read the hostName
read -p "Enter your NoIP.com hostName: " hostName

# Set a default value for the userAgent
userAgent="NoIP-Java/1.0 no-reply@noip.local"
read -p "Enter your userAgent ($userAgent): " -r userInput
if [[ -n "$userInput" ]]; then
    userAgent="$userInput"
fi

echo ""
echo "Downloading noip.jar..."

# Download the JAR
mkdir -p ~/noip
wget -q -O ~/noip/noip.jar "$jar_url"

# Create settings.json
cat > ~/noip/settings.json << EOF
{
  "userName": "$userName",
  "password": "$password",
  "hostName": "$hostName",
  "userAgent": "$userAgent"
}
EOF

echo "Configuration saved to ~/noip/settings.json"
echo ""
echo "Running No-IP update..."

# Run with encrypted password if applicable
if [[ -n "${encryptorKey:-}" ]]; then
    NOIP_ENCRYPTOR_KEY="$encryptorKey" java -jar ~/noip/noip.jar -settings ~/noip/settings.json
else
    java -jar ~/noip/noip.jar -settings ~/noip/settings.json
fi
