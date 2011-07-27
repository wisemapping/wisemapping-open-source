/*
 *    Copyright [2011] [wisemapping]
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

mindplot.ImageIcon = new Class(
    {
        Extends:mindplot.Icon,
        initialize:function(iconModel, topic, designer) {
            $assert(iconModel, 'iconModel can not be null');
            $assert(topic, 'topic can not be null');
            $assert(designer, 'designer can not be null');
            this._topic = topic;
            this._iconModel = iconModel;
            this._designer = designer;

            // Build graph image representation ...
            var iconType = iconModel.getIconType();
            var imgUrl = this._getImageUrl(iconType);

            this.parent(imgUrl);

            //Remove
            var divContainer = designer.getWorkSpace().getScreenManager().getContainer();
            var tip = mindplot.Tip.getInstance(divContainer);

            var container = new Element('div');
            var removeImage = new Element('img');
            removeImage.src = "../images/bin.png";
            removeImage.inject(container);

            if (!$defined(designer._viewMode) || ($defined(designer._viewMode) && !designer._viewMode)) {

                removeImage.addEvent('click', function(event) {
                    var actionRunner = designer._actionRunner;
                    var command = new mindplot.commands.RemoveIconFromTopicCommand(this._topic.getId(), iconModel);
                    actionRunner.execute(command);
                    tip.forceClose();
                }.bindWithEvent(this));

                //Icon
                var image = this.getImage();
                image.addEventListener('click', function(event) {
                    var iconType = iconModel.getIconType();
                    var newIconType = this._getNextFamilyIconId(iconType);
                    iconModel.setIconType(newIconType);

                    var imgUrl = this._getImageUrl(newIconType);
                    this._image.setHref(imgUrl);

                    //        // @Todo: Support revert of change icon ...
                    //        var actionRunner = designer._actionRunner;
                    //        var command = new mindplot.commands.ChangeIconFromTopicCommand(this._topic.getId());
                    //        this._actionRunner.execute(command);


                }.bindWithEvent(this));

                var imageIcon = this;
                image.addEventListener('mouseover', function(event) {
                    tip.open(event, container, imageIcon);
                });
                image.addEventListener('mouseout', function(event) {
                    tip.close(event);
                });
                image.addEventListener('mousemove', function(event) {
                    tip.updatePosition(event);
                });

            }
        },

      _getImageUrl : function(iconId) {
            return "../icons/" + iconId + ".png";
        },

        getModel : function() {
            return this._iconModel;
        },

        _getNextFamilyIconId : function(iconId) {

            var familyIcons = this._getFamilyIcons(iconId);
            $assert(familyIcons != null, "Family Icon not found!");

            var result = null;
            for (var i = 0; i < familyIcons.length && result == null; i++) {
                if (familyIcons[i] == iconId) {
                    var nextIconId;
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

        _getFamilyIcons : function(iconId) {
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

        getId : function() {
            return this._iconType;
        },

        getUiId : function() {
            return this._uiId;
        }
    }
);


mindplot.ImageIcon.prototype.ICON_FAMILIES = [
    {"id": "face", "icons" : ["face_plain","face_sad","face_crying","face_smile","face_surprise","face_wink"]},
    {"id": "funy", "icons" : ["funy_angel","funy_devilish","funy_glasses","funy_grin","funy_kiss","funy_monkey"]},
    {"id": "conn", "icons" : ["conn_connect","conn_disconnect"]},
    {"id": "sport", "icons" : ["sport_basketball","sport_football","sport_golf","sport_raquet","sport_shuttlecock","sport_soccer","sport_tennis"]},
    {"id": "bulb", "icons" : ["bulb_light_on","bulb_light_off"]},
    {"id": "thumb", "icons" : ["thumb_thumb_up","thumb_thumb_down"]},
    {"id": "tick", "icons" : ["tick_tick","tick_cross"]},
    {"id": "onoff", "icons" : ["onoff_clock","onoff_clock_red","onoff_add","onoff_delete","onoff_status_offline","onoff_status_online"]},
    {"id": "money", "icons" : ["money_money","money_dollar","money_euro","money_pound","money_yen","money_coins","money_ruby"]},
    {"id": "time", "icons" : ["time_calendar","time_clock","time_hourglass"]},
    {"id": "chart", "icons" : ["chart_bar","chart_line","chart_curve","chart_pie","chart_organisation"]},
    {"id": "sign", "icons" : ["sign_warning","sign_info","sign_stop","sign_help","sign_cancel"]},
    {"id": "hard", "icons" : ["hard_cd","hard_computer","hard_controller","hard_driver_disk","hard_ipod","hard_keyboard","hard_mouse","hard_printer"]},
    {"id": "soft", "icons" : ["soft_bug","soft_cursor","soft_database_table","soft_database","soft_feed","soft_folder_explore","soft_rss","soft_penguin"]},
    {"id": "arrow", "icons" : ["arrow_up","arrow_down","arrow_left","arrow_right"]},
    {"id": "arrowc", "icons" : ["arrowc_rotate_anticlockwise","arrowc_rotate_clockwise","arrowc_turn_left","arrowc_turn_right"]},
    {"id": "people", "icons" : ["people_group","people_male1","people_male2","people_female1","people_female2"]},
    {"id": "mail", "icons" : ["mail_envelop","mail_mailbox","mail_edit","mail_list"]},
    {"id": "flag", "icons" : ["flag_blue","flag_green","flag_orange","flag_pink","flag_purple","flag_yellow"]},
    {"id": "bullet", "icons" : ["bullet_black","bullet_blue","bullet_green","bullet_orange","bullet_red","bullet_pink","bullet_purple"]},
    {"id": "tag", "icons" : ["tag_blue","tag_green","tag_orange","tag_red","tag_pink","tag_yellow"]},
    {"id": "object", "icons" : ["object_bell","object_clanbomber","object_key","object_pencil","object_phone","object_magnifier","object_clip","object_music","object_star","object_wizard","object_house","object_cake","object_camera","object_palette","object_rainbow"]}
]



