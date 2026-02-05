package com.agorohov.java_project_dumper;

import org.eclipse.jgit.ignore.IgnoreNode;

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

    private FileDumper() {}

    public static void dump(Config config, IgnoreNode ignoreNode) throws IOException {
        Path rootPath = config.getRootPath();
        Path outputPath = config.getOutputPath();

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

                                if (bytesRead > config.getMaxFileSizeBytes()) {
                                    writer.write("[Truncated: size limit exceeded]" + System.lineSeparator());
                                    break;
                                }

                                if (lineCount >= config.getMaxFileLines()) {
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

            System.out.println("Project dump completed: " + outputPath);
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