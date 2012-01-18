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

mindplot.StandaloneActionDispatcher = new Class({
    Extends: mindplot.ActionDispatcher,
    initialize: function(commandContext) {
        this.parent(commandContext);
        this._actionRunner = new mindplot.DesignerActionRunner(commandContext, this);
    },

    hasBeenChanged: function() {
        // @todo: This don't seems to belong here.
        this._actionRunner.hasBeenChanged();
    },

    addIconToTopic: function(topicId, iconType) {
        var command = new mindplot.commands.AddIconToTopicCommand(topicId, iconType);
        this.execute(command);
    },

    changeLinkToTopic: function(topicId, url) {
        var command = new mindplot.commands.ChangeLinkToTopicCommand(topicId, url);
        this.execute(command);
    },

    addTopic:function(nodeModel, parentTopicId, animated) {
        var command = new mindplot.commands.AddTopicCommand(nodeModel, parentTopicId, animated);
        this.execute(command);
    },

    changeNoteToTopic: function(topicId, text) {
        var command = new mindplot.commands.ChangeNoteToTopicCommand(topicId, text);
        this.execute(command);
    },

    connectByRelation: function(model) {
        var command = new mindplot.commands.AddRelationshipCommand(model);
        this.execute(command);
    },

    deleteTopics: function(topicsIds, relIds) {
        var command = new mindplot.commands.DeleteCommand(topicsIds, relIds);
        this.execute(command);
    },

    dragTopic: function(topicId, position, order, parentTopic) {
        var command = new mindplot.commands.DragTopicCommand(topicId, position, order, parentTopic);
        this.execute(command);
    },

    moveTopic: function(topicId, position) {
        $assert($defined(topicId), "topicsId can not be null");
        $assert($defined(position), "position can not be null");

        var commandFunc = function(topic, value) {
            var result = topic.getPosition();
            mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeMoveEvent, {node:topic.getModel(),position:value});
            return result;
        };

        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, topicId, position);
        this.execute(command);
    },

    moveControlPoint: function(ctrlPoint, point) {
        var command = new mindplot.commands.MoveControlPointCommand(ctrlPoint, point);
        this.execute(command);
    },

    removeIconFromTopic: function(topicId, iconModel) {
        var command = new mindplot.commands.RemoveIconFromTopicCommand(topicId, iconModel);
        this.execute(command);
    },
    removeLinkFromTopic: function(topicId) {
        var command = new mindplot.commands.RemoveLinkFromTopicCommand(topicId);
        this.execute(command);
    },

    removeNoteFromTopic: function(topicId) {
        var command = new mindplot.commands.RemoveNoteFromTopicCommand(topicId);
        this.execute(command);
    },

    changeFontStyleToTopic: function(topicsIds) {

        var commandFunc = function(topic) {
            var result = topic.getFontStyle();
            var style = (result == "italic") ? "normal" : "italic";
            topic.setFontStyle(style, true);
            return result;
        };
        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, topicsIds);
        this.execute(command);

    },

    changeTextToTopic : function(topicsIds, text) {
        $assert($defined(topicsIds), "topicsIds can not be null");

        var commandFunc = function(topic, value) {
            var result = topic.getText();
            topic.setText(value);
            return result;
        };

        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, topicsIds, text);
        this.execute(command);
    },

    changeFontFamilyToTopic: function(topicIds, fontFamily) {
        $assert(topicIds, "topicIds can not be null");
        $assert(fontFamily, "fontFamily can not be null");


        var commandFunc = function(topic, fontFamily) {
            var result = topic.getFontFamily();
            topic.setFontFamily(fontFamily, true);

            topic._adjustShapes();
            return result;
        };

        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, topicIds, fontFamily);
        this.execute(command);
    },

    changeFontColorToTopic: function(topicsIds, color) {
        $assert(topicsIds, "topicIds can not be null");
        $assert(color, "color can not be null");

        var commandFunc = function(topic, color) {
            var result = topic.getFontColor();
            topic.setFontColor(color, true);
            return result;
        };

        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, topicsIds, color);
        command.discartDuplicated = "fontColorCommandId";
        this.execute(command);
    },

    changeBackgroundColorToTopic: function(topicsIds, color) {
        $assert(topicsIds, "topicIds can not be null");
        $assert(color, "color can not be null");

        var commandFunc = function(topic, color) {
            var result = topic.getBackgroundColor();
            topic.setBackgroundColor(color);
            return result;
        };

        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, topicsIds, color);
        command.discartDuplicated = "backColor";
        this.execute(command);
    },

    changeBorderColorToTopic : function(topicsIds, color) {
        $assert(topicsIds, "topicIds can not be null");
        $assert(color, "topicIds can not be null");

        var commandFunc = function(topic, color) {
            var result = topic.getBorderColor();
            topic.setBorderColor(color);
            return result;
        };

        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, topicsIds, color);
        command.discartDuplicated = "borderColorCommandId";
        this.execute(command);
    },

    changeFontSizeToTopic : function(topicsIds, size) {
        $assert(topicsIds, "topicIds can not be null");
        $assert(size, "size can not be null");

        var commandFunc = function(topic, size) {
            var result = topic.getFontSize();
            topic.setFontSize(size, true);

            topic._adjustShapes();
            return result;
        };

        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, topicsIds, size);
        this.execute(command);
    },

    changeShapeTypeToTopic : function(topicsIds, shapeType) {
        $assert(topicsIds, "topicsIds can not be null");
        $assert(shapeType, "shapeType can not be null");

        var commandFunc = function(topic, shapeType) {
            var result = topic.getShapeType();
            topic.setShapeType(shapeType, true);
            return result;
        };

        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, topicsIds, shapeType);
        this.execute(command);
    },

    changeFontWeightToTopic : function(topicsIds) {
        $assert(topicsIds, "topicsIds can not be null");

        var commandFunc = function(topic) {
            var result = topic.getFontWeight();
            var weight = (result == "bold") ? "normal" : "bold";
            topic.setFontWeight(weight, true);

            topic._adjustShapes();
            return result;
        };

        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, topicsIds);
        this.execute(command);
    },

    shrinkBranch : function(topicsIds, collapse) {
        $assert(topicsIds, "topicsIds can not be null");

        var commandFunc = function(topic, isShrink) {
            topic.setChildrenShrunken(isShrink);
            return !isShrink;
        };

        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, topicsIds, collapse);
        this.execute(command, false);
    },

    execute:function(command) {
        this._actionRunner.execute(command);
        mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.DoLayout);
    }

});

mindplot.CommandContext = new Class({
    initialize: function(designer) {
        $assert(designer, "designer can not be null");
        this._designer = designer;
    },

    findTopics:function(topicsIds) {
        $assert($defined(topicsIds), "topicsIds can not be null");
        if (!(topicsIds instanceof Array)) {
            topicsIds = [topicsIds];
        }

        var designerTopics = this._designer.getModel().getTopics();
        return  designerTopics.filter(function(topic) {
            return topicsIds.contains(topic.getId());
        });
    },

    deleteTopic:function(topic) {
        this._designer._removeNode(topic);
    },

    createTopic:function(model, isVisible) {
        $assert(model, "model can not be null");
        var result = this._designer._nodeModelToNodeGraph(model, isVisible);
        return  result;
    },

    createModel:function() {
        var mindmap = this._designer.getMindmap();
        return mindmap.createNode(mindplot.NodeModel.MAIN_TOPIC_TYPE);
    },

    connect:function(childTopic, parentTopic, isVisible) {
        childTopic.connectTo(parentTopic, this._designer._workspace, isVisible);
    } ,

    disconnect:function(topic) {
        topic.disconnect(this._designer._workspace);
    },

    createRelationship:function(model) {
        $assert(model, "model cannot be null");
        return this._designer.createRelationship(model);
    },
    removeRelationship:function(model) {
        this._designer.removeRelationship(model);
    },

    findRelationships:function(lineIds) {
        var result = [];
        lineIds.forEach(function(lineId) {
            var line = this._designer.getModel().getRelationshipsById()[lineId];
            if ($defined(line)) {
                result.push(line);
            }
        }.bind(this));
        return result;
    }
});


