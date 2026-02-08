package com.agorohov.java_project_dumper;

import java.nio.file.Paths;

import static com.agorohov.java_project_dumper.Options.DEBUG;
import static com.agorohov.java_project_dumper.Options.HELP;
import static com.agorohov.java_project_dumper.Options.MAX_FILE_LINES;
import static com.agorohov.java_project_dumper.Options.MAX_FILE_SIZE;
import static com.agorohov.java_project_dumper.Options.NO_GITIGNORE;
import static com.agorohov.java_project_dumper.Options.OUTPUT;
import static com.agorohov.java_project_dumper.Options.PATH;
import static com.agorohov.java_project_dumper.Options.SHOW_DEFAULT_IGNORE;

public final class ArgumentParser {

    private ArgumentParser() {}

    public static Config parse(String[] args) {
        Config config = new Config();

        for (String arg : args) {
            if (arg.equals(HELP.getValue())) {
                config.setRunMode(RunMode.SHOW_HELP);
                return config;
            } else if (arg.equals(SHOW_DEFAULT_IGNORE.getValue())) {
                config.setRunMode(RunMode.SHOW_DEFAULT_IGNORE);
                return config;
            } else if (arg.startsWith(PATH.getValue())) {
                config.setRootPath(Paths.get(arg.substring(PATH.getValue().length())));
            } else if (arg.startsWith(OUTPUT.getValue())) {
                config.setOutputPath(Paths.get(arg.substring(OUTPUT.getValue().length())));
            } else if (arg.startsWith(MAX_FILE_SIZE.getValue())) {
                config.setMaxFileSizeBytes(parseSize(arg.substring(MAX_FILE_SIZE.getValue().length())));
            } else if (arg.startsWith(MAX_FILE_LINES.getValue())) {
                config.setMaxFileLines(parseLines(arg.substring(MAX_FILE_LINES.getValue().length())));
            } else if (arg.equals(NO_GITIGNORE.getValue())) {
                config.setUseGitignore(false);
            } else if (arg.equals(DEBUG.getValue())) {
                config.setDebug(true);
            } else {
                throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }

        if (config.getOutputPath() == null) {
            config.setOutputPath(config.getRootPath().resolve(Constants.DEFAULT_OUTPUT_FILENAME));
        }

        return config;
    }

    private static long parseSize(String sizeString) {
        sizeString = sizeString.toUpperCase().trim();
        long size;
        try {
            if (sizeString.endsWith("KB")) {
                size = Long.parseLong(sizeString.replace("KB", "").trim()) * 1024;
            } else if (sizeString.endsWith("MB")) {
                size = Long.parseLong(sizeString.replace("MB", "").trim()) * 1024 * 1024;
            } else {
                size = Long.parseLong(sizeString);      // bytes by default
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid size format: " + sizeString);
        }
        return size;
    }

    private static int parseLines(String linesString) {
        int lines;
        try {
            lines = Integer.parseInt(linesString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid lines format");
        }
        return lines;
    }
}
