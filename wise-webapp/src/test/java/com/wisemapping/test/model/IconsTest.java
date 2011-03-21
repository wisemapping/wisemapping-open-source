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
    final private static String CURRENT_JSON_ICONS = "[{\"id\": \"flag\", \"icons\" : [\"flag_blue\",\"flag_green\",\"flag_orange\",\"flag_pink\",\"flag_purple\",\"flag_yellow\"]},{\"id\": \"bullet\", \"icons\" : [\"bullet_black\",\"bullet_blue\",\"bullet_green\",\"bullet_orange\",\"bullet_red\",\"bullet_pink\",\"bullet_purple\"]},{\"id\": \"tag\", \"icons\" : [\"tag_blue\",\"tag_green\",\"tag_orange\",\"tag_red\",\"tag_pink\",\"tag_yellow\"]},{\"id\": \"face\", \"icons\" : [\"face_plain\",\"face_sad\",\"face_crying\",\"face_smile\",\"face_surprise\",\"face_wink\"]},{\"id\": \"funy\", \"icons\" : [\"funy_angel\",\"funy_devilish\",\"funy_glasses\",\"funy_grin\",\"funy_kiss\",\"funy_monkey\"]},{\"id\": \"arrow\", \"icons\" : [\"arrow_up\",\"arrow_down\",\"arrow_left\",\"arrow_right\"]},{\"id\": \"arrowc\", \"icons\" : [\"arrowc_rotate_anticlockwise\",\"arrowc_rotate_clockwise\",\"arrowc_turn_left\",\"arrowc_turn_right\"]},{\"id\": \"conn\", \"icons\" : [\"conn_connect\",\"conn_disconnect\"]},{\"id\": \"bulb\", \"icons\" : [\"bulb_light_on\",\"bulb_light_off\"]},{\"id\": \"thumb\", \"icons\" : [\"thumb_thumb_up\",\"thumb_thumb_down\"]},{\"id\": \"tick\", \"icons\" : [\"tick_tick\",\"tick_cross\"]},{\"id\": \"onoff\", \"icons\" : [\"onoff_clock\",\"onoff_clock_red\",\"onoff_add\",\"onoff_delete\"]},{\"id\": \"money\", \"icons\" : [\"money_money\",\"money_dollar\",\"money_euro\",\"money_pound\",\"money_yen\",\"money_coins\",\"money_ruby\"]},{\"id\": \"chart\", \"icons\" : [\"chart_bar\",\"chart_line\",\"chart_curve\",\"chart_pie\",\"chart_organisation\"]},]";

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
                final File file = new File("src/main/webapp/icons/", pngName);
                Assert.assertTrue(file.exists(), "Could not be found:" + file.getAbsolutePath());
            }


        }
    }

    @Test
    void jsonGenerationRepresentation() {

        IconFamily[] values = IconFamily.values();
        final StringBuilder result = new StringBuilder("[");

        for (IconFamily family : values) {
            result.append("{");
            result.append("\"id\": \"" + family.name().toLowerCase() + "\"");
            result.append(", \"icons\" : [");

            final List<MindmapIcon> iconByFamily = MindmapIcons.getIconByFamily(family);
            for (int i = 0; i < iconByFamily.size(); i++) {
                if (i != 0) {
                    result.append(",");
                }
                MindmapIcon mindmapIcon = iconByFamily.get(i);
                result.append("\"" + mindmapIcon.getId() + "\"");
            }

            result.append("]},");
        }
        result.append("]");
        System.out.println(result.toString());
        Assert.assertEquals(result.toString(), CURRENT_JSON_ICONS, "Some change has been introduced in the icons library. Please, check the IconIcons.js and update the variable.mindplot.ImageIcon.prototype.ICON_FAMILIES");

    }

}
