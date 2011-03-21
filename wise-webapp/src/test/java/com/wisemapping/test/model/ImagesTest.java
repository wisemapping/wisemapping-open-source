package com.wisemapping.test.model;


import com.wisemapping.model.IconFamily;
import com.wisemapping.model.MindmapIcon;
import com.wisemapping.model.MindmapIcons;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

@Test
public class ImagesTest {
    @Test
    void checkImagesByFamily() throws IllegalAccessException {
        List<MindmapIcon> iconByFamily = MindmapIcons.getIconByFamily(IconFamily.BULLET);
        Assert.assertEquals(iconByFamily.size(), 7);
    }

}
