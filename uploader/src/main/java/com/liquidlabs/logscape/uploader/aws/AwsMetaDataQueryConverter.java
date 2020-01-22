package com.liquidlabs.logscape.uploader.aws;

import com.liquidlabs.logscape.uploader.FileMetaDataQueryService;
import com.liquidlabs.logscape.uploader.fixture.FixturedFileMetaDataQueryService;
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
