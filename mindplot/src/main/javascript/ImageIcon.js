/*
 *    Copyright [2015] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.ImageIcon = new Class({
    Extends: mindplot.Icon,
    initialize: function (topic, iconModel, readOnly) {
        $assert(iconModel, 'iconModel can not be null');
        $assert(topic, 'topic can not be null');

        this._topicId = topic.getId();
        this._featureModel = iconModel;

        // Build graph image representation ...
        var iconType = iconModel.getIconType();
        var imgUrl = this._getImageUrl(iconType);
        this.parent(imgUrl);

        if (!readOnly) {

            //Icon
            var image = this.getImage();
            var me = this;
            image.addEvent('click', function () {

                var iconType = iconModel.getIconType();
                var newIconType = me._getNextFamilyIconId(iconType);
                iconModel.setIconType(newIconType);

                var imgUrl = me._getImageUrl(newIconType);
                me._image.setHref(imgUrl);

            });
            this._image.setCursor('pointer');
        }
    },

    _getImageUrl: function (iconId) {
        return "icons/" + iconId + ".png";
    },

    getModel: function () {
        return this._featureModel;
    },

    _getNextFamilyIconId: function (iconId) {

        var familyIcons = this._getFamilyIcons(iconId);
        $assert(familyIcons != null, "Family Icon not found!");

        var result = null;
        for (var i = 0; i < familyIcons.length && result == null; i++) {
            if (familyIcons[i] == iconId) {
                //Is last one?
                if (i == (familyIcons.length - 1)) {
                    result = familyIcons[0];
                } else {
                    result = familyIcons[i + 1];
                }
                break;
            }
        }

        return result;
    },

    _getFamilyIcons: function (iconId) {
        $assert(iconId != null, "id must not be null");
        $assert(iconId.indexOf("_") != -1, "Invalid icon id (it must contain '_')");

        var result = null;
        for (var i = 0; i < mindplot.ImageIcon.prototype.ICON_FAMILIES.length; i++) {
            var family = mindplot.ImageIcon.prototype.ICON_FAMILIES[i];
            var iconFamilyId = iconId.substr(0, iconId.indexOf("_"));

            if (family.id == iconFamilyId) {
                result = family.icons;
                break;
            }
        }
        return result;
    },

    remove: function () {
        var actionDispatcher = mindplot.ActionDispatcher.getInstance();
        var featureId = this._featureModel.getId();
        var topicId = this._topicId;
        actionDispatcher.removeFeatureFromTopic(topicId, featureId);
    }
});

mindplot.ImageIcon.prototype.ICON_FAMILIES = [
    {"id": "face", "icons": ["face_plain", "face_sad", "face_crying", "face_smile", "face_surprise", "face_wink"]},
    {"id": "funy", "icons": ["funy_angel", "funy_devilish", "funy_glasses", "funy_grin", "funy_kiss", "funy_monkey"]},
    {"id": "conn", "icons": ["conn_connect", "conn_disconnect"]},
    {
        "id": "sport",
        "icons": ["sport_basketball", "sport_football", "sport_golf", "sport_raquet", "sport_shuttlecock", "sport_soccer", "sport_tennis"]
    },
    {"id": "bulb", "icons": ["bulb_light_on", "bulb_light_off"]},
    {"id": "thumb", "icons": ["thumb_thumb_up", "thumb_thumb_down"]},
    {"id": "tick", "icons": ["tick_tick", "tick_cross"]},
    {
        "id": "onoff",
        "icons": ["onoff_clock", "onoff_clock_red", "onoff_add", "onoff_delete", "onoff_status_offline", "onoff_status_online"]
    },
    {
        "id": "money",
        "icons": ["money_money", "money_dollar", "money_euro", "money_pound", "money_yen", "money_coins", "money_ruby"]
    },
    {"id": "time", "icons": ["time_calendar", "time_clock", "time_hourglass"]},
    {
        "id": "number",
        "icons": ["number_1", "number_2", "number_3", "number_4", "number_5", "number_6", "number_7", "number_8", "number_9"]
    },
    {"id": "chart", "icons": ["chart_bar", "chart_line", "chart_curve", "chart_pie", "chart_organisation"]},
    {"id": "sign", "icons": ["sign_warning", "sign_info", "sign_stop", "sign_help", "sign_cancel"]},
    {
        "id": "hard",
        "icons": ["hard_cd", "hard_computer", "hard_controller", "hard_driver_disk", "hard_ipod", "hard_keyboard", "hard_mouse", "hard_printer"]
    },
    {
        "id": "soft",
        "icons": ["soft_bug", "soft_cursor", "soft_database_table", "soft_database", "soft_feed", "soft_folder_explore", "soft_rss", "soft_penguin"]
    },
    {"id": "arrow", "icons": ["arrow_up", "arrow_down", "arrow_left", "arrow_right"]},
    {
        "id": "arrowc",
        "icons": ["arrowc_rotate_anticlockwise", "arrowc_rotate_clockwise", "arrowc_turn_left", "arrowc_turn_right"]
    },
    {"id": "people", "icons": ["people_group", "people_male1", "people_male2", "people_female1", "people_female2"]},
    {"id": "mail", "icons": ["mail_envelop", "mail_mailbox", "mail_edit", "mail_list"]},
    {"id": "flag", "icons": ["flag_blue", "flag_green", "flag_orange", "flag_pink", "flag_purple", "flag_yellow"]},
    {
        "id": "bullet",
        "icons": ["bullet_black", "bullet_blue", "bullet_green", "bullet_orange", "bullet_red", "bullet_pink", "bullet_purple"]
    },
    {"id": "tag", "icons": ["tag_blue", "tag_green", "tag_orange", "tag_red", "tag_pink", "tag_yellow"]},
    {
        "id": "object",
        "icons": ["object_bell", "object_clanbomber", "object_key", "object_pencil", "object_phone", "object_magnifier", "object_clip", "object_music", "object_star", "object_wizard", "object_house", "object_cake", "object_camera", "object_palette", "object_rainbow"]
    },
    {
        "id": "weather",
        "icons": ["weather_clear-night", "weather_clear", "weather_few-clouds-night", "weather_few-clouds", "weather_overcast", "weather_severe-alert", "weather_showers-scattered", "weather_showers", "weather_snow", "weather_storm"]
    },
    {"id": "task", "icons": ["task_0", "task_25", "task_50", "task_75", "task_100"]}
];

