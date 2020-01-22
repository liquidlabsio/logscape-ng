package com.liquidlabs.logscape.uploader.aws;

import com.liquidlabs.logscape.uploader.Storage;
import com.liquidlabs.logscape.uploader.fixture.FixturedStorageService;
import io.quarkus.runtime.LaunchMode;
import org.eclipse.microprofile.config.spi.Converter;

public class AwsStorageConverter implements Converter<Storage> {
    @Override
    public Storage convert(String mode) {
        if (mode.equalsIgnoreCase(LaunchMode.TEST.name())) {
            return new FixturedStorageService();
        }
        return new AwsS3StorageService();
    }
}
