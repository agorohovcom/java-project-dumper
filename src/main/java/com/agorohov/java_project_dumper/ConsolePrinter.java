package com.agorohov.java_project_dumper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class ConsolePrinter {

    private static final Logger log = LoggerFactory.getLogger(ConsolePrinter.class);

    private ConsolePrinter() {}

    public static void printUsage(int exitStatus) {
        System.out.println(Constants.HELP_STRING);
        System.exit(exitStatus);
    }

    public static void printDefaultIgnore() {
        try (InputStream is = ArgumentParser.class.getResourceAsStream(Constants.DEFAULT_IGNORE_RESOURCE)) {
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

    public static void printSuccess(Config config, DumpStats stats) {
        System.out.println("Project dump completed: " + config.getOutputPath());
        System.out.println();
        System.out.println(stats);
    }

    public static void handleError(Exception e) {
        System.err.println();
        if (e.getMessage() != null && !e.getMessage().isBlank()) {
            System.err.println("Error: " + e.getMessage());
        } else {
            System.err.println("Error occurred: " + e.getClass().getSimpleName());
        }

        log.debug("Error details: ", e);
        System.err.println();
        System.err.println("Run with the --help flag to get usage help");
        System.err.println();

        System.exit(1);
    }
}
