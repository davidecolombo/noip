# No-IP Java DNS Updater
[![Donate](https://img.shields.io/badge/PayPal-00457C?style=flat&logo=paypal&logoColor=white)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=T9USZAMJHNBBC&lc=IT&item_name=No-IP%20Java%20DNS%20Updater&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted)
![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=java&logoColor=white)
[![CircleCI](https://circleci.com/gh/davidecolombo/noip/tree/master.svg?style=shield)](https://circleci.com/gh/davidecolombo/noip/tree/master)
[![Known Vulnerabilities](https://snyk.io//test/github/davidecolombo/noip/badge.svg?targetFile=pom.xml)](https://snyk.io//test/github/davidecolombo/noip?targetFile=pom.xml)
![Code Size](https://img.shields.io/github/languages/code-size/davidecolombo/noip)
![License](https://img.shields.io/github/license/davidecolombo/noip)

This is a Java DNS updater for [No-IP](https://www.noip.com/), an alternative to [DUC](https://www.noip.com/download) (DNS Update Client). This updater is using both [Ipify](https://www.ipify.org/) and No-IP APIs to retrieve your current IP address and update your No-IP hostname. See: [settings.json](src/test/resources/settings.json)

| Property | Description |
| --- | --- |
| _userName_ | your No-IP username |
| _password_ | your No-IP password |
| _hostName_ | the hostname(s) (host.domain.com) or group(s) (group_name) to be updated |
| _userAgent_ | HTTP User-Agent to help No-IP identify your client |

Note: when making an update it’s important to configure through the `userAgent` property an HTTP User-Agent in order to help No-IP identify different clients that access the system. Clients that don’t supply a User-Agent risk being blocked from the system. Your user agent should be in the following format:
```
NameOfUpdateProgram/VersionNumber maintainercontact@domain.com
```
## Quick Start
One-liner to download, configure and execute:

**Linux:**
```bash
bash <(curl -s https://raw.githubusercontent.com/davidecolombo/noip/master/install.sh)
```

**Windows (PowerShell):**
```powershell
powershell -ExecutionPolicy Bypass -Command "Invoke-RestMethod -Uri 'https://raw.githubusercontent.com/davidecolombo/noip/master/install.ps1' | Invoke-Expression"
```

Or download and run locally:
```powershell
Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/davidecolombo/noip/master/install.ps1' -OutFile install.ps1
.\install.ps1
```

## Password Encryption (Optional)

For enhanced security, you can encrypt your password in the settings file instead of storing it as plaintext. The application uses AES-256 encryption with a master key that you provide at runtime.

### Encrypting Your Password

**Option 1: Using the CLI**

```bash
java -jar noip.jar -encrypt "your_plaintext_password" -encryptor-key "your_master_key"
```

**Option 2: Using the script (Windows)**

```powershell
.\encrypt.ps1 -Password "your_plaintext_password" -Key "your_master_key"
```

This will output something like:
```
ENC(xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx)
```

### Configuration File

Update your `settings.json` to use the encrypted password:

```json
{
	"userName": "your_username",
	"password": "ENC(xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx)",
	"hostName": "yourhost.ddns.net",
	"userAgent": "MyApp/1.0 your@email.com"
}
```

### Running with Encrypted Password

Provide the master key at runtime using one of these methods (priority order):

**1. CLI argument** (highest priority)
```bash
java -jar noip.jar -settings settings.json -encryptor-key "your_master_key"
```

**2. Environment variable**
```bash
export NOIP_ENCRYPTOR_KEY="your_master_key"
java -jar noip.jar -settings settings.json
```

**3. System property** (lowest priority)
```bash
java -Dnoip.encryptor.key="your_master_key" -jar noip.jar -settings settings.json
```

### Additional CLI Commands

- **Encrypt**: `-encrypt <password>` - Encrypt a plaintext password
- **Decrypt**: `-decrypt <encrypted_value>` - Decrypt an encrypted value to verify
- **Encryptor Key**: `-encryptor-key <key>` - Specify the encryption key (highest priority)

### Security Notes

- The master key is **never** stored in the configuration file
- If the password does not start with `ENC(`, it is treated as plaintext (backward compatible)
- Keep your master key safe - without it, the encrypted password cannot be decrypted
- On Unix systems, consider using a `.env` file loaded by your cron job to set the environment variable

## Environment Variables

You can override settings from the JSON file using environment variables. Environment variables take precedence over file settings.

| Variable | Description | Default |
| --- | --- | --- |
| `NOIP_USERNAME` | Override username | - |
| `NOIP_PASSWORD` | Override password (plaintext) | - |
| `NOIP_USER_AGENT` | Override user-agent | `NoIP-Java/1.0 no-reply@noip.local` |
| `NOIP_HOSTNAME` | Override hostname | - |
| `NOIP_ENCRYPTOR_KEY` | Encryption key for encrypted passwords | - |

### Example

```bash
export NOIP_USERNAME="myuser"
export NOIP_PASSWORD="mypass"
export NOIP_USER_AGENT="MyApp/1.0 my@email.com"
export NOIP_HOSTNAME="myhost.ddns.net"
java -jar noip.jar -settings settings.json
```

### Priority Order

For each setting, the priority is:
1. Environment variable (highest)
2. Encrypted password in file (for password only)
3. Plaintext value in file (lowest)

### Encryption Key Priority

The encryption key is resolved in this order:
1. CLI argument (`-encryptor-key`)
2. Environment variable (`NOIP_ENCRYPTOR_KEY`)
3. System property (`noip.encryptor.key`)

## Scheduling
Please note you may want to schedule the application execution in order to keep updated your dynamic DNS, and the simplest way on *NIX systems is probably using [Cron](https://en.wikipedia.org/wiki/Cron). Example:
```
*/30 * * * * sudo NOIP_ENCRYPTOR_KEY="your_master_key" java -cp /home/user/noip.jar io.github.davidecolombo.noip.App -settings /home/user/settings.json > /home/user/noip-log.txt 2>&1
```

## Example Output

Successful execution (IP unchanged - "nochg"):
```
Running No-IP updater with:
  Username: <username>
  Hostname: <hostname>

SLF4J(I): Connected with provider of type [ch.qos.logback.classic.spi.LogbackServiceProvider]
[main] INFO  u.o.l.s.context.SysOutOverSLF4J - Replaced standard System.out and System.err PrintStreams with SLF4JPrintStreams
[main] INFO  u.o.l.s.context.SysOutOverSLF4J - Redirected System.out and System.err to SLF4J for this context
[main] INFO  io.github.davidecolombo.noip.App - Starting No-IP update process with settings file: src\test\resources\settings.json
[main] INFO  i.g.d.noip.noip.NoIpUpdater - No-IP configuration loaded and validated successfully
[main] INFO  i.g.d.noip.noip.NoIpUpdater - Retrieving current IP address from Ipify API
[main] INFO  i.g.d.noip.noip.NoIpUpdater - Retrieved IP address '<ip_address>' from Ipify in <time>ms
[main] INFO  i.g.d.noip.noip.NoIpUpdater - Updating No-IP hostname '<hostname>' to IP address '<ip_address>'
[main] INFO  i.g.d.noip.noip.NoIpUpdater - No-IP API request completed in <time>ms - HTTP status: 200 OK
[main] INFO  i.g.d.noip.noip.NoIpUpdater - No-IP response for hostname '<hostname>': nochg <ip_address>
[main] WARN  io.github.davidecolombo.noip.App - No-IP update completed with status code: 1
[main] INFO  io.github.davidecolombo.noip.App - Application exiting with status code: 1
```

### Exit Codes

| Code | Description |
| --- | --- |
| 0 | Success - IP address was updated |
| 1 | Success - IP address is already up to date (nochg) |
| 2 | Error - Invalid hostname |
| 3 | Error - Authentication failed (badauth) |
| -1 | Error - Unexpected error |

Or using a wrapper script that sets the environment variable: