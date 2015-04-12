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

mindplot.StandaloneActionDispatcher = new Class(/** @lends StandaloneActionDispatcher */{
    Extends: mindplot.ActionDispatcher,
    /**
     * @extends mindplot.ActionDispatcher
     * @constructs
     * @param {mindplot.CommandContext} commandContext
     */
    initialize: function (commandContext) {
        this.parent(commandContext);
        this._actionRunner = new mindplot.DesignerActionRunner(commandContext, this);
    },

    /** */
    addTopics: function (models, parentTopicsId) {
        var command = new mindplot.commands.AddTopicCommand(models, parentTopicsId);
        this.execute(command);
    },

    /** */
    addRelationship: function (model) {
        var command = new mindplot.commands.AddRelationshipCommand(model);
        this.execute(command);
    },

    /** */
    deleteEntities: function (topicsIds, relIds) {
        var command = new mindplot.commands.DeleteCommand(topicsIds, relIds);
        this.execute(command);
    },

    /** */
    dragTopic: function (topicId, position, order, parentTopic) {
        var command = new mindplot.commands.DragTopicCommand(topicId, position, order, parentTopic);
        this.execute(command);
    },

    /** */
    moveTopic: function (topicId, position) {
        $assert($defined(topicId), "topicsId can not be null");
        $assert($defined(position), "position can not be null");

        var commandFunc = function (topic, value) {
            var result = topic.getPosition();
            mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeMoveEvent, {
                node: topic.getModel(),
                position: value
            });
            return result;
        };

        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, topicId, position);
        this.execute(command);
    },

    /** */
    moveControlPoint: function (ctrlPoint, point) {
        var command = new mindplot.commands.MoveControlPointCommand(ctrlPoint, point);
        this.execute(command);
    },

    /** */
    changeFontStyleToTopic: function (topicsIds) {

        var commandFunc = function (topic) {
            var result = topic.getFontStyle();
            var style = (result == "italic") ? "normal" : "italic";
            topic.setFontStyle(style, true);
            return result;
        };
        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, topicsIds);
        this.execute(command);

    },

    /** */
    changeTextToTopic: function (topicsIds, text) {
        $assert($defined(topicsIds), "topicsIds can not be null");

        var commandFunc = function (topic, value) {
            var result = topic.getText();
            topic.setText(value);
            return result;
        };
        commandFunc.commandType = "changeTextToTopic";

        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, topicsIds, text);
        this.execute(command);
    },

    /** */
    changeFontFamilyToTopic: function (topicIds, fontFamily) {
        $assert(topicIds, "topicIds can not be null");
        $assert(fontFamily, "fontFamily can not be null");


        var commandFunc = function (topic, fontFamily) {
            var result = topic.getFontFamily();
            topic.setFontFamily(fontFamily, true);

            topic._adjustShapes();
            return result;
        };

        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, topicIds, fontFamily);
        this.execute(command);
    },

    /** */
    changeFontColorToTopic: function (topicsIds, color) {
        $assert(topicsIds, "topicIds can not be null");
        $assert(color, "color can not be null");

        var commandFunc = function (topic, color) {
            var result = topic.getFontColor();
            topic.setFontColor(color, true);
            return result;
        };

        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, topicsIds, color);
        command.discardDuplicated = "fontColorCommandId";
        this.execute(command);
    },

    /** */
    changeBackgroundColorToTopic: function (topicsIds, color) {
        $assert(topicsIds, "topicIds can not be null");
        $assert(color, "color can not be null");

        var commandFunc = function (topic, color) {
            var result = topic.getBackgroundColor();
            topic.setBackgroundColor(color);
            return result;
        };

        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, topicsIds, color);
        command.discardDuplicated = "backColor";
        this.execute(command);
    },

    /** */
    changeBorderColorToTopic: function (topicsIds, color) {
        $assert(topicsIds, "topicIds can not be null");
        $assert(color, "topicIds can not be null");

        var commandFunc = function (topic, color) {
            var result = topic.getBorderColor();
            topic.setBorderColor(color);
            return result;
        };

        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, topicsIds, color);
        command.discardDuplicated = "borderColorCommandId";
        this.execute(command);
    },

    /** */
    changeFontSizeToTopic: function (topicsIds, size) {
        $assert(topicsIds, "topicIds can not be null");
        $assert(size, "size can not be null");

        var commandFunc = function (topic, size) {
            var result = topic.getFontSize();
            topic.setFontSize(size, true);

            topic._adjustShapes();
            return result;
        };

        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, topicsIds, size);
        this.execute(command);
    },

    /** */
    changeShapeTypeToTopic: function (topicsIds, shapeType) {
        $assert(topicsIds, "topicsIds can not be null");
        $assert(shapeType, "shapeType can not be null");

        var commandFunc = function (topic, shapeType) {
            var result = topic.getShapeType();
            topic.setShapeType(shapeType, true);
            return result;
        };

        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, topicsIds, shapeType);
        this.execute(command);
    },

    /** */
    changeFontWeightToTopic: function (topicsIds) {
        $assert(topicsIds, "topicsIds can not be null");

        var commandFunc = function (topic) {
            var result = topic.getFontWeight();
            var weight = (result == "bold") ? "normal" : "bold";
            topic.setFontWeight(weight, true);

            topic._adjustShapes();
            return result;
        };

        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, topicsIds);
        this.execute(command);
    },

    /** */
    shrinkBranch: function (topicsIds, collapse) {
        $assert(topicsIds, "topicsIds can not be null");

        var commandFunc = function (topic, isShrink) {
            topic.setChildrenShrunken(isShrink);
            return !isShrink;
        };

        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, topicsIds, collapse);
        this.execute(command, false);
    },

    /** */
    addFeatureToTopic: function (topicId, featureType, attributes) {
        var command = new mindplot.commands.AddFeatureToTopicCommand(topicId, featureType, attributes);
        this.execute(command);
    },

    /** */
    changeFeatureToTopic: function (topicId, featureId, attributes) {
        var command = new mindplot.commands.ChangeFeatureToTopicCommand(topicId, featureId, attributes);
        this.execute(command);
    },

    /** */
    removeFeatureFromTopic: function (topicId, featureId) {
        var command = new mindplot.commands.RemoveFeatureFromTopicCommand(topicId, featureId);
        this.execute(command);
    },

    /** */
    execute: function (command) {
        this._actionRunner.execute(command);
    }

});

mindplot.CommandContext = new Class(/** @lends CommandContext */{
    /**
     * @constructs
     * @param {mindplot.Designer} designer
     */
    initialize: function (designer) {
        $assert(designer, "designer can not be null");
        this._designer = designer;
    },

    /** */
    findTopics: function (topicsIds) {
        $assert($defined(topicsIds), "topicsIds can not be null");
        if (!(topicsIds instanceof Array)) {
            topicsIds = [topicsIds];
        }

        var designerTopics = this._designer.getModel().getTopics();
        var result = designerTopics.filter(function (topic) {
            return topicsIds.contains(topic.getId());
        });

        if (result.length != topicsIds.length) {
            var ids = designerTopics.map(function (topic) {
                return topic.getId();
            });
            $assert(result.length == topicsIds.length, "Could not find topic. Result:" + result + ", Filter Criteria:" + topicsIds + ", Current Topics: [" + ids + "]");
        }
        return result;
    },

    /** */
    deleteTopic: function (topic) {
        this._designer.removeTopic(topic);
    },

    /** */
    createTopic: function (model) {
        $assert(model, "model can not be null");
        return this._designer.nodeModelToNodeGraph(model);
    },

    /** */
    createModel: function () {
        var mindmap = this._designer.getMindmap();
        return mindmap.createNode(mindplot.NodeModel.MAIN_TOPIC_TYPE);
    },

    /** */
    addTopic: function (topic) {
        var mindmap = this._designer.getMindmap();
        return mindmap.addBranch(topic.getModel());
    },

    /** */
    connect: function (childTopic, parentTopic) {
        childTopic.connectTo(parentTopic, this._designer._workspace);
    },

    /** */
    disconnect: function (topic) {
        topic.disconnect(this._designer._workspace);
    },

    /** */
    addRelationship: function (model) {
        $assert(model, "model cannot be null");
        return this._designer.addRelationship(model);
    },

    /** */
    deleteRelationship: function (relationship) {
        this._designer.deleteRelationship(relationship);
    },

    /** */
    findRelationships: function (relIds) {
        $assert($defined(relIds), "relId can not be null");
        if (!(relIds instanceof Array)) {
            relIds = [relIds];
        }

        var designerRel = this._designer.getModel().getRelationships();
        return designerRel.filter(function (rel) {
            return relIds.contains(rel.getId());
        });
    },

    /** */
    moveTopic: function (topic, position) {
        $assert(topic, "topic cannot be null");
        $assert(position, "position cannot be null");
        mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeMoveEvent, {
            node: topic.getModel(),
            position: position
        });
    }
});


