package com.wisemapping.rest.model;

import com.wisemapping.model.Label;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class RestLabelList {

    @NotNull private final List<RestLabel> restLabels;

    public RestLabelList(@NotNull final List<Label> labels) {
        this.restLabels = new ArrayList<>(labels.size());
        for (Label label : labels) {
            this.restLabels.add(new RestLabel(label));
        }
    }

    @NotNull @XmlElement(name = "label")
    public List<RestLabel> getLabels() {
        return restLabels;
    }

}
