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
mindplot.layout.TestSuite = new Class({
    Extends: mindplot.layout.ChildrenSorterStrategy,

    initialize:function() {
        $("basicTest").setStyle("display","block");

        this.testAligned();
        this.testEvents();
        this.testEventsComplex();
        this.testDisconnect();
        this.testReconnect();
        this.testRemoveNode();
        this.testSize();
        this.testReconnectSingleNode();
    },

    testAligned: function() {

        var position = {x:0,y:0};
        var manager = new mindplot.layout.LayoutManager(0, mindplot.layout.TestSuite.ROOT_NODE_SIZE);

        manager.addNode(1, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(2, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(3, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(4, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.connectNode(0, 1, 0);
        manager.connectNode(1, 2, 0);
        manager.connectNode(2, 3, 0);
        manager.connectNode(3, 4, 0);

        manager.layout();
        manager.plot("testAligned", {width:1200,height:200});

        // All nodes should be vertically aligned
        $assert(manager.find(0).getPosition().y == manager.find(1).getPosition().y, "Nodes are not aligned");
        $assert(manager.find(0).getPosition().y == manager.find(2).getPosition().y, "Nodes are not aligned");
        $assert(manager.find(0).getPosition().y == manager.find(3).getPosition().y, "Nodes are not aligned");
        $assert(manager.find(0).getPosition().y == manager.find(4).getPosition().y, "Nodes are not aligned");
    },

    testEvents: function() {
        console.log("testEvents:");
        var position = {x:0,y:0};
        var manager = new mindplot.layout.LayoutManager(0, mindplot.layout.TestSuite.ROOT_NODE_SIZE);

        // Add 3 nodes...
        manager.addNode(1, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(2, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(3, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(4, mindplot.layout.TestSuite.NODE_SIZE, {x:0, y: 60});

        // Now connect one with two....
        manager.connectNode(0, 1, 0);
        manager.connectNode(0, 2, 1);
        manager.connectNode(1, 3, 0);

        // Basic layout repositioning ...
        console.log("\t--- Updated tree ---");
        var events = [];
        manager.addEvent('change', function(event) {
            console.log("\tUpdated nodes: {id:" + event.getId() + ", order: " + event.getOrder() + ",position: {" + event.getPosition().x + "," + event.getPosition().y + "}");
            events.push(event);
        });
        manager.layout(true);
        manager.plot("testEvents1", {width:800, height:200});

        // Ok, if a new node is added, this an event should be fired  ...
        console.log("\t---- Layout without changes should not affect the tree  ---");
        events.empty();
        manager.layout(true);
        manager.plot("testEvents2", {width:800, height:200});

        $assert(events.length == 0, "Unnecessary tree updated.");

        console.log("\n");
    },

    testEventsComplex: function() {
        console.log("testEventsComplex:");
        var position = {x:0,y:0};
        var manager = new mindplot.layout.LayoutManager(0, mindplot.layout.TestSuite.ROOT_NODE_SIZE);

        // Add 3 nodes...
        manager.addNode(1, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(2, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(3, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(4, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(5, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(6, mindplot.layout.TestSuite.NODE_SIZE, {x:0, y:60});

        // Now connect one with two....
        manager.connectNode(0, 1, 0);
        manager.connectNode(0, 2, 1);
        manager.connectNode(0, 3, 2);
        manager.connectNode(3, 4, 0);
        manager.connectNode(3, 5, 1);

        var events = [];
        manager.addEvent('change', function(event) {
            console.log("\tUpdated nodes: {id:" + event.getId() + ", order: " + event.getOrder() + ",position: {" + event.getPosition().x + "," + event.getPosition().y + "}");
            events.push(event);
        });

        // Reposition ...
        manager.layout(true);
        manager.plot("testEventsComplex1", {width:800, height:200});

        // Add a new node and connect. Only children nodes should be affected.
        console.log("\t---- Connect a new node  ---");

        events.empty();
        manager.connectNode(3, 6, 2);
        manager.layout(true);
        manager.plot("testEventsComplex2", {width:800, height:200});

        //TODO(gb): fix this. only 4 (reposition of nodes 1,4,5,6) events should be fired, actually 6 are
//        $assert(events.length == 6, "Only 4 nodes should be repositioned.");

        console.log("\n");
    },

    testDisconnect: function() {
        console.log("testDisconnect:");
        var position = {x:0,y:0};
        var manager = new mindplot.layout.LayoutManager(0, mindplot.layout.TestSuite.ROOT_NODE_SIZE);

        // Prepare a sample graph ...
        manager.addNode(1, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(2, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(3, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(4, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(5, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(6, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(7, mindplot.layout.TestSuite.NODE_SIZE, position);

        manager.connectNode(0, 1, 0);
        manager.connectNode(1, 2, 0);
        manager.connectNode(1, 3, 1);
        manager.connectNode(1, 4, 2);
        manager.connectNode(4, 5, 0);
        manager.connectNode(5, 6, 0);
        manager.connectNode(5, 7, 1);

        var events = [];
        manager.addEvent('change', function(event) {
            var pos = event.getPosition();
            var posStr = pos ? ",position: {" + pos.x + "," + pos.y : "";
            var node = manager.find(event.getId());
            console.log("\tUpdated nodes: {id:" + event.getId() + ", order: " + event.getOrder() + posStr + "}");
            events.push(event);
        });
        manager.layout(true);
        manager.plot("testDisconnect1", {width:1200, height:400});

        // Now, disconnect one node ...
        console.log("--- Disconnect a single node ---");
        events.empty();
        manager.disconnectNode(2);
        manager.layout(true);
        manager.plot("testDisconnect2", {width:1200, height:400});

//        $assert(events.some(function(event) {return event.getId() == 2;}), "Event for disconnected node seems not to be propagated");
        $assert(manager._treeSet.getParent(manager.find(2)) == null, "Node 2 should have no parent, it was disconnected");

//        Great, let's disconnect a node with children.
        console.log("--- Disconnect a node with children ---");
        manager.disconnectNode(4);
        manager.layout(true);
        manager.plot("testDisconnect3", {width:1200, height:400});

        $assert(events.some(function(event) {return event.getId() == 4;}), "Event for disconnected node seems not to be propagated");
        $assert(manager._treeSet.getParent(manager.find(4)) == null, "Node 4 should have no parent, it was disconnected");
        var childrenOfNode4 = manager._treeSet.getChildren(manager.find(4));
        var childrenOfNode5 = manager._treeSet.getChildren(manager.find(5));
        $assert(childrenOfNode4.contains(manager.find(5)), "Node 5 still should be the child of node 4");
        $assert(childrenOfNode5.contains(manager.find(6)) && childrenOfNode5.contains(manager.find(7)), "Nodes 6 and 7 still should be the children of node 5");

        console.log("\n");
    },

    testReconnect: function() {
        var position = {x:0,y:0};
        var manager = new mindplot.layout.LayoutManager(0, mindplot.layout.TestSuite.ROOT_NODE_SIZE);

        manager.addNode(1, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(2, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(3, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(4, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(5, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(6, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(7, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(8, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(9, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(10, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(11, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(12, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.connectNode(0, 1, 0);
        manager.connectNode(0, 2, 1);
        manager.connectNode(0, 3, 2);
        manager.connectNode(0, 4, 3);
        manager.connectNode(0, 5, 4);
        manager.connectNode(1, 6, 0);
        manager.connectNode(1, 7, 1);
        manager.connectNode(7, 8, 0);
        manager.connectNode(8, 9, 0);
        manager.connectNode(5, 10, 0);
        manager.connectNode(6, 11, 0);
        manager.connectNode(6, 12, 1);

        manager.layout();
        manager.plot("testReconnect1",{width:1200, height:400});

        var childrenOfNode6BeforeReconnect = manager._treeSet.getChildren(manager.find(6));

        // Reconnect node 6 to node 4
        manager.disconnectNode(6);
        manager.connectNode(4,6,0);
        manager.layout();
        manager.plot("testReconnect2",{width:1200, height:400});

        var childrenOfNode4AfterReconnect = manager._treeSet.getChildren(manager.find(4));
        var childrenOfNode6AfterReconnect = manager._treeSet.getChildren(manager.find(6));
        $assert(childrenOfNode4AfterReconnect.contains(manager.find(6)), "Node 6 should be the child of node 4");
        $assert(childrenOfNode6BeforeReconnect == childrenOfNode6AfterReconnect, "The children of node 6 should be the same");

    },

    testRemoveNode: function() {
        console.log("testRemoveNode:");
        var position = {x:0,y:0};
        var manager = new mindplot.layout.LayoutManager(0, mindplot.layout.TestSuite.ROOT_NODE_SIZE);

        // Prepare a sample graph ...
        manager.addNode(1, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(2, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(3, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(4, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(5, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(6, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(7, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(8, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(9, mindplot.layout.TestSuite.NODE_SIZE, position);

        manager.connectNode(0, 1, 0);
        manager.connectNode(0, 5, 1);
        manager.connectNode(0, 6, 2);
        manager.connectNode(0, 7, 3);
        manager.connectNode(0, 8, 4);
        manager.connectNode(0, 9, 5);
        manager.connectNode(1, 2, 0);
        manager.connectNode(1, 3, 1);
        manager.connectNode(3, 4, 0);

        var events = [];
        manager.addEvent('change', function(event) {
            var pos = event.getPosition();
            var posStr = pos ? ",position: {" + pos.x + "," + event.getPosition().y : "";
            console.log("\tUpdated nodes: {id:" + event.getId() + ", order: " + event.getOrder() + posStr + "}");
            events.push(event);
        });
        manager.layout(true);
        manager.plot("testRemoveNode1", {width:1000, height:200});

        // Test removal of a connected node ...
        console.log("\t--- Remove node 3  ---");
        manager.removeNode(3);
        manager.layout(true);
        manager.plot("testRemoveNode2", {width:1000, height:200});

        // Remove a node from the root node
        console.log("\t--- Remove node 6  ---");
        manager.removeNode(6);
        manager.layout(true);
        manager.plot("testRemoveNode3", {width:1000, height:200});

        // Remove a node from the root node
        console.log("\t--- Remove node 5  ---");
        manager.removeNode(5);
        manager.layout(true);
        manager.plot("testRemoveNode4", {width:1000, height:200});

        $assert(manager.find(1).getPosition().y == manager.find(2).getPosition().y, "After removal of node 3, nodes 1 and 2 should be alingned");
        console.log("\n");
    },

    testSize: function() {
        var position = {x:0, y:0};
        var manager = new mindplot.layout.LayoutManager(0, mindplot.layout.TestSuite.ROOT_NODE_SIZE);

        manager.addNode(1, {width: 60, height: 60}, position);
        manager.addNode(2, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(3, {width: 260, height: 30}, position);
        manager.addNode(4, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(5, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(7, {width: 80, height: 80}, position);
        manager.addNode(8, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(9, {width: 30, height: 30}, position);
        manager.addNode(10, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(11, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(12, {width: 100, height: 70}, position);
        manager.addNode(13, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(14, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(15, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(16, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(17, mindplot.layout.TestSuite.NODE_SIZE, position);

        manager.connectNode(0,1,0);
        manager.connectNode(1,16,0);
        manager.connectNode(0,2,1);
        manager.connectNode(0,3,2);
        manager.connectNode(0,4,3);
        manager.connectNode(0,5,4);
        manager.connectNode(4,7,0);
        manager.connectNode(7,15,0);
        manager.connectNode(7,17,1);
        manager.connectNode(4,8,1);
        manager.connectNode(8,9,0);
        manager.connectNode(3,10,0);
        manager.connectNode(3,11,1);
        manager.connectNode(9,12,0);
        manager.connectNode(9,13,1);
        manager.connectNode(13,14,0);

        manager.layout();
        manager.plot("testSize1", {width: 1400, height: 400});

        var graph2 = manager.plot("testSize2", {width: 1400, height: 400});
        this._plotPrediction(graph2, manager.predict(0, {x:-145, y:400}));
        this._plotPrediction(graph2, manager.predict(9, {x:-330, y:70}));
        this._plotPrediction(graph2, manager.predict(9, {x:-330, y:120}));
        this._plotPrediction(graph2, manager.predict(0, {x:15, y:20}));

        var graph3 = manager.plot("testSize3", {width: 1400, height: 400});
        this._plotPrediction(graph3, manager.predict(0, null));
        this._plotPrediction(graph3, manager.predict(9, null));
        this._plotPrediction(graph3, manager.predict(3, null));
        this._plotPrediction(graph3, manager.predict(1, null));

        manager.updateNodeSize(7, {width:80, height:120});
        manager.layout();
        manager.plot("testSize4", {width: 1400, height: 400});

        manager.updateNodeSize(7, {width:200, height:30});
        manager.layout();
        manager.plot("testSize5", {width: 1400, height: 400});
    },

    testReconnectSingleNode: function() {
        console.log("testReconnectSingleNode:");
        var position = {x:0,y:0};
        var manager = new mindplot.layout.LayoutManager(0, mindplot.layout.TestSuite.ROOT_NODE_SIZE);

        // Prepare a sample graph ...
        manager.addNode(1, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.connectNode(0, 1, 0);
        manager.layout();
        var graph = manager.plot("testReconnectSingleNode1", {width:1000, height:400});
        this._plotPrediction(graph, manager.predict(0, {x:-50, y:0}));


        manager.disconnectNode(1);
        manager.connectNode(0,1,1);
        manager.layout();
        manager.plot("testReconnectSingleNode2", {width:1000, height:400});
    },

    _plotPrediction: function(canvas, prediction) {
        var position = prediction.position;
        var order = prediction.order;
        console.log("\t\tprediction {order:" + order + ", position: (" + position.x + "," + position.y + ")}");
        var cx = position.x + canvas.width / 2 - mindplot.layout.TestSuite.NODE_SIZE.width / 2;
        var cy = position.y + canvas.height / 2 - mindplot.layout.TestSuite.NODE_SIZE.height / 2;
        canvas.rect(cx, cy, mindplot.layout.TestSuite.NODE_SIZE.width, mindplot.layout.TestSuite.NODE_SIZE.height);
    }
});

mindplot.layout.TestSuite.NODE_SIZE = {width:80, height:30},
mindplot.layout.TestSuite.ROOT_NODE_SIZE = {width:120, height:40}