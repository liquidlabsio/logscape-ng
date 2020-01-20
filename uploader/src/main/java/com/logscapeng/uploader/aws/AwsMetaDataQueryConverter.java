package com.logscapeng.uploader.aws;

import com.logscapeng.uploader.FileMetaDataQueryService;
import com.logscapeng.uploader.fixture.FixturedFileMetaDataQueryService;
import com.logscapeng.uploader.fixture.FixturedStorageService;
import org.eclipse.microprofile.config.spi.Converter;

/**
 * TODO: Replace with @Alternative
 */
public class AwsMetaDataQueryConverter implements Converter<FileMetaDataQueryService> {
    @Override
    public FileMetaDataQueryService convert(String s) {
        if (s.equalsIgnoreCase("TEST")) {
            return new FixturedFileMetaDataQueryService();
        }
        return new AwsFileMetaDataQueryService();
    }
}
