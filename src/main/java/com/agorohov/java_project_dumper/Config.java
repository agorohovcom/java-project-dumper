package com.agorohov.java_project_dumper;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {

    private Path rootPath = Paths.get(Constants.DEFAULT_ROOT);
    private Path outputPath = null;
    private long maxFileSizeBytes = Constants.MAX_FILE_SIZE_BYTES_DEFAULT;
    private int maxFileLines = Constants.MAX_FILE_LINES_DEFAULT;
    private boolean useGitignore = true;

    public Path getRootPath() {
        return rootPath;
    }

    public void setRootPath(Path rootPath) {
        this.rootPath = rootPath;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(Path outputPath) {
        this.outputPath = outputPath;
    }

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public int getMaxFileLines() {
        return maxFileLines;
    }

    public void setMaxFileLines(int maxFileLines) {
        this.maxFileLines = maxFileLines;
    }

    public boolean isUseGitignore() {
        return useGitignore;
    }

    public void setUseGitignore(boolean useGitignore) {
        this.useGitignore = useGitignore;
    }
}
