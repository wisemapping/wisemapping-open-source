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

package com.wisemapping.test.service;

import com.wisemapping.service.DisposableEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Disposable Email Service Tests")
class DisposableEmailServiceTest {

    private DisposableEmailService disposableEmailService;

    @BeforeEach
    void setUp() {
        disposableEmailService = new DisposableEmailService();
    }

    @Test
    @DisplayName("Should return false when blocking is disabled")
    void shouldReturnFalseWhenBlockingDisabled() {
        ReflectionTestUtils.setField(disposableEmailService, "blockingEnabled", false);
        disposableEmailService.loadDisposableDomains();

        assertFalse(disposableEmailService.isDisposableEmail("test@10minutemail.com"));
        assertFalse(disposableEmailService.isDisposableEmail("user@tempmail.org"));
    }

    @Test
    @DisplayName("Should return false for null email")
    void shouldReturnFalseForNullEmail() {
        ReflectionTestUtils.setField(disposableEmailService, "blockingEnabled", true);
        disposableEmailService.loadDisposableDomains();

        assertFalse(disposableEmailService.isDisposableEmail(null));
    }

    @Test
    @DisplayName("Should detect known disposable email domains")
    void shouldDetectKnownDisposableEmailDomains() {
        ReflectionTestUtils.setField(disposableEmailService, "blockingEnabled", true);
        disposableEmailService.loadDisposableDomains();

        assertTrue(disposableEmailService.isDisposableEmail("test@10minutemail.com"));
        assertTrue(disposableEmailService.isDisposableEmail("user@tempmail.us"));
        assertTrue(disposableEmailService.isDisposableEmail("someone@guerrillamail.com"));
    }

    @Test
    @DisplayName("Should allow legitimate email domains")
    void shouldAllowLegitimateEmailDomains() {
        ReflectionTestUtils.setField(disposableEmailService, "blockingEnabled", true);
        disposableEmailService.loadDisposableDomains();

        assertFalse(disposableEmailService.isDisposableEmail("user@gmail.com"));
        assertFalse(disposableEmailService.isDisposableEmail("test@yahoo.com"));
        assertFalse(disposableEmailService.isDisposableEmail("contact@company.com"));
        assertFalse(disposableEmailService.isDisposableEmail("admin@wisemapping.org"));
    }

    @ParameterizedTest
    @DisplayName("Should handle case insensitive email domains")
    @ValueSource(strings = {
        "TEST@10minutemail.com",
        "User@TEMPMAIL.US", 
        "Someone@GuerillaMail.COM",
        "test@10MINUTEMAIL.com"
    })
    void shouldHandleCaseInsensitiveEmailDomains(String email) {
        ReflectionTestUtils.setField(disposableEmailService, "blockingEnabled", true);
        disposableEmailService.loadDisposableDomains();

        assertTrue(disposableEmailService.isDisposableEmail(email));
    }

    @ParameterizedTest
    @DisplayName("Should handle invalid email formats gracefully")
    @ValueSource(strings = {
        "invalid-email",
        "@domain.com",
        "user@",
        "",
        "user@@domain.com",
        "user@domain@com"
    })
    void shouldHandleInvalidEmailFormatsGracefully(String invalidEmail) {
        ReflectionTestUtils.setField(disposableEmailService, "blockingEnabled", true);
        disposableEmailService.loadDisposableDomains();

        assertFalse(disposableEmailService.isDisposableEmail(invalidEmail));
    }

    @Test
    @DisplayName("Should extract domain correctly from valid emails")
    void shouldExtractDomainCorrectly() {
        ReflectionTestUtils.setField(disposableEmailService, "blockingEnabled", true);
        disposableEmailService.loadDisposableDomains();

        assertTrue(disposableEmailService.isDisposableEmail("user.name+tag@10minutemail.com"));
        assertTrue(disposableEmailService.isDisposableEmail("test.email@tempmail.us"));
    }
}