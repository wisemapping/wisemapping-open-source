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

package com.wisemapping.service.spam;

import com.wisemapping.model.Mindmap;
import com.wisemapping.model.SpamStrategyType;
import com.wisemapping.service.MindmapService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;

@Component
public class UserBehaviorStrategy implements SpamDetectionStrategy {

    private final MindmapService mindmapService;

    public UserBehaviorStrategy(MindmapService mindmapService) {
        this.mindmapService = mindmapService;
    }

    @Override
    @Transactional(readOnly = true)
    public SpamDetectionResult detectSpam(Mindmap mindmap) {
        if (mindmap == null || mindmap.getCreator() == null || mindmap.getCreationTime() == null) {
            return SpamDetectionResult.notSpam();
        }

        Calendar now = Calendar.getInstance();
        Calendar creationTime = mindmap.getCreationTime();
        
        // Rule 1: Map created within 5 minutes = spam
        long diffInMillis = now.getTimeInMillis() - creationTime.getTimeInMillis();
        long minutesSinceCreation = diffInMillis / (60 * 1000);
        if (minutesSinceCreation < 5) {
            return SpamDetectionResult.spam("Map created within 5 minutes",
                    String.format("Created: %s, Current: %s, Minutes: %d", 
                                creationTime.getTime(), now.getTime(), minutesSinceCreation), getType());
        }

        // Rule 2: Bot behavior detection - map modified within 5 minutes of most recent map creation
        if (mindmap.getLastModificationTime() == null) {
            return SpamDetectionResult.notSpam();
        }

        try {
            // Get all maps for this user
            List<Mindmap> userMaps = mindmapService.findMindmapsByUser(mindmap.getCreator());
            
            if (userMaps.size() > 1) {
                // Find the most recent created map by this user (excluding the current map being checked)
                Calendar mostRecentCreationTime = userMaps.stream()
                    .filter(m -> !m.equals(mindmap)) // Exclude current map
                    .map(Mindmap::getCreationTime)
                    .filter(Objects::nonNull)
                    .max(Calendar::compareTo)
                    .orElse(null);

                if (mostRecentCreationTime != null) {
                    // Check if current map was modified within 5 minutes of the most recent map creation
                    Calendar currentMapModificationTime = mindmap.getLastModificationTime();
                    long modDiffInMillis = currentMapModificationTime.getTimeInMillis() - mostRecentCreationTime.getTimeInMillis();
                    long minutesDifference = modDiffInMillis / (60 * 1000);

                    // If modification time is within 5 minutes of most recent map creation, it's bot behavior
                    if (Math.abs(minutesDifference) <= 5) {
                        return SpamDetectionResult.spam("Bot behavior detected - map modified within 5 minutes of recent map creation",
                                String.format("User: %s, Current map modified: %s, Most recent map created: %s, Minutes difference: %d",
                                            mindmap.getCreator().getEmail(),
                                            currentMapModificationTime.getTime(),
                                            mostRecentCreationTime.getTime(),
                                            minutesDifference), getType());
                    }
                }
            }

        } catch (Exception e) {
            // If we can't retrieve user maps, don't fail the detection
            return SpamDetectionResult.notSpam();
        }

        return SpamDetectionResult.notSpam();
    }

    @Override
    public SpamStrategyType getType() {
        return SpamStrategyType.USER_BEHAVIOR;
    }
}