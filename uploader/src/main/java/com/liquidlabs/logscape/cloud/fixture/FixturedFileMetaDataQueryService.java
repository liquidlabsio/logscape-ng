package com.liquidlabs.logscape.cloud.fixture;

import com.liquidlabs.logscape.cloud.query.FileMeta;
import com.liquidlabs.logscape.cloud.query.FileMetaDataQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FixturedFileMetaDataQueryService implements FileMetaDataQueryService {

    private final Logger log = LoggerFactory.getLogger(FixturedFileMetaDataQueryService.class);

    public static final Map<String, FileMeta> storage = new HashMap<>();

    public FixturedFileMetaDataQueryService(){
        log.info("Created");
    }

    @Override
    public void put(FileMeta fileMeta) {
        storage.put(fileMeta.filename, fileMeta);
    }

    @Override
    public FileMeta find(String tenant, String filename) {
        return storage.get(filename);
    }

    @Override
    public byte[] get(String tenant, String filename) {
        return storage.get(filename).getFileContent();
    }

    @Override
    public FileMeta delete(String tenant, String filename) {
        return storage.remove(filename);
    }

    @Override
    public List<FileMeta> query(String tenant, String filenamePart, String tagNamePart) {
        return storage.values()
                .stream().filter(entry -> entry.getFilename().contains(filenamePart))
                .collect(Collectors.toList());
    }

    @Override
    public List<FileMeta> list() {
        return new ArrayList<>(storage.values());
    }
}
