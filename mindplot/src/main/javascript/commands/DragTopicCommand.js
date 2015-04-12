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

mindplot.commands.DragTopicCommand = new Class(/** @lends DragTopicCommand */{
    Extends:mindplot.Command,
    /** 
     * @classdesc This command class handles do/undo of dragging a topic to a new position.
     * @constructs
     * @param {String} topicId id of the topic to drag
     * @param {Object} position
     * @param {Number} order the order property (children of one node are displayed in order from 0 to n)
     * @param {mindplot.Topic} parentTopic the topic to be made the dragged topic's new parent
     * @extends mindplot.Command
     */
    initialize:function (topicId, position, order, parentTopic) {
        $assert(topicId, "topicId must be defined");

        this._topicsId = topicId;
        if ($defined(parentTopic))
            this._parentId = parentTopic.getId();

        this.parent();
        this._position = position;
        this._order = order;
    },

    /** 
     * Overrides abstract parent method 
     */
    execute:function (commandContext) {

        var topic = commandContext.findTopics(this._topicsId)[0];
        topic.setVisibility(false);

        // Save old position ...
        var origParentTopic = topic.getOutgoingConnectedTopic();

        // In this case, topics are positioned using order ...
        var origOrder = topic.getOrder();
        var origPosition = topic.getPosition();

        // Disconnect topic ..
        if ($defined(origParentTopic) && origParentTopic != this._parentId) {
            commandContext.disconnect(topic);
        }

        // Set topic order ...
        if (this._order != null) {
            topic.setOrder(this._order);
        } else if (this._position != null) {
            commandContext.moveTopic(topic, this._position);
        } else {
            $assert("Illegal command state exception.");
        }

        // Finally, connect topic ...
        if (origParentTopic != this._parentId) {

            if ($defined(this._parentId)) {
                var parentTopic = commandContext.findTopics(this._parentId)[0];
                commandContext.connect(topic, parentTopic);
            }

            // Backup old parent id ...
            this._parentId = null;
            if ($defined(origParentTopic)) {
                this._parentId = origParentTopic.getId();
            }
        }
        topic.setVisibility(true);

        // Store for undo ...
        this._order = origOrder;
        this._position = origPosition;

    },

    /** 
     * Overrides abstract parent method
     * @see {@link mindplot.Command.undoExecute} 
     */
    undoExecute:function (commandContext) {
        this.execute(commandContext);
    }
});