package com.liquidlabs.logscape.uploader;

import com.liquidlabs.logscape.uploader.aws.AwsFileMetaDataQueryService;
import com.liquidlabs.logscape.uploader.fixture.FixturedFileMetaDataQueryService;
import io.quarkus.runtime.LaunchMode;
import org.eclipse.microprofile.config.spi.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

public class QueryFactoryConvertor implements Converter<FileMetaDataQueryService> {

    private final Logger log = LoggerFactory.getLogger(QueryFactoryConvertor.class);
    @Override
    public FileMetaDataQueryService convert(String mode) {

        log.info("Mode:" + mode);

        if (mode.equalsIgnoreCase(LaunchMode.TEST.name())) {
            return new FixturedFileMetaDataQueryService();
        }

        // TODO: Why isnt AwsFileMetXXX available as a bean?
        return new AwsFileMetaDataQueryService();
    }
}
