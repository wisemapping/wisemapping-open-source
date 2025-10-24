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

package com.wisemapping.test.rest;

import com.wisemapping.model.Account;
import com.wisemapping.model.AuthenticationType;
import com.wisemapping.rest.model.RestUser;
import com.wisemapping.rest.model.RestUserRegistration;
import com.wisemapping.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class TestDataManager {
    
    private static final String ADMIN_EMAIL = "admin@wisemapping.org";
    private static final String ADMIN_PASSWORD = "testAdmin123";
    private static final AtomicLong counter = new AtomicLong(System.currentTimeMillis());
    
    @Autowired
    private UserService userService;
    
    private final List<String> createdUserEmails = new ArrayList<>();
    
    public String getAdminEmail() {
        return ADMIN_EMAIL;
    }
    
    public String getAdminPassword() {
        return ADMIN_PASSWORD;
    }
    
    public RestUser createTestUser() {
        String email = generateUniqueEmail();
        RestUser user = new RestUser();
        user.setEmail(email);
        user.setFirstname("Test");
        user.setLastname("User");
        user.setPassword("testPassword123");
        
        createdUserEmails.add(email);
        return user;
    }
    
    public RestUserRegistration createTestUserRegistration() {
        String email = generateUniqueEmail();
        RestUserRegistration registration = RestUserRegistration.create(email, "testPassword123", "Test", "User");
        
        createdUserEmails.add(email);
        return registration;
    }
    
    public Account createAndSaveUser() {
        // Use existing test user instead of creating new one to avoid service dependency issues
        try {
            Account existingUser = userService.getUserBy("test@wisemapping.org");
            if (existingUser != null) {
                return existingUser;
            }
        } catch (Exception e) {
            // If test user doesn't exist, fall back to creating via mock
        }
        
        // Create mock user for test purposes
        String email = "test@wisemapping.org";
        Account user = new Account();
        user.setEmail(email);
        user.setFirstname("Test");
        user.setLastname("User");
        user.setPassword("password");
        user.setAuthenticationType(AuthenticationType.DATABASE);
        
        return user;
    }
    
    public List<RestUserRegistration> generateMultipleUsers(int count) {
        List<RestUserRegistration> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String email = generateUniqueEmail();
            RestUserRegistration user = RestUserRegistration.create(email, "testPassword123", "Test" + i, "User" + i);
            
            users.add(user);
            createdUserEmails.add(email);
        }
        return users;
    }
    
    public String generateUniqueEmail() {
        return "test-" + counter.incrementAndGet() + "@example.org";
    }
    
    public String generateMapTitle() {
        return "Test Map " + counter.incrementAndGet();
    }
    
    public String generateLabelTitle() {
        return "Test Label " + counter.incrementAndGet();
    }
    
    public RestUserRegistration createInvalidUserRegistration(String field, Object value) {
        String email = generateUniqueEmail();
        String firstname = "Test";
        String lastname = "User";
        String password = "testPassword123";
            
        switch (field) {
            case "email":
                email = (String) value;
                break;
            case "firstname":
                firstname = (String) value;
                break;
            case "lastname":
                lastname = (String) value;
                break;
            case "password":
                password = (String) value;
                break;
        }
        
        return RestUserRegistration.create(email, password, firstname, lastname);
    }
    
    public void cleanupTestData() {
        for (String email : createdUserEmails) {
            try {
                Account user = userService.getUserBy(email);
                if (user != null) {
                    userService.removeUser(user);
                }
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
        createdUserEmails.clear();
    }
}