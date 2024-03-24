package com.wisemapping.rest.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.wisemapping.model.MindmapLabel;
import org.jetbrains.annotations.NotNull;

import jakarta.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class RestLabelList {

    @NotNull private final List<RestLabel> restLabels;

    public RestLabelList(){
        this.restLabels = new ArrayList<>();
    }

    public RestLabelList(@NotNull final List<MindmapLabel> labels) {
        this.restLabels = new ArrayList<>(labels.size());
        for (MindmapLabel label : labels) {
            this.restLabels.add(new RestLabel(label));
        }
    }

    @NotNull @XmlElement(name = "label")
    public List<RestLabel> getLabels() {
        return restLabels;
    }

}
