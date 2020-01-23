package com.liquidlabs.logscape.uploader;

import com.liquidlabs.logscape.uploader.StorageIndexer;
import com.liquidlabs.logscape.uploader.fixture.FixturedIndexerService;
import org.eclipse.microprofile.config.spi.Converter;

public class IndexingFactoryConverter implements Converter<StorageIndexer> {
    @Override
    public StorageIndexer convert(String s) {
        return new FixturedIndexerService();
    }
}
