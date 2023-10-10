package com.amazon.amazon.controller;

import com.amazon.amazon.service.interfacePackage.S3ReadClientService;
import com.amazon.amazon.service.interfacePackage.S3WriteClientService;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import jakarta.websocket.server.PathParam;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api-s3")
public class MyController {
    private S3ReadClientService s3ReadClientService;
    private S3WriteClientService s3Service;

    public MyController(S3WriteClientService s3WriteClientService, S3ReadClientService s3ReadClientService, S3WriteClientService s3Service) {
        this.s3ReadClientService = s3ReadClientService;
        this.s3Service = s3WriteClientService;
    }

    @PostMapping("/createBucket/{bucketName}")
    public ResponseEntity<String> createBucket(@PathVariable String bucketName){
        boolean successfullyCreated = s3Service.createBucket(bucketName);
        if(successfullyCreated){
            return ResponseEntity.ok("Successfully created");
        }else{
            return ResponseEntity.internalServerError()
                    .body("Failed to create one");
        }
    }

    @PostMapping("/createDirectory/{bucketName}/{folderName}")
    public ResponseEntity<String> createDirectory(@PathVariable String bucketName, @PathVariable String folderName){
        boolean successfullyCreatedDirectory = s3Service.createDirectory(bucketName, folderName);
        if(successfullyCreatedDirectory){
            return ResponseEntity.ok("Successfully created the directory");
        }else{
            return ResponseEntity.badRequest().body("Already Exist Directory with the name provided!");
        }
    }

    @PostMapping("/{bucketName}/upload")
    public boolean uploadFile(@RequestParam("file") MultipartFile file, @PathVariable("bucketName") String bucketName) throws IOException {
        boolean bucket = s3Service.createBucket(bucketName);
        String folderName = getFileExtension(file.getOriginalFilename());
        s3Service.createDirectory(bucketName, folderName);
        InputStream inputStream = file.getInputStream();
        boolean uploadFiles = s3Service.uploadFiles(bucketName, folderName, file.getOriginalFilename(), file.getContentType(), inputStream);
        if (!uploadFiles){
            return false;
        }else {
            return true;
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        }
        return fileName.substring(dotIndex + 1);
    }

    @GetMapping("/file/{bucketName}/{folderName}/{fileName}")
    public ResponseEntity<byte[]> getFile(@PathVariable String folderName, @PathVariable String bucketName, @PathVariable String fileName) {
        S3Object s3Object = s3Service.getFile(folderName, fileName, bucketName);
        InputStream inputStream = s3Object.getObjectContent();
        byte[] bytes;
        try {
            bytes = IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(s3Object.getObjectMetadata().getContentType()));
        headers.setContentLength(bytes.length);
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @DeleteMapping("/deletebucket/{bucketName}")
    public String deleteBucket(@PathVariable String bucketName){
        String deletedBucket = s3Service.deleteBucket(bucketName);
        return "Bucket Delete Successfully : "+deletedBucket;
    }


    @GetMapping("/buckets/{bucketName}")
    public ResponseEntity<Set<String>> getAllFiles(@PathVariable ("bucketName") String bucketName) {
        Set<String> allFiles = s3Service.getAllFiles(bucketName);
        return new ResponseEntity<>(allFiles, HttpStatus.OK);
    }

    @DeleteMapping("/deleteFolder/{bucketName}/{folderName}")
    public String deleteFolderFromS3(@PathVariable String folderName, @PathVariable String bucketName, @RequestParam String subDirectoryPath){
        String fullPath = subDirectoryPath +"/"+folderName;
        String deleteFolder = s3Service.deleteFolder(fullPath, bucketName);
        return folderName+" "+deleteFolder;
    }


    //ReadClientService controller starts here

    @GetMapping("/getBuckets")
    public List<String> listBuckets() {
        return s3ReadClientService.listBuckets();
    }

    @GetMapping("/isBucketExist/{bucketName}")
    public boolean isBucketExist(@PathVariable ("bucketName") String bucketName) {
        return s3ReadClientService.isBucketExist(bucketName);
    }

    @GetMapping("/listDirectory/{bucketName}")
    public List<String> listDirectory(@PathVariable String bucketName) {
        return s3ReadClientService.listDirectory(bucketName);
    }

    @GetMapping("/isDirectoryExist/{bucketName}/{directoryPath}")
    public boolean isDirectoryExist(@PathVariable String bucketName, @PathVariable String directoryPath) {
        return s3ReadClientService.isDirectoryExist(bucketName, directoryPath);
    }

    @GetMapping("/isFileExist/{bucketName}/{fileName}")
    public boolean isFileExist(@PathVariable String bucketName, @PathVariable String fileName) {
        return s3ReadClientService.isFileExist(bucketName, fileName);
    }

}
