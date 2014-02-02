package com.wisemapping.service;

import com.wisemapping.dao.LabelMindmapManager;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.LabelMindmap;
import org.jetbrains.annotations.NotNull;

public class LabelMindmapServiceImpl implements LabelMindmapService {

    private LabelMindmapManager labelMindmapManager;

    public void setLabelMindmapManager(LabelMindmapManager labelMindmapManager) {
        this.labelMindmapManager = labelMindmapManager;
    }

    @Override
    public void removeLabelFromMindmap(@NotNull LabelMindmap labelMindmap) throws WiseMappingException {
        this.labelMindmapManager.removeLabelMindmap(labelMindmap);
    }

    @Override
    public LabelMindmap getLabelMindmap(int labelId, int mindmapId) {
        return this.labelMindmapManager.getLabelMindmap(labelId, mindmapId);
    }
}
