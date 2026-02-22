#!/bin/bash
set -euo pipefail
readonly version="1.0.3"

# Read the userName
read -p "Enter your NoIP.com userName: " userName

# Read the password, suppressing the input and displaying a prompt message
read -s -p "Enter your NoIP.com password: " password

# Print a newline after the password is entered
echo

# Read the hostName
read -p "Enter your NoIP.com hostName: " hostName

# Set a default value for the userAgent
userAgent="TestApp/1.0 maintainercontact@domain.com"

# Read the userAgent, if provided
read -p "Enter your userAgent ($userAgent): " -r userInput

# If the user provided an input, use it
if [ -n "$userInput" ]; then
  userAgent="$userInput"
fi

wget -O ~/noip.jar \
https://github.com/davidecolombo/noip/releases/download/v${version}/noip-${version}-jar-with-dependencies.jar && \
echo "{
\"userName\": \"${userName}\",
\"password\": \"${password}\",
\"hostName\": \"${hostName}\",
\"userAgent\": \"${userAgent}\"
}" > ~/settings.json && \
java -cp ~/noip.jar io.github.davidecolombo.noip.App -settings ~/settings.json
