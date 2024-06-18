package com.mywechat.entity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-24 19:05
 */
@Component
public class Appconfig {

    @Value("${ws.port:}")
    private Integer port;
    @Value("${project.folder:}")
    private String projectFolder;
    @Value("${admin.emails:}")
    private String adminEmails;

    public Integer getPort() {
        return port;
    }

    public String getProjectFolder() {
        return projectFolder;
    }

    public String getAdminEmails() {
        return adminEmails;
    }
}
