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
    @NotNull private String icon;

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

    @Nullable
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

    @NotNull
    public String getIcon() {
        return icon;
    }

    public void setIcon(@NotNull String icon) {
        this.icon = icon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Label)) return false;

        Label label = (Label) o;

        return id == label.id && creator.getId() == label.creator.getId()
                &&  !(parent != null ? !parent.equals(label.parent) : label.parent != null);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + title.hashCode();
        result = 31 * result + creator.hashCode();
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        return result;
    }

}
