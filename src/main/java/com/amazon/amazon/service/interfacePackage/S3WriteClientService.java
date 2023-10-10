package com.amazon.amazon.service.interfacePackage;

import com.amazonaws.services.s3.model.S3Object;

import java.io.InputStream;
import java.util.Set;

public interface S3WriteClientService {
    public boolean createBucket(String bucketName);
    public boolean createDirectory(String bucketName, String folderName);
    public boolean uploadFiles(String bucketName, String folder, String fileName, String fileType, InputStream inputStream);
    public S3Object getFile(String folderName, String fileName, String bucketName);
    public String deleteBucket(String bucketName);
    public String deleteFolder(String folderName, String bucketName);
    public Set<String> getAllFiles(String bucketName);

}
