package com.logscapeng.uploader;

public interface Storage {
    /**
     * Store and Capture the Storage URL
     * @param upload
     * @param region
     * @return
     */
    FileMeta upload(FileMeta upload, String region);

    byte[] get(String storageUrl);
}
