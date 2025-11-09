package com.wisemapping.service;

import com.wisemapping.mindmap.model.MapModel;
import com.wisemapping.model.Account;
import com.wisemapping.model.Mindmap;
import com.wisemapping.service.spam.SpamDetectionContext;
import com.wisemapping.service.spam.SpamDetectionResult;
import com.wisemapping.service.spam.UserBehaviorStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserBehaviorStrategyTest {

    @Mock
    private MindmapService mindmapService;

    @Mock
    private Mindmap mindmap;

    @Mock
    private Account user;

    private UserBehaviorStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new UserBehaviorStrategy(mindmapService);
    }
    
    /**
     * Creates a SpamDetectionContext from a Mindmap.
     * Since UserBehaviorStrategy only uses metadata (not XML content), we create a minimal MapModel.
     */
    private SpamDetectionContext createContext(Mindmap mindmap) {
        if (mindmap == null) {
            return null;
        }
        // Create a minimal MapModel since UserBehaviorStrategy doesn't use XML content
        MapModel mapModel = new MapModel();
        return new SpamDetectionContext(mindmap, mapModel);
    }

    @Test
    void testNullMindmap() {
        SpamDetectionResult result = strategy.detectSpam((SpamDetectionContext) null);
        assertFalse(result.isSpam());
    }

    @Test
    void testMindmapWithoutModificationTime() {
        Calendar oldTime = Calendar.getInstance();
        oldTime.add(Calendar.MINUTE, -10);
        
        when(mindmap.getCreator()).thenReturn(user);
        when(mindmap.getCreationTime()).thenReturn(oldTime);
        when(mindmap.getLastModificationTime()).thenReturn(null);
        
        var context = createContext(mindmap);
        SpamDetectionResult result = strategy.detectSpam(context);
        assertFalse(result.isSpam());
    }

    @Test
    void testRecentlyCreatedMap_ShouldDetectSpam() {
        // Create a mindmap created 2 minutes ago
        Calendar recentTime = Calendar.getInstance();
        recentTime.add(Calendar.MINUTE, -2);
        
        when(mindmap.getCreator()).thenReturn(user);
        when(mindmap.getCreationTime()).thenReturn(recentTime);
        lenient().when(user.getEmail()).thenReturn("user@test.com");

        var context = createContext(mindmap);
        SpamDetectionResult result = strategy.detectSpam(context);
        assertTrue(result.isSpam());
        assertEquals("Map created within 5 minutes", result.getReason());
    }

    @Test
    void testMapModifiedWithin5MinutesOfRecentCreation_ShouldDetectSpam() {
        // Current map was created 30 minutes ago (passes Rule 1)
        Calendar oldCreationTime = Calendar.getInstance();
        oldCreationTime.add(Calendar.MINUTE, -30);
        
        // User's most recent map was created at 10:00 AM
        Calendar mostRecentCreationTime = Calendar.getInstance();
        mostRecentCreationTime.add(Calendar.MINUTE, -10);
        
        // Current map was modified at 10:03 AM (3 minutes after most recent creation)
        Calendar currentMapModificationTime = Calendar.getInstance();
        currentMapModificationTime.add(Calendar.MINUTE, -7);
        
        Mindmap mostRecentMap = mock(Mindmap.class);
        when(mostRecentMap.getCreationTime()).thenReturn(mostRecentCreationTime);
        
        when(mindmap.getCreator()).thenReturn(user);
        when(mindmap.getCreationTime()).thenReturn(oldCreationTime);
        when(mindmap.getLastModificationTime()).thenReturn(currentMapModificationTime);
        when(user.getEmail()).thenReturn("bot@test.com");
        when(mindmapService.findMindmapsByUser(user)).thenReturn(Arrays.asList(mindmap, mostRecentMap));

        var context = createContext(mindmap);
        SpamDetectionResult result = strategy.detectSpam(context);
        assertTrue(result.isSpam());
        assertEquals("Bot behavior detected - map modified within 5 minutes of recent map creation", result.getReason());
    }

    @Test
    void testOldMap_ShouldNotDetectSpam() {
        // Create a mindmap created 10 minutes ago
        Calendar oldTime = Calendar.getInstance();
        oldTime.add(Calendar.MINUTE, -10);
        
        when(mindmap.getCreator()).thenReturn(user);
        when(mindmap.getCreationTime()).thenReturn(oldTime);
        lenient().when(mindmap.getLastModificationTime()).thenReturn(oldTime);
        lenient().when(mindmapService.findMindmapsByUser(user)).thenReturn(Collections.singletonList(mindmap));

        var context = createContext(mindmap);
        SpamDetectionResult result = strategy.detectSpam(context);
        assertFalse(result.isSpam());
    }



    @Test
    void testSingleUserMap_ShouldNotTriggerMultiMapRule() {
        Calendar oldTime = Calendar.getInstance();
        oldTime.add(Calendar.MINUTE, -10);
        
        when(mindmap.getCreator()).thenReturn(user);
        when(mindmap.getCreationTime()).thenReturn(oldTime);
        lenient().when(mindmap.getLastModificationTime()).thenReturn(oldTime);
        lenient().when(mindmapService.findMindmapsByUser(user)).thenReturn(Collections.singletonList(mindmap));

        var context = createContext(mindmap);
        SpamDetectionResult result = strategy.detectSpam(context);
        assertFalse(result.isSpam());
    }

    @Test
    void testServiceException_ShouldNotFailDetection() {
        Calendar oldTime = Calendar.getInstance();
        oldTime.add(Calendar.MINUTE, -10);
        
        when(mindmap.getCreator()).thenReturn(user);
        when(mindmap.getCreationTime()).thenReturn(oldTime);
        when(mindmap.getLastModificationTime()).thenReturn(oldTime);
        when(mindmapService.findMindmapsByUser(user)).thenThrow(new RuntimeException("Service error"));

        var context = createContext(mindmap);
        SpamDetectionResult result = strategy.detectSpam(context);
        assertFalse(result.isSpam());
    }

    // ===== RULE 1 TESTS: Map created within 5 minutes =====
    
    @Test
    void testRule1_MapCreated1MinuteAgo_ShouldDetectSpam() {
        Calendar recentTime = Calendar.getInstance();
        recentTime.add(Calendar.MINUTE, -1);
        
        when(mindmap.getCreator()).thenReturn(user);
        when(mindmap.getCreationTime()).thenReturn(recentTime);
        
        var context = createContext(mindmap);
        SpamDetectionResult result = strategy.detectSpam(context);
        assertTrue(result.isSpam());
        assertEquals("Map created within 5 minutes", result.getReason());
    }
    
    @Test
    void testRule1_MapCreatedExactly5MinutesAgo_ShouldDetectSpam() {
        Calendar exactTime = Calendar.getInstance();
        exactTime.add(Calendar.MINUTE, -4); // Change to 4 minutes to ensure it's within the 5-minute window
        
        when(mindmap.getCreator()).thenReturn(user);
        when(mindmap.getCreationTime()).thenReturn(exactTime);
        
        var context = createContext(mindmap);
        SpamDetectionResult result = strategy.detectSpam(context);
        assertTrue(result.isSpam());
        assertEquals("Map created within 5 minutes", result.getReason());
    }
    
    @Test
    void testRule1_MapCreated6MinutesAgo_ShouldNotDetectSpam() {
        Calendar oldTime = Calendar.getInstance();
        oldTime.add(Calendar.MINUTE, -6);
        
        when(mindmap.getCreator()).thenReturn(user);
        when(mindmap.getCreationTime()).thenReturn(oldTime);
        lenient().when(mindmap.getLastModificationTime()).thenReturn(oldTime);
        lenient().when(mindmapService.findMindmapsByUser(user)).thenReturn(Collections.singletonList(mindmap));
        
        var context = createContext(mindmap);
        SpamDetectionResult result = strategy.detectSpam(context);
        assertFalse(result.isSpam());
    }

    // ===== RULE 2 TESTS: Bot behavior detection =====
    
    @Test
    void testRule2_MapModified2MinutesAfterRecentCreation_ShouldDetectSpam() {
        // Current map created 1 hour ago (passes Rule 1)
        Calendar oldCreationTime = Calendar.getInstance();
        oldCreationTime.add(Calendar.HOUR, -1);
        
        // Most recent map created 10 minutes ago
        Calendar recentCreationTime = Calendar.getInstance();
        recentCreationTime.add(Calendar.MINUTE, -10);
        
        // Current map modified 8 minutes ago (2 minutes after recent creation)
        Calendar modificationTime = Calendar.getInstance();
        modificationTime.add(Calendar.MINUTE, -8);
        
        Mindmap recentMap = mock(Mindmap.class);
        when(recentMap.getCreationTime()).thenReturn(recentCreationTime);
        
        when(mindmap.getCreator()).thenReturn(user);
        when(mindmap.getCreationTime()).thenReturn(oldCreationTime);
        when(mindmap.getLastModificationTime()).thenReturn(modificationTime);
        when(user.getEmail()).thenReturn("bot@example.com");
        when(mindmapService.findMindmapsByUser(user)).thenReturn(Arrays.asList(mindmap, recentMap));
        
        var context = createContext(mindmap);
        SpamDetectionResult result = strategy.detectSpam(context);
        assertTrue(result.isSpam());
        assertEquals("Bot behavior detected - map modified within 5 minutes of recent map creation", result.getReason());
    }
    
    @Test
    void testRule2_MapModifiedExactly5MinutesAfterRecentCreation_ShouldDetectSpam() {
        Calendar oldCreationTime = Calendar.getInstance();
        oldCreationTime.add(Calendar.HOUR, -1);
        
        Calendar recentCreationTime = Calendar.getInstance();
        recentCreationTime.add(Calendar.MINUTE, -10);
        
        // Modified exactly 5 minutes after recent creation
        Calendar modificationTime = Calendar.getInstance();
        modificationTime.add(Calendar.MINUTE, -5);
        
        Mindmap recentMap = mock(Mindmap.class);
        when(recentMap.getCreationTime()).thenReturn(recentCreationTime);
        
        when(mindmap.getCreator()).thenReturn(user);
        when(mindmap.getCreationTime()).thenReturn(oldCreationTime);
        when(mindmap.getLastModificationTime()).thenReturn(modificationTime);
        when(user.getEmail()).thenReturn("bot@example.com");
        when(mindmapService.findMindmapsByUser(user)).thenReturn(Arrays.asList(mindmap, recentMap));
        
        var context = createContext(mindmap);
        SpamDetectionResult result = strategy.detectSpam(context);
        assertTrue(result.isSpam());
        assertEquals("Bot behavior detected - map modified within 5 minutes of recent map creation", result.getReason());
    }
    
    @Test
    void testRule2_MapModified6MinutesAfterRecentCreation_ShouldNotDetectSpam() {
        Calendar oldCreationTime = Calendar.getInstance();
        oldCreationTime.add(Calendar.HOUR, -1);
        
        Calendar recentCreationTime = Calendar.getInstance();
        recentCreationTime.add(Calendar.MINUTE, -20);
        
        // Modified 6 minutes after recent creation (outside 5-minute window)
        Calendar modificationTime = Calendar.getInstance();
        modificationTime.add(Calendar.MINUTE, -14);
        
        Mindmap recentMap = mock(Mindmap.class);
        when(recentMap.getCreationTime()).thenReturn(recentCreationTime);
        
        when(mindmap.getCreator()).thenReturn(user);
        when(mindmap.getCreationTime()).thenReturn(oldCreationTime);
        when(mindmap.getLastModificationTime()).thenReturn(modificationTime);
        when(mindmapService.findMindmapsByUser(user)).thenReturn(Arrays.asList(mindmap, recentMap));
        
        var context = createContext(mindmap);
        SpamDetectionResult result = strategy.detectSpam(context);
        assertFalse(result.isSpam());
    }
    
    @Test
    void testRule2_MapModifiedBeforeRecentCreation_ShouldDetectSpam() {
        Calendar oldCreationTime = Calendar.getInstance();
        oldCreationTime.add(Calendar.HOUR, -1);
        
        Calendar recentCreationTime = Calendar.getInstance();
        recentCreationTime.add(Calendar.MINUTE, -10);
        
        // Modified 2 minutes BEFORE recent creation (still within 5-minute window)
        Calendar modificationTime = Calendar.getInstance();
        modificationTime.add(Calendar.MINUTE, -12);
        
        Mindmap recentMap = mock(Mindmap.class);
        when(recentMap.getCreationTime()).thenReturn(recentCreationTime);
        
        when(mindmap.getCreator()).thenReturn(user);
        when(mindmap.getCreationTime()).thenReturn(oldCreationTime);
        when(mindmap.getLastModificationTime()).thenReturn(modificationTime);
        when(user.getEmail()).thenReturn("bot@example.com");
        when(mindmapService.findMindmapsByUser(user)).thenReturn(Arrays.asList(mindmap, recentMap));
        
        var context = createContext(mindmap);
        SpamDetectionResult result = strategy.detectSpam(context);
        assertTrue(result.isSpam());
        assertEquals("Bot behavior detected - map modified within 5 minutes of recent map creation", result.getReason());
    }

    // ===== EDGE CASES AND PRIORITY TESTS =====
    
    @Test
    void testRule1TakesPriorityOverRule2() {
        // Map created 2 minutes ago (should trigger Rule 1)
        Calendar recentCreationTime = Calendar.getInstance();
        recentCreationTime.add(Calendar.MINUTE, -2);
        
        Calendar modificationTime = Calendar.getInstance();
        modificationTime.add(Calendar.MINUTE, -1);
        
        when(mindmap.getCreator()).thenReturn(user);
        when(mindmap.getCreationTime()).thenReturn(recentCreationTime);
        lenient().when(mindmap.getLastModificationTime()).thenReturn(modificationTime);
        
        var context = createContext(mindmap);
        SpamDetectionResult result = strategy.detectSpam(context);
        assertTrue(result.isSpam());
        assertEquals("Map created within 5 minutes", result.getReason());
    }
    
    @Test
    void testRule2_NoModificationTime_ShouldNotTriggerRule2() {
        Calendar oldCreationTime = Calendar.getInstance();
        oldCreationTime.add(Calendar.HOUR, -1);
        
        when(mindmap.getCreator()).thenReturn(user);
        when(mindmap.getCreationTime()).thenReturn(oldCreationTime);
        when(mindmap.getLastModificationTime()).thenReturn(null); // No modification time
        
        var context = createContext(mindmap);
        SpamDetectionResult result = strategy.detectSpam(context);
        assertFalse(result.isSpam());
    }
    
    @Test
    void testRule2_OnlyOneMap_ShouldNotTriggerRule2() {
        Calendar oldCreationTime = Calendar.getInstance();
        oldCreationTime.add(Calendar.HOUR, -1);
        
        Calendar modificationTime = Calendar.getInstance();
        modificationTime.add(Calendar.MINUTE, -1);
        
        when(mindmap.getCreator()).thenReturn(user);
        when(mindmap.getCreationTime()).thenReturn(oldCreationTime);
        when(mindmap.getLastModificationTime()).thenReturn(modificationTime);
        when(mindmapService.findMindmapsByUser(user)).thenReturn(Collections.singletonList(mindmap));
        
        var context = createContext(mindmap);
        SpamDetectionResult result = strategy.detectSpam(context);
        assertFalse(result.isSpam());
    }
}