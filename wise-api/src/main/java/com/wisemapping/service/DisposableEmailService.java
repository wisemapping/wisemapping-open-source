/*
 *    Copyright [2022] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.wisemapping.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

@Service
public class DisposableEmailService {

    private static final Logger logger = LogManager.getLogger();
    private Set<String> disposableDomains = new HashSet<>();

    @Value("${app.registration.disposable-email.blocking.enabled:false}")
    private boolean blockingEnabled;

    @PostConstruct
    public void loadDisposableDomains() {
        if (!blockingEnabled) {
            logger.info("Disposable email blocking is disabled");
            return;
        }

        try (InputStream inputStream = getClass().getResourceAsStream("/disposable-email-domains.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            
            String domain;
            while ((domain = reader.readLine()) != null) {
                domain = domain.trim().toLowerCase();
                if (!domain.isEmpty() && !domain.startsWith("#")) {
                    disposableDomains.add(domain);
                }
            }
            logger.info("Loaded {} disposable email domains", disposableDomains.size());
        } catch (IOException e) {
            logger.warn("Could not load disposable email domains list: {}", e.getMessage());
        }
    }

    public boolean isDisposableEmail(String email) {
        if (!blockingEnabled || email == null) {
            return false;
        }

        String domain = extractDomain(email);
        return domain != null && disposableDomains.contains(domain.toLowerCase());
    }

    private String extractDomain(String email) {
        int atIndex = email.lastIndexOf('@');
        if (atIndex > 0 && atIndex < email.length() - 1) {
            return email.substring(atIndex + 1);
        }
        return null;
    }
}