package com.liquidlabs.logscape.uploader.fixture;

import com.liquidlabs.logscape.uploader.FileMeta;
import com.liquidlabs.logscape.uploader.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class FixturedStorageService implements Storage {
    private final Logger log = LoggerFactory.getLogger(FixturedStorageService.class);

    static Map<String, byte[]> storage = new HashMap<>();

    public FixturedStorageService(){
        log.info("Created");
    }

    @Override
    public FileMeta upload(FileMeta upload, String region) {
        log.info("uploading:" + upload);
        upload.setStorageUrl("s3://somebucket/"+region + "/" + upload.getFilename() + "-to-time=" + upload.getToTime());
        storage.put(upload.getStorageUrl(), upload.fileContent);
        upload.setFileContent(new byte[0]);
        return upload;
    }

    @Override
    public byte[] get(String storageUrl) {
        return storage.get(storageUrl);
    }
}
