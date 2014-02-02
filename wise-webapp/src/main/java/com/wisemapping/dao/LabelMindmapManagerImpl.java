package com.wisemapping.dao;

import com.wisemapping.model.LabelMindmap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.List;


public class LabelMindmapManagerImpl extends HibernateDaoSupport
        implements LabelMindmapManager {

    @Nullable
    @Override
    public LabelMindmap getLabelMindmap(int labelId, int mindmapId) {
        LabelMindmap result = null;
        List<LabelMindmap> list = getHibernateTemplate().find("from com.wisemapping.model.LabelMindmap wisemapping where mindmap_id=? and label_id=? ", new Object[]{mindmapId, labelId});
        assert list.size() <= 1;
        if (list != null && !list.isEmpty()) {
            result = list.get(0);
        }
        return result;
    }

    @Override
    public void removeLabelMindmap(@NotNull LabelMindmap labelMindmap) {
        getHibernateTemplate().delete(labelMindmap);
    }
}
