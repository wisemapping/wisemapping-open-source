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

mindplot.layout.EventBusDispatcher = new Class(/** @lends EventBusDispatcher */{
    /**
     * @constructs
     */
    initialize:function() {
        this.registerBusEvents();
    },

    /**
     * @param {mindplot.layout.LayoutManager} layoutManager
     */
    setLayoutManager : function(layoutManager) {
        this._layoutManager = layoutManager;
    },

    /**
     * register bus events
     */
    registerBusEvents:function () {
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeAdded, this._nodeAdded.bind(this));
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeRemoved, this._nodeRemoved.bind(this));
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeResizeEvent, this._nodeResizeEvent.bind(this));
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeMoveEvent, this._nodeMoveEvent.bind(this));
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeDisconnectEvent, this._nodeDisconnectEvent.bind(this));
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeConnectEvent, this._nodeConnectEvent.bind(this));
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeShrinkEvent, this._nodeShrinkEvent.bind(this));
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.DoLayout, this._doLayout.bind(this));
    },

    _nodeResizeEvent: function(args) {
        this._layoutManager.updateNodeSize(args.node.getId(), args.size);
    },

    _nodeMoveEvent: function(args) {
        this._layoutManager.moveNode(args.node.getId(), args.position);
    },

    _nodeDisconnectEvent: function(node) {
        this._layoutManager.disconnectNode(node.getId());
    },

    _nodeConnectEvent: function(args) {
        this._layoutManager.connectNode(args.parentNode.getId(), args.childNode.getId(), args.childNode.getOrder());

    },

    _nodeShrinkEvent: function(node) {
        this._layoutManager.updateShrinkState(node.getId(), node.areChildrenShrunken());
    },

    _nodeAdded: function(node) {
        // Central topic must not be added twice ...
        if (node.getId() != 0) {
            this._layoutManager.addNode(node.getId(), {width:10,height:10}, node.getPosition());
            this._layoutManager.updateShrinkState(node.getId(), node.areChildrenShrunken());
        }
    },

    _nodeRemoved: function(node) {
        this._layoutManager.removeNode(node.getId());
    },

    _doLayout: function() {
//        (function() {
        this._layoutManager.layout(true);
//        console.log("---------");
//        this._layoutManager.dump();
//        console.log("---------");
//        console.log("---------");
//        }).delay(0, this);
    },

    /** @return layout manager */
    getLayoutManager: function() {
        return this._layoutManager;
    }

});