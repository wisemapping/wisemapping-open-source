package com.wisemapping.service;

import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.Label;
import com.wisemapping.model.LabelMindmap;
import com.wisemapping.model.Mindmap;
import org.jetbrains.annotations.NotNull;


public interface LabelMindmapService {

    void removeLabelFromMindmap(@NotNull final LabelMindmap labelMindmap) throws WiseMappingException;

    LabelMindmap getLabelMindmap(int labelId, int mindmapId);
}
