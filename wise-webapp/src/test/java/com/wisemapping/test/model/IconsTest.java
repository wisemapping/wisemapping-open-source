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
    final private static String CURRENT_JSON_ICONS = "[{\"id\": \"face\", \"icons\" : [\"face_plain\",\"face_sad\",\"face_crying\",\"face_smile\",\"face_surprise\",\"face_wink\"]},{\"id\": \"funy\", \"icons\" : [\"funy_angel\",\"funy_devilish\",\"funy_glasses\",\"funy_grin\",\"funy_kiss\",\"funy_monkey\"]},{\"id\": \"conn\", \"icons\" : [\"conn_connect\",\"conn_disconnect\"]},{\"id\": \"sport\", \"icons\" : [\"sport_basketball\",\"sport_football\",\"sport_golf\",\"sport_raquet\",\"sport_shuttlecock\",\"sport_soccer\",\"sport_tennis\"]},{\"id\": \"bulb\", \"icons\" : [\"bulb_light_on\",\"bulb_light_off\"]},{\"id\": \"thumb\", \"icons\" : [\"thumb_thumb_up\",\"thumb_thumb_down\"]},{\"id\": \"tick\", \"icons\" : [\"tick_tick\",\"tick_cross\"]},{\"id\": \"onoff\", \"icons\" : [\"onoff_clock\",\"onoff_clock_red\",\"onoff_add\",\"onoff_delete\",\"onoff_status_offline\",\"onoff_status_online\"]},{\"id\": \"money\", \"icons\" : [\"money_money\",\"money_dollar\",\"money_euro\",\"money_pound\",\"money_yen\",\"money_coins\",\"money_ruby\"]},{\"id\": \"time\", \"icons\" : [\"time_calendar\",\"time_clock\",\"time_hourglass\"]},{\"id\": \"chart\", \"icons\" : [\"chart_bar\",\"chart_line\",\"chart_curve\",\"chart_pie\",\"chart_organisation\"]},{\"id\": \"sign\", \"icons\" : [\"sign_warning\",\"sign_info\",\"sign_stop\",\"sign_help\",\"sign_cancel\"]},{\"id\": \"hard\", \"icons\" : [\"hard_cd\",\"hard_computer\",\"hard_controller\",\"hard_driver_disk\",\"hard_ipod\",\"hard_keyboard\",\"hard_mouse\",\"hard_printer\"]},{\"id\": \"soft\", \"icons\" : [\"soft_bug\",\"soft_cursor\",\"soft_database_table\",\"soft_database\",\"soft_feed\",\"soft_folder_explore\",\"soft_rss\",\"soft_penguin\"]},{\"id\": \"arrow\", \"icons\" : [\"arrow_up\",\"arrow_down\",\"arrow_left\",\"arrow_right\"]},{\"id\": \"arrowc\", \"icons\" : [\"arrowc_rotate_anticlockwise\",\"arrowc_rotate_clockwise\",\"arrowc_turn_left\",\"arrowc_turn_right\"]},{\"id\": \"people\", \"icons\" : [\"people_group\",\"people_male1\",\"people_male2\",\"people_female1\",\"people_female2\"]},{\"id\": \"mail\", \"icons\" : [\"mail_envelop\",\"mail_mailbox\",\"mail_edit\",\"mail_list\"]},{\"id\": \"flag\", \"icons\" : [\"flag_blue\",\"flag_green\",\"flag_orange\",\"flag_pink\",\"flag_purple\",\"flag_yellow\"]},{\"id\": \"bullet\", \"icons\" : [\"bullet_black\",\"bullet_blue\",\"bullet_green\",\"bullet_orange\",\"bullet_red\",\"bullet_pink\",\"bullet_purple\"]},{\"id\": \"tag\", \"icons\" : [\"tag_blue\",\"tag_green\",\"tag_orange\",\"tag_red\",\"tag_pink\",\"tag_yellow\",\"tag_purple\"]},{\"id\": \"object\", \"icons\" : [\"object_bell\",\"object_clanbomber\",\"object_key\",\"object_pencil\",\"object_phone\",\"object_magnifier\",\"object_clip\",\"object_music\",\"object_star\",\"object_wizard\",\"object_house\",\"object_cake\",\"object_camera\",\"object_palette\",\"object_rainbow\"]},{\"id\": \"weather\", \"icons\" : [\"weather_clear-night\",\"weather_clear\",\"weather_few-clouds-night\",\"weather_few-clouds\",\"weather_overcast\",\"weather_severe-alert\",\"weather_showers-scattered\",\"weather_showers\",\"weather_snow\",\"weather_storm\"]},{\"id\": \"task\", \"icons\" : [\"task_0\",\"task_25\",\"task_50\",\"task_75\",\"task_100\"]},{\"id\": \"number\", \"icons\" : [\"number_1\",\"number_2\",\"number_3\",\"number_4\",\"number_5\",\"number_6\",\"number_7\",\"number_8\",\"number_9\"]},]";

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
                final File file = new File("src/main/webapp/map-icons/icons", pngName);
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
        Assert.assertEquals(result.toString(), CURRENT_JSON_ICONS, "Some change has been introduced in the icons library. Please, check the IconIcons.js and update the variable.mindplot.ImageIcon.prototype.ICON_FAMILIES");

    }

}
