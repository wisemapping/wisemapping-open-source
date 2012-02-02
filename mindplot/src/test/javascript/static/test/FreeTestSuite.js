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
mindplot.layout.FreeTestSuite = new Class({
    Extends: mindplot.layout.TestSuite,

    initialize:function() {
        $("freeTest").setStyle("display","block");

        this.testFreePosition();
        this.testFreePredict();
        this.testReconnectFreeNode();
        this.testSiblingOverlapping();
        this.testRootNodeChildrenPositioning();
    },

    testFreePosition: function() {
        console.log("testFreePosition:");
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
        manager.addNode(10, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(11, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(12, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(13, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(14, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(15, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(16, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(17, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(18, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(19, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(20, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(21, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(22, mindplot.layout.TestSuite.NODE_SIZE, position);

        manager.connectNode(0,1,0).connectNode(0,2,1).connectNode(0,3,2).connectNode(0,4,3);
        manager.connectNode(4,21,0).connectNode(4,22,0);
        manager.connectNode(1,5,0);
        manager.connectNode(5,6,0).connectNode(6,8,0).connectNode(8,9,0);
        manager.connectNode(5,7,1).connectNode(7,10,0);
        manager.connectNode(3,11,0).connectNode(11,14,0).connectNode(14,18,0).connectNode(14,19,1).connectNode(14,20,2);
        manager.connectNode(3,12,1).connectNode(12,15,0).connectNode(12,16,1).connectNode(12,17,2);
        manager.connectNode(3,13,2);

        manager.layout();
        manager.plot("testFreePosition1", {width:1400, height:600});

        console.log("\tmove node 12 to (300,30):");
        manager.moveNode(12, {x:300, y:30});
        manager.layout(true);
        manager.plot("testFreePosition2", {width:1400, height:600});
        this._assertFreePosition(manager, 12, {x:300, y:30})

        console.log("\tmove node 13 to (340,180):");
        var node13Pos = {x:340, y:180};
        manager.moveNode(13, node13Pos);
        manager.layout(true);
        manager.plot("testFreePosition3", {width:1400, height:600});
        this._assertFreePosition(manager, 13, node13Pos);

        console.log("\tmove node 11 to (250,-50):");
        manager.moveNode(11, {x:250, y:-50});
        manager.layout(true);
        manager.plot("testFreePosition4", {width:1400, height:600});
        this._assertFreePosition(manager, 11, {x:250, y:-50});
        $assert(manager.find(13).getPosition().x == node13Pos.x && manager.find(13).getPosition().y == node13Pos.y,
            "Node 13 shouldn't have moved");

        console.log("\tmove node 7 to (350,-190):");
        manager.moveNode(7, {x:350, y:-190});
        manager.layout(true);
        manager.plot("testFreePosition5", {width:1400, height:600});
        this._assertFreePosition(manager, 7, {x:350, y:-190});

        console.log("\tadd node 23 to 12:");
        manager.addNode(23, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.connectNode(12,23,3);
        manager.layout(true);
        manager.plot("testFreePosition6", {width:1400, height:600});
        this._assertFreePosition(manager, null, null);

        console.log("\tmove node 4 to (-300, 190):");
        manager.moveNode(4, {x:-300, y:190});
        manager.layout(true);
        manager.plot("testFreePosition7", {width:1400, height:600});
        this._assertFreePosition(manager, 4, {x:-300, y:190});

        console.log("\tadd node 24 to 3:");
        manager.addNode(24, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.connectNode(3,24,3);
        manager.layout(true);
        manager.plot("testFreePosition8", {width:1400, height:600});
        this._assertFreePosition(manager, null, null);

        console.log("\tadd node 25 to 17:");
        manager.addNode(25, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.connectNode(17,25,0);
        manager.layout(true);
        manager.plot("testFreePosition9", {width:1400, height:600});
        this._assertFreePosition(manager, null, null);

        console.log("OK!\n\n");
    },

    testFreePredict: function() {
        console.log("testFreePredict:");
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
        manager.addNode(10, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(11, mindplot.layout.TestSuite.NODE_SIZE, position);

        manager.connectNode(0, 1, 0);
        manager.connectNode(0, 2, 1);
        manager.connectNode(0, 3, 2);
        manager.connectNode(3, 4, 0);
        manager.connectNode(3, 5, 1);
        manager.connectNode(3, 6, 2);
        manager.connectNode(5, 7, 0);
        manager.connectNode(5, 8, 1);
        manager.connectNode(5, 11, 2);
        manager.connectNode(2, 9, 0);
        manager.connectNode(2, 10, 1);

        manager.layout();
        var graph = manager.plot("testFreePredict1", {width:1000, height:400});

        var pos1 = {x: 370, y:80};
        var predict1 = manager.predict(5, 11, pos1, true);
        this._plotPrediction(graph, predict1);
        $assert(predict1.position.x == pos1.x && predict1.position.y == pos1.y, "free predict should return the same position");

        var pos2 = {x: -200, y:80};
        var predict2 = manager.predict(0, 2, pos2, true);
        this._plotPrediction(graph, predict2);
        $assert(predict2.position.x == pos2.x && predict2.position.y == pos2.y, "free predict should return the same position");

        var pos3 = {x: 200, y:30};
        var node5 = manager.find(5);
        var predict3 = manager.predict(3, 5, pos3, true);
        this._plotPrediction(graph, predict3);
        $assert(predict3.position.x == node5.getPosition().x && predict3.position.y == pos3.y, "free predict should return the x-coordinate of the node");

        var pos4 = {x: -100, y:45};
        var node10 = manager.find(10);
        var predict4 = manager.predict(2, 10, pos4, true);
        this._plotPrediction(graph, predict4);
        $assert(predict4.position.x == node10.getPosition().x && predict4.position.y == pos4.y, "free predict should return the x-coordinate of the node");

        console.log("OK!\n\n");
    },

    testReconnectFreeNode: function() {
        console.log("testReconnectFreeNode:");
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
        manager.addNode(10, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(11, mindplot.layout.TestSuite.NODE_SIZE, position);

        manager.connectNode(0, 1, 0);
        manager.connectNode(0, 2, 1);
        manager.connectNode(0, 3, 2);
        manager.connectNode(3, 4, 0);
        manager.connectNode(3, 5, 1);
        manager.connectNode(3, 6, 2);
        manager.connectNode(5, 7, 0);
        manager.connectNode(5, 8, 1);
        manager.connectNode(5, 11, 2);
        manager.connectNode(2, 9, 0);
        manager.connectNode(2, 10, 1);

        manager.layout();
        manager.plot("testReconnectFreeNode1", {width:1000, height:400});

        console.log("\tmove node 5");
        manager.moveNode(5, {x:250, y:30});
        manager.layout();
        manager.plot("testReconnectFreeNode2", {width:1000, height:400});
        this._assertFreePosition(manager, 5, {x:250, y:30});

        console.log("\treconnect node 5 to node 2");
        manager.disconnectNode(5);
        manager.connectNode(2,5,2);
        manager.layout();
        manager.plot("testReconnectFreeNode3", {width:1000, height:400});
        $assert(manager.find(5).getPosition().y > manager.find(10).getPosition().y &&
            manager.find(5).getPosition().x == manager.find(10).getPosition().x, "Node 5 is incorrectly positioned"
        );
        $assert(manager.find(5).getOrder() == 2, "Node 5 should have order 2");

        console.log("\tmove node 8");
        manager.moveNode(8, {x:-370, y:60});
        manager.layout();
        manager.plot("testReconnectFreeNode4", {width:1000, height:400});
        this._assertFreePosition(manager, 8, {x:-370, y:60});

        console.log("\treconnect node 5 to node 10");
        manager.disconnectNode(5);
        manager.connectNode(10,5,0);
        manager.layout();
        manager.plot("testReconnectFreeNode5", {width:1000, height:400});
        $assert(manager.find(5).getPosition().y == manager.find(10).getPosition().y &&
            manager.find(5).getPosition().x < manager.find(10).getPosition().x, "Node 5 is incorrectly positioned"
        );
        $assert(manager.find(5).getOrder() == 0, "Node 5 should have order 0");

        console.log("reconnect node 5 to node 3");
        manager.disconnectNode(5);
        manager.connectNode(3,5,2);
        manager.layout();
        manager.plot("testReconnectFreeNode6", {width:1000, height:400});
        $assert(manager.find(5).getPosition().y > manager.find(6).getPosition().y &&
            manager.find(5).getPosition().x == manager.find(6).getPosition().x, "Node 5 is incorrectly positioned"
        );
        $assert(manager.find(5).getOrder() == 2, "Node 5 should have order 2");

        console.log("\tmove node 8");
        manager.moveNode(8, {x:370, y:30});
        manager.layout();
        manager.plot("testReconnectFreeNode7", {width:1000, height:400});
        this._assertFreePosition(manager, 8, {x:370, y:30});

        console.log("OK!\n\n");
    },

    testSiblingOverlapping: function() {
        console.log("testSiblingOverlapping:");
        var position = {x:0,y:0};
        var manager = new mindplot.layout.LayoutManager(0, mindplot.layout.TestSuite.ROOT_NODE_SIZE);

        // Prepare a sample graph ...
        manager.addNode(1, mindplot.layout.TestSuite.NODE_SIZE, position).connectNode(0,1,0);
        manager.addNode(2, mindplot.layout.TestSuite.NODE_SIZE, position).connectNode(1,2,0);
        manager.addNode(3, mindplot.layout.TestSuite.NODE_SIZE, position).connectNode(1,3,1);
        manager.addNode(4, mindplot.layout.TestSuite.NODE_SIZE, position).connectNode(1,4,2);
        manager.addNode(5, mindplot.layout.TestSuite.NODE_SIZE, position).connectNode(1,5,3);
        manager.addNode(6, mindplot.layout.TestSuite.NODE_SIZE, position).connectNode(1,6,4);
        manager.addNode(7, mindplot.layout.TestSuite.NODE_SIZE, position).connectNode(1,7,5);
        manager.addNode(8, mindplot.layout.TestSuite.NODE_SIZE, position).connectNode(1,8,6);
        manager.addNode(9, mindplot.layout.TestSuite.NODE_SIZE, position).connectNode(0,9,4);
        manager.layout();
        manager.plot("testSiblingOverlapping1", {width:800, height:600});

        console.log("\tmove node 2");
        manager.moveNode(2, {x:250, y: -30});
        manager.layout();
        manager.plot("testSiblingOverlapping2", {width:800, height:600});
        this._assertFreePosition(manager, 2, {x:250, y: -30});

        console.log("\tmove node 7");
        manager.moveNode(7, {x:250, y: 100});
        manager.layout();
        manager.plot("testSiblingOverlapping3", {width:800, height:600});
        this._assertFreePosition(manager, 7, {x:250, y: 100});

        console.log("OK!\n\n");
    },

    testRootNodeChildrenPositioning: function() {
        console.log("testRootNodeChildrenPositioning:");
        var position = {x:0,y:0};
        var manager = new mindplot.layout.LayoutManager(0, mindplot.layout.TestSuite.ROOT_NODE_SIZE);

        // Prepare a sample graph ...
        manager.addNode(1, mindplot.layout.TestSuite.NODE_SIZE, position).connectNode(0,1,0);
        manager.addNode(2, mindplot.layout.TestSuite.NODE_SIZE, position).connectNode(0,2,1);
        manager.addNode(3, mindplot.layout.TestSuite.NODE_SIZE, position).connectNode(0,3,2);
        manager.addNode(4, mindplot.layout.TestSuite.NODE_SIZE, position).connectNode(0,4,3);
        manager.addNode(5, mindplot.layout.TestSuite.NODE_SIZE, position).connectNode(0,5,4);
        manager.addNode(6, mindplot.layout.TestSuite.NODE_SIZE, position).connectNode(0,6,5);
        manager.layout();
        manager.plot("testRootNodeChildrenPositioning1", {width:800, height:600});

        console.log("\tmove node 1");
        manager.moveNode(1, {x:150, y:0});
        manager.layout();
        manager.plot("testRootNodeChildrenPositioning2", {width:800, height:600});
        this._assertFreePosition(manager, 1, {x:150, y:0});

        console.log("\tmove node 4");
        manager.moveNode(4, {x:-140, y:30});
        manager.layout();
        manager.plot("testRootNodeChildrenPositioning3", {width:800, height:600});
        this._assertFreePosition(manager, 4, {x:-140, y:30});

        console.log("\tmove node 2");
        manager.moveNode(2, {x:-150, y:-50});
        manager.layout();
        manager.plot("testRootNodeChildrenPositioning4", {width:800, height:600});
        this._assertFreePosition(manager, 2, {x:-150, y:-50});

        //TODO(gb): fix this. It's not working
//        console.log("\tmove node 6");
//        manager.moveNode(6, {x:-150, y:-50});
//        manager.layout();
//        manager.plot("testRootNodeChildrenPositioning5", {width:800, height:600});
//        this._assertFreePosition(manager, 6, {x:-150, y:-50});

        console.log("OK!\n\n");
    },

    _assertFreePosition: function(manager, id, position) {
        if (id != null && position.x != null && position.y != null) {
            var node = manager.find(id);
            $assert(node.getPosition().x == position.x && node.getPosition().y == position.y,
                "Freely moved node " + id + " is not left at free position (" + position.x + "," + position.y + "). " +
                    "Actual position: (" + node.getPosition().x + "," + node.getPosition().y + ")");
        }

        var treeSet = manager._treeSet;
        treeSet._rootNodes.forEach(function(rootNode) {
            var heightById = rootNode.getSorter().computeChildrenIdByHeights(treeSet, rootNode);
            this._assertBranchCollision(treeSet, rootNode, heightById);
        }, this);
    },

    _assertBranchCollision: function(treeSet, node, heightById) {
        var children = treeSet.getChildren(node);
        var childOfRootNode = treeSet._rootNodes.contains(node);

        children.forEach(function(child) {
            var height = heightById[child.getId()];
            var siblings = treeSet.getSiblings(child);
            if (childOfRootNode) {
                siblings = siblings.filter(function(sibling) {
                    return (child.getOrder() % 2) == (sibling.getOrder() % 2);
                })
            }
            siblings.forEach(function(sibling) {
                this._branchesOverlap(child, sibling, heightById);
            }, this);
        }, this);

        children.forEach(function(child) {
            this._assertBranchCollision(treeSet, child, heightById);
        }, this)
    },

    _branchesOverlap: function(branchA, branchB, heightById) {
        var topA = branchA.getPosition().y - heightById[branchA.getId()]/2;
        var bottomA = branchA.getPosition().y + heightById[branchA.getId()]/2;
        var topB = branchB.getPosition().y - heightById[branchB.getId()]/2;
        var bottomB = branchB.getPosition().y + heightById[branchB.getId()]/2;

        $assert(topA >= bottomB || bottomA <= topB, "Branches " + branchA.getId() + " and " +  branchB.getId() + " overlap");
    }
});