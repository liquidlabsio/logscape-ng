package com.liquidlabs.logscape.uploader.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.liquidlabs.logscape.uploader.FileMeta;
import com.liquidlabs.logscape.uploader.Storage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class AwsS3StorageService implements Storage {

    private final Logger log = LoggerFactory.getLogger(AwsS3StorageService.class);

    @ConfigProperty(name = "logscape.prefix", defaultValue = "logscape.")
    private String PREFIX;


    public AwsS3StorageService(){
        log.info("Created");
    }

    @Override
    public FileMeta upload(final String region, final FileMeta upload) {
        bind();

        String bucketName = getBucketName(upload.getTenant());
        String filePath = upload.resource + "/" + upload.filename;

        log.info("uploading:" + upload + " bucket:" + upload.getTenant());

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.addUserMetadata("tags", upload.getTags().toString());
        objectMetadata.addUserMetadata("tenant", upload.tenant);
        objectMetadata.addUserMetadata("length", "" + upload.fileContent.length);


        File file = createTempFile(upload.fileContent);
        long contentLength = file.length();
        long partSize = 5 * 1024 * 1024; // Set part size to 5 MB.

        try {
            AmazonS3 s3Client = getAmazonS3Client(region);


            if (!s3Client.doesBucketExistV2(bucketName)) {
                log.info("Bucket:{} doesnt exist, creating", bucketName);
                Bucket bucket = s3Client.createBucket(bucketName);
            }

            // Create a list of ETag objects. You retrieve ETags for each object part uploaded,
            // then, after each individual part has been uploaded, pass the list of ETags to
            // the request to complete the upload.
            List<PartETag> partETags = new ArrayList<>();

            // Initiate the multipart upload.
            InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, filePath, objectMetadata);
            InitiateMultipartUploadResult initResponse = s3Client.initiateMultipartUpload(initRequest);


            // Upload the file parts.
            long filePosition = 0;
            for (int i = 1; filePosition < upload.fileContent.length; i++) {
                // Because the last part could be less than 5 MB, adjust the part size as needed.
                partSize = Math.min(partSize, (contentLength - filePosition));

                // Create the request to upload a part.
                UploadPartRequest uploadRequest = new UploadPartRequest()
                        .withBucketName(bucketName)
                        .withKey(filePath)
                        .withUploadId(initResponse.getUploadId())
                        .withPartNumber(i)
                        .withFileOffset(filePosition)
                        .withFile(file)
                        .withPartSize(partSize);

                // Upload the part and add the response's ETag to our list.
                UploadPartResult uploadResult = s3Client.uploadPart(uploadRequest);
                partETags.add(uploadResult.getPartETag());

                filePosition += partSize;
            }

            log.debug("ETags:" + partETags);
            // Complete the multipart upload.
            CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(bucketName, filePath,
                    initResponse.getUploadId(), partETags);
            CompleteMultipartUploadResult completeMultipartUploadResult = s3Client.completeMultipartUpload(compRequest);

            upload.storageUrl = completeMultipartUploadResult.getLocation();

        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            log.error("AmazonServiceException S3 Upload failed to process:{}", upload, e);
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            log.error("SdkClientException S3 not responding:{}", upload, e);
        } finally {
            file.delete();
        }

        return upload;
    }

    private String getBucketName(String tenant) {
        return (PREFIX + "." + tenant).toLowerCase();
    }

    private static AmazonS3 getAmazonS3Client(String region) {
        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
//                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .build();
    }

    synchronized private void bind() {
        if (PREFIX == null) {
            PREFIX = ConfigProvider.getConfig().getValue("logscape.prefix", String.class);
        }
    }

    /**
     * TODO: Seek to offset/compression-handling[on-off]/direct-download etd
     * @param region
     * @param tenant
     * @param storageUrl
     * @return
     */
    @Override
    public byte[] get(String region, String tenant, String storageUrl) {
        bind();


        try {
            AmazonS3 s3Client = getAmazonS3Client(region);
            S3Object s3object = s3Client.getObject(getBucketName(tenant), new URI(storageUrl).getPath().substring(1));
            S3ObjectInputStream inputStream = s3object.getObjectContent();

            return IOUtils.toByteArray(inputStream);

        } catch (Exception e) {
            log.error("Failed to retrieve {} / {}", tenant, storageUrl, e);
            throw new RuntimeException(e);
        }
    }

    private File createTempFile(byte[] filecontent) {
        try {
            File tempFile = File.createTempFile("test", ".tmp");
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(filecontent);
            fos.flush();
            fos.close();
            return tempFile;
        } catch (IOException e) {
            log.error("Failed to created temp file:{}", e);
            e.printStackTrace();
        }
        throw new RuntimeException("Failed to create temp file");
    }


    public static void main(String[] args) throws IOException {
        Regions clientRegion = Regions.DEFAULT_REGION;
        String bucketName = "*** Bucket name ***";
        String keyName = "*** Key name ***";
        String filePath = "*** Path to file to upload ***";

        File file = new File(filePath);
        long contentLength = file.length();
        long partSize = 5 * 1024 * 1024; // Set part size to 5 MB.

        try {
            AmazonS3 s3Client = getAmazonS3Client(clientRegion.getName());

            // Create a list of ETag objects. You retrieve ETags for each object part uploaded,
            // then, after each individual part has been uploaded, pass the list of ETags to
            // the request to complete the upload.
            List<PartETag> partETags = new ArrayList<PartETag>();

            // Initiate the multipart upload.
            InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, keyName);
            InitiateMultipartUploadResult initResponse = s3Client.initiateMultipartUpload(initRequest);

            // Upload the file parts.
            long filePosition = 0;
            for (int i = 1; filePosition < contentLength; i++) {
                // Because the last part could be less than 5 MB, adjust the part size as needed.
                partSize = Math.min(partSize, (contentLength - filePosition));

                // Create the request to upload a part.
                UploadPartRequest uploadRequest = new UploadPartRequest()
                        .withBucketName(bucketName)
                        .withKey(keyName)
                        .withUploadId(initResponse.getUploadId())
                        .withPartNumber(i)
                        .withFileOffset(filePosition)
                        .withFile(file)
                        .withPartSize(partSize);

                // Upload the part and add the response's ETag to our list.
                UploadPartResult uploadResult = s3Client.uploadPart(uploadRequest);
                partETags.add(uploadResult.getPartETag());

                filePosition += partSize;
            }

            // Complete the multipart upload.
            CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(bucketName, keyName,
                    initResponse.getUploadId(), partETags);
            s3Client.completeMultipartUpload(compRequest);
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }
    }


}