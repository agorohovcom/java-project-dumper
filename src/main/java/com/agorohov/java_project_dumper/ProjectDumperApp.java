package com.agorohov.java_project_dumper;

import org.eclipse.jgit.ignore.IgnoreNode;

import java.io.IOException;

public final class ProjectDumperApp {

    private ProjectDumperApp() {
    }

    public static void execute(String[] args) {
        try {
            Config config = parseArguments(args);
            IgnoreNode ignoreNode = loadIgnoreRules(config);
            dumpProject(config, ignoreNode);
            logSuccess(config);
        } catch (Exception e) {
            handleError(e);
            System.exit(1);
        }
    }

    private static Config parseArguments(String[] args) {
        return ArgumentParser.parse(args);
    }

    private static IgnoreNode loadIgnoreRules(Config config) throws IOException {
        return IgnoreLoader.loadIgnoreNode(config.getRootPath(), config.isUseGitignore());
    }

    private static void dumpProject(Config config, IgnoreNode ignoreNode) throws IOException {
        FileDumper.dump(config, ignoreNode);
    }

    private static void logSuccess(Config config) {
        System.out.println("Project dump completed: " + config.getOutputPath());
    }

    private static void handleError(Exception e) {
        System.err.println("Unexpected error: " + e.getMessage());
    }
}
