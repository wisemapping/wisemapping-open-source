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
mindplot.nlayout.TestSuite = new Class({
    Extends: mindplot.nlayout.ChildrenSorterStrategy,

    initialize:function() {
        this.testAligned();
        this.testSymmetry();
        this.testBalanced();
        this.testEvents();
        this.testEventsComplex();
        this.testDisconnect();
        this.testReconnect();
        this.testRemoveNode();
        this.testFreePosition();
    },

    testAligned: function() {

        var position = {x:0,y:0};
        var manager = new mindplot.nlayout.LayoutManager(0, mindplot.nlayout.TestSuite.ROOT_NODE_SIZE);

        manager.addNode(1, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(2, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(3, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(4, mindplot.nlayout.TestSuite.NODE_SIZE, position);
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

    testSymmetry: function() {
        var position = {x:0,y:0};
        var manager = new mindplot.nlayout.LayoutManager(0, mindplot.nlayout.TestSuite.ROOT_NODE_SIZE);

        manager.addNode(1, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(2, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(3, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(4, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(5, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(6, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(7, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(8, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(9, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(10, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(11, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(12, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(13, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(14, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.connectNode(0, 14, 0);
        manager.connectNode(14, 13, 0);
        manager.connectNode(13, 1, 0);
        manager.connectNode(13, 2, 1);
        manager.connectNode(13, 3, 2);
        manager.connectNode(13, 4, 3);
        manager.connectNode(13, 5, 4);
        manager.connectNode(1, 6, 0);
        manager.connectNode(1, 7, 1);
        manager.connectNode(7, 8, 0);
        manager.connectNode(8, 9, 0);
        manager.connectNode(5, 10, 0);
        manager.connectNode(6, 11, 0);
        manager.connectNode(6, 12, 1);

        manager.layout();
        manager.plot("testSymmetry",{width:1600, height:400});

        // All nodes should be positioned symmetrically with respect to their common ancestors
        $assert(manager.find(14).getPosition().y == -manager.find(13).getPosition().y, "Symmetry is not respected");
        $assert(manager.find(5).getPosition().y == -manager.find(11).getPosition().y, "Symmetry is not respected");
        $assert(manager.find(11).getPosition().y - manager.find(6).getPosition().y == -(manager.find(12).getPosition().y - manager.find(6).getPosition().y), "Symmetry is not respected");
        $assert(manager.find(8).getPosition().y - manager.find(1).getPosition().y == -(manager.find(11).getPosition().y - manager.find(1).getPosition().y), "Symmetry is not respected");
        $assert(manager.find(9).getPosition().y - manager.find(1).getPosition().y == -(manager.find(11).getPosition().y - manager.find(1).getPosition().y), "Symmetry is not respected");
    },

    testBalanced: function() {
        var position = {x:0, y:0};
        var plotsize = {width:1000, height:200};
        var manager = new mindplot.nlayout.LayoutManager(0, mindplot.nlayout.TestSuite.ROOT_NODE_SIZE);

        manager.addNode(1, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.connectNode(0, 1, 0);
        manager.layout();
        manager.plot("testBalanced1", plotsize);

        manager.addNode(2, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.connectNode(0, 2, 1);
        manager.layout();
        manager.plot("testBalanced2", plotsize);

        manager.addNode(3, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.connectNode(0, 3, 2);
        manager.layout();
        manager.plot("testBalanced3", plotsize);

        manager.addNode(4, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.connectNode(0, 4, 3);
        manager.layout();
        manager.plot("testBalanced4", plotsize);

        manager.addNode(5, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.connectNode(0, 5, 4);
        manager.layout();
        manager.plot("testBalanced5", plotsize);

        manager.addNode(6, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.connectNode(0, 6, 5);
        manager.layout();
        manager.plot("testBalanced6", plotsize);

        manager.addNode(7, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(8, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(9, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.connectNode(3, 7, 0)
        manager.connectNode(7, 8, 0)
        manager.connectNode(7, 9, 1);
        manager.layout();
        manager.plot("testBalanced7", plotsize);

        manager.addNode(10, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(11, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(12, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.connectNode(6, 10, 0)
        manager.connectNode(10, 11, 0)
        manager.connectNode(10, 12, 1);
        manager.layout();
        manager.plot("testBalanced8", plotsize);

        $assert(manager.find(1).getPosition().x > 0, "even order nodes must be at right of central topic");
        $assert(manager.find(3).getPosition().x > 0, "even order nodes must be at right of central topic");
        $assert(manager.find(5).getPosition().x > 0, "even order nodes must be at right of central topic");

        $assert(manager.find(2).getPosition().x < 0, "odd order nodes must be at right of central topic");
        $assert(manager.find(4).getPosition().x < 0, "odd order nodes must be at right of central topic");
        $assert(manager.find(6).getPosition().x < 0, "odd order nodes must be at right of central topic");

        $assert(manager.find(7).getPosition().x > manager.find(3).getPosition().x, "children of 1st level even order nodes must be to the right");
        $assert(manager.find(8).getPosition().x > manager.find(7).getPosition().x, "children of 1st level even order nodes must be to the right");
        $assert(manager.find(9).getPosition().x > manager.find(7).getPosition().x, "children of 1st level even order nodes must be to the right");

        $assert(manager.find(10).getPosition().x < manager.find(6).getPosition().x, "children of 1st level odd order nodes must be to the left");
        $assert(manager.find(11).getPosition().x < manager.find(10).getPosition().x, "children of 1st level odd order nodes must be to the left");
        $assert(manager.find(12).getPosition().x < manager.find(10).getPosition().x, "children of 1st level odd order nodes must be to the left");
    },

    testEvents: function() {
        console.log("testEvents:");
        var position = {x:0,y:0};
        var manager = new mindplot.nlayout.LayoutManager(0, mindplot.nlayout.TestSuite.ROOT_NODE_SIZE);

        // Add 3 nodes...
        manager.addNode(1, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(2, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(3, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(4, mindplot.nlayout.TestSuite.NODE_SIZE, {x:0, y: 60});

        // Now connect one with two....
        manager.connectNode(0, 1, 0);
        manager.connectNode(0, 2, 0);
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
        var manager = new mindplot.nlayout.LayoutManager(0, mindplot.nlayout.TestSuite.ROOT_NODE_SIZE);

        // Add 3 nodes...
        manager.addNode(1, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(2, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(3, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(4, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(5, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(6, mindplot.nlayout.TestSuite.NODE_SIZE, {x:0, y:60});

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
        $assert(events.length == 6, "Only 4 nodes should be repositioned.");

        console.log("\n");
    },

    testDisconnect: function() {
        console.log("testDisconnect:");
        var position = {x:0,y:0};
        var manager = new mindplot.nlayout.LayoutManager(0, mindplot.nlayout.TestSuite.ROOT_NODE_SIZE);

        // Prepare a sample graph ...
        manager.addNode(1, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(2, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(3, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(4, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(5, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(6, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(7, mindplot.nlayout.TestSuite.NODE_SIZE, position);

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

        $assert(events.some(function(event) {return event.getId() == 2;}), "Event for disconnected node seems not to be propagated");
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
        var manager = new mindplot.nlayout.LayoutManager(0, mindplot.nlayout.TestSuite.ROOT_NODE_SIZE);

        manager.addNode(1, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(2, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(3, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(4, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(5, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(6, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(7, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(8, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(9, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(10, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(11, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(12, mindplot.nlayout.TestSuite.NODE_SIZE, position);
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
        var manager = new mindplot.nlayout.LayoutManager(0, mindplot.nlayout.TestSuite.ROOT_NODE_SIZE);

        // Prepare a sample graph ...
        manager.addNode(1, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(2, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(3, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(4, mindplot.nlayout.TestSuite.NODE_SIZE, position);

        manager.connectNode(0, 1, 0);
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

        $assert(manager.find(1).getPosition().y == manager.find(2).getPosition().y, "After removal of node 3, nodes 1 and 2 should be alingned");
        console.log("\n");
    },

    testFreePosition: function() {
        var position = {x:0,y:0};
        var manager = new mindplot.nlayout.LayoutManager(0, mindplot.nlayout.TestSuite.ROOT_NODE_SIZE);

        // Prepare a sample graph ...
        manager.addNode(1, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(2, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(3, mindplot.nlayout.TestSuite.NODE_SIZE, position);
        manager.addNode(4, mindplot.nlayout.TestSuite.NODE_SIZE, position);

        manager.connectNode(0, 4, 0);
        manager.connectNode(4, 1, 0);
        manager.connectNode(4, 2, 1);
        manager.connectNode(4, 3, 2);

        manager.layout();
        manager.plot("testFreePosition", {width:800, height:400});

        //TODO(gb): make asserts
    }
});

mindplot.nlayout.TestSuite.NODE_SIZE = {width:80, height:30},
mindplot.nlayout.TestSuite.ROOT_NODE_SIZE = {width:120, height:40}

