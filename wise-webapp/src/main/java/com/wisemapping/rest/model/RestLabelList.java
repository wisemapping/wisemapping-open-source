package com.wisemapping.rest.model;

import com.wisemapping.model.Label;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "labels")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class RestLabelList {

    @NotNull private final List<RestLabel> restLabels;

    public RestLabelList(){
        this.restLabels = new ArrayList<>();
    }

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
