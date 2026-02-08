package com.agorohov.java_project_dumper;

public enum Options {
    HELP("--help"),
    SHOW_DEFAULT_IGNORE("--show-default-ignore"),
    PATH("--path="),
    OUTPUT("--output="),
    MAX_FILE_SIZE("--max-file-size="),
    MAX_FILE_LINES("--max-file-lines="),
    NO_GITIGNORE("--no-gitignore"),
    DEBUG("--debug");

    private final String value;

    Options(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
