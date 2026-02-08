# Java Project Dumper (jpdumper)

Java Project Dumper is a simple CLI tool that exports the structure and source files of a Java project into a single text file. It respects `.gitignore` and custom ignore patterns.

---

## Features

- Dumps all text files of a Java project into a single file.
- Skips binary files automatically.
- Supports `.gitignore` and `.projectdumperignore`.
- Limits output by file size and number of lines per file.
- Easy to use via CLI.

---

## Installation

Build the project using Gradle:

```bash
./gradlew build
```

The resulting JAR will be located at `build/libs/java-project-dumper.jar`.

---

## Usage

```bash
java -jar java-project-dumper.jar [options]
```

---

## Options

- `--path=<path>` - Root path of the project (default: current directory)
- `--output=<file>` - Output dump file (default: projectdump.txt)
- `--max-file-size=<size>` - Maximum file size to read, e.g., 1Mb, 4000kb, default: 1Mb
- `--max-file-lines=<n>` - Maximum number of lines to read per file (default: 2000)
- `--no-gitignore` - Do not use .gitignore rules
- `--debug` - Print DEBUG logs
- `--show-default-ignore` - Show the list of default ignored files and directories
- `--help` - Show this help message

---

## Ignore Patterns

By default, typical directories and temporary files are ignored.
To customize, create a `.projectdumperignore` file in the project root.
Syntax is similar to `.gitignore`, including `!` for negation.

---

## Example

```bash
java -jar java-project-dumper.jar --path=D:/Projects/java-project-dumper --max-file-size=100kb --max-file-lines=500
```

This will dump the project into `D:\Projects\java-project-dumper\projectdump.txt`, limiting each file to 100 KB or 500 lines.

---

## License

This project is licensed under the [MIT License](LICENSE). See the `LICENSE` file for details.