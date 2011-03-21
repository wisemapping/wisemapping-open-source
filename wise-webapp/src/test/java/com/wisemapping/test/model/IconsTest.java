package com.wisemapping.test.model;


import com.wisemapping.model.IconFamily;
import com.wisemapping.model.MindmapIcon;
import com.wisemapping.model.MindmapIcons;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;

@Test
public class IconsTest {
    @Test
    void checkImagesByFamily() throws IllegalAccessException {
        List<MindmapIcon> iconByFamily = MindmapIcons.getIconByFamily(IconFamily.BULLET);
        Assert.assertEquals(iconByFamily.size(), 7);
    }


    @Test
    void checkPngFile() throws IllegalAccessException {

        IconFamily[] values = IconFamily.values();
        for (IconFamily family : values) {
            final List<MindmapIcon> iconByFamily = MindmapIcons.getIconByFamily(family);
            for (MindmapIcon mindmapIcon : iconByFamily) {
                final String pngName = mindmapIcon.getId() + ".png";
                File file = new File("src/main/webapp/icons/", pngName);
                if (!file.exists()) {
                    System.err.println("File not found:" + pngName);
                }
//                else {
//                    System.err.println("Found:" + pngName);
//                }

            }


        }


    }


}
