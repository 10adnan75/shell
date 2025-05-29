[![Build & Test](https://github.com/10adnan75/shell/actions/workflows/ci.yml/badge.svg)](https://github.com/10adnan75/shell/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/10adnan75/shell/branch/main/graph/badge.svg)](https://codecov.io/gh/10adnan75/shell)
[![Javadoc](https://img.shields.io/badge/docs-javadoc-blue)](https://10adnan75.github.io/shell/api/)
[![GitHub release](https://img.shields.io/github/v/release/10adnan75/shell.svg)](https://github.com/10adnan75/shell/releases)
[![GitHub issues](https://img.shields.io/github/issues/10adnan75/shell.svg)](https://github.com/10adnan75/shell/issues)
[![GitHub pull requests](https://img.shields.io/github/issues-pr/10adnan75/shell.svg)](https://github.com/10adnan75/shell/pulls)
[![GitHub contributors](https://img.shields.io/github/contributors/10adnan75/shell.svg)](https://github.com/10adnan75/shell/graphs/contributors)
[![progress-banner](https://backend.codecrafters.io/progress/shell/542592db-4059-464e-b531-1a3ba30b51c9)](https://app.codecrafters.io/users/codecrafters-bot?r=2qF)

![Java](https://img.shields.io/badge/java-17%2B-blue)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)

---

# Demo

![Shell Demo](demo.gif)

*Showcase your shell in action!*

**How to add your own demo:**
- Record a GIF or video of your shell using a tool like [asciinema](https://asciinema.org/), [peek](https://github.com/phw/peek), or your favorite screen recorder.
- Save the file as `demo.gif` in the project root (or update the README to point to your file).
- Commit and push the GIF to your repository.

---

# Features

- POSIX-like command parsing
- Built-in commands: cd, pwd, echo, exit, type, history
- External command execution
- Pipelines (`|`)
- Output and error redirection (`>`, `>>`, `2>`, `2>>`)
- Tab completion for commands and files
- Command history navigation (up/down arrows)
- Modular, OOP codebase
- PDF and Markdown documentation
- **Test utilities for contributors** (see Testing section)

---

# Contributing

Contributions are welcome! To contribute:

1. **Fork** the repository on GitHub.
2. **Clone** your fork:
   ```sh
   git clone https://github.com/your-username/shell.git
   cd shell
   ```
3. **Create a branch** for your feature or fix:
   ```sh
   git checkout -b feature/your-feature-name
   ```
4. **Make your changes** and add tests if possible.
5. **Commit** and **push** your branch:
   ```sh
   git add .
   git commit -m "Describe your changes"
   git push origin feature/your-feature-name
   ```
6. **Open a Pull Request** on GitHub and describe your changes.

**Guidelines:**
- Use clear commit messages.
- Follow Java best practices and code style.
- Add tests for new features when possible.
- For major changes, open an issue first to discuss your idea.

---

# Testing

To run all tests:

```sh
mvn test
```

**Test utilities are available in `src/test/java/core/`:**
- `TestOutputCapture` – Capture and assert on System.out/System.err.
- `TestFileUtils` – Manage temporary files and directories.
- `TestShellRunner` – Run shell commands and capture output.

Use these helpers to write robust and maintainable tests for your shell project.

---

# Code Formatting

To format all Java files using Google Java Format:

```sh
google-java-format -r src/**/*.java
```

Install with Homebrew:
```sh
brew install google-java-format
```

---

# FAQ / Troubleshooting

**Q: I get a Java version error.**  
A: Make sure you have Java 17 or higher installed (`java -version`). Project is compatible with Java 17, 21, and 23.

**Q: Pandoc or pdflatex not found.**  
A: Install [Pandoc](https://pandoc.org/) and [TeX Live/MacTeX](https://www.tug.org/mactex/).

**Q: How do I add a new builtin?**  
A: See the "Extending the Shell" section in the documentation or `PROJECT_DOCUMENTATION.md`.

**Q: My shell prompt or output looks wrong in tests.**  
A: Make sure you are not printing the prompt in non-interactive mode and that all output is flushed before the prompt.

**Q: How do I run the shell on Windows?**  
A: This project is designed for Unix-like systems. For Windows, use WSL or a compatible terminal.

---

# Project Information

- **Author:** Adnan Mazharuddin Shaikh
- **Email:** adnanmazharuddinshaikh@gmail.com
- **GitHub:** [10adnan75](https://github.com/10adnan75)
- **Copyright:** © 2025 Adnan Mazharuddin Shaikh. All rights reserved.
- **License:** MIT License (see [LICENSE](LICENSE) for details)
- **Trademark:** Adnan Mazharuddin Shaikh™

---

# How to Run This Project

## Requirements
- Java 17 or higher (JDK; works with Java 17, 21, or 23)
- Maven (for building and running)
- (Optional) [Pandoc](https://pandoc.org/) and [TeX Live/MacTeX](https://www.tug.org/mactex/) for generating PDF documentation

## Setup & Usage

1. **Clone the repository:**
   ```sh
   git clone https://github.com/10adnan75/shell.git
   cd shell
   ```

2. **Build the project:**
   ```sh
   mvn clean package
   ```

3. **Run the shell:**
   ```sh
   ./your_program.sh
   ```
   Or, run directly with Java:
   ```sh
   mvn exec:java -Dexec.mainClass=Main
   ```

4. **(Optional) Generate documentation PDF:**
   ```sh
   pandoc PROJECT_DOCUMENTATION.md -o PROJECT_DOCUMENTATION.pdf --pdf-engine=pdflatex
   ```

5. **(Optional) Generate Javadoc:**
   ```sh
   mvn javadoc:javadoc
   ```
   The generated documentation will be in `target/site/apidocs/`.

## Notes
- The entry point is `src/main/java/Main.java`.
- All source code is in `src/main/java/core/` and `src/main/java/builtins/`.
- The shell supports built-in commands, external commands, pipelines, redirection, tab completion, and command history.

---

# Project Structure

```
codecrafters-shell-java/
├── src/
│   ├── main/
│   │   └── java/
│   │       ├── Main.java                # Entry point
│   │       ├── core/                    # Core logic (input, parsing, execution, etc.)
│   │       └── builtins/                # Built-in shell commands
│   └── test/
│       └── java/
│           └── core/
│               ├── CommandHandlerTest.java   # Unit tests
│               ├── TestOutputCapture.java    # Test utility: capture output
│               ├── TestFileUtils.java        # Test utility: temp files/dirs
│               └── TestShellRunner.java      # Test utility: run shell commands
├── target/                              # Build artifacts (generated by Maven)
├── pom.xml                              # Maven build file
├── your_program.sh                      # Script to run the shell
├── PROJECT_DOCUMENTATION.md             # Project documentation (Markdown)
├── PROJECT_DOCUMENTATION.pdf            # Project documentation (PDF)
├── README.md                            # This file
├── LICENSE                              # License file
└── ...
```

---

# References

- [Codecrafters Shell Challenge](https://app.codecrafters.io/courses/shell/overview)
- [Java ProcessBuilder Documentation](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/ProcessBuilder.html)
- [POSIX Shell Command Language](https://pubs.opengroup.org/onlinepubs/9699919799/utilities/V3_chap02.html)
- [Maven](https://maven.apache.org/)
- [Pandoc](https://pandoc.org/)
- [TeX Live/MacTeX](https://www.tug.org/mactex/)
- [Java SE Documentation](https://docs.oracle.com/en/java/javase/17/)
- [google-java-format](https://github.com/google/google-java-format)

---

# Credits / Acknowledgments

- Inspired by the [Codecrafters Shell Challenge](https://app.codecrafters.io/courses/shell/overview)
- Thanks to the open source community and Java documentation authors!
- Special thanks to anyone who contributes to this project.

---

This is a starting point for Java solutions to the
["Build Your Own Shell" Challenge](https://app.codecrafters.io/courses/shell/overview).

In this challenge, you'll build your own POSIX compliant shell that's capable of
interpreting shell commands, running external programs and builtin commands like
cd, pwd, echo and more. Along the way, you'll learn about shell command parsing,
REPLs, builtin commands, and more.

**Note**: If you're viewing this repo on GitHub, head over to
[codecrafters.io](https://codecrafters.io) to try the challenge.

# Passing the first stage

The entry point for your `shell` implementation is in `src/main/java/Main.java`.
Study and uncomment the relevant code, and push your changes to pass the first
stage:

```sh
git commit -am "pass 1st stage" # any msg
git push origin master
```