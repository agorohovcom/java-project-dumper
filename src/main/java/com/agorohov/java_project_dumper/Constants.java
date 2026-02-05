package com.agorohov.java_project_dumper;

public final class Constants {

    private Constants() {}

    public static final String DEFAULT_ROOT = ".";
    public static final long MAX_FILE_SIZE_BYTES_DEFAULT = 1024 * 1024;     // 1 Mb
    public static final int MAX_FILE_LINES_DEFAULT = 2000;

    public static final int SAMPLE_SIZE = 8192;                             // 8 kb
    public static final double BAD_BYTES_LIMIT = 0.1;                       // 10%

    public static final String DEFAULT_OUTPUT_FILENAME = "projectdump.txt";
    public static final String DEFAULT_IGNORE_RESOURCE = "/default-ignore.patterns";

    public static final String GITIGNORE_FILENAME = ".gitignore";
    public static final String IGNORE_FILENAME = ".projectdumperignore";

    public static final String HELP_STRING = """
                Usage: java -jar project-dumper.jar [options]
                
                Options:
                  --path=<path>             Root path of project (default: current dir)
                  --output=<file>           Output dump file (default: projectdump.txt in root)
                  --max-file-size=<size>    Max file size (e.g., 1Mb, 4000kb, default: 1Mb)
                  --max-file-lines=<n>      Max lines in file to read (default: 2000)
                  --no-gitignore            Do not include .gitignore rules
                  --debug                   Print DEBUG logs
                  --show-default-ignore     Show list of default ignored dirs and files
                  --help                    Show this help
                
                See details on https://github.com/agorohovcom/java-project-dumper
                """;

    public static final String SEPARATOR = "-------------------------------------";
    public static final String APP_URL = "https://github.com/agorohovcom/java-project-dumper";
}
