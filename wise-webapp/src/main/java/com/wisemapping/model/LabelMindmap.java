package com.wisemapping.model;

import java.io.Serializable;

public class LabelMindmap implements Serializable{

    private int mindmapId;
    private int labelId;

    public int getMindmapId() {
        return mindmapId;
    }

    public void setMindmapId(int mindmapId) {
        this.mindmapId = mindmapId;
    }

    public int getLabelId() {
        return labelId;
    }

    public void setLabelId(int labelId) {
        this.labelId = labelId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LabelMindmap)) return false;

        LabelMindmap that = (LabelMindmap) o;

        return labelId == that.labelId && mindmapId == that.mindmapId;

    }

    @Override
    public int hashCode() {
        int result = mindmapId;
        result = 31 * result + labelId;
        return result;
    }
}
