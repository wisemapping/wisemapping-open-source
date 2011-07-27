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

mindplot.SingleCommandDispatcher = new Class(
{
    Extends:mindplot.BaseCommandDispatcher,
    initialize: function() {

    },
    addIconToTopic: function() {
        throw "method must be implemented.";
    },
    addLinkToTopic: function() {
        throw "method must be implemented.";
    },
    addNoteToTopic: function() {
        throw "method must be implemented.";
    },addRelationship: function() {
        throw "method must be implemented.";
    },addTopic: function() {
        throw "method must be implemented.";
    },changeIcon: function() {
        throw "method must be implemented.";
    },deleteTopic: function() {
        throw "method must be implemented.";
    },dragTopic: function() {
        throw "method must be implemented.";
    },moveControllPoint: function() {
        throw "method must be implemented.";
    } ,removeIconFromTopic: function() {
        throw "method must be implemented.";
    },removeLinkFromTopic: function() {
        throw "method must be implemented.";
    },removeNodeFromTopic: function() {
        throw "method must be implemented.";
    }
});

