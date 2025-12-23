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
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "MINDMAP_XML")
public class MindmapXml implements Serializable {

    @Id
    @Column(name = "mindmap_id")
    private Integer mindmapId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mindmap_id")
    @MapsId
    @JsonIgnore
    private Mindmap mindmap;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "xml", nullable = false)
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.VARBINARY)
    private byte[] zippedXml = new byte[] {};

    public MindmapXml() {
    }

    public MindmapXml(@NotNull Mindmap mindmap) {
        this.setMindmap(mindmap);
    }

    public Integer getMindmapId() {
        return mindmapId;
    }

    public Mindmap getMindmap() {
        return mindmap;
    }

    public void setMindmap(Mindmap mindmap) {
        this.mindmap = mindmap;
        if (mindmap != null && mindmap.getId() != 0) {
            this.mindmapId = mindmap.getId();
        } else if (mindmap == null) {
            this.mindmapId = null;
        }
    }

    @NotNull
    public byte[] getZippedXml() {
        return zippedXml != null ? zippedXml : new byte[] {};
    }

    public void setZippedXml(@NotNull byte[] zippedXml) {
        this.zippedXml = zippedXml;
    }
}
