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

mindplot.widget.IMenu = new Class({
    initialize : function(designer, containerId, mapId) {
        $assert(designer, "designer can not be null");
        $assert(containerId, "containerId can not be null");

        this._designer = designer;
        this._toolbarElems = [];
        this._containerId = containerId;
        this._mapId = mapId;
    },

    _registerEvents : function(designer) {

        // Register on close events ...
        this._toolbarElems.forEach(function(elem) {
            elem.addEvent('show', function() {
                this.clear()
            }.bind(this));
        }.bind(this));

        designer.addEvent('onblur', function() {
            var topics = designer.getModel().filterSelectedTopics();
            var rels = designer.getModel().filterSelectedRelations();

            this._toolbarElems.forEach(function(button) {
                var disable = false;
                if (button.isTopicAction() && button.isRelAction()) {
                    disable = rels.length == 0 && topics.length == 0;

                } else if (!button.isTopicAction() && !button.isRelAction()) {
                    disable = false;
                }
                else if (button.isTopicAction() && topics.length == 0) {
                    disable = true;
                } else if (button.isRelAction() && rels.length == 0) {
                    disable = true;
                }

                if (disable) {
                    button.disable();
                } else {
                    button.enable();
                }

            })
        }.bind(this));

        designer.addEvent('onfocus', function() {
            var topics = designer.getModel().filterSelectedTopics();
            var rels = designer.getModel().filterSelectedRelations();

            this._toolbarElems.forEach(function(button) {
                if (button.isTopicAction() && topics.length > 0) {
                    button.enable();
                }

                if (button.isRelAction() && rels.length > 0) {
                    button.enable();
                }
            })
        }.bind(this));
    },

    clear : function() {
        this._toolbarElems.forEach(function(item) {
            item.hide();
        });
    },

    _save:function (saveElem, designer, saveHistory) {

        console.log("save!!!!");        //TODO(gb): Remove trace!!!
        // Load map content ...
        var mindmap = designer.getMindmap();
        var mindmapProp = designer.getMindmapProperties();

        // Display save message ..
        if (saveHistory) {
            $notify("Saving ...");
            saveElem.setStyle('cursor', 'wait');
        } else {
            console.log("Saving without history ...");
        }

        // Call persistence manager for saving ...
        var persistenceManager = mindplot.PersitenceManager.getInstance();
        persistenceManager.save(mindmap, mindmapProp, saveHistory, {
            onSuccess: function() {
                if (saveHistory) {
                    saveElem.setStyle('cursor', 'pointer');
                    $notify("Save complete");
                }
            },
            onError: function() {
                if (saveHistory) {
                    saveElem.setStyle('cursor', 'pointer');
                    $notify("Save could not be completed. Try latter");
                }
            }
        });
    }
})