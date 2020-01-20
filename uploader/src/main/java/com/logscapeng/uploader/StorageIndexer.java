package com.logscapeng.uploader;

public interface StorageIndexer {

    /**
     * Capture
     * - uncompessed file size - capture if needed
     * - compressed file size
     * - eoln character
     * - number of lines
     * - time-from
     * - time-to
     * @param fileMeta
     * @return
     */
    FileMeta enrichMeta(FileMeta fileMeta);

    FileMeta index(FileMeta fileMeta, String cloudRegion);
}
