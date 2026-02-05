package com.agorohov.java_project_dumper;

import org.eclipse.jgit.ignore.IgnoreNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class IgnoreLoader {

    private IgnoreLoader() {}

    private static final Logger log = LoggerFactory.getLogger(IgnoreLoader.class);

    public static IgnoreNode loadIgnoreNode(Path rootPath, boolean useGitignore) throws IOException {
        IgnoreNode node = new IgnoreNode();

        // Default ignores
        try (InputStream is = FileDumper.class.getResourceAsStream(Constants.DEFAULT_IGNORE_RESOURCE)) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    int rulesCount = loadIgnoreRules(reader, node);
                    logDebugLoadedRules(rulesCount, Constants.DEFAULT_IGNORE_RESOURCE);
                }
            } else {
                log.warn("Resource {} not found in JAR", Constants.DEFAULT_IGNORE_RESOURCE);
            }
        } catch (IOException e) {
            log.error("Error loading default ignore patterns: {}", e.getMessage());
        }

        // .gitignore
        if (useGitignore) {
            Path gitIgnore = rootPath.resolve(Constants.GITIGNORE_FILENAME);
            if (Files.exists(gitIgnore)) {
                try (BufferedReader reader = Files.newBufferedReader(gitIgnore, StandardCharsets.UTF_8)) {
                    int rulesCount = loadIgnoreRules(reader, node);
                    logDebugLoadedRules(rulesCount, Constants.GITIGNORE_FILENAME);
                } catch (IOException e) {
                    log.debug("Error reading .gitignore ({}): {}", Constants.GITIGNORE_FILENAME, e.getMessage());
                }
            }
        }

        // User's .projectdumperignore
        Path ignoreFile = rootPath.resolve(Constants.IGNORE_FILENAME);
        if (Files.exists(ignoreFile)) {
            try (BufferedReader reader = Files.newBufferedReader(ignoreFile, StandardCharsets.UTF_8)) {
                int rulesCount = loadIgnoreRules(reader, node);
                logDebugLoadedRules(rulesCount, Constants.IGNORE_FILENAME);
            } catch (IOException e) {
                log.error("Error reading {}: {}", Constants.IGNORE_FILENAME, e.getMessage());
            }
        }

        return node;
    }

    private static int loadIgnoreRules(BufferedReader reader, IgnoreNode node) throws IOException {
        List<String> rules = reader.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .toList();

        String content = String.join(System.lineSeparator(), rules) + System.lineSeparator();

        try (InputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            node.parse(in);
        }

        return rules.size();
    }

    private static void logDebugLoadedRules(int rulesCount, String rulesSource) {
        log.debug("Loaded {} rules from {}", rulesCount, rulesSource);
    }
}
