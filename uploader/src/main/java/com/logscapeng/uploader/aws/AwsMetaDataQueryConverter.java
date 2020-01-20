package com.logscapeng.uploader.aws;

import com.logscapeng.uploader.FileMetaDataQueryService;
import com.logscapeng.uploader.fixture.FixturedFileMetaDataQueryService;
import com.logscapeng.uploader.fixture.FixturedStorageService;
import io.quarkus.runtime.LaunchMode;
import org.eclipse.microprofile.config.spi.Converter;

/**
 * TODO: Replace with @Alternative
 */
public class AwsMetaDataQueryConverter implements Converter<FileMetaDataQueryService> {
    @Override
    public FileMetaDataQueryService convert(String mode) {
        if (mode.equalsIgnoreCase(LaunchMode.TEST.name())) {
            return new FixturedFileMetaDataQueryService();
        }
        return new AwsFileMetaDataQueryService();
    }
}
