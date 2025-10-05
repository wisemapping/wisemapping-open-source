package com.wisemapping.service;

import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Service to provide build information including build number, date, and version.
 * This information is automatically populated by Spring Boot's build-info plugin.
 */
@Service
public class BuildInfoService {

    private final BuildProperties buildProperties;

    public BuildInfoService(Optional<BuildProperties> buildProperties) {
        this.buildProperties = buildProperties.orElse(null);
    }

    /**
     * Get the application version
     */
    public String getVersion() {
        return buildProperties != null ? buildProperties.getVersion() : "unknown";
    }

    /**
     * Get the build time as a formatted string
     */
    public String getBuildTime() {
        if (buildProperties != null && buildProperties.getTime() != null) {
            return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault())
                    .format(buildProperties.getTime());
        }
        return "unknown";
    }

    /**
     * Get the build time as an Instant
     */
    public Instant getBuildTimeInstant() {
        return buildProperties != null ? buildProperties.getTime() : null;
    }

    /**
     * Get the build number (if available)
     */
    public String getBuildNumber() {
        return buildProperties != null ? buildProperties.get("build.number") : "unknown";
    }

    /**
     * Get the Git commit ID (if available)
     */
    public String getGitCommitId() {
        return buildProperties != null ? buildProperties.get("git.commit.id") : "unknown";
    }

    /**
     * Get the Git branch (if available)
     */
    public String getGitBranch() {
        return buildProperties != null ? buildProperties.get("git.branch") : "unknown";
    }

    /**
     * Get the Git commit time (if available)
     */
    public String getGitCommitTime() {
        if (buildProperties != null) {
            String commitTime = buildProperties.get("git.commit.time");
            if (commitTime != null) {
                try {
                    Instant instant = Instant.parse(commitTime);
                    return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                            .withZone(ZoneId.systemDefault())
                            .format(instant);
                } catch (Exception e) {
                    return commitTime;
                }
            }
        }
        return "unknown";
    }

    /**
     * Get the Maven version used for the build
     */
    public String getMavenVersion() {
        return buildProperties != null ? buildProperties.get("maven.version") : "unknown";
    }

    /**
     * Get the Java version used for the build
     */
    public String getJavaVersion() {
        return buildProperties != null ? buildProperties.get("java.version") : System.getProperty("java.version");
    }

    /**
     * Get the Java vendor used for the build
     */
    public String getJavaVendor() {
        return buildProperties != null ? buildProperties.get("java.vendor") : System.getProperty("java.vendor");
    }

    /**
     * Get the operating system used for the build
     */
    public String getOsName() {
        return buildProperties != null ? buildProperties.get("os.name") : System.getProperty("os.name");
    }

    /**
     * Get the operating system version used for the build
     */
    public String getOsVersion() {
        return buildProperties != null ? buildProperties.get("os.version") : System.getProperty("os.version");
    }

    /**
     * Get the operating system architecture used for the build
     */
    public String getOsArch() {
        return buildProperties != null ? buildProperties.get("os.arch") : System.getProperty("os.arch");
    }

    /**
     * Get the user who performed the build
     */
    public String getBuildUser() {
        return buildProperties != null ? buildProperties.get("user.name") : System.getProperty("user.name");
    }

    /**
     * Get the Maven group ID
     */
    public String getGroupId() {
        return buildProperties != null ? buildProperties.get("group") : "unknown";
    }

    /**
     * Get the Maven artifact ID
     */
    public String getArtifactId() {
        return buildProperties != null ? buildProperties.get("artifact") : "unknown";
    }

    /**
     * Get the Maven project name
     */
    public String getProjectName() {
        return buildProperties != null ? buildProperties.get("name") : "unknown";
    }

    /**
     * Get the Maven project description
     */
    public String getProjectDescription() {
        return buildProperties != null ? buildProperties.get("description") : "unknown";
    }

    /**
     * Get the build timestamp
     */
    public String getBuildTimestamp() {
        return buildProperties != null ? buildProperties.get("build.timestamp") : "unknown";
    }

    /**
     * Get the source encoding used for the build
     */
    public String getSourceEncoding() {
        return buildProperties != null ? buildProperties.get("encoding.source") : "unknown";
    }

    /**
     * Get the Java source version used for compilation
     */
    public String getJavaSourceVersion() {
        return buildProperties != null ? buildProperties.get("java.source") : "unknown";
    }

    /**
     * Get the Java target version used for compilation
     */
    public String getJavaTargetVersion() {
        return buildProperties != null ? buildProperties.get("java.target") : "unknown";
    }

    /**
     * Get all build information as a formatted string
     */
    public String getBuildInfoSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Version: ").append(getVersion()).append("\n");
        sb.append("Build Time: ").append(getBuildTime()).append("\n");
        sb.append("Build Timestamp: ").append(getBuildTimestamp()).append("\n");
        sb.append("Build Number: ").append(getBuildNumber()).append("\n");
        sb.append("Git Commit: ").append(getGitCommitId()).append("\n");
        sb.append("Git Branch: ").append(getGitBranch()).append("\n");
        sb.append("Git Commit Time: ").append(getGitCommitTime()).append("\n");
        sb.append("Maven Version: ").append(getMavenVersion()).append("\n");
        sb.append("Java Version: ").append(getJavaVersion()).append(" (Source: ").append(getJavaSourceVersion()).append(", Target: ").append(getJavaTargetVersion()).append(")").append("\n");
        sb.append("Java Vendor: ").append(getJavaVendor()).append("\n");
        sb.append("OS: ").append(getOsName()).append(" ").append(getOsVersion()).append(" (").append(getOsArch()).append(")").append("\n");
        sb.append("Build User: ").append(getBuildUser()).append("\n");
        sb.append("Source Encoding: ").append(getSourceEncoding()).append("\n");
        sb.append("Project: ").append(getGroupId()).append(":").append(getArtifactId()).append(" - ").append(getProjectName());
        return sb.toString();
    }

    /**
     * Check if build properties are available
     */
    public boolean isBuildInfoAvailable() {
        return buildProperties != null;
    }
}
