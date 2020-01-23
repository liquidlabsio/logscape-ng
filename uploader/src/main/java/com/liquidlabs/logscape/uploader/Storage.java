package com.liquidlabs.logscape.uploader;

public interface Storage {
    /**
     * Store and Capture the Storage URL
     * @param region
     * @param upload
     * @return
     */
    FileMeta upload(String region, FileMeta upload);

    byte[] get(String region, String tenant, String storageUrl);
}
