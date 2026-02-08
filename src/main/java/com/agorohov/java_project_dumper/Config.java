package com.agorohov.java_project_dumper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {

    private RunMode runMode = RunMode.DUMP;
    private Path rootPath = Paths.get(Constants.DEFAULT_ROOT);
    private Path outputPath = null;
    private long maxFileSizeBytes = Constants.MAX_FILE_SIZE_BYTES_DEFAULT;
    private int maxFileLines = Constants.MAX_FILE_LINES_DEFAULT;
    private boolean useGitignore = true;
    private boolean debug = false;

    public void validateConfig() {
        Path root = this.getRootPath();
        if (!Files.exists(root)) {
            throw new IllegalArgumentException("Root path is not exists: " + root);
        }
        if (!Files.isDirectory(root)) {
            throw new IllegalArgumentException("Path is not a directory: " + root);
        }
        if (!Files.isReadable(root)) {
            throw new IllegalArgumentException("Path is not accessible: " + root);
        }

        Path out = this.getOutputPath();
        Path parent = out.getParent();
        if (parent != null) {
            if (!Files.exists(parent)) {
                throw new IllegalArgumentException("Directory for the output file is not exists: " + parent);
            }
            if (!Files.isWritable(parent)) {
                throw new IllegalArgumentException("No write permissions to: " + parent);
            }
        }

        if (this.getMaxFileSizeBytes() <= 0) {
            throw new IllegalArgumentException("max-file-size must be > 0");
        }
        if (this.getMaxFileLines() < 1) {
            throw new IllegalArgumentException("max-file-lines must be ≥ 1");
        }
    }

    public RunMode getRunMode() {
        return runMode;
    }

    public void setRunMode(RunMode runMode) {
        this.runMode = runMode;
    }

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

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
