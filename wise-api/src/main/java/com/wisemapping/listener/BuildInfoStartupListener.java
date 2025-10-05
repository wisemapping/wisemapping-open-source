package com.wisemapping.listener;

import com.wisemapping.service.BuildInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener that logs build information when the application is ready.
 * This provides visibility into which version and build is running.
 */
@Component
public class BuildInfoStartupListener {

    private static final Logger logger = LoggerFactory.getLogger(BuildInfoStartupListener.class);

    private final BuildInfoService buildInfoService;

    public BuildInfoStartupListener(BuildInfoService buildInfoService) {
        this.buildInfoService = buildInfoService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("=== WiseMapping API Startup ===");
        logger.info("Application is ready and running!");
        
        if (buildInfoService.isBuildInfoAvailable()) {
            logger.info("Build Information:");
            logger.info("  Version: {}", buildInfoService.getVersion());
            logger.info("  Build Time: {}", buildInfoService.getBuildTime());
            logger.info("  Build Number: {}", buildInfoService.getBuildNumber());
            logger.info("  Git Commit: {} ({})", buildInfoService.getGitCommitId(), buildInfoService.getGitBranch());
            logger.info("  Git Commit Time: {}", buildInfoService.getGitCommitTime());
            logger.info("  Build Environment:");
            logger.info("    Maven Version: {}", buildInfoService.getMavenVersion());
            logger.info("    Java Version: {} ({})", buildInfoService.getJavaVersion(), buildInfoService.getJavaVendor());
            logger.info("    OS: {} {} ({})", buildInfoService.getOsName(), buildInfoService.getOsVersion(), buildInfoService.getOsArch());
            logger.info("    Build User: {}", buildInfoService.getBuildUser());
            logger.info("  Project: {}:{} - {}", buildInfoService.getGroupId(), buildInfoService.getArtifactId(), buildInfoService.getProjectName());
        } else {
            logger.warn("Build information is not available. Make sure to run 'mvn clean compile' to generate build properties.");
        }
        
        logger.info("=== End Startup Information ===");
    }
}
