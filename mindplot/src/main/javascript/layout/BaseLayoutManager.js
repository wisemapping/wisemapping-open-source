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

mindplot.layout.BaseLayoutManager = new Class({

    options: {

    },

    initialize: function(designer, options) {
        this.setOptions(options);
        this._createBoard();
        this._designer = designer;
//        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeResizeEvent, this._nodeResizeEvent.bind(this));
//        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeMoveEvent, this._nodeMoveEvent.bind(this));
//        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.ONodeDisconnectEvent, this._nodeDisconnectEvent.bind(this));
//        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.ONodeConnectEvent, this._nodeConnectEvent.bind(this));
//        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeRepositionateEvent, this._nodeRepositionateEvent.bind(this));
//        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeShrinkEvent, this._nodeShrinkEvent.bind(this));
    },

    _nodeResizeEvent:function(node) {
    },

    _nodeMoveEvent:function(node) {
        var modifiedTopics = [];
        this.getTopicBoardForTopic(node).updateChildrenPosition(node, modifiedTopics);
    },

    _nodeDisconnectEvent:function(targetNode, node) {
        var modifiedTopics = [];
        this.getTopicBoardForTopic(targetNode).removeTopicFromBoard(node, modifiedTopics);
    },

    _nodeConnectEvent:function(targetNode, node) {
        var modifiedTopics = [];
        this.getTopicBoardForTopic(targetNode).addBranch(node, modifiedTopics);
    },

    _nodeRepositionateEvent:function(node) {
        var modifiedTopics = [];
        this.getTopicBoardForTopic(node).updateChildrenPosition(node, modifiedTopics);
    },

    _nodeShrinkEvent:function(node) {
    },

    _createBoard:function() {
        this._boards = new Hash();
    },
    getTopicBoardForTopic:function(node) {
        var id = node.getId();
        var result = this._boards[id];
        if (!$defined(result)) {
            result = this._addNode(node);
        }
        return result;
    },
    _addNode:function(node) {
        var board = null;
        if (this._isCentralTopic(node))
            board = this._createCentralTopicBoard(node);
        else
            board = this._createMainTopicBoard(node);
        var id = node.getId();
        this._boards[id] = board;
        return board;
    },
    _createMainTopicBoard:function(node) {
        return new mindplot.layout.boards.Board(node, this);
    },
    _createCentralTopicBoard:function(node) {
        return new mindplot.layout.boards.Board(node, this);
    },
    prepareNode:function(node, children) {

    },
    addHelpers:function(node) {

    },
    needsPrepositioning:function() {
        return true;
    },
    getDesigner:function() {
        return this._designer;
    },
    _isCentralTopic:function(node) {
        var type = node.getModel().getType();
        return type == mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE;
    },

    getClassName:function() {
        return mindplot.layout.BaseLayoutManager.NAME;
    }
});

mindplot.layout.BaseLayoutManager.NAME = "BaseLayoutManager";

mindplot.layout.BaseLayoutManager.implement(new Events);
mindplot.layout.BaseLayoutManager.implement(new Options);