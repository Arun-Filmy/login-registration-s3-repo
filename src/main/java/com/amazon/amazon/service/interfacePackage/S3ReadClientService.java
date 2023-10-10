package com.amazon.amazon.service.interfacePackage;
import java.util.List;
public interface S3ReadClientService {
    List<String> listBuckets();
    boolean isBucketExist(String bucketName);
    List<String> listDirectory(String bucketName);
    boolean isDirectoryExist(String bucketName, String directoryPath);
    boolean isFileExist(String bucketName, String fileName);
}