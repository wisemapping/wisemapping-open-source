package com.wisemapping.rest.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wisemapping.model.Label;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

@JsonAutoDetect(
        fieldVisibility = NONE,
        setterVisibility = PUBLIC_ONLY,
        isGetterVisibility = NONE,
        getterVisibility = PUBLIC_ONLY
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestLabel {

    @JsonIgnore
    private final Label label;

    public RestLabel() {
        this(new Label());
    }

    public RestLabel(@NotNull final Label label) {
        this.label = label;
    }

    public void setParent(final Label parent) {
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

    public void setColor(final String color) {
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
