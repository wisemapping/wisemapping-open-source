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

mindplot.BaseCommandDispatcher = new Class({

    initialize: function() {
    },

    addIconToTopic: function(topicId, iconType) {
        throw "method must be implemented.";
    },

    addLinkToTopic: function(topicId, url) {
        throw "method must be implemented.";
    },

    addNoteToTopic: function(topicId, text) {
        throw "method must be implemented.";
    },

    addRelationship: function(model, mindmap) {
        throw "method must be implemented.";
    },

    addTopic: function(model, parentTopicId, animated) {
        throw "method must be implemented.";
    },

    changeIcon: function(topicId, iconId, iconType) {
        throw "method must be implemented.";
    },

    deleteTopic: function(topicsIds) {
        throw "method must be implemented.";
    },

    dragTopic: function(topicId) {
        throw "method must be implemented.";
    },

    moveControlPoint: function(trlPointController, point) {
        throw "method must be implemented.";
    },

    removeIconFromTopic: function(topicId, iconModel) {
        throw "method must be implemented.";
    },

    removeLinkFromTopic: function(topicId) {
        throw "method must be implemented.";
    },

    removeNodeFromTopic: function(topicId) {
        throw "method must be implemented.";
    }
});

