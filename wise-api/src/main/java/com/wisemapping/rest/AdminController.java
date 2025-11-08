/*
 *    Copyright [2007-2025] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.wisemapping.rest;

import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.exceptions.PasswordTooShortException;
import com.wisemapping.exceptions.PasswordTooLongException;
import com.wisemapping.exceptions.PasswordChangeNotAllowedException;
import com.wisemapping.model.AuthenticationType;
import com.wisemapping.model.Collaboration;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.Account;
import com.wisemapping.model.SuspensionReason;
import com.wisemapping.metrics.MindmapListingMetricsRecorder;
import com.wisemapping.rest.model.RestUser;
import com.wisemapping.rest.model.PaginatedResponse;
import com.wisemapping.service.MindmapService;
import com.wisemapping.service.MetricsService;
import com.wisemapping.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;

@RestController
@RequestMapping("/api/restful/admin")
@PreAuthorize("isAuthenticated() and hasRole('ROLE_ADMIN')")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 200;
    private static final int MAX_PAGE_INDEX = 10_000;

    @Qualifier("userService")
    @Autowired
    private UserService userService;

    @Qualifier("mindmapService")
    @Autowired
    private MindmapService mindmapService;

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private com.wisemapping.service.BuildInfoService buildInfoService;

    @Autowired
    private MindmapListingMetricsRecorder mindmapListingMetricsRecorder;

    @Value("${app.admin.user:}")
    private String adminUser;

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    private boolean isAdmin(String email) {
        return email != null && adminUser != null && email.trim().endsWith(adminUser);
    }

    @Value("${spring.datasource.driver-class-name:}")
    private String datasourceDriver;

    @Value("${spring.datasource.username:}")
    private String datasourceUsername;

    @Value("${spring.jpa.hibernate.ddl-auto:}")
    private String hibernateDdlAuto;

    @Value("${spring.application.name:}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${app.monitoring.performance.log-mindmap-listing:false}")
    private boolean logMindmapListingMetrics;

    @RequestMapping(method = RequestMethod.GET, value = "/users", produces = {"application/json"})
    @ResponseBody
    public PaginatedResponse<com.wisemapping.rest.model.AdminRestUser> getAllUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "filterActive", required = false) Boolean filterActive,
            @RequestParam(value = "filterSuspended", required = false) Boolean filterSuspended,
            @RequestParam(value = "filterAuthType", required = false) String filterAuthType) {
        
        final int safePage = sanitizePage(page);
        final int safePageSize = sanitizePageSize(pageSize);
        
        // Use optimized query that only adds filter conditions that are actually set
        final List<Account> users = userService.getUsersWithFilters(
            search, filterActive, filterSuspended, filterAuthType, safePage, safePageSize);
        final long totalElements = userService.countUsersWithFilters(
            search, filterActive, filterSuspended, filterAuthType);
        
        final List<com.wisemapping.rest.model.AdminRestUser> restUsers = users.stream()
                .map(user -> new com.wisemapping.rest.model.AdminRestUser(user, isAdmin(user.getEmail())))
                .collect(java.util.stream.Collectors.toList());
        
        return new PaginatedResponse<>(restUsers, safePage, safePageSize, totalElements);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/users/{id}", produces = {"application/json"})
    @ResponseBody
    public com.wisemapping.rest.model.AdminRestUser getUserById(@PathVariable int id) {
        final Account userBy = userService.getUserBy(id);
        if (userBy == null) {
            throw new IllegalArgumentException("User could not be found");
        }
        return new com.wisemapping.rest.model.AdminRestUser(userBy, isAdmin(userBy.getEmail()));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/users/email/{email:.+}", produces = {"application/json"})
    @ResponseBody
    public com.wisemapping.rest.model.AdminRestUser getUserByEmail(@PathVariable String email) {
        final Account user = userService.getUserBy(email);
        if (user == null) {
            throw new IllegalArgumentException("User '" + email + "' could not be found");
        }
        return new com.wisemapping.rest.model.AdminRestUser(user, isAdmin(user.getEmail()));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/users", consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(value = HttpStatus.CREATED)
    public void createUser(@RequestBody RestUser user, final HttpServletResponse response) throws WiseMappingException {
        if (user == null) {
            throw new IllegalArgumentException("User could not be found");
        }

        // User already exists ?
        final String email = user.getEmail();
        if (userService.getUserBy(email) != null) {
            throw new IllegalArgumentException("User already exists with this email.");
        }

        // Run some other validations ...
        final Account delegated = user.getDelegated();
        final String lastname = delegated.getLastname();
        if (lastname == null || lastname.isEmpty()) {
            throw new IllegalArgumentException("lastname can not be null");
        }

        final String firstName = delegated.getFirstname();
        if (firstName == null || firstName.isEmpty()) {
            throw new IllegalArgumentException("firstname can not be null");
        }


        final String password = delegated.getPassword();
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("password can not be null");
        }

        if (password.length() < Account.MIN_PASSWORD_LENGTH_SIZE) {
            throw new PasswordTooShortException();
        }

        if (password.length() > Account.MAX_PASSWORD_LENGTH_SIZE) {
            throw new PasswordTooLongException();
        }

        // Finally create the user ...
        delegated.setAuthenticationType(AuthenticationType.DATABASE);
        userService.createUser(delegated, false, true);
        
        // Track user registration
        String emailProvider = metricsService.extractEmailProvider(delegated.getEmail());
        metricsService.trackUserRegistration(delegated, emailProvider);
        
        response.setHeader("Location", "/api/restful/admin/users/" + delegated.getId());
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/users/{id}", consumes = {"application/json"}, produces = {"application/json"})
    @ResponseBody
    @Transactional
    public RestUser updateUser(@RequestBody RestUser userUpdate, @PathVariable int id) {
        if (userUpdate == null) {
            throw new IllegalArgumentException("User data can not be null");
        }

        final Account existingUser = userService.getUserBy(id);
        if (existingUser == null) {
            throw new IllegalArgumentException("User '" + id + "' could not be found");
        }

        // Update user fields
        final Account delegated = userUpdate.getDelegated();
        if (delegated.getFirstname() != null) {
            existingUser.setFirstname(delegated.getFirstname());
        }
        if (delegated.getLastname() != null) {
            existingUser.setLastname(delegated.getLastname());
        }
        if (delegated.getEmail() != null && !delegated.getEmail().equals(existingUser.getEmail())) {
            // Check if email is already taken by another user
            final Account userWithEmail = userService.getUserBy(delegated.getEmail());
            if (userWithEmail != null && !userWithEmail.equals(existingUser)) {
                throw new IllegalArgumentException("Email already exists");
            }
            existingUser.setEmail(delegated.getEmail());
        }
        if (delegated.getLocale() != null) {
            existingUser.setLocale(delegated.getLocale());
        }
        if (delegated.isAllowSendEmail() != existingUser.isAllowSendEmail()) {
            existingUser.setAllowSendEmail(delegated.isAllowSendEmail());
        }

        userService.updateUser(existingUser);
        final Account currentUser = com.wisemapping.security.Utils.getUser(true);
        return new RestUser(existingUser, isAdmin(currentUser.getEmail()));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/users/{id}/suspension", consumes = {"application/json"}, produces = {"application/json"})
    @ResponseBody
    @Transactional
    public RestUser updateUserSuspension(@RequestBody Map<String, Object> suspensionData, @PathVariable int id) {
        if (suspensionData == null) {
            throw new IllegalArgumentException("Suspension data can not be null");
        }

        final Account existingUser = userService.getUserBy(id);
        if (existingUser == null) {
            throw new IllegalArgumentException("User '" + id + "' could not be found");
        }

        // Update suspension status
        if (suspensionData.containsKey("suspended")) {
            Boolean suspended = (Boolean) suspensionData.get("suspended");
            if (suspended != null) {
                if (suspended) {
                    // Suspend user
                    if (suspensionData.containsKey("suspensionReason")) {
                        String reasonStr = (String) suspensionData.get("suspensionReason");
                        if (reasonStr != null && !reasonStr.isEmpty()) {
                            try {
                                SuspensionReason reason = SuspensionReason.valueOf(reasonStr.toUpperCase());
                                existingUser.suspend(reason);
                            } catch (IllegalArgumentException e) {
                                throw new IllegalArgumentException("Invalid suspension reason: " + reasonStr);
                            }
                        } else {
                            existingUser.suspend();
                        }
                    } else {
                        existingUser.suspend();
                    }
                } else {
                    // Unsuspend user using centralized method that handles mindmap restoration
                    userService.unsuspendUser(existingUser);
                    return new RestUser(existingUser, isAdmin(existingUser.getEmail()));
                }
            }
        }

        userService.updateUser(existingUser);
        
        // Fetch the user again to ensure we have the latest state from the database
        final Account updatedUser = userService.getUserBy(id);
        final Account currentUser = com.wisemapping.security.Utils.getUser(true);
        return new RestUser(updatedUser, isAdmin(currentUser.getEmail()));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/users/{id}/password", consumes = {"text/plain"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void changePassword(@RequestBody String password, @PathVariable int id) throws PasswordTooShortException, PasswordTooLongException, PasswordChangeNotAllowedException {
        if (password == null) {
            throw new IllegalArgumentException("Password can not be null");
        }

        if (password.length() < Account.MIN_PASSWORD_LENGTH_SIZE) {
            throw new PasswordTooShortException();
        }

        if (password.length() > Account.MAX_PASSWORD_LENGTH_SIZE) {
            throw new PasswordTooLongException();
        }

        final Account user = userService.getUserBy(id);
        if (user == null) {
            throw new IllegalArgumentException("User '" + id + "' could not be found");
        }
        
        // Check if password changes are allowed for this user's authentication type
        if (!user.isPasswordChangeAllowed()) {
            throw new PasswordChangeNotAllowedException("Cannot change password for user '" + user.getEmail() + 
                "' - password changes are not allowed for external authentication providers (Google, LDAP, Facebook).");
        }
        
        user.setPassword(password);
        userService.changePassword(user);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/users/{id}/activate")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void activateUser(@PathVariable int id) {
        final Account user = userService.getUserBy(id);
        if (user == null) {
            throw new IllegalArgumentException("User '" + id + "' could not be found");
        }
        
        // Check if user is already active
        if (user.isActive()) {
            throw new IllegalArgumentException("User '" + user.getEmail() + "' is already activated");
        }
        
        // Activate the user by setting activation date
        user.setActivationDate(java.util.Calendar.getInstance());
        userService.updateUser(user);
        
        // Send activation confirmation email
        // Note: This uses the "activateAccount" notification which sends a success email
        // userService's notificationService.activateAccount(user);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/users/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteUserByEmail(@PathVariable int id) throws WiseMappingException {
        final Account user = userService.getUserBy(id);
        if (user == null) {
            throw new IllegalArgumentException("User '" + id + "' could not be found");
        }

        final List<Collaboration> collaborations = mindmapService.findCollaborations(user);
        final java.util.Set<Integer> processedMindmapIds = new java.util.HashSet<>();
        
        for (Collaboration collaboration : collaborations) {
            final Mindmap mindmap = collaboration.getMindMap();
            // Skip if this mindmap was already processed (user was creator and mindmap was fully deleted)
            if (processedMindmapIds.contains(mindmap.getId())) {
                continue;
            }
            
            // Track mindmaps where user is creator (will be fully deleted)
            final boolean isCreator = mindmap.getCreator().identityEquality(user);
            if (isCreator) {
                processedMindmapIds.add(mindmap.getId());
            }
            
            mindmapService.removeMindmap(mindmap, user);
        }
        userService.removeUser(user);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/users/{id}/maps", produces = {"application/json"})
    @ResponseBody
    public List<com.wisemapping.rest.model.AdminRestMap> getUserMaps(@PathVariable int id) {
        final Account user = userService.getUserBy(id);
        if (user == null) {
            throw new IllegalArgumentException("User '" + id + "' could not be found");
        }

        final List<Mindmap> mindmaps = mindmapService.findMindmapsByUser(user);
        return mindmaps.stream()
                .filter(m -> m.getCreator().identityEquality(user))
                .map(com.wisemapping.rest.model.AdminRestMap::new)
                .collect(Collectors.toList());
    }

    // Maps management endpoints
    @RequestMapping(method = RequestMethod.GET, value = "/maps", produces = {"application/json"})
    @ResponseBody
    public PaginatedResponse<com.wisemapping.rest.model.AdminRestMap> getAllMaps(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "sortBy", defaultValue = "title") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder,
            @RequestParam(value = "filterPublic", required = false) Boolean filterPublic,
            @RequestParam(value = "filterLocked", required = false) Boolean filterLocked,
            @RequestParam(value = "filterSpam", required = false) Boolean filterSpam,
            @RequestParam(value = "dateFilter", defaultValue = "1") String dateFilter) {

        final int safePage = sanitizePage(page);
        final int safePageSize = sanitizePageSize(pageSize);
        
        if (search != null && !search.trim().isEmpty()) {
            // Search mindmaps - using optimized AdminRestMap DTO
            final List<Mindmap> mindmaps = mindmapService.searchMindmaps(
                    search, filterPublic, filterLocked, filterSpam, safePage, safePageSize);
            final long totalElements = mindmapService.countMindmapsBySearch(search, filterPublic, filterLocked, filterSpam);
            final List<com.wisemapping.rest.model.AdminRestMap> restMaps = mindmaps.stream()
                    .map(com.wisemapping.rest.model.AdminRestMap::new)
                    .collect(java.util.stream.Collectors.toList());
            return new PaginatedResponse<>(restMaps, safePage, safePageSize, totalElements);
        } else {
            // Get all mindmaps with pagination and date filtering - using optimized AdminRestMap DTO
            final List<Mindmap> mindmaps = mindmapService.getAllMindmaps(
                    filterPublic, filterLocked, filterSpam, dateFilter, safePage, safePageSize);
            final long totalElements = mindmapService.countAllMindmaps(filterPublic, filterLocked, filterSpam, dateFilter);
            final List<com.wisemapping.rest.model.AdminRestMap> restMaps = mindmaps.stream()
                    .map(com.wisemapping.rest.model.AdminRestMap::new)
                    .collect(java.util.stream.Collectors.toList());
            return new PaginatedResponse<>(restMaps, safePage, safePageSize, totalElements);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/maps/{id}", produces = {"application/json"})
    @ResponseBody
    public com.wisemapping.rest.model.RestMap getMapById(@PathVariable int id) {
        final Mindmap mindmap = mindmapService.findMindmapById(id);
        if (mindmap == null) {
            throw new IllegalArgumentException("Map could not be found");
        }
        return new com.wisemapping.rest.model.RestMap(mindmap);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/maps/{id}", consumes = {"application/json"}, produces = {"application/json"})
    @ResponseBody
    public com.wisemapping.rest.model.RestMap updateMap(@RequestBody com.wisemapping.rest.model.RestMap mapUpdate, @PathVariable int id) throws WiseMappingException {
        if (mapUpdate == null) {
            throw new IllegalArgumentException("Map data can not be null");
        }

        final Mindmap existingMap = mindmapService.findMindmapById(id);
        if (existingMap == null) {
            throw new IllegalArgumentException("Map '" + id + "' could not be found");
        }

        // Update map fields
        final Mindmap delegated = mapUpdate.getDelegated();
        if (delegated.getTitle() != null) {
            existingMap.setTitle(delegated.getTitle());
        }
        if (delegated.getDescription() != null) {
            existingMap.setDescription(delegated.getDescription());
        }
        if (delegated.isPublic() != existingMap.isPublic()) {
            existingMap.setPublic(delegated.isPublic());
        }
        // Lock functionality not implemented yet in Mindmap model
        // if (delegated.isLocked() != existingMap.isLocked()) {
        //     existingMap.setLocked(delegated.isLocked());
        // }

        mindmapService.updateMindmap(existingMap, true);
        return new com.wisemapping.rest.model.RestMap(existingMap);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/maps/{id}/spam", consumes = {"application/json"}, produces = {"application/json"})
    @ResponseBody
    public com.wisemapping.rest.model.AdminRestMap updateMapSpamStatus(@RequestBody Map<String, Boolean> spamData, @PathVariable int id) throws WiseMappingException {
        if (spamData == null || !spamData.containsKey("isSpam")) {
            throw new IllegalArgumentException("Spam status data is required");
        }

        final Mindmap existingMap = mindmapService.findMindmapById(id);
        if (existingMap == null) {
            throw new IllegalArgumentException("Map '" + id + "' could not be found");
        }

        Boolean isSpam = spamData.get("isSpam");
        if (isSpam != null) {
            existingMap.setSpamDetected(isSpam);
            if (isSpam) {
                // When manually marking as spam, set the spam type to UNKNOWN (manual)
                existingMap.setSpamTypeCode(com.wisemapping.model.SpamStrategyType.UNKNOWN);
                existingMap.setSpamDescription("Manually marked as spam by admin");
            } else {
                // When unmarking as spam, clear the spam type and description
                existingMap.setSpamTypeCode(null);
                existingMap.setSpamDescription(null);
            }
            mindmapService.updateMindmap(existingMap, true);
        }

        return new com.wisemapping.rest.model.AdminRestMap(existingMap);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/maps/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteMap(@PathVariable int id) throws WiseMappingException {
        final Mindmap mindmap = mindmapService.findMindmapById(id);
        if (mindmap == null) {
            throw new IllegalArgumentException("Map '" + id + "' could not be found");
        }

        // Get the admin user to perform the deletion
        final Account adminUser = userService.getUserBy(1); // Assuming admin user ID is 1
        mindmapService.removeMindmap(mindmap, adminUser);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/maps/{id}/xml", produces = {"application/xml"})
    @ResponseBody
    public String getMapXml(@PathVariable int id) {
        final Mindmap mindmap = mindmapService.findMindmapById(id);
        if (mindmap == null) {
            throw new IllegalArgumentException("Map could not be found");
        }
        
        try {
            // Return the XML content of the mindmap
            return mindmap.getXmlStr();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve map XML content", e);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/system/info", produces = {"application/json"})
    @ResponseBody
    public Map<String, Object> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();
        
        // Application Info
        Map<String, Object> appInfo = new HashMap<>();
        appInfo.put("name", applicationName.isEmpty() ? "WiseMapping API" : applicationName);
        appInfo.put("port", serverPort);
        systemInfo.put("application", appInfo);
        
        // Build Info
        Map<String, Object> buildInfo = new HashMap<>();
        buildInfo.put("version", buildInfoService.getVersion());
        buildInfo.put("buildTime", buildInfoService.getBuildTime());
        buildInfo.put("buildNumber", buildInfoService.getBuildNumber());
        buildInfo.put("gitCommitId", buildInfoService.getGitCommitId());
        buildInfo.put("gitBranch", buildInfoService.getGitBranch());
        buildInfo.put("gitCommitTime", buildInfoService.getGitCommitTime());
        buildInfo.put("mavenVersion", buildInfoService.getMavenVersion());
        buildInfo.put("javaVersion", buildInfoService.getJavaVersion());
        buildInfo.put("javaVendor", buildInfoService.getJavaVendor());
        buildInfo.put("osName", buildInfoService.getOsName());
        buildInfo.put("osVersion", buildInfoService.getOsVersion());
        buildInfo.put("osArch", buildInfoService.getOsArch());
        buildInfo.put("buildUser", buildInfoService.getBuildUser());
        buildInfo.put("groupId", buildInfoService.getGroupId());
        buildInfo.put("artifactId", buildInfoService.getArtifactId());
        buildInfo.put("projectName", buildInfoService.getProjectName());
        buildInfo.put("projectDescription", buildInfoService.getProjectDescription());
        buildInfo.put("available", buildInfoService.isBuildInfoAvailable());
        systemInfo.put("build", buildInfo);
        
        // Database Info
        Map<String, Object> dbInfo = new HashMap<>();
        dbInfo.put("driver", datasourceDriver);
        dbInfo.put("url", maskSensitiveInfo(datasourceUrl));
        dbInfo.put("username", datasourceUsername);
        dbInfo.put("hibernateDdlAuto", hibernateDdlAuto);
        systemInfo.put("database", dbInfo);
        
        // JVM Info
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        
        Map<String, Object> jvmInfo = new HashMap<>();
        jvmInfo.put("javaVersion", System.getProperty("java.version"));
        jvmInfo.put("javaVendor", System.getProperty("java.vendor"));
        jvmInfo.put("uptime", runtimeBean.getUptime());
        jvmInfo.put("startTime", runtimeBean.getStartTime());
        jvmInfo.put("maxMemory", memoryBean.getHeapMemoryUsage().getMax());
        jvmInfo.put("usedMemory", memoryBean.getHeapMemoryUsage().getUsed());
        jvmInfo.put("totalMemory", memoryBean.getHeapMemoryUsage().getCommitted());
        jvmInfo.put("availableProcessors", osBean.getAvailableProcessors());
        jvmInfo.put("systemLoadAverage", osBean.getSystemLoadAverage());
        systemInfo.put("jvm", jvmInfo);
        
        // System Stats
        Map<String, Object> stats = new HashMap<>();
        try {
            // Use count queries instead of loading all records
            long totalUsers = userService.countAllUsers();
            stats.put("totalUsers", totalUsers);
            
            long totalMindmaps = mindmapService.countAllMindmaps(false); // false = include non-spam
            stats.put("totalMindmaps", totalMindmaps);
        } catch (Exception e) {
            stats.put("error", "Failed to retrieve stats: " + e.getMessage());
        }
        systemInfo.put("statistics", stats);

        Map<String, Object> listingMetrics = new HashMap<>();
        listingMetrics.put("enabled", logMindmapListingMetrics);
        mindmapListingMetricsRecorder.latest().ifPresent(snapshot -> {
            listingMetrics.put("lastUpdated", snapshot.capturedAt().toEpochMilli());
            listingMetrics.put("mapCount", snapshot.mapCount());
            listingMetrics.put("collaborationCount", snapshot.collaborationCount());
            listingMetrics.put("totalTimeMs", snapshot.totalTimeMillis());
            listingMetrics.put("executedStatements", snapshot.executedStatements());
            listingMetrics.put("entityFetches", snapshot.entityFetches());
            listingMetrics.put("collectionFetches", snapshot.collectionFetches());
            listingMetrics.put("entityLoads", snapshot.entityLoads());

            List<Map<String, Object>> segmentList = snapshot.segments()
                    .stream()
                    .map(segment -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("name", segment.name());
                        item.put("timeMs", segment.timeMillis());
                        item.put("ratio", segment.ratio());
                        return item;
                    })
                    .toList();
            listingMetrics.put("segments", segmentList);

            List<Map<String, Object>> queryList = snapshot.topQueries()
                    .stream()
                    .map(query -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("sql", query.sql());
                        item.put("executions", query.executions());
                        return item;
                    })
                    .toList();
            listingMetrics.put("topQueries", queryList);
        });
        systemInfo.put("mindmapListingMetrics", listingMetrics);
        
        return systemInfo;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/system/build-info", produces = {"application/json"})
    @ResponseBody
    public Map<String, Object> getBuildInfo() {
        Map<String, Object> buildInfo = new HashMap<>();
        
        // Basic build info
        buildInfo.put("version", buildInfoService.getVersion());
        buildInfo.put("buildTime", buildInfoService.getBuildTime());
        buildInfo.put("buildNumber", buildInfoService.getBuildNumber());
        buildInfo.put("available", buildInfoService.isBuildInfoAvailable());
        
        // Git info
        Map<String, Object> gitInfo = new HashMap<>();
        gitInfo.put("commitId", buildInfoService.getGitCommitId());
        gitInfo.put("branch", buildInfoService.getGitBranch());
        gitInfo.put("commitTime", buildInfoService.getGitCommitTime());
        buildInfo.put("git", gitInfo);
        
        // Build environment info
        Map<String, Object> buildEnv = new HashMap<>();
        buildEnv.put("mavenVersion", buildInfoService.getMavenVersion());
        buildEnv.put("javaVersion", buildInfoService.getJavaVersion());
        buildEnv.put("javaVendor", buildInfoService.getJavaVendor());
        buildEnv.put("buildUser", buildInfoService.getBuildUser());
        buildInfo.put("buildEnvironment", buildEnv);
        
        // OS info
        Map<String, Object> osInfo = new HashMap<>();
        osInfo.put("name", buildInfoService.getOsName());
        osInfo.put("version", buildInfoService.getOsVersion());
        osInfo.put("architecture", buildInfoService.getOsArch());
        buildInfo.put("operatingSystem", osInfo);
        
        // Project info
        Map<String, Object> projectInfo = new HashMap<>();
        projectInfo.put("groupId", buildInfoService.getGroupId());
        projectInfo.put("artifactId", buildInfoService.getArtifactId());
        projectInfo.put("name", buildInfoService.getProjectName());
        projectInfo.put("description", buildInfoService.getProjectDescription());
        buildInfo.put("project", projectInfo);
        
        // Summary
        buildInfo.put("summary", buildInfoService.getBuildInfoSummary());
        
        return buildInfo;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/system/health", produces = {"application/json"})
    @ResponseBody
    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        
        // Database Health
        try {
            // Use a lightweight count query instead of loading all users
            userService.countAllUsers(); // Simple database connectivity test
            health.put("database", "UP");
        } catch (Exception e) {
            health.put("database", "DOWN");
            health.put("databaseError", e.getMessage());
        }
        
        // Memory Health
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        health.put("memory", "UP");
        health.put("memoryUsagePercent", Math.round(memoryUsagePercent * 100.0) / 100.0);
        
        if (memoryUsagePercent > 90) {
            health.put("memory", "WARNING");
        }
        
        return health;
    }

    private String maskSensitiveInfo(String url) {
        if (url == null || url.isEmpty()) {
            return "Not configured";
        }
        
        // Mask password in JDBC URL
        if (url.contains("password=")) {
            return url.replaceAll("password=[^;]*", "password=***");
        }
        
        return url;
    }

    private int sanitizePage(int requestedPage) {
        if (requestedPage < 0) {
            logger.warn("Admin API received negative page {}. Using 0 instead.", requestedPage);
            return 0;
        }
        if (requestedPage > MAX_PAGE_INDEX) {
            logger.warn("Admin API page {} exceeds configured limit {}. Using cap.", requestedPage, MAX_PAGE_INDEX);
            return MAX_PAGE_INDEX;
        }
        return requestedPage;
    }

    private int sanitizePageSize(int requestedPageSize) {
        if (requestedPageSize <= 0) {
            logger.warn("Admin API received non-positive pageSize {}. Using default {}.", requestedPageSize, DEFAULT_PAGE_SIZE);
            return DEFAULT_PAGE_SIZE;
        }
        if (requestedPageSize > MAX_PAGE_SIZE) {
            logger.warn("Admin API pageSize {} exceeds configured limit {}. Using cap.", requestedPageSize, MAX_PAGE_SIZE);
            return MAX_PAGE_SIZE;
        }
        return requestedPageSize;
    }
}
