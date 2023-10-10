package com.amazon.amazon.service.impl;

import com.amazon.amazon.service.interfacePackage.S3ReadClientService;
import com.amazon.amazon.service.interfacePackage.S3WriteClientService;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.Delimiter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.naming.SelectorContext.prefix;

@Service
public class S3ReadClientServiceImpl implements S3ReadClientService {

    private AmazonS3 s3Client;
    private S3WriteClientService s3WriteClientService;
    public S3ReadClientServiceImpl(AmazonS3 s3Client, S3WriteClientService s3WriteClientService) {
        this.s3WriteClientService = s3WriteClientService;
        this.s3Client = s3Client;
    }

    @Override
    @Builder
    public List<String> listBuckets() {
        List<Bucket> buckets = s3Client.listBuckets();
        List<String> allBuckets = buckets.stream()
                .map(Bucket::getName)
                .collect(Collectors.toList());
        return allBuckets;
    }

    @Override
    public boolean isBucketExist(String bucketName1) {
        return s3Client.doesBucketExistV2(bucketName1);
    }

    @Override
    public List<String> listDirectory(String bucketName) {
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName);
        ListObjectsV2Result result;
        List<String> objectKeys = new ArrayList<>();
        do {
            result = s3Client.listObjectsV2(req);
            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                objectKeys.add(objectSummary.getKey());
            }
            req.setContinuationToken(result.getNextContinuationToken());
        } while(result.isTruncated() == true );

        return objectKeys;
    }

    @Override
    public boolean isDirectoryExist(String bucketName, String folderName) {
        Set<String> allFiles = s3WriteClientService.getAllFiles(bucketName);
        if(allFiles.contains(folderName)){
            return true;
        }
        return false;

    }


    @Override
    public boolean isFileExist(String bucketName, String fileName) {
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
            String arr[] = s.split("/");
            for(String a : arr) {
                folders.add(a);
            }
            if (folders.contains(fileName)){
                return true;
            }

        }
        return false;
    }

}
