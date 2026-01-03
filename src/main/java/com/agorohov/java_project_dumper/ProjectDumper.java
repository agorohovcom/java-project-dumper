package com.agorohov.java_project_dumper;

import org.eclipse.jgit.ignore.IgnoreNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProjectDumper {

    private static final String DEFAULT_OUTPUT_FILENAME = "projectdump.txt";
    private static final String DEFAULT_ROOT = ".";
    private static final String IGNORE_FILENAME = ".projectdumperignore";
    private static final String GITIGNORE_FILENAME = ".gitignore";
    private static final String DEFAULT_IGNORE_RESOURCE = "/default-ignore.patterns";
    private static final int SAMPLE_SIZE = 8192;                    // 8 kb
    private static final double BAD_BYTES_LIMIT = 0.1;              // 10%
    private static final long MAX_FILE_SIZE_BYTES = 1024 * 1024;    // 1 Mb
    private static final int MAX_FILE_LINES = 2000;                 // oh, what you think?

    private static final Logger log = LoggerFactory.getLogger(ProjectDumper.class);

    public static void main(String[] args) throws IOException {
        Config config = parseArgs(args);

        IgnoreNode ignoreNode = loadIgnoreNode(config.rootPath, config.useGitignore);

        dumpProject(config, ignoreNode);
    }

    private static IgnoreNode loadIgnoreNode(Path rootPath, boolean useGitignore) throws IOException {
        IgnoreNode node = new IgnoreNode();

        // User's .projectdumperignore
        Path ignoreFile = rootPath.resolve(IGNORE_FILENAME);
        if (Files.exists(ignoreFile)) {
            try (BufferedReader reader = Files.newBufferedReader(ignoreFile, StandardCharsets.UTF_8)) {
                loadIgnoreRules(reader, node, ignoreFile.toString());
            } catch (IOException e) {
                log.error("Error reading {}: {}", IGNORE_FILENAME, e.getMessage());
            }
        }

        // .gitignore
        if (useGitignore) {
            Path gitIgnore = rootPath.resolve(GITIGNORE_FILENAME);
            if (Files.exists(gitIgnore)) {
                try (BufferedReader reader = Files.newBufferedReader(gitIgnore, StandardCharsets.UTF_8)) {
                    loadIgnoreRules(reader, node, gitIgnore.toString());
                } catch (IOException e) {
                    log.error("Error reading .gitignore ({}): {}", GITIGNORE_FILENAME, e.getMessage());
                }
            }
        }

        // Default ignores
        try (InputStream is = ProjectDumper.class.getResourceAsStream(DEFAULT_IGNORE_RESOURCE)) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    loadIgnoreRules(reader, node, DEFAULT_IGNORE_RESOURCE);
                }
            } else {
                log.warn("Warning: Resource {} not found in JAR", DEFAULT_IGNORE_RESOURCE);
            }
        } catch (IOException e) {
            log.error("Error loading default ignore patterns: {}", e.getMessage());
        }

        return node;
    }

    private static void loadIgnoreRules(BufferedReader reader, IgnoreNode node, String sourceName) throws IOException {
        StringBuilder sb = new StringBuilder();

        reader.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .forEach(p -> sb.append(p).append(System.lineSeparator()));

        try (InputStream in = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8))) {
            node.parse(in);
        }

        log.debug("Ignore rules from {} were loaded", sourceName);
    }

    private static void dumpProject(Config config, IgnoreNode ignoreNode) throws IOException {
        Path rootPath = config.rootPath;
        Path outputPath = config.outputPath;

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.write("---------------------" + System.lineSeparator());
            writer.write("Java project dump " + timestamp + System.lineSeparator());
            writer.write("---------------------" + System.lineSeparator() + System.lineSeparator());

            Path normalizedOutput = outputPath.toAbsolutePath().normalize();
            Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    String pathStr = toUnixPath(rootPath, dir);

                    if (pathStr.isEmpty()) {
                        return FileVisitResult.CONTINUE; // root
                    }

                    Boolean ignored = ignoreNode.checkIgnored(pathStr, true);

                    if (Boolean.TRUE.equals(ignored)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // Excluding output file
                    Path normalizedFile = file.toAbsolutePath().normalize();
                    if (normalizedFile.equals(normalizedOutput)) {
                        return FileVisitResult.CONTINUE;
                    }

                    String pathStr = toUnixPath(rootPath, file);

                    Boolean ignored = ignoreNode.checkIgnored(pathStr, false);
                    if (Boolean.TRUE.equals(ignored)) {
                        return FileVisitResult.CONTINUE;
                    }

                    writer.write("<<<FILE: " + toUnixPath(rootPath, file) + ">>>" + System.lineSeparator());

                    if (isTextFile(file)) {
                        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                            String line;
                            long bytesRead = 0;
                            int lineCount = 0;

                            while ((line = reader.readLine()) != null) {
                                String lineWithNl = line + System.lineSeparator();
                                byte[] bytes = lineWithNl.getBytes(StandardCharsets.UTF_8);
                                bytesRead += bytes.length;

                                writer.write(lineWithNl);

                                lineCount++;

                                if (bytesRead > config.maxFileSizeBytes) {
                                    writer.write("[Truncated: size limit exceeded]" + System.lineSeparator());
                                    break;
                                }

                                if (lineCount >= config.maxFileLines) {
                                    writer.write("[Truncated: line limit exceeded]" + System.lineSeparator());
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            writer.write("[Cannot read file: "
                                    + e.getClass().getSimpleName() + "]"
                                    + System.lineSeparator());
                        }
                    } else {
                        writer.write("Binary file: skipped" + System.lineSeparator());
                    }

                    writer.write("<<<END FILE>>>" + System.lineSeparator() + System.lineSeparator());

                    return FileVisitResult.CONTINUE;
                }

                private String toUnixPath(Path root, Path file) {
                    return root.relativize(file).toString().replace("\\", "/");
                }
            });

            log.info("Project dump completed: {}", outputPath);
        }
    }

    private static boolean isTextFile(Path file) throws IOException {
        if (Files.size(file) == 0) {
            return true;
        }

        try (InputStream is = Files.newInputStream(file)) {
            byte[] buffer = new byte[SAMPLE_SIZE];
            int bytesRead = is.read(buffer);
            if (bytesRead == -1) {
                return true;
            }

            int nonTextCount = 0;
            for (int i = 0; i < bytesRead; i++) {
                int b = buffer[i] & 0xFF;
                // Accepted bytes:
                // \t (9), \n (10), \r (13), \f (12)
                // printable ASCII 32-126
                // UTF-8 continuation bytes и high-ASCII (128-255)
                if (b < 9 || (b > 13 && b < 32) || b == 127) {
                    nonTextCount++;
                }
            }

            // If most that 5-10% "bad" bytes - it's binary file
            double badRatio = (double) nonTextCount / bytesRead;
            return badRatio <= BAD_BYTES_LIMIT;
        }
    }

    private static class Config {
        Path rootPath = Paths.get(DEFAULT_ROOT);
        Path outputPath = null;
        long maxFileSizeBytes = MAX_FILE_SIZE_BYTES;
        int maxFileLines = MAX_FILE_LINES;
        boolean useGitignore = true;
    }

    private static Config parseArgs(String[] args) {
        Config config = new Config();

        for (String arg : args) {
            if (arg.equals("--help")) {
                printUsage(0);
            } else if (arg.equals("--show-default-ignore")) {
                printDefaultIgnore();
            } else if (arg.startsWith("--path=")) {
                config.rootPath = Paths.get(arg.substring("--path=".length()));
            } else if (arg.startsWith("--output=")) {
                config.outputPath = Paths.get(arg.substring("--output=".length()));
            } else if (arg.startsWith("--max-file-size=")) {
                config.maxFileSizeBytes = parseSize(arg.substring("--max-file-size=".length()));
            } else if (arg.startsWith("--max-file-lines=")) {
                config.maxFileLines = parseLines(arg.substring("--max-file-lines=".length()));
            } else if (arg.equals("--no-gitignore")) {
                config.useGitignore = false;
            } else {
                System.err.println("Unknown argument: " + arg + System.lineSeparator());
                printUsage(1);
            }
        }

        if (config.outputPath == null) {
            config.outputPath = config.rootPath.resolve(DEFAULT_OUTPUT_FILENAME);
        }

        return config;
    }

    private static long parseSize(String sizeString) {
        sizeString = sizeString.toUpperCase().trim();
        long size = 0;
        try {
            if (sizeString.endsWith("KB")) {
                size = Long.parseLong(sizeString.replace("KB", "").trim()) * 1024;
            } else if (sizeString.endsWith("MB")) {
                size = Long.parseLong(sizeString.replace("MB", "").trim()) * 1024 * 1024;
            } else {
                size = Long.parseLong(sizeString);      // bytes by default
            }
        } catch (NumberFormatException e) {
            log.error("Invalid size format");
            System.exit(1);
        }
        return size;
    }

    private static int parseLines(String linesString) {
        int lines = 0;
        try {
            lines = Integer.parseInt(linesString);
        } catch (NumberFormatException e) {
            log.error("Invalid lines format");
            System.exit(1);
        }
        return lines;
    }

    private static void printUsage(int exitStatus) {
        String helpString = """
                Usage: java -jar project-dumper.jar [options]
                
                Options:
                  --path=<path>             Root path of project (default: current dir)
                  --output=<file>           Output dump file (default: projectdump.txt in root)
                  --max-file-size=<size>    Max file size (e.g., 1Mb, 4000kb, default: 1Mb)
                  --max-file-lines=<n>      Max lines in file to read (default: 2000)
                  --no-gitignore            Do not include .gitignore rules
                  --show-default-ignore     Show list of default ignored dirs and files
                  --help                    Show this help
                
                See details on https://github.com/agorohovcom/java-project-dumper
                """;
        System.out.println(helpString);
        System.exit(exitStatus);
    }

    private static void printDefaultIgnore() {
        try (InputStream is = ProjectDumper.class.getResourceAsStream(DEFAULT_IGNORE_RESOURCE)) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    reader.lines()
                            .map(String::trim)
                            .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                            .forEach(System.out::println);
                }
            } else {
                System.out.println("Default ignores not found");
            }
        } catch (IOException e) {
            System.out.println("Error loading default ignores");
            System.exit(1);
        }

        System.exit(0);
    }
}