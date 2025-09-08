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

package com.wisemapping.scheduler;

import com.wisemapping.service.SpamDetectionBatchService;
import com.wisemapping.service.SpamUserSuspensionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulerUnitTest {

    @Mock
    private SpamDetectionBatchService spamDetectionBatchService;

    @Mock
    private SpamUserSuspensionService spamUserSuspensionService;

    @InjectMocks
    private SpamDetectionScheduler spamDetectionScheduler;

    @InjectMocks
    private SpamUserSuspensionScheduler spamUserSuspensionScheduler;

    @BeforeEach
    void setUp() {
        // Set enabled to true by default
        ReflectionTestUtils.setField(spamDetectionScheduler, "enabled", true);
        ReflectionTestUtils.setField(spamUserSuspensionScheduler, "enabled", true);
    }

    @Test
    void testSpamDetectionScheduler_WhenEnabled_ShouldCallService() {
        // Act
        spamDetectionScheduler.processSpamDetection();

        // Assert
        verify(spamDetectionBatchService, times(1)).processPublicMapsSpamDetection();
    }

    @Test
    void testSpamDetectionScheduler_WhenDisabled_ShouldNotCallService() {
        // Arrange
        ReflectionTestUtils.setField(spamDetectionScheduler, "enabled", false);

        // Act
        spamDetectionScheduler.processSpamDetection();

        // Assert
        verify(spamDetectionBatchService, never()).processPublicMapsSpamDetection();
    }

    @Test
    void testSpamDetectionScheduler_OnStartup_ShouldCallService() {
        // Act
        spamDetectionScheduler.processSpamDetectionOnStartup();

        // Assert
        verify(spamDetectionBatchService, times(1)).processPublicMapsSpamDetection();
    }

    @Test
    void testSpamDetectionScheduler_OnStartup_WhenDisabled_ShouldNotCallService() {
        // Arrange
        ReflectionTestUtils.setField(spamDetectionScheduler, "enabled", false);

        // Act
        spamDetectionScheduler.processSpamDetectionOnStartup();

        // Assert
        verify(spamDetectionBatchService, never()).processPublicMapsSpamDetection();
    }

    @Test
    void testSpamUserSuspensionScheduler_WhenEnabled_ShouldCallService() {
        // Act
        spamUserSuspensionScheduler.processSpamUserSuspension();

        // Assert
        verify(spamUserSuspensionService, times(1)).processSpamUserSuspension();
    }

    @Test
    void testSpamUserSuspensionScheduler_WhenDisabled_ShouldNotCallService() {
        // Arrange
        ReflectionTestUtils.setField(spamUserSuspensionScheduler, "enabled", false);

        // Act
        spamUserSuspensionScheduler.processSpamUserSuspension();

        // Assert
        verify(spamUserSuspensionService, never()).processSpamUserSuspension();
    }

    @Test
    void testSpamDetectionScheduler_WithException_ShouldHandleGracefully() {
        // Arrange
        doThrow(new RuntimeException("Test exception")).when(spamDetectionBatchService).processPublicMapsSpamDetection();

        // Act & Assert
        assertDoesNotThrow(() -> spamDetectionScheduler.processSpamDetection());
        verify(spamDetectionBatchService, times(1)).processPublicMapsSpamDetection();
    }

    @Test
    void testSpamUserSuspensionScheduler_WithException_ShouldHandleGracefully() {
        // Arrange
        doThrow(new RuntimeException("Test exception")).when(spamUserSuspensionService).processSpamUserSuspension();

        // Act & Assert
        assertDoesNotThrow(() -> spamUserSuspensionScheduler.processSpamUserSuspension());
        verify(spamUserSuspensionService, times(1)).processSpamUserSuspension();
    }

    @Test
    void testSpamDetectionScheduler_Configuration_ShouldBeInjectable() {
        // Test that the configuration values can be set
        ReflectionTestUtils.setField(spamDetectionScheduler, "enabled", false);
        
        // Act
        spamDetectionScheduler.processSpamDetection();
        
        // Assert
        verify(spamDetectionBatchService, never()).processPublicMapsSpamDetection();
    }

    @Test
    void testSpamUserSuspensionScheduler_Configuration_ShouldBeInjectable() {
        // Test that the configuration values can be set
        ReflectionTestUtils.setField(spamUserSuspensionScheduler, "enabled", false);
        
        // Act
        spamUserSuspensionScheduler.processSpamUserSuspension();
        
        // Assert
        verify(spamUserSuspensionService, never()).processSpamUserSuspension();
    }
}
