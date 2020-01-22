package com.liquidlabs.logscape.uploader.fixture;

import com.liquidlabs.logscape.uploader.Storage;
import org.eclipse.microprofile.config.spi.Converter;

public class FixturedStorageConverter implements Converter<Storage> {

    @Override
    public Storage convert(String s) {
        return new FixturedStorageService();
    }
}
