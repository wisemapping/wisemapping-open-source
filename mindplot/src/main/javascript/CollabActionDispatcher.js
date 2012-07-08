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

mindplot.BrixActionDispatcher = new Class({
    Extends: mindplot.ActionDispatcher,
    initialize: function(commandContext, fireOnChange) {
        this.parent(commandContext, fireOnChange);
        this._commandContext = commandContext;
    },

    dragTopic: function(topicId, position, order, parentTopic) {
        var framework = this._getFramework();
        var node = framework.getTopic(topicId);

        // Set node order ...
        if (order != null) {
            node.setOrder(order);
        } else if (position != null) {
            // Set position ...
            node.setPosition(position);
        } else {
            $assert("Illegal commnand state exception.");
        }
        // Finally, connect node ...
        if ($defined(this._parentId)) {
            var parentNode = topic.findTopics([this._parentId])[0];
            node.disconnect();
            node.connect(parentNode);
        }
    },

    changeTextToTopic : function(topicsIds, text) {
        var framework = this._getFramework();
        var topicId;
        if (!(topicsIds instanceof Array)) {
            topicId = topicsIds;
        } else {
            topicId = topicsIds[0];
        }
        var node = framework.getTopic(topicId);
        node.setText(text);

    },

    _getFramework:function () {
        return mindplot.collaboration.CollaborationManager.getInstance().getCollaborativeFramework();
    },

    addTopics : function(nodeModel, parentTopicId) {
        var framework = this._getFramework();
        var cmindmap = framework.getModel();

        var cparent = $defined(parentTopicId) ? framework.getTopic(parentTopicId) : cmindmap.getCentralTopic();
        var cnode = cmindmap.createNode(nodeModel.getType(), nodeModel.getId());
        nodeModel.copyTo(cnode);

        cnode.connectTo(cparent);
    },

    changeFontSizeToTopic : function(topicsIds, size) {
        topicsIds.each(function(topicId) {
            var framework = this._getFramework();
            var topic = framework.getTopic(topicId);
            topic.setFontSize(size, true);
        }.bind(this));
    },

    changeFontColorToTopic : function(topicsIds, color) {
        topicsIds.each(function(topicId) {
            var framework = this._getFramework();
            var topic = framework.getTopic(topicId);
            topic.setFontColor(color, true);
        }.bind(this));
    },

    changeFontFamilyToTopic : function(topicsIds, family) {
        topicsIds.each(function(topicId) {
            var framework = this._getFramework();
            var topic = framework.getTopic(topicId);
            topic.setFontFamily(family, true);
        }.bind(this));
    },

    changeFontStyleToTopic : function(topicsIds) {
        topicsIds.each(function(topicId) {
            var framework = this._getFramework();
            var topic = framework.getTopic(topicId);
            var style = ( topic.getFontStyle() == "italic") ? "normal" : "italic";
            topic.setFontStyle(style, true);
        }.bind(this));
    },

    changeShapeTypeToTopic : function(topicsIds, shapeType) {
        topicsIds.each(function(topicId) {
            var framework = this._getFramework();
            var topic = framework.getTopic(topicId);
            topic.setShapeType(shapeType);
        }.bind(this))
    },

    changeFontWeightToTopic : function(topicsIds) {
        topicsIds.each(function(topicId) {
            var framework = this._getFramework();
            var topic = framework.getTopic(topicId);
            var weight = (topic.getFontWeight() == "bold") ? "normal" : "bold";
            topic.setFontWeight(weight, true);
        }.bind(this));
    },

    changeBackgroundColorToTopic : function(topicsIds, color) {
        topicsIds.each(function(topicId) {
            var framework = this._getFramework();
            var topic = framework.getTopic(topicId);
            topic.setBackgroundColor(color, true);
        }.bind(this));

    },

    changeBorderColorToTopic : function(topicsIds, color) {
        topicsIds.each(function(topicId) {
            var framework = this._getFramework();
            var topic = framework.getTopic(topicId);
            topic.setBorderColor(color);
        }.bind(this));
    },

    deleteEntities : function(topicsIds, relIds) {
        $assert(topicsIds, "topicsIds can not be null");
        var framework = this._getFramework();
        var mindmap = framework.getModel();

        topicsIds.each(function(topicId) {
            var topic = framework.getTopic(topicId);
            topic.deleteNode();
        });
    }
});

