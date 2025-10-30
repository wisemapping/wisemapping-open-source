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

package com.wisemapping.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Calendar;

/**
 * Entity representing spam detection information for a mindmap.
 * This table is separate from the main MINDMAP table to denormalize it.
 * 
 * IMPORTANT: Caching is DISABLED for this entity to prevent inconsistencies.
 * Mindmap data changes frequently and must always reflect the latest state.
 */
@Entity
@Table(name = "MINDMAP_SPAM_INFO")
public class MindmapSpamInfo {
    
    @Id
    @Column(name = "mindmap_id")
    private Integer mindmapId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mindmap_id")
    @MapsId
    private Mindmap mindmap;
    
    @Column(name = "spam_detected", nullable = false)
    private boolean spamDetected = false;
    
    @Column(name = "spam_description", columnDefinition = "TEXT")
    private String spamDescription;
    
    @Column(name = "spam_detection_version", nullable = false)
    private int spamDetectionVersion = 0;
    
    @Convert(converter = SpamStrategyTypeConverter.class)
    @Column(name = "spam_type_code", columnDefinition = "CHAR(1)")
    private SpamStrategyType spamTypeCode;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Calendar createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Calendar updatedAt;
    
    // Default constructor
    public MindmapSpamInfo() {
    }
    
    // Constructor with mindmap
    public MindmapSpamInfo(Mindmap mindmap) {
        this.mindmap = mindmap;
        this.mindmapId = mindmap.getId();
    }
    
    // Getters and setters
    public Integer getMindmapId() {
        return mindmapId;
    }
    
    public void setMindmapId(Integer mindmapId) {
        this.mindmapId = mindmapId;
    }
    
    public Mindmap getMindmap() {
        return mindmap;
    }
    
    public void setMindmap(Mindmap mindmap) {
        this.mindmap = mindmap;
        this.mindmapId = mindmap != null ? mindmap.getId() : null;
    }
    
    public boolean isSpamDetected() {
        return spamDetected;
    }
    
    public void setSpamDetected(boolean spamDetected) {
        this.spamDetected = spamDetected;
    }
    
    public String getSpamDescription() {
        return spamDescription;
    }
    
    public void setSpamDescription(String spamDescription) {
        this.spamDescription = spamDescription;
    }
    
    public int getSpamDetectionVersion() {
        return spamDetectionVersion;
    }
    
    public void setSpamDetectionVersion(int spamDetectionVersion) {
        this.spamDetectionVersion = spamDetectionVersion;
    }
    
    public SpamStrategyType getSpamTypeCode() {
        return spamTypeCode;
    }

    public void setSpamTypeCode(SpamStrategyType spamTypeCode) {
        this.spamTypeCode = spamTypeCode;
    }
    
    public Calendar getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Calendar createdAt) {
        this.createdAt = createdAt;
    }
    
    public Calendar getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Calendar updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        MindmapSpamInfo that = (MindmapSpamInfo) o;
        
        return mindmapId != null ? mindmapId.equals(that.mindmapId) : that.mindmapId == null;
    }
    
    @Override
    public int hashCode() {
        return mindmapId != null ? mindmapId.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "MindmapSpamInfo{" +
                "mindmapId=" + mindmapId +
                ", spamDetected=" + spamDetected +
                ", spamDetectionVersion=" + spamDetectionVersion +
                ", spamTypeCode='" + spamTypeCode + '\'' +
                '}';
    }
}
