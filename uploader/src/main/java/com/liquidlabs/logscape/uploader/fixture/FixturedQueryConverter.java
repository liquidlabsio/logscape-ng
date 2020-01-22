package com.liquidlabs.logscape.uploader.fixture;

import com.liquidlabs.logscape.uploader.FileMetaDataQueryService;
import org.eclipse.microprofile.config.spi.Converter;

public class FixturedQueryConverter implements Converter<FileMetaDataQueryService> {
    @Override
    public FileMetaDataQueryService convert(String s) {
        return new FixturedFileMetaDataQueryService();
    }
}
