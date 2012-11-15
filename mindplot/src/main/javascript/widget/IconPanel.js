/*
 *    Copyright [2012] [wisemapping]
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

mindplot.widget.IconPanel = new Class({
    Extends:mindplot.widget.ToolbarPaneItem,
    initialize:function (buttonId, model) {
        this.parent(buttonId, model);
    },

    _updateSelectedItem:function () {
        return this.getPanelElem();

    },

    buildPanel:function () {
        var content = new Element('div', {'class':'toolbarPanel', 'id':'IconsPanel'});
        content.setStyles({width:253, height:230, padding:5});
        content.addEvent("click", function (event) {
            event.stopPropagation()
        });

        var count = 0;
        for (var i = 0; i < mindplot.ImageIcon.prototype.ICON_FAMILIES.length; i = i + 1) {
            var familyIcons = mindplot.ImageIcon.prototype.ICON_FAMILIES[i].icons;
            for (var j = 0; j < familyIcons.length; j = j + 1) {
                // Separate icons by line ...
                var familyContent;
                if ((count % 12) == 0) {
                    familyContent = new Element('div').inject(content);
                }

                var iconId = familyIcons[j];
                var img = new Element('img', {
                    id:iconId,
                    src:mindplot.ImageIcon.prototype._getImageUrl(iconId)
                });
                img.setStyles({width:16,
                    height:16,
                    padding:"0px 2px",
                    cursor:'pointer'
                }).inject(familyContent);

                var panel = this;
                var model = this.getModel();
                img.addEvent('click', function (event) {
                    model.setValue(this.id);
                    panel.hide();
                }.bind(img));

                count = count + 1;
            }
        }
        return content;
    }
});