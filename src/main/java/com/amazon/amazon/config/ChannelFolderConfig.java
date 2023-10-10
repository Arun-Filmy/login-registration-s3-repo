package com.amazon.amazon.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Getter
@Configuration
@ConfigurationProperties(prefix = "channel.folders")
public class ChannelFolderConfig {

    private Map<String, String> folders = new HashMap<>();

    public void setFolders(Map<String, String> folders) {
        this.folders = folders;
    }
}
