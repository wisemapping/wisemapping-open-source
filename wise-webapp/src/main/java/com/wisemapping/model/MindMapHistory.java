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

package com.wisemapping.model;

import com.wisemapping.util.ZipUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jakarta.persistence.*;
import java.io.IOException;
import java.util.Calendar;

@Entity
@Table(name = "MINDMAP_HISTORY")
public class MindMapHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "creation_date")
    private Calendar creationTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "editor_id", nullable = true,unique = false)
    private User editor;

    @Column(name = "xml")
    private byte[] zippedXml;

    @Column(name = "mindmap_id")
    private int mindmapId;

    public MindMapHistory() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMindmapId() {
        return mindmapId;
    }

    public void setMindmapId(int id) {
        this.mindmapId = id;
    }

    @NotNull
    public Calendar getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Calendar creationTime) {
        this.creationTime = creationTime;
    }

    @Nullable
    public User getEditor() {
        return editor;
    }

    public void setEditor(@Nullable User editor) {
        this.editor = editor;
    }

    public byte[] getZippedXml() {
        return zippedXml;
    }

    public void setZippedXml(byte[] value) {
        zippedXml = value;
    }

    public byte[] getUnzipXml() throws IOException {
        return ZipUtils.zipToBytes(getZippedXml());
    }
}
