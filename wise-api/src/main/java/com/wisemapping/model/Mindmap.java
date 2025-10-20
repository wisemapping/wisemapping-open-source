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

import com.wisemapping.exceptions.AccessDeniedSecurityException;
import com.wisemapping.exceptions.InvalidMindmapException;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.util.ZipUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Entity
@Table(name = "MINDMAP")
public class Mindmap implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "creation_date")
    private Calendar creationTime;

    @Column(name = "edition_date")
    private Calendar lastModificationTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id")
    @JsonIgnore
    private Account creator;

    @ManyToOne
    @JoinColumn(name = "last_editor_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonIgnore
    private Account lastEditor;

    private String description;

    @Column(name = "public")
    private boolean isPublic;

    @OneToOne(mappedBy = "mindmap", fetch = FetchType.LAZY)
    @JsonIgnore
    private MindmapSpamInfo spamInfo;

    @OneToMany(mappedBy = "mindMap", orphanRemoval = true, cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @Fetch(FetchMode.JOIN)
    @JsonIgnore
    private Set<Collaboration> collaborations = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE})
    @Fetch(FetchMode.JOIN)
    @JoinTable(
            name = "R_LABEL_MINDMAP",
            joinColumns = @JoinColumn(name = "mindmap_id"),
            inverseJoinColumns = @JoinColumn(name = "label_id"))
    @JsonIgnore
    private Set<MindmapLabel> labels = new LinkedHashSet<>();

    private String title;

    @Column(name = "xml")
    @Basic(fetch = FetchType.LAZY)
    @JsonIgnore
    private byte[] zippedXml;

    public Mindmap() {
    }

    public void setUnzipXml(@NotNull byte[] value) {
        try {
            final byte[] zip = ZipUtils.bytesToZip(value);
            this.setZippedXml(zip);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setXmlStr(@NotNull String xml) throws InvalidMindmapException {
        // Is a valid mindmap ... ?
        MindmapUtils.verifyMindmap(xml);
        this.setUnzipXml(xml.getBytes(StandardCharsets.UTF_8));
    }

    @NotNull
    public byte[] getUnzipXml() {
        byte[] result = new byte[]{};
        if (zippedXml != null) {
            try {
                final byte[] zip = this.getZippedXml();
                result = ZipUtils.zipToBytes(zip);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return result;
    }

    @NotNull
    public String getXmlStr() throws UnsupportedEncodingException {
        return new String(this.getUnzipXml(), StandardCharsets.UTF_8);
    }

    @NotNull
    public byte[] getZippedXml() {
        return zippedXml;
    }

    public void setZippedXml(@NotNull byte[] value) {
        this.zippedXml = value;
    }

    public Set<Collaboration> getCollaborations() {
        return collaborations;
    }

    public void setCollaborations(Set<Collaboration> collaborations) {
        this.collaborations = collaborations;
    }

    public void addCollaboration(@NotNull Collaboration collaboration) {
        collaborations.add(collaboration);
    }

    public void removedCollaboration(@NotNull Collaboration collaboration) {
        // https://stackoverflow.com/questions/25125210/hibernate-persistentset-remove-operation-not-working
        this.collaborations.remove(collaboration);
        // Set mindMap to null to maintain referential integrity before deletion
        // This prevents the collaboration from being in an inconsistent state
        collaboration.setMindMap(null);
    }

    public void removedCollaboration(@NotNull Set<Collaboration> collaborations) {
        this.collaborations.removeAll(collaborations);
    }

    @NotNull
    public Set<MindmapLabel> getLabels() {
        return labels;
    }

    public void setLabels(@NotNull final Set<MindmapLabel> labels) {
        this.labels = labels;
    }

    public void addLabel(@NotNull final MindmapLabel label) {
        this.labels.add(label);
    }

    public Optional<Collaboration> findCollaboration(@NotNull Collaborator collaborator) {
        return this.collaborations
                .stream()
                .filter(c -> c.getCollaborator().identityEquality(collaborator))
                .findAny();
    }

    @Nullable
    public Collaboration findCollaboration(@NotNull String email) {
        Collaboration result = null;
        for (Collaboration collaboration : collaborations) {
            if (collaboration.getCollaborator().getEmail().equals(email)) {
                result = collaboration;
                break;
            }
        }
        return result;
    }

    public boolean isCreator(@NotNull Account user) {
        return this.getCreator() != null && this.getCreator().identityEquality(user);
    }

    public boolean isPublic() {
        return isPublic;
    }


    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean isSpamDetected() {
        return spamInfo != null && spamInfo.isSpamDetected();
    }

    public void setSpamDetected(boolean spamDetected) {
        if (spamInfo == null) {
            spamInfo = new MindmapSpamInfo(this);
        }
        spamInfo.setSpamDetected(spamDetected);
    }

    public String getSpamDescription() {
        return spamInfo != null ? spamInfo.getSpamDescription() : null;
    }

    public void setSpamDescription(String spamDescription) {
        if (spamInfo == null) {
            spamInfo = new MindmapSpamInfo(this);
        }
        spamInfo.setSpamDescription(spamDescription);
    }

    public int getSpamDetectionVersion() {
        return spamInfo != null ? spamInfo.getSpamDetectionVersion() : 0;
    }

    public void setSpamDetectionVersion(int spamDetectionVersion) {
        if (spamInfo == null) {
            spamInfo = new MindmapSpamInfo(this);
        }
        spamInfo.setSpamDetectionVersion(spamDetectionVersion);
    }

    public SpamStrategyType getSpamTypeCode() {
        return spamInfo != null ? spamInfo.getSpamTypeCode() : null;
    }

    public void setSpamTypeCode(SpamStrategyType spamTypeCode) {
        if (spamInfo == null) {
            spamInfo = new MindmapSpamInfo(this);
        }
        spamInfo.setSpamTypeCode(spamTypeCode);
    }

    public MindmapSpamInfo getSpamInfo() {
        return spamInfo;
    }

    public void setSpamInfo(MindmapSpamInfo spamInfo) {
        this.spamInfo = spamInfo;
    }

    public Calendar getLastModificationTime() {
        return lastModificationTime;
    }

    public void setLastModificationTime(Calendar lastModificationTime) {
        this.lastModificationTime = lastModificationTime;
    }

    @Nullable
    public Account getLastEditor() {
        return lastEditor;
    }

    public void setLastEditor(@Nullable Account lastEditor) {
        this.lastEditor = lastEditor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @NotNull
    public String getXmlAsJsLiteral()
            throws IOException {
        String xml = this.getXmlStr();

        xml = xml.replace("'", "\\'");
        xml = xml.replace("\n", "\\n");
        xml = xml.replace("\r", "");

        xml = xml.replace("\\b", "\\\\b");
        xml = xml.replace("\\t", "\\\\t");
        xml = xml.replace("\\r", "\\\\r");
        xml = xml.replace("\\f", "\\\\f");
        xml = xml.trim();
        return xml;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Calendar getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Calendar creationTime) {
        this.creationTime = creationTime;
    }

    public void setCreator(@NotNull Account creator) {
        this.creator = creator;
    }

    public Account getCreator() {
        return creator;
    }

    @Nullable
    private CollaborationProperties findUserProperty(@NotNull Collaborator collaborator) {
        final Optional<Collaboration> collaboration = this.findCollaboration(collaborator);
        return collaboration.map(Collaboration::getCollaborationProperties).orElse(null);
    }

    public void setStarred(@NotNull Collaborator collaborator, boolean value) throws WiseMappingException {
        final CollaborationProperties collaborationProperties = findCollaborationProperties(collaborator);
        collaborationProperties.setStarred(value);
    }

    @NotNull
    public CollaborationProperties findCollaborationProperties(@NotNull Collaborator collaborator) throws WiseMappingException {
        return Objects.requireNonNull(this.findCollaborationProperties(collaborator, true));
    }

    @Nullable
    public CollaborationProperties findCollaborationProperties(@NotNull Collaborator collaborator, boolean forceCheck) throws WiseMappingException {
        if (collaborator == null) {
            throw new IllegalStateException("Collaborator can not be null");
        }

        final Optional<Collaboration> collaboration = this.findCollaboration(collaborator);
        CollaborationProperties result = null;
        if (collaboration.isPresent()) {
            result = collaboration.get().getCollaborationProperties();
        } else {
            if (forceCheck)
                throw new AccessDeniedSecurityException("Collaborator " + collaborator.getEmail() + " could not access " + this.getId());
        }
        return result;
    }

    public boolean isStarred(@NotNull Collaborator collaborator) {
        final CollaborationProperties collaboratorProperty = this.findUserProperty(collaborator);
        return collaboratorProperty != null && collaboratorProperty.getStarred();
    }

    public static String getDefaultMindmapXml(@NotNull final String title) {
        return getDefaultMindmapXml(title, "mindmap");
    }

    public static String getDefaultMindmapXml(@NotNull final String title, @NotNull final String layout) {
        return "<map version=\"tango\" theme=\"prism\" layout=\"" + layout + "\">" +
                "<topic central=\"true\" text=\"" +
                escapeXmlAttribute(title) +
                "\"/></map>";
    }

    static private String escapeXmlAttribute(String attValue) {
        // Hack: Find out of the box function.
        String result = attValue.replace("&", "&amp;");
        result = result.replace("<", "&lt;");
        result = result.replace("gt", "&gt;");
        result = result.replace("\"", "&quot;");
        return result;
    }

    public Mindmap shallowClone() {
        final Mindmap result = new Mindmap();
        result.setDescription(this.getDescription());
        result.setTitle(this.getTitle());
        result.setUnzipXml(this.getUnzipXml());

        return result;
    }

    public boolean hasPermissions(@Nullable Collaborator collaborator, @NotNull CollaborationRole role) {
        boolean result = false;
        if (collaborator != null) {
            final Optional<Collaboration> collaboration = this.findCollaboration(collaborator);
            if (collaboration.isPresent()) {
                result = collaboration.get().hasPermissions(role);
            }
        }
        return result;

    }

    public boolean hasLabel(@NotNull final String name) {
        for (MindmapLabel label : this.labels) {
            if (label.getTitle().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void removeLabel(@NotNull final MindmapLabel label) {
        this.labels.remove(label);
    }
}
