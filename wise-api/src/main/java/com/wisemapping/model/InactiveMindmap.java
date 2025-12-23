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

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.LazyGroup;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Entity representing mindmaps that have been moved from inactive users.
 * These mindmaps are no longer accessible through normal application flows.
 */
@Entity
@Table(name = "MINDMAP_INACTIVE_USER")
public class InactiveMindmap implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "original_mindmap_id")
    private int originalMindmapId;

    @Column(name = "creation_date")
    private Calendar creationTime;

    @Column(name = "edition_date")
    private Calendar lastModificationTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    @JsonIgnore
    private Account creator;

    @ManyToOne(optional = true)
    @JoinColumn(name = "last_editor_id")
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonIgnore
    private Account lastEditor;

    private String description;

    @Column(name = "public")
    private boolean isPublic;

    private String title;

    @Column(name = "xml")
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @LazyGroup("xmlContent")
    @JsonIgnore
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.VARBINARY)
    private byte[] zippedXml;

    @Column(name = "migration_date")
    private Calendar migrationDate;

    @Column(name = "migration_reason")
    private String migrationReason;

    public InactiveMindmap() {
    }

    public InactiveMindmap(Mindmap originalMindmap, String migrationReason) {
        this.originalMindmapId = originalMindmap.getId();
        this.creationTime = originalMindmap.getCreationTime();
        this.lastModificationTime = originalMindmap.getLastModificationTime();
        this.creator = originalMindmap.getCreator();
        this.lastEditor = originalMindmap.getLastEditor();
        this.description = originalMindmap.getDescription();
        this.isPublic = originalMindmap.isPublic();
        this.title = originalMindmap.getTitle();
        this.zippedXml = originalMindmap.getZippedXml();
        this.migrationDate = Calendar.getInstance();
        this.migrationReason = migrationReason;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOriginalMindmapId() {
        return originalMindmapId;
    }

    public void setOriginalMindmapId(int originalMindmapId) {
        this.originalMindmapId = originalMindmapId;
    }

    public Calendar getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Calendar creationTime) {
        this.creationTime = creationTime;
    }

    public Calendar getLastModificationTime() {
        return lastModificationTime;
    }

    public void setLastModificationTime(Calendar lastModificationTime) {
        this.lastModificationTime = lastModificationTime;
    }

    public Account getCreator() {
        return creator;
    }

    public void setCreator(Account creator) {
        this.creator = creator;
    }

    public Account getLastEditor() {
        return lastEditor;
    }

    public void setLastEditor(Account lastEditor) {
        this.lastEditor = lastEditor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public byte[] getZippedXml() {
        return zippedXml;
    }

    public void setZippedXml(byte[] zippedXml) {
        this.zippedXml = zippedXml;
    }

    public Calendar getMigrationDate() {
        return migrationDate;
    }

    public void setMigrationDate(Calendar migrationDate) {
        this.migrationDate = migrationDate;
    }

    public String getMigrationReason() {
        return migrationReason;
    }

    public void setMigrationReason(String migrationReason) {
        this.migrationReason = migrationReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof InactiveMindmap))
            return false;
        InactiveMindmap that = (InactiveMindmap) o;
        return id == that.id && originalMindmapId == that.originalMindmapId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "InactiveMindmap{" +
                "id=" + id +
                ", originalMindmapId=" + originalMindmapId +
                ", title='" + title + '\'' +
                ", migrationDate=" + migrationDate +
                ", migrationReason='" + migrationReason + '\'' +
                '}';
    }
}
