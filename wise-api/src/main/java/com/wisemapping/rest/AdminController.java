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
import com.wisemapping.exceptions.PasswordChangeNotAllowedException;
import com.wisemapping.model.AuthenticationType;
import com.wisemapping.model.Collaboration;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.Account;
import com.wisemapping.model.SuspensionReason;
import com.wisemapping.rest.model.RestUser;
import com.wisemapping.rest.model.PaginatedResponse;
import com.wisemapping.service.MindmapService;
import com.wisemapping.service.MetricsService;
import com.wisemapping.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
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
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping("/api/restful/admin")
@PreAuthorize("isAuthenticated() and hasRole('ROLE_ADMIN')")
public class AdminController {
    private static final Logger logger = LogManager.getLogger(AdminController.class);
    
    @Qualifier("userService")
    @Autowired
    private UserService userService;

    @Qualifier("mindmapService")
    @Autowired
    private MindmapService mindmapService;

    @Autowired
    private MetricsService metricsService;

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

    @RequestMapping(method = RequestMethod.GET, value = "/users", produces = {"application/json"})
    @ResponseBody
    public PaginatedResponse<com.wisemapping.rest.model.AdminRestUser> getAllUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "search", required = false) String search) {
        
        if (search != null && !search.trim().isEmpty()) {
            // Search users - using optimized AdminRestUser DTO
            final List<Account> users = userService.searchUsers(search, page, pageSize);
            final long totalElements = userService.countUsersBySearch(search);
            final List<com.wisemapping.rest.model.AdminRestUser> restUsers = users.stream()
                    .map(user -> new com.wisemapping.rest.model.AdminRestUser(user, isAdmin(user.getEmail())))
                    .collect(java.util.stream.Collectors.toList());
            return new PaginatedResponse<>(restUsers, page, pageSize, totalElements);
        } else {
            // Get all users with pagination - using optimized AdminRestUser DTO
            final List<Account> users = userService.getAllUsers(page, pageSize);
            final long totalElements = userService.countAllUsers();
            final List<com.wisemapping.rest.model.AdminRestUser> restUsers = users.stream()
                    .map(user -> new com.wisemapping.rest.model.AdminRestUser(user, isAdmin(user.getEmail())))
                    .collect(java.util.stream.Collectors.toList());
            return new PaginatedResponse<>(restUsers, page, pageSize, totalElements);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/users/{id}", produces = {"application/json"})
    @ResponseBody
    public RestUser getUserById(@PathVariable int id) {
        final Account userBy = userService.getUserBy(id);
        if (userBy == null) {
            throw new IllegalArgumentException("User could not be found");
        }
        return new RestUser(userBy, isAdmin(userBy.getEmail()));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/users/email/{email:.+}", produces = {"application/json"})
    @ResponseBody
    public RestUser getUserByEmail(@PathVariable String email) {
        final Account user = userService.getUserBy(email);
        if (user == null) {
            throw new IllegalArgumentException("User '" + email + "' could not be found");
        }
        return new RestUser(user, isAdmin(user.getEmail()));
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

        // Finally create the user ...
        delegated.setAuthenticationType(AuthenticationType.DATABASE);
        userService.createUser(delegated, false, true);
        
        // Track user registration
        String emailProvider = metricsService.extractEmailProvider(delegated.getEmail());
        metricsService.trackUserRegistration(delegated, emailProvider);
        
        response.setHeader("Location", "/api/restful/admin/users/" + user.getId());
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
                    // Unsuspend user
                    existingUser.unsuspend();
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
    public void changePassword(@RequestBody String password, @PathVariable int id) throws PasswordChangeNotAllowedException {
        if (password == null) {
            throw new IllegalArgumentException("Password can not be null");
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

    @RequestMapping(method = RequestMethod.DELETE, value = "/users/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteUserByEmail(@PathVariable int id) throws WiseMappingException {
        final Account user = userService.getUserBy(id);
        if (user == null) {
            throw new IllegalArgumentException("User '" + id + "' could not be found");
        }

        final List<Collaboration> collaborations = mindmapService.findCollaborations(user);
        for (Collaboration collaboration : collaborations) {
            final Mindmap mindmap = collaboration.getMindMap();
            mindmapService.removeMindmap(mindmap, user);
        }
        userService.removeUser(user);
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
        
        if (search != null && !search.trim().isEmpty()) {
            // Search mindmaps - using optimized AdminRestMap DTO
            final List<Mindmap> mindmaps = mindmapService.searchMindmaps(search, filterPublic, filterLocked, filterSpam, page, pageSize);
            final long totalElements = mindmapService.countMindmapsBySearch(search, filterPublic, filterLocked, filterSpam);
            final List<com.wisemapping.rest.model.AdminRestMap> restMaps = mindmaps.stream()
                    .map(com.wisemapping.rest.model.AdminRestMap::new)
                    .collect(java.util.stream.Collectors.toList());
            return new PaginatedResponse<>(restMaps, page, pageSize, totalElements);
        } else {
            // Get all mindmaps with pagination and date filtering - using optimized AdminRestMap DTO
            final List<Mindmap> mindmaps = mindmapService.getAllMindmaps(filterPublic, filterLocked, filterSpam, dateFilter, page, pageSize);
            final long totalElements = mindmapService.countAllMindmaps(filterPublic, filterLocked, filterSpam, dateFilter);
            final List<com.wisemapping.rest.model.AdminRestMap> restMaps = mindmaps.stream()
                    .map(com.wisemapping.rest.model.AdminRestMap::new)
                    .collect(java.util.stream.Collectors.toList());
            return new PaginatedResponse<>(restMaps, page, pageSize, totalElements);
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
        
        return systemInfo;
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
}
