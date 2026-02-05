package com.agorohov.java_project_dumper;

public class DumpStats {
    private int totalFiles = 0;
    private int textFiles = 0;
    private int binarySkipped = 0;
    private int skippedDirs = 0;
    private int ignoredTextFiles = 0;
    private int truncatedBySize = 0;
    private int truncatedByLines = 0;

    public void incTotalFiles() {
        totalFiles++;
    }

    public void incTextFiles() {
        textFiles++;
    }

    public void incIgnoredTextFiles() {
        ignoredTextFiles++;
    }

    public void incBinarySkipped() {
        binarySkipped++;
    }

    public void incSkippedDirs() {
        skippedDirs++;
    }

    public void incTruncatedBySize() {
        truncatedBySize++;
    }

    public void incTruncatedByLines() {
        truncatedByLines++;
    }

    @Override
    public String toString() {
        return """
                Dump statistics:
                  Total files considered:   %d
                  Text files dumped:        %d
                  Binary files skipped:     %d
                  Ignored directories:      %d
                  Ignored text files:       %d
                  Truncated by size limit:  %d
                  Truncated by line limit:  %d
                """.formatted(
                totalFiles,
                textFiles,
                binarySkipped,
                skippedDirs,
                ignoredTextFiles,
                truncatedBySize,
                truncatedByLines
        );
    }
}
