package com.amazon.amazon.service.impl;

import com.amazon.amazon.config.ChannelFolderConfig;
import com.amazon.amazon.service.interfacePackage.ChannelMetaDataService;
import com.amazon.amazon.service.interfacePackage.S3WriteClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.Map;
@Service
public class ChannelMetaDataServiceImpl implements ChannelMetaDataService {
    private final S3WriteClientService s3WriteClientService;
    private final String videoFolder;
    private final String[] videoSubFolders;
    private final String posterFolder;
    private final String secondaryFolder;

    @Autowired
    public ChannelMetaDataServiceImpl(
            S3WriteClientService s3WriteClientService,
            @Value("${folder-structure.video-folder}") String videoFolder,
            @Value("${folder-structure.video-sub-folders}") String videoSubFolders,
            @Value("${folder-structure.poster-folder}") String posterFolder,
            @Value("${folder-structure.secondary-folder}") String secondaryFolder) {
        this.s3WriteClientService = s3WriteClientService;
        this.videoFolder = videoFolder;
        this.videoSubFolders = videoSubFolders.split(",");
        this.posterFolder = posterFolder;
        this.secondaryFolder = secondaryFolder;
    }

    @Override
    public boolean create(String channelName) {
        // Create the channel bucket
        boolean bucketCreated = s3WriteClientService.createBucket(channelName);
        if (!bucketCreated) {
            return false; // Bucket creation failed
        }

        // Create folders inside the channel bucket
        boolean foldersCreated = createFoldersInBucket(channelName);
        if (!foldersCreated) {
            return false; // Folder creation failed
        }

        // Upload the metafile
        boolean metafileUploaded = uploadMetafile(channelName);
        if (!metafileUploaded) {
            return false; // Metafile upload failed
        }

        return true; // Channel metadata created successfully
    }

    private boolean createFoldersInBucket(String channelName) {
        // Define the folder structure
        String[] folders = {
                "Video/Program",
                "Video/Promos",
                "Video/Fillers",
                "Poster",
                "Secondary"
        };

        // Create folders in the channel bucket
        for (String folder : folders) {
            boolean folderCreated = s3WriteClientService.createDirectory(channelName, folder);
            if (!folderCreated) {
                return false; // Folder creation failed
            }
        }

        return true; // All folders created successfully
    }

    private boolean uploadMetafile(String channelName) {
        String metafileName = "Metafile";
        String contentType = "text/plain"; // Assuming it's a plain text file

        // Read the metafile content from a predefined source (e.g., a template file)
        String metafileContent = readMetafileContent();
        String folderName = channelName+metafileName;

        // Convert the content to an input stream
        InputStream inputStream = new ByteArrayInputStream(metafileContent.getBytes());

        // Upload the metafile to the channel bucket
        boolean metafileUploaded = s3WriteClientService.uploadFiles(channelName,
                folderName, metafileName, contentType, inputStream);
        if (!metafileUploaded) {
            return false; // Metafile failed upload
        }

        return true; // Metafile uploaded successfully
    }

    private String readMetafileContent() {
        // Logic to read the metafile content from a predefined source (e.g., a template file)
        // Replace this with your implementation
        return "This is the content of the metafile.";
    }
}
