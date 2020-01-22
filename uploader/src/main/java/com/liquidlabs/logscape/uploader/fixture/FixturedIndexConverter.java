package com.liquidlabs.logscape.uploader.fixture;

import com.liquidlabs.logscape.uploader.StorageIndexer;
import org.eclipse.microprofile.config.spi.Converter;

public class FixturedIndexConverter implements Converter<StorageIndexer> {
    @Override
    public StorageIndexer convert(String s) {
        return new FixturedIndexerService();
    }
}
