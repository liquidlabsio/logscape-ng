package com.liquidlabs.logscape.uploader;

import com.liquidlabs.logscape.uploader.aws.AwsS3StorageService;
import com.liquidlabs.logscape.uploader.fixture.FixturedStorageService;
import io.quarkus.runtime.LaunchMode;
import org.eclipse.microprofile.config.spi.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageFactoryConvertor implements Converter<Storage> {
    private final Logger log = LoggerFactory.getLogger(StorageFactoryConvertor.class);
    @Override
    public Storage convert(String mode) {
        log.info("Mode:" + mode);
        if (mode.equalsIgnoreCase(LaunchMode.TEST.name())) {
            return new FixturedStorageService();
        }
        return new AwsS3StorageService();
    }
}
