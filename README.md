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
```
bash <(curl -s https://raw.githubusercontent.com/davidecolombo/noip/master/install.sh)
```
## Scheduling
Please note you may want to schedule the application execution in order to keep updated your dynamic DNS, and the simplest way on *NIX systems is probably using [Cron](https://en.wikipedia.org/wiki/Cron). Example:
```
*/30 * * * * sudo DISPLAY=:1 java -cp /home/user/noip.jar io.github.davidecolombo.noip.App -settings /home/user/settings.json > /home/user/noip-log.txt 2>&1
```