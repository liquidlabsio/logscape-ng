package com.logscapeng.uploader.fixture;

import com.logscapeng.uploader.Storage;
import org.eclipse.microprofile.config.spi.Converter;

public class FixturedStorageConverter implements Converter<Storage> {

    @Override
    public Storage convert(String s) {
        return new FixturedStorageService();
    }
}
