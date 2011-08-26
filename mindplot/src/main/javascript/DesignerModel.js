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

mindplot.DesignerModel = new Class({
    initialize : function(options) {
        this._zoom = options.zoom;
        this._topics = [];
        this._relationships = {};
    },

    getZoom : function() {
        return this._zoom;
    },

    setZoom : function(zoom) {
        this._zoom = zoom;
    },

    getTopics : function() {
        return this._topics;
    },

    getCentralTopic : function() {
        var topics = this.getTopics();
        return topics[0];
    },

    filterSelectedTopics : function() {
        var result = [];
        for (var i = 0; i < this._topics.length; i++) {
            if (this._topics[i].isOnFocus()) {
                result.push(this._topics[i]);
            }
        }
        return result;
    },

    filterSelectedRelations : function() {
        var result = [];
        for (var id in this._relationships) {
            var relationship = this._relationships[id];
            if (relationship.isOnFocus()) {
                result.push(relationship);
            }
        }
        return result;
    },

    getObjects : function() {
        var result = [].append(this._topics);
        for (var id in this._relationships) {
            result.push(this._relationships[id]);
        }
        return result;
    },

    removeTopic : function(topic) {
        $assert(topic, "topic can not be null");
        this._topics.erase(topic);
    },

    addTopic : function(topic) {
        $assert(topic, "topic can not be null");
        this._topics.push(topic);
    },


    filterTopicsIds : function(validate, errorMsg) {
        var result = [];
        var topics = this.filterSelectedTopics();

        if (topics.length == 0) {
            core.Monitor.getInstance().logMessage('At least one element must be selected to execute this operation.');
        } else {
            var isValid = true;
            for (var i = 0; i < topics.length; i++) {
                var selectedNode = topics[i];
                if ($defined(validate)) {
                    isValid = validate(selectedNode);
                }

                // Add node only if it's valid.
                if (isValid) {
                    result.push(selectedNode.getId());
                } else {
                    core.Monitor.getInstance().logMessage(errorMsg);
                }
            }
        }
        return result;
    },

    filterRelationIds : function(validate, errorMsg) {
        var result = [];
        var relationships = this.filterSelectedRelations();
        if (relationships.length == 0) {
            core.Monitor.getInstance().logMessage('At least one element must be selected to execute this operation.');
        } else {
            var isValid = true;
            for (var j = 0; j < relationships.length; j++) {
                var selectedLine = relationships[j];
                isValid = true;
                if ($defined(validate)) {
                    isValid = validate(selectedLine);
                }

                if (isValid) {
                    result.push(selectedLine.getId());
                } else {
                    core.Monitor.getInstance().logMessage(errorMsg);
                }
            }
        }
        return result;
    },

    getRelationshipsById : function() {
        return this._relationships;
    },

    selectedTopic : function() {
        var topics = this.filterSelectedTopics();
        return (topics.length > 0) ? topics[0] : null;
    }

});