package com.agorohov.java_project_dumper;

import org.eclipse.jgit.ignore.IgnoreNode;

import java.io.IOException;

public final class ProjectDumperApp {

    private ProjectDumperApp() {
    }

    public static void execute(String[] args) {
        try {
            Config config = parseArguments(args);
            checkRunMode(config);
            LoggingConfigurator.configure(config.isDebug());
            IgnoreNode ignoreNode = loadIgnoreRules(config);
            DumpStats stats = dumpProject(config, ignoreNode);
            ConsolePrinter.printSuccess(config, stats);
        } catch (Exception e) {
            ConsolePrinter.handleError(e);
        }
    }

    private static Config parseArguments(String[] args) {
        return ArgumentParser.parse(args);
    }

    private static void checkRunMode(Config config) {
        switch (config.getRunMode()) {
            case SHOW_HELP -> ConsolePrinter.printUsage(0);
            case SHOW_DEFAULT_IGNORE -> ConsolePrinter.printDefaultIgnore();
            default -> config.validateConfig();
        }
    }

    private static IgnoreNode loadIgnoreRules(Config config) throws IOException {
        return IgnoreLoader.loadIgnoreNode(config.getRootPath(), config.isUseGitignore());
    }

    private static DumpStats dumpProject(Config config, IgnoreNode ignoreNode) throws IOException {
        return FileDumper.dump(config, ignoreNode);
    }
}
