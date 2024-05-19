# shrtlin - A Kotlin Multiplatform URL Shortener
`shrtlin` is a feature-rich, open-source URL shortening service built with Kotlin Multiplatform.
It provides a seamless experience across Android, iOS, Web, and Desktop, powered by a Ktor backend and PostgreSQL database.

## About

shrtlin leverages the power of Kotlin Multiplatform to deliver a unified codebase for multiple platforms. It offers a user-friendly interface for shortening URLs and managing your links, all while ensuring secure authentication using JWT tokens and Proof of Work.

## Features

* **Easy URL shortening:** Quickly shorten URLs using intuitive interfaces on Android, iOS, Web, and Desktop.
* **Seamless Authentication:** Securely authenticate using JWT tokens and Proof of Work.
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
sudo yay -S git jdk21-openjdk docker docker-compose postgresql --noconfirm 
```

**Set JAVA_HOME for oh-my-zsh (Copy-paste this into your `~/.zshrc`):**

```bash
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"
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