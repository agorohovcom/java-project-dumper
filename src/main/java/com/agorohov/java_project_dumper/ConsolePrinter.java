package com.agorohov.java_project_dumper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class ConsolePrinter {

    private ConsolePrinter() {}

    public static void printUsage(int exitStatus) {
        String helpString = Constants.HELP_STRING;
        System.out.println(helpString);
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
}
