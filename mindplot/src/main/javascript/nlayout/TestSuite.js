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
        this.testGrid();
        this.testEvents();
        this.testEventsComplex();
        this.testDisconnect();
        this.testReconnect();
        this.testRemoveNode();
        this.testFreePosition();
    },

    testAligned: function() {

        var size = {width:25,height:25};
        var position = {x:0,y:0};
        var manager = new mindplot.nlayout.LayoutManager(0, size);

        manager.addNode(1, size, position);
        manager.addNode(2, size, position);
        manager.addNode(3, size, position);
        manager.addNode(4, size, position);
        manager.connectNode(0, 1, 0);
        manager.connectNode(1, 2, 0);
        manager.connectNode(2, 3, 0);
        manager.connectNode(3, 4, 0);

        manager.layout();
        manager.dump();
        manager.plot("testAligned", {width:300,height:200});

        // All nodes should be vertically aligned
        $assert(manager.find(0).getPosition().y == manager.find(1).getPosition().y, "Nodes are not aligned");
        $assert(manager.find(0).getPosition().y == manager.find(2).getPosition().y, "Nodes are not aligned");
        $assert(manager.find(0).getPosition().y == manager.find(3).getPosition().y, "Nodes are not aligned");
        $assert(manager.find(0).getPosition().y == manager.find(4).getPosition().y, "Nodes are not aligned");
    },

    testSymmetry: function() {
        var size = {width:25,height:25};
        var position = {x:0,y:0};
        var manager = new mindplot.nlayout.LayoutManager(0, size);

        manager.addNode(1, size, position);
        manager.addNode(2, size, position);
        manager.addNode(3, size, position);
        manager.addNode(4, size, position);
        manager.addNode(5, size, position);
        manager.addNode(6, size, position);
        manager.addNode(7, size, position);
        manager.addNode(8, size, position);
        manager.addNode(9, size, position);
        manager.addNode(10, size, position);
        manager.addNode(11, size, position);
        manager.addNode(12, size, position);
        manager.addNode(13, size, position);
        manager.addNode(14, size, position);
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
        manager.dump();
        manager.plot("testSymmetry",{width:500, height:300});

        //TODO(gb): make asserts
    },

    testBalanced: function() {
        var size = {width:80, height:30};
        var plotsize = {width: 800, height:400};
        var position = {x:0, y:0};
        var manager = new mindplot.nlayout.LayoutManager(0, {width:120, height:40});

        manager.addNode(1, size, position);
        manager.connectNode(0, 1, 0);
        manager.layout();
        manager.plot("testBalanced1", plotsize);

        manager.addNode(2, size, position);
        manager.connectNode(0, 2, 1);
        manager.layout();
        manager.plot("testBalanced2", plotsize);

        manager.addNode(3, size, position);
        manager.connectNode(0, 3, 2);
        manager.layout();
        manager.plot("testBalanced3", plotsize);

        manager.addNode(4, size, position);
        manager.connectNode(0, 4, 3);
        manager.layout();
        manager.plot("testBalanced4", plotsize);

        manager.addNode(5, size, position);
        manager.connectNode(0, 5, 4);
        manager.layout();
        manager.plot("testBalanced5", plotsize);

        manager.addNode(6, size, position);
        manager.connectNode(0, 6, 5);
        manager.layout();
        manager.plot("testBalanced6", plotsize);

        manager.addNode(7, size, position);
        manager.addNode(8, size, position);
        manager.addNode(9, size, position);
        manager.connectNode(3, 7, 0)
        manager.connectNode(7, 8, 0)
        manager.connectNode(7, 9, 1);
        manager.layout();
        manager.plot("testBalanced7", plotsize);

        manager.addNode(10, size, position);
        manager.addNode(11, size, position);
        manager.addNode(12, size, position);
        manager.connectNode(6, 10, 0)
        manager.connectNode(10, 11, 0)
        manager.connectNode(10, 12, 1);
        manager.layout();
        manager.plot("testBalanced8", plotsize);
    },

    testGrid: function() {
        var size = {width:25,height:25};
        var position = {x:0,y:0};
        var manager = new mindplot.nlayout.LayoutManager(0, size);

        manager.addNode(1, size, position);
        manager.connectNode(0, 1, 0);
        manager.layout();
        manager.plot("testGrid1");

        manager.addNode(2, size, position);
        manager.connectNode(0, 2, 1);
        manager.layout();
        manager.plot("testGrid2");

        manager.addNode(3, size, position);
        manager.connectNode(0, 3, 2);
        manager.layout();
        manager.plot("testGrid3");

        manager.addNode(4, size, position);
        manager.connectNode(0, 4, 3);
        manager.layout();
        manager.plot("testGrid4");

        manager.addNode(5, size, position);
        manager.addNode(6, size, position);
        manager.addNode(7, size, position);
        manager.connectNode(2, 5, 0);
        manager.connectNode(2, 6, 1);
        manager.connectNode(6, 7, 0);
        manager.layout();
        manager.plot("testGrid5", {width:300, height:300});

        manager.dump();

        //TODO(gb): make asserts
    },

    testEvents: function() {
        var size = {width:25,height:25};
        var position = {x:0,y:0};
        var manager = new mindplot.nlayout.LayoutManager(0, size);

        // Add 3 nodes...
        manager.addNode(1, size, position);
        manager.addNode(2, size, position);
        manager.addNode(3, size, position);
        manager.addNode(4, size, position);

        // Now connect one with two....
        manager.connectNode(0, 1, 0);
        manager.connectNode(0, 2, 0);
        manager.connectNode(1, 3, 0);

        // Basic layout repositioning ...
        console.log("-- Updated tree ---");
        var events = [];
        manager.addEvent('change', function(event) {
            console.log("Updated nodes: {id:" + event.getId() + ", order: " + event.getOrder() + ",position: {" + event.getPosition().x + "," + event.getPosition().y + "}");
            events.push(event);
        });
        manager.layout(true);
        manager.dump();
        manager.plot("testEvents1");

        // Ok, if a new node is added, this an event should be fired  ...
        console.log("---- Layout without changes should not affect the tree  ---");
        events.empty();
        manager.layout(true);
        manager.plot("testEvents2");

        $assert(events.length == 0, "Unnecessary tree updated.");
    },

    testEventsComplex: function() {
        var size = {width:25,height:25};
        var position = {x:0,y:0};
        var manager = new mindplot.nlayout.LayoutManager(0, size);

        // Add 3 nodes...
        manager.addNode(1, size, position);
        manager.addNode(2, size, position);
        manager.addNode(3, size, position);
        manager.addNode(4, size, position);

        // Now connect one with two....
        manager.connectNode(0, 1, 0);
        manager.connectNode(1, 2, 0);
        manager.connectNode(1, 3, 1);

        var events = [];
        manager.addEvent('change', function(event) {
            console.log("Updated nodes: {id:" + event.getId() + ", order: " + event.getOrder() + ",position: {" + event.getPosition().x + "," + event.getPosition().y + "}");
            events.push(event);
        });

        // Reposition ...
        manager.layout(true);
        manager.dump();
        manager.plot("testEventsComplex1");

        // Add a new node and connect. Only children nodes should be affected.
        console.log("---- Connect a new node  ---");

        events.empty();
        manager.connectNode(1, 4, 2);
        manager.layout(true);
        manager.dump();
        manager.plot("testEventsComplex2");

        // @todo: This seems no to be ok...
        $assert(events.length == 4, "Only 3 nodes should be repositioned.");
    },

    testDisconnect: function() {
        var size = {width:25,height:25};
        var position = {x:0,y:0};
        var manager = new mindplot.nlayout.LayoutManager(0, size);

        // Prepare a sample graph ...
        manager.addNode(1, size, position);
        manager.addNode(2, size, position);
        manager.addNode(3, size, position);
        manager.addNode(4, size, position);

        manager.connectNode(0, 1, 0);
        manager.connectNode(1, 2, 0);
        manager.connectNode(1, 3, 1);
        manager.connectNode(3, 4, 0);

        var events = [];
        manager.addEvent('change', function(event) {

            var pos = event.getPosition();
            var posStr = pos ? ",position: {" + pos.x + "," + pos.y : "";
            var node = manager.find(event.getId());
            console.log("Updated nodes: {id:" + event.getId() + ", order: " + event.getOrder() + posStr + "}");
            events.push(event);
        });
        manager.layout(true);
        manager.dump();
        manager.plot("testDisconnect1", {width:300, height:200});

        // Now, disconnect one node ...
        console.log("--- Disconnect a single node ---");
        events.empty();
        manager.disconnectNode(2);
        manager.layout(true);
        manager.dump();
        manager.plot("testDisconnect2", {width:300, height:200});

        $assert(events.some(
            function(event) {
                return event.getId() == 2;
            }), "Event for disconnected node seems not to be propagated");

        // Great, let's disconnect a not with children.
        console.log("--- Disconnect a node with children ---");
        manager.disconnectNode(3);
        manager.layout(true);
        manager.dump();
        manager.plot("testDisconnect3", {width:300, height:200});

        $assert(events.some(
            function(event) {
                return event.getId() == 2;
            }), "Event for disconnected node seems not to be propagated");
    },

    testReconnect: function() {
        var size = {width:25,height:25};
        var position = {x:0,y:0};
        var manager = new mindplot.nlayout.LayoutManager(0, size);

        manager.addNode(1, size, position);
        manager.addNode(2, size, position);
        manager.addNode(3, size, position);
        manager.addNode(4, size, position);
        manager.addNode(5, size, position);
        manager.addNode(6, size, position);
        manager.addNode(7, size, position);
        manager.addNode(8, size, position);
        manager.addNode(9, size, position);
        manager.addNode(10, size, position);
        manager.addNode(11, size, position);
        manager.addNode(12, size, position);
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
        manager.dump();
        manager.plot("testReconnect1",{width:400, height:300});

        // Reconnect node 6 to node 4
        manager.disconnectNode(6);
        manager.connectNode(4,6,0);
        manager.layout();
        manager.dump();
        manager.plot("testReconnect2",{width:400, height:300});

        //TODO(gb): make asserts
    },

    testRemoveNode: function() {
        var size = {width:25,height:25};
        var position = {x:0,y:0};
        var manager = new mindplot.nlayout.LayoutManager(0, size);

        // Prepare a sample graph ...
        manager.addNode(1, size, position);
        manager.addNode(2, size, position);
        manager.addNode(3, size, position);
        manager.addNode(4, size, position);

        manager.connectNode(0, 1, 0);
        manager.connectNode(1, 2, 0);
        manager.connectNode(1, 3, 1);
        manager.connectNode(3, 4, 0);

        var events = [];
        manager.addEvent('change', function(event) {
            var pos = event.getPosition();
            var posStr = pos ? ",position: {" + pos.x + "," + event.getPosition().y : "";
            console.log("Updated nodes: {id:" + event.getId() + ", order: " + event.getOrder() + posStr + "}");
            events.push(event);
        });
        manager.layout(true);
        manager.dump();
        manager.plot("testRemoveNode1", {width:300, height:200});

        // Test removal of a connected node ...
        console.log("--- Remove node 3  ---");
        manager.removeNode(3);
        manager.layout(true);
        manager.dump();
        manager.plot("testRemoveNode2");
    },

    testFreePosition: function() {
        var size = {width:25,height:25};
        var position = {x:0,y:0};
        var manager = new mindplot.nlayout.LayoutManager(0, size);

        // Prepare a sample graph ...
        manager.addNode(1, size, position);
        manager.addNode(2, size, position);
        manager.addNode(3, size, position);
        manager.addNode(4, size, position);

        manager.connectNode(0, 4, 0);
        manager.connectNode(4, 1, 0);
        manager.connectNode(4, 2, 1);
        manager.connectNode(4, 3, 2);

        manager.layout();
        manager.plot("testFreePosition");
    }


});

