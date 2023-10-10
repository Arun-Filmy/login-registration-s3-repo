package com.amazon.amazon.service.impl;

import com.amazon.amazon.service.interfacePackage.S3WriteClientService;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

@Service
public class S3WriteClientServiceImpl implements S3WriteClientService {
    private final AmazonS3 s3Client;
    public S3WriteClientServiceImpl(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    //create bucket code here
    @Override
    public boolean createBucket(String bucketName) {
        boolean valid = isValidBucketName(bucketName);
        if (!s3Client.doesBucketExistV2(bucketName) && valid) {
            Bucket bucket = s3Client.createBucket(bucketName);
            return true;
        }else {
            return false;
        }
    }

    public static boolean isValidBucketName(String bucketName) {
        return bucketName.matches("^[a-z0-9][a-z0-9-]{1,61}[a-z0-9]$");
    }

    //create folder 
    @Override
    public boolean createDirectory(String bucketName, String folderName) {
        Set<String> allFiles = getAllFiles(bucketName);
        if(allFiles.contains(folderName)){
            return false;
        }
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0);
        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, folderName+"/", emptyContent, metadata);
        s3Client.putObject(putObjectRequest);
        return true;
    }

    @Override
    public boolean uploadFiles(String bucketName, String folderName, String fileName, String fileType, InputStream inputStream) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(fileType);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, folderName + "/" + fileName, inputStream, metadata);
        s3Client.putObject(putObjectRequest);
        return true;
    }
    @Override
    public S3Object getFile(String folderName, String fileName, String bucketName) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, folderName + "/" + fileName);
        return s3Client.getObject(getObjectRequest);
    }

    @Override
    public String deleteBucket(String bucketName) {
        System.out.println(" - removing objects from bucket");
        ObjectListing object_listing = s3Client.listObjects(bucketName);
        System.out.println(" - object listed");
        if (object_listing == null){
            s3Client.deleteBucket(bucketName);
            System.out.println(" - removed the empty bucket");
            return bucketName;
        }
        while (true) {
            for (Iterator<?> iterator =
                 object_listing.getObjectSummaries().iterator();
                 iterator.hasNext(); ) {
                S3ObjectSummary summary = (S3ObjectSummary) iterator.next();
                System.out.println(" - Inside while loop and now delete the object");
                s3Client.deleteObject(bucketName, summary.getKey());
            }
            System.out.println(" - removed object from s3 of aws");

            // more object_listing to retrieve?
            if (object_listing.isTruncated()) {
                object_listing = s3Client.listNextBatchOfObjects(object_listing);
            } else {
                break;
            }
        }
        s3Client.deleteBucket(bucketName);
        return bucketName;
    }
    @Override
    public String deleteFolder(String fullPath, String bucketName) {

        ObjectListing objectListing = s3Client.listObjects(bucketName, fullPath);

        while (true) {
            List<DeleteObjectsRequest.KeyVersion> keysToDelete = new ArrayList<>();

            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                keysToDelete.add(new DeleteObjectsRequest.KeyVersion(objectSummary.getKey()));
            }

            if (!keysToDelete.isEmpty()) {
                DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(bucketName).withKeys(keysToDelete);
                s3Client.deleteObjects(deleteRequest);
            }
            if (objectListing.isTruncated()) {
                objectListing = s3Client.listNextBatchOfObjects(objectListing);
            } else {
                break;
            }
        }

        s3Client.deleteObject(bucketName, fullPath);
        return " Directory Deleted Successfully";
    }

    @Override
    public Set<String> getAllFiles(String bucketName) {
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName);
        ListObjectsV2Result result;
        List<String> objectKeys = new ArrayList<>();
        Set<String> folders = new HashSet<>();
        do {
            result = s3Client.listObjectsV2(req);
            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                objectKeys.add(objectSummary.getKey());
            }
            req.setContinuationToken(result.getNextContinuationToken());
        } while(result.isTruncated() == true );

        for(String s : objectKeys) {
            String arr[] = s.substring(0, s.lastIndexOf("/")).split("/");
            for(String a : arr) {
                folders.add(a);
            }
        }
        return folders;
    }

}

