package com.wisemapping.model;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Label {

    //~ Instance fields ......................................................................................
    private int id;
    @NotNull private String title;
    @NotNull private User creator;
    @Nullable private Label parent;
    @NotNull private String color;

    public void setParent(@Nullable Label parent) {
        this.parent = parent;
    }

    @Nullable
    public Label getParent() {
        return parent;
    }

    public void setCreator(@NotNull User creator) {
        this.creator = creator;
    }

    @NotNull
    public User getCreator() {
        return creator;
    }

    @NotNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NotNull String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NotNull
    public String getColor() {
        return color;
    }

    public void setColor(@NotNull String color) {
        this.color = color;
    }
}
