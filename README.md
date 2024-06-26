[![Build and Create Release](https://github.com/samoylenkodmitry/shrtlin/actions/workflows/build.yml/badge.svg)](https://shrtl.in/T1)
[![GitHub Release](https://img.shields.io/github/v/release/samoylenkodmitry/shrtlin?include_prereleases)](https://shrtl.in/S1)
[![GitHub License](https://img.shields.io/github/license/samoylenkodmitry/shrtlin)](https://shrtl.in/R1)
[![GitHub top language](https://img.shields.io/github/languages/top/samoylenkodmitry/shrtlin)](https://shrtl.in/Q1)
[![Website](https://img.shields.io/website?url=https%3A%2F%2Fshrtl.in%2F)](https://shrtl.in/P1)


| ![Static Badge](https://img.shields.io/badge/platform-android-android) | ![Static Badge](https://img.shields.io/badge/platform-ios-ios) | ![Static Badge](https://img.shields.io/badge/platform-web(wasm)-web) | ![Static Badge](https://img.shields.io/badge/platform-web(js)-web) | ![Static Badge](https://img.shields.io/badge/platform-desktop%20(jvm%20windows)-windows) | ![Static Badge](https://img.shields.io/badge/platform-desktop%20(jvm%20linux)-linux) | ![Static Badge](https://img.shields.io/badge/platform-desktop%20(jvm%20macOS)-linux) |
| --- | --- | --- | --- | --- | --- | --- |
| yes | yes | yes | yes | yes | yes | yes |
---

[![shrtlin](https://raw.githubusercontent.com/samoylenkodmitry/shrtlin/main/logo.svg)](https://shrtl.in/P1)


# shrtlin
`shrtlin` is a feature-rich, open-source `URL shortening` service.
It provides a seamless experience across Android, iOS, Web, and Desktop Linux, Mac, Windows.

## About

`shrtlin` leverages the power of Kotlin Multiplatform to deliver a unified codebase for multiple platforms.
It offers a user-friendly interface for shortening URLs and managing your links, 
all while ensuring secure authentication using JWT tokens and Proof of Work.
Powered by a Ktor backend and PostgreSQL database.

## Features

* **Easy URL shortening:** Quickly shorten URLs using intuitive interfaces on Android, iOS, Web, and Desktop.
* **Seamless Authentication:** Securely authenticate using JWT tokens and Proof of Work, no email, respects your privacy.
* **URL Management:** View, edit, and delete your shortened URLs.
* **Cross-platform compatibility:** Enjoy a consistent experience across Android, iOS, Web, and Desktop.
* **PostgreSQL database:**  Reliable and scalable data storage.
* **Easy deployment:** Deploy the entire application on a single VPS.

## Roadmap

* **Analytics:** Track click-through rates and gain insights into your shortened URLs.
* **QR Codes:** Generate QR codes for your shortened URLs for easy sharing.
* **Enhanced URL Management:** Implement features like custom slugs, expiration dates, and more.

## Getting Started

**Prerequisites:**

* Your own VPS (Virtual Private Server) with Linux installed

**Arch Linux Installation (Single Command):**

```bash
sudo yay -S git jdk17-openjdk docker docker-compose postgresql wget unzip vim --noconfirm 
```

### Arch Linux extra preparation:
```bash

yay linux-headers #must have linux-headers installed
sudo modprobe bridge br_netfilter overlay #must have bridge and br_netfilter modules loaded
sudo systemctl enable docker
sudo systemctl start docker
systemctl status docker #check docker status
sudo usermod -aG docker $USER #add user to docker group
docker swarm init #init docker swarm for storing secrets
sudo ufw status #check firewall status, ports 80, 443 must be open

```

**Set JAVA_HOME for oh-my-zsh (Copy-paste this into your `~/.zshrc`):**

```bash
export JAVA_HOME="/usr/lib/jvm/java-17-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"
```

**Steps:**

##### TLDR
```bash
git clone https://github.com/samoylenkodmitry/shrtlin.git && cd shrtlin && chmod +x ./deploy.sh && ./deploy.sh
```

1. Clone the repository: `git clone https://github.com/samoylenkodmitry/shrtlin.git`
2. **Ensure the prerequisites are installed and configured as described above.**
3. Build and run the application: `./deploy.sh`
4. Check environment the created `.env`

## License

This project is licensed under the [LICENSE](LICENSE).

## Contributing

We welcome contributions from the community! Please feel free to submit issues, feature requests, or pull requests to help us improve shrtlin.
