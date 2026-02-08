package com.agorohov.java_project_dumper;

import org.eclipse.jgit.ignore.IgnoreNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class FileDumper {

    private static final Logger log = LoggerFactory.getLogger(FileDumper.class);

    private FileDumper() {}

    public static DumpStats dump(Config config, IgnoreNode ignoreNode) throws IOException {
        Path rootPath = config.getRootPath();
        Path outputPath = config.getOutputPath();

        log.debug("Starting project dump: root={}, output={}",
                rootPath.toAbsolutePath(), outputPath.toAbsolutePath());

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.write(Constants.SEPARATOR + System.lineSeparator());
            writer.write("Java project dump " + timestamp + System.lineSeparator());
            writer.write(Constants.SEPARATOR + System.lineSeparator() + System.lineSeparator());

            DumpStats stats = new DumpStats();

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
                        stats.incSkippedDirs();
                        log.debug("Skipping directory by ignore rule: {}", pathStr);
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

                    stats.incTotalFiles();
                    String pathStr = toUnixPath(rootPath, file);

                    Boolean ignored = ignoreNode.checkIgnored(pathStr, false);
                    if (Boolean.TRUE.equals(ignored)) {
                        stats.incIgnoredTextFiles();
                        log.debug("Ignoring file by rule: {}", pathStr);
                        return FileVisitResult.CONTINUE;
                    }

                    writer.write("<<<FILE: " + toUnixPath(rootPath, file) + ">>>" + System.lineSeparator());

                    if (isTextFile(file)) {
                        stats.incTextFiles();
                        log.debug("Dumping text file: {}", file);
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

                                if (bytesRead > config.getMaxFileSizeBytes()) {
                                    writer.write("[Truncated: size limit exceeded]" + System.lineSeparator());
                                    stats.incTruncatedBySize();
                                    log.debug("File truncated by size limit: {}", pathStr);
                                    break;
                                }

                                if (lineCount >= config.getMaxFileLines()) {
                                    writer.write("[Truncated: line limit exceeded]" + System.lineSeparator());
                                    stats.incTruncatedByLines();
                                    log.debug("File truncated by line limit: {}", pathStr);
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            writer.write("[Cannot read file: "
                                    + e.getClass().getSimpleName() + "]"
                                    + System.lineSeparator());
                            log.warn("Cannot read file {}: {}", file, e.getMessage());
                        }
                    } else {
                        writer.write("Binary file: skipped" + System.lineSeparator());
                        stats.incBinarySkipped();
                        log.debug("Detected binary file: {}", file);
                    }

                    writer.write("<<<END FILE>>>" + System.lineSeparator() + System.lineSeparator());

                    return FileVisitResult.CONTINUE;
                }

                private String toUnixPath(Path root, Path file) {
                    return root.relativize(file).toString().replace("\\", "/");
                }
            });

            writer.write(System.lineSeparator());
            writer.write(Constants.SEPARATOR);
            writer.write(System.lineSeparator());
            writer.write(System.lineSeparator());
            writer.write(stats.toString());
            writer.write(System.lineSeparator());
            writer.write(Constants.SEPARATOR);
            writer.write(System.lineSeparator());
            writer.write(System.lineSeparator());
            writer.write("Powered by Java Project Dumper © agorohovcom");
            writer.write(System.lineSeparator());
            writer.write(Constants.APP_URL);
            writer.write(System.lineSeparator());

            return stats;
        }
    }

    private static boolean isTextFile(Path file) throws IOException {
        if (Files.size(file) == 0) {
            return true;
        }

        try (InputStream is = Files.newInputStream(file)) {
            byte[] buffer = new byte[Constants.SAMPLE_SIZE];
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
            return badRatio <= Constants.BAD_BYTES_LIMIT;
        }
    }
}