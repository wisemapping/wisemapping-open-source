package com.wisemapping.rest.model;

import com.wisemapping.model.Label;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.NONE;
import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.PUBLIC_ONLY;

@XmlRootElement(name = "label")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonAutoDetect(
        fieldVisibility = NONE,
        setterVisibility = PUBLIC_ONLY,
        isGetterVisibility = NONE,
        getterVisibility = PUBLIC_ONLY
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestLabel {

    @JsonIgnore
    private Label label;

    public RestLabel() {
        this(new Label());
    }

    public RestLabel(@NotNull final Label label) {
        this.label = label;
    }

    public void setParent(@NotNull final Label parent) {
        this.label.setParent(parent);
    }

    @Nullable
    public Label getParent() {
        return this.label.getParent();
    }

    @Nullable
    public String getTitle() {
        return this.label.getTitle();
    }

    public int getId() {
        return label.getId();
    }

    public void setId(int id) {
        label.setId(id);
    }

    public void setTitle(String title) {
        label.setTitle(title);
    }

    public void setColor(@NotNull final String color) {
        label.setColor(color);
    }

    @Nullable public String getColor() {
        return label.getColor();
    }

    @JsonIgnore
    public Label getDelegated() {
        return label;
    }
}
