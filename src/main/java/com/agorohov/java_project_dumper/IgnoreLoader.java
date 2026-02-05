package com.agorohov.java_project_dumper;

import org.eclipse.jgit.ignore.IgnoreNode;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class IgnoreLoader {

    private IgnoreLoader() {}

    public static IgnoreNode loadIgnoreNode(Path rootPath, boolean useGitignore) throws IOException {
        IgnoreNode node = new IgnoreNode();

        // User's .projectdumperignore
        Path ignoreFile = rootPath.resolve(Constants.IGNORE_FILENAME);
        if (Files.exists(ignoreFile)) {
            try (BufferedReader reader = Files.newBufferedReader(ignoreFile, StandardCharsets.UTF_8)) {
                loadIgnoreRules(reader, node);
            } catch (IOException e) {
                System.err.println("Error reading " + Constants.IGNORE_FILENAME + ": " + e.getMessage());
            }
        }

        // .gitignore
        if (useGitignore) {
            Path gitIgnore = rootPath.resolve(Constants.GITIGNORE_FILENAME);
            if (Files.exists(gitIgnore)) {
                try (BufferedReader reader = Files.newBufferedReader(gitIgnore, StandardCharsets.UTF_8)) {
                    loadIgnoreRules(reader, node);
                } catch (IOException e) {
                    System.err.println("Error reading .gitignore (" + Constants.GITIGNORE_FILENAME + "): " + e.getMessage());
                }
            }
        }

        // Default ignores
        try (InputStream is = FileDumper.class.getResourceAsStream(Constants.DEFAULT_IGNORE_RESOURCE)) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    loadIgnoreRules(reader, node);
                }
            } else {
                System.out.println("Warning: Resource " + Constants.DEFAULT_IGNORE_RESOURCE + " not found in JAR");
            }
        } catch (IOException e) {
            System.err.println("Error loading default ignore patterns: " + e.getMessage());
        }

        return node;
    }

    private static void loadIgnoreRules(BufferedReader reader, IgnoreNode node) throws IOException {
        StringBuilder sb = new StringBuilder();

        reader.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .forEach(p -> sb.append(p).append(System.lineSeparator()));

        try (InputStream in = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8))) {
            node.parse(in);
        }
    }
}
