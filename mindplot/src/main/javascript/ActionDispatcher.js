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

//noinspection JSUnusedLocalSymbols
mindplot.ActionDispatcher = new Class({
    Implements:[mindplot.Events],
    initialize: function(commandContext) {
        $assert(commandContext, "commandContext can not be null");
    },

    addRelationship: function(model, mindmap) {
        throw "method must be implemented.";
    },

    addTopics: function(models, parentTopicId) {
        throw "method must be implemented.";
    },

    deleteEntities: function(topicsIds, relIds) {
        throw "method must be implemented.";
    },

    dragTopic: function(topicId, position, order, parentTopic) {
        throw "method must be implemented.";
    },

    moveTopic: function(topicId, position) {
        throw "method must be implemented.";
    },

    moveControlPoint: function(ctrlPoint, point) {
        throw "method must be implemented.";
    },

    changeFontFamilyToTopic: function(topicIds, fontFamily) {
        throw "method must be implemented.";
    },

    changeFontStyleToTopic: function(topicsIds) {
        throw "method must be implemented.";
    },

    changeFontColorToTopic: function(topicsIds, color) {
        throw "method must be implemented.";
    },

    changeFontSizeToTopic : function(topicsIds, size) {
        throw "method must be implemented.";
    },

    changeBackgroundColorToTopic: function(topicsIds, color) {
        throw "method must be implemented.";
    },

    changeBorderColorToTopic: function(topicsIds, color) {
        throw "method must be implemented.";
    },

    changeShapeTypeToTopic : function(topicsIds, shapeType) {
        throw "method must be implemented.";
    },

    changeFontWeightToTopic : function(topicsIds) {
        throw "method must be implemented.";
    },

    changeTextToTopic : function(topicsIds, text) {
        throw "method must be implemented.";
    },

    shrinkBranch : function(topicsIds, collapse) {
        throw "method must be implemented.";
    },

    addFeatureToTopic : function(topicId, type, attributes) {
        throw "method must be implemented.";
    },

    changeFeatureToTopic : function(topicId, featureId, attributes) {
        throw "method must be implemented.";
    },

    removeFeatureFromTopic : function(topicId, featureId) {
        throw "method must be implemented.";
    }
});

mindplot.ActionDispatcher.setInstance = function(dispatcher) {
    mindplot.ActionDispatcher._instance = dispatcher;
};

mindplot.ActionDispatcher.getInstance = function() {
    return mindplot.ActionDispatcher._instance;
};

