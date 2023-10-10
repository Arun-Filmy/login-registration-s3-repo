package com.amazon.amazon.controller;

import com.amazon.amazon.service.interfacePackage.ChannelMetaDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/channels")
public class ChannelController {

    Logger logger = LoggerFactory.getLogger(ChannelController.class.getName());
    private final ChannelMetaDataService channelMetaDataService;

    public ChannelController(ChannelMetaDataService channelMetaDataService) {
        this.channelMetaDataService = channelMetaDataService;
    }

    @PostMapping("newChannel/{channelName}")
    public ResponseEntity<String> createChannel(@PathVariable String channelName) {
        boolean success = channelMetaDataService.create(channelName);
        if (success) {
            return ResponseEntity.ok("Channel created successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create a new channel, " +
                            "either name is already exists or you are not following naming convention");
        }
    }
}

