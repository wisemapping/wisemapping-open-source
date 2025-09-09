package com.wisemapping.dao;

import com.wisemapping.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MindmapManagerImplSpamTypeTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query nativeQuery;

    private MindmapManagerImpl mindmapManager;

    @BeforeEach
    void setUp() {
        mindmapManager = new MindmapManagerImpl();
        // Use reflection to inject the mock EntityManager
        try {
            java.lang.reflect.Field field = MindmapManagerImpl.class.getDeclaredField("entityManager");
            field.setAccessible(true);
            field.set(mindmapManager, entityManager);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock EntityManager", e);
        }
    }

    @Test
    void testUpdateMindmapSpamInfo_ShouldConvertSpamTypeEnumToCharacter() {
        // Test all spam strategy types
        SpamStrategyType[] spamTypes = {
            SpamStrategyType.CONTACT_INFO,
            SpamStrategyType.FEW_NODES,
            SpamStrategyType.USER_BEHAVIOR,
            SpamStrategyType.KEYWORD_PATTERN
        };

        for (SpamStrategyType spamType : spamTypes) {
            // Arrange
            MindmapSpamInfo spamInfo = createSpamInfo(1, true, spamType, "Test description", 1);
            
            when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
            when(nativeQuery.setParameter(anyInt(), any())).thenReturn(nativeQuery);
            when(nativeQuery.executeUpdate()).thenReturn(1);

            // Act
            mindmapManager.updateMindmapSpamInfo(spamInfo);

            // Assert - Verify that the enum is converted to its character code
            verify(nativeQuery).setParameter(eq(4), eq(spamType.getCode())); // Should be Character, not enum
        }
    }

    @Test
    void testUpdateMindmapSpamInfo_WithNullSpamType_ShouldPassNull() {
        // Arrange
        MindmapSpamInfo spamInfo = createSpamInfo(1, false, null, null, 1);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyInt(), any())).thenReturn(nativeQuery);
        when(nativeQuery.executeUpdate()).thenReturn(1);

        // Act
        mindmapManager.updateMindmapSpamInfo(spamInfo);

        // Assert - Verify that null spam type is passed as null
        verify(nativeQuery).setParameter(eq(4), eq(null)); // Should be null Character
    }

    @Test
    void testUpdateMindmapSpamInfo_SpamTypeConversion_ShouldWorkForAllTypes() {
        // Test specific character codes
        SpamStrategyType contactInfo = SpamStrategyType.CONTACT_INFO;
        SpamStrategyType fewNodes = SpamStrategyType.FEW_NODES;
        SpamStrategyType userBehavior = SpamStrategyType.USER_BEHAVIOR;
        SpamStrategyType keywordPattern = SpamStrategyType.KEYWORD_PATTERN;

        // Verify the character codes
        assertEquals('C', contactInfo.getCode());
        assertEquals('F', fewNodes.getCode());
        assertEquals('U', userBehavior.getCode());
        assertEquals('K', keywordPattern.getCode());

        // Test each type
        MindmapSpamInfo[] spamInfos = {
            createSpamInfo(1, true, contactInfo, "Contact info spam", 1),
            createSpamInfo(2, true, fewNodes, "Few nodes spam", 1),
            createSpamInfo(3, true, userBehavior, "User behavior spam", 1),
            createSpamInfo(4, true, keywordPattern, "Keyword pattern spam", 1)
        };

        for (MindmapSpamInfo spamInfo : spamInfos) {
            when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
            when(nativeQuery.setParameter(anyInt(), any())).thenReturn(nativeQuery);
            when(nativeQuery.executeUpdate()).thenReturn(1);

            // Act
            mindmapManager.updateMindmapSpamInfo(spamInfo);

            // Assert - Verify the correct character is passed
            verify(nativeQuery).setParameter(eq(4), eq(spamInfo.getSpamTypeCode().getCode()));
        }
    }

    @Test
    void testUpdateMindmapSpamInfo_ShouldNotPassEnumDirectly() {
        // Arrange
        MindmapSpamInfo spamInfo = createSpamInfo(1, true, SpamStrategyType.CONTACT_INFO, "Test description", 1);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyInt(), any())).thenReturn(nativeQuery);
        when(nativeQuery.executeUpdate()).thenReturn(1);

        // Act
        mindmapManager.updateMindmapSpamInfo(spamInfo);

        // Assert - Verify that we don't pass the enum directly
        verify(nativeQuery, never()).setParameter(eq(4), eq(SpamStrategyType.CONTACT_INFO));
        
        // But we do pass the character code
        verify(nativeQuery).setParameter(eq(4), eq('C'));
    }

    private MindmapSpamInfo createSpamInfo(int mindmapId, boolean spamDetected, SpamStrategyType spamType, String description, int version) {
        MindmapSpamInfo spamInfo = new MindmapSpamInfo();
        spamInfo.setMindmapId(mindmapId);
        spamInfo.setSpamDetected(spamDetected);
        spamInfo.setSpamTypeCode(spamType);
        spamInfo.setSpamDescription(description);
        spamInfo.setSpamDetectionVersion(version);
        return spamInfo;
    }
}
