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

package com.wisemapping.scheduler;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.wisemapping.service.SpamDetectionBatchService;
import com.wisemapping.service.SpamUserSuspensionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for spam-related schedulers.
 * Note: Schedulers are controlled by @ConditionalOnProperty at bean creation time.
 * These tests verify the scheduler logic when they're enabled and created.
 */
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

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setup() {
        // Create a ListAppender to capture log messages for verification
        listAppender = new ListAppender<>();
        listAppender.start();
        
        // We attach the appender to the loggers and disable additivity (setAdditive(false)).
        // This prevents the expected "ERROR" logs and their stack traces from being 
        // printed to the console during test execution, while still allowing 
        // our listAppender to capture them for assertion.
        ch.qos.logback.classic.Logger detectionLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(SpamDetectionScheduler.class);
        detectionLogger.addAppender(listAppender);
        detectionLogger.setAdditive(false);

        ch.qos.logback.classic.Logger suspensionLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(SpamUserSuspensionScheduler.class);
        suspensionLogger.addAppender(listAppender);
        suspensionLogger.setAdditive(false);
    }

    @AfterEach
    void tearDown() {
        listAppender.stop();
        
        // Restore default logger behavior by re-enabling additivity and detaching the test appender
        ch.qos.logback.classic.Logger detectionLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(SpamDetectionScheduler.class);
        detectionLogger.detachAppender(listAppender);
        detectionLogger.setAdditive(true);

        ch.qos.logback.classic.Logger suspensionLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(SpamUserSuspensionScheduler.class);
        suspensionLogger.detachAppender(listAppender);
        suspensionLogger.setAdditive(true);
    }

    @Test
    void testSpamDetectionScheduler_ShouldCallService() {
        // Act
        spamDetectionScheduler.processSpamDetection();

        // Assert
        verify(spamDetectionBatchService, times(1)).processPublicMapsSpamDetection();
    }

    @Test
    void testSpamUserSuspensionScheduler_ShouldCallService() {
        // Act
        spamUserSuspensionScheduler.processSpamUserSuspension();

        // Assert
        verify(spamUserSuspensionService, times(1)).processSpamUserSuspension();
    }

    @Test
    void testSpamDetectionScheduler_WithException_ShouldHandleGracefully() {
        // Arrange: Simulate a failure in the underlying service
        doThrow(new RuntimeException("Test exception")).when(spamDetectionBatchService).processPublicMapsSpamDetection();

        // Act & Assert: The scheduler should catch the exception and not propagate it
        assertDoesNotThrow(() -> spamDetectionScheduler.processSpamDetection());
        verify(spamDetectionBatchService, times(1)).processPublicMapsSpamDetection();

        // Assert: Verify that an ERROR was logged even though it was suppressed from the console
        assertTrue(listAppender.list.stream()
                .anyMatch(event -> event.getLevel() == Level.ERROR && 
                          event.getFormattedMessage().contains("Scheduled spam detection task failed")));
    }

    @Test
    void testSpamUserSuspensionScheduler_WithException_ShouldHandleGracefully() {
        // Arrange: Simulate a failure in the underlying service
        doThrow(new RuntimeException("Test exception")).when(spamUserSuspensionService).processSpamUserSuspension();

        // Act & Assert: The scheduler should catch the exception and not propagate it
        assertDoesNotThrow(() -> spamUserSuspensionScheduler.processSpamUserSuspension());
        verify(spamUserSuspensionService, times(1)).processSpamUserSuspension();

        // Assert: Verify that an ERROR was logged even though it was suppressed from the console
        assertTrue(listAppender.list.stream()
                .anyMatch(event -> event.getLevel() == Level.ERROR && 
                          event.getFormattedMessage().contains("Scheduled spam user suspension task failed")));
    }
}
