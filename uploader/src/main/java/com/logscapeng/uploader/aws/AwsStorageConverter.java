package com.logscapeng.uploader.aws;

import com.logscapeng.uploader.Storage;
import com.logscapeng.uploader.fixture.FixturedStorageService;
import io.quarkus.runtime.configuration.ProfileManager;
import org.eclipse.microprofile.config.spi.Converter;

public class AwsStorageConverter implements Converter<Storage> {
    @Override
    public Storage convert(String s) {
        if (s.equals("TEST")) {
            return new FixturedStorageService();
        }
        return new AwsS3StorageService();
    }
}
