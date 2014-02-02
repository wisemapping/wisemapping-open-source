package com.wisemapping.rest.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wisemapping.model.LabelMindmap;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.jetbrains.annotations.NotNull;

import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.NONE;
import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.PUBLIC_ONLY;

@JsonAutoDetect(
        fieldVisibility = NONE,
        setterVisibility = PUBLIC_ONLY,
        isGetterVisibility = NONE,
        getterVisibility = PUBLIC_ONLY
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestLabelMindmap {

    @JsonIgnore
    private LabelMindmap labelMindmap;

    public RestLabelMindmap() {
        this(new LabelMindmap());
    }

    public RestLabelMindmap(@NotNull final LabelMindmap labelMindmap) {
        this.labelMindmap = labelMindmap;
    }

    public void setLabelId(final int labelId) {
        this.labelMindmap.setLabelId(labelId);
    }

    public int getLabelId() {
        return this.labelMindmap.getLabelId();
    }

    public void setMindmapId(final int mindmapId) {
        labelMindmap.setMindmapId(mindmapId);
    }

    public int getMindmapId() {
        return this.labelMindmap.getMindmapId();
    }

}
