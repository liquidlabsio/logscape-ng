package com.liquidlabs.logscape.cloud;

import com.liquidlabs.logscape.cloud.fixture.FixturedIndexerService;
import com.liquidlabs.logscape.cloud.storage.StorageIndexer;
import org.eclipse.microprofile.config.spi.Converter;

public class IndexingFactoryConverter implements Converter<StorageIndexer> {
    @Override
    public StorageIndexer convert(String s) {
        return new FixturedIndexerService();
    }
}
