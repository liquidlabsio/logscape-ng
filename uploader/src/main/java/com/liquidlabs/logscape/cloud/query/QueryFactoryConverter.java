package com.liquidlabs.logscape.cloud.query;

import com.liquidlabs.logscape.cloud.aws.AWS;
import com.liquidlabs.logscape.cloud.aws.AwsFileMetaDataQueryService;
import com.liquidlabs.logscape.cloud.fixture.FixturedFileMetaDataQueryService;
import io.quarkus.runtime.LaunchMode;
import org.eclipse.microprofile.config.spi.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryFactoryConverter implements Converter<FileMetaDataQueryService> {

    private final Logger log = LoggerFactory.getLogger(QueryFactoryConverter.class);
    @Override
    public FileMetaDataQueryService convert(String mode) {

        log.info("Mode:" + mode);

        if (mode.equalsIgnoreCase(LaunchMode.TEST.name())) {
            return new FixturedFileMetaDataQueryService();
        } else if (mode.equalsIgnoreCase(AWS.CONFIG)) {
            return new AwsFileMetaDataQueryService();
        }
        return new FixturedFileMetaDataQueryService();
    }
}
