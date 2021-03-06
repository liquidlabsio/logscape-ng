package com.liquidlabs.logscape.cloud.storage;

import com.liquidlabs.logscape.cloud.query.FileMeta;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * First (naive) implementation.
 */
@Path("/storage")
public class StorageResource {

    private final Logger log = LoggerFactory.getLogger(StorageResource.class);

    @ConfigProperty(name = "cloud.region", defaultValue = "eu-west-2")
    String cloudRegion;

    @ConfigProperty(name = "logscape.storage.uploader")
    Storage uploader;

    @ConfigProperty(name = "logscape.storage.indexer")
    StorageIndexer indexer;

    @ConfigProperty(name = "logscape.storage.query")
    com.liquidlabs.logscape.cloud.query.FileMetaDataQueryService query;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String id() {
        return StorageResource.class.getCanonicalName();
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response uploadFile(@MultipartForm FileMeta fileMeta) {

        try {

            fileMeta.size = fileMeta.fileContent.length;

            // this series of actions should be put on an event queue
            com.liquidlabs.logscape.cloud.query.FileMeta indexedFile = indexer.enrichMeta(fileMeta);
            com.liquidlabs.logscape.cloud.query.FileMeta storedAndIndexedFile = uploader.upload(cloudRegion, indexedFile);
            // ideally we would trigger an indexing function from the S3 bucket write.
            // for now Im doing it in process here.
            com.liquidlabs.logscape.cloud.query.FileMeta stored = indexer.index(storedAndIndexedFile, cloudRegion);


            query.put(stored);

            Response.ResponseBuilder responseBuilder = Response.status(200).entity("Uploaded and Indexed:" + stored);
            return responseBuilder.build();
        } catch (Exception ex) {
            log.error("Error handling request:" + fileMeta, ex);
            Response.ResponseBuilder responseBuilder = Response.status(500).entity(ex.getMessage());
            return responseBuilder.build();
        }
    }

    @GET
    @Path("/import")
    @Produces(MediaType.APPLICATION_JSON)
    public List<com.liquidlabs.logscape.cloud.query.FileMeta> importFromStorage(@QueryParam("tenant") String tenant, @QueryParam("storageId") String storageId,
                                                                                @QueryParam("includeFileMask") String includeFileMask, @QueryParam("tags") String tags) {
        List<com.liquidlabs.logscape.cloud.query.FileMeta> imported = uploader.importFromStorage(cloudRegion, tenant, storageId, includeFileMask, tags);
        imported.stream().forEach(fileMeta -> query.put(indexer.index(fileMeta, cloudRegion)));
        return imported;
    }
    @GET
    @Path("/removeImported")
    @Produces(MediaType.APPLICATION_JSON)
    public List<com.liquidlabs.logscape.cloud.query.FileMeta> removeByStorageId(@QueryParam("tenant") String tenant, @QueryParam("storageId") String storageId,
                                                                                @QueryParam("includeFileMask") String includeFileMask) {
        List<com.liquidlabs.logscape.cloud.query.FileMeta> removed = uploader.removeByStorageId(cloudRegion, tenant, storageId, includeFileMask);
        removed.stream().forEach(fileMeta -> query.delete(fileMeta.getTenant(), fileMeta.getFilename()));
        return removed;
    }

}
