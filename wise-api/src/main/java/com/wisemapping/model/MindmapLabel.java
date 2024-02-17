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


import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "MINDMAP_LABEL")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MindmapLabel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    private String title;
    @NotNull
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = true, unique = true)
    @NotNull
    private Account creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_label_id", nullable = true)
    @Nullable
    private MindmapLabel parent;

    public void setParent(@Nullable MindmapLabel parent) {
        this.parent = parent;
    }

    @Nullable
    public MindmapLabel getParent() {
        return parent;
    }

    public void setCreator(@NotNull Account creator) {
        this.creator = creator;
    }

    @NotNull
    public Account getCreator() {
        return creator;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Nullable
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MindmapLabel)) return false;

        final MindmapLabel label = (MindmapLabel) o;
        return id == label.id && creator.getId() == label.creator.getId()
                && Objects.equals(parent, label.parent);
    }

    @Override
    public int hashCode() {
        long result = title.hashCode();
        result = 31 * result + (creator != null ? creator.hashCode() : 0);
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        return (int) result;
    }

}
