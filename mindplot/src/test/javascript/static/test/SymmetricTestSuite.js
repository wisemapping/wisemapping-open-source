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
mindplot.layout.SymmetricTestSuite = new Class({
    Extends: mindplot.layout.TestSuite,

    initialize:function() {
        $("symmetricTest").setStyle("display","block");

        this.testSymmetry();
        this.testSymmetricPredict();
    },

    testSymmetry: function() {
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
        manager.addNode(13, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(14, mindplot.layout.TestSuite.NODE_SIZE, position);
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

    testSymmetricPredict: function() {
        console.log("testSymmetricPredict:");
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

        console.log("\tAdded as child of node 9 and dropped at (-280, 45):");
        var graph1 = manager.plot("testSymmetricPredict1", {width:1000, height:400});
        this._plotPrediction(graph1, manager.predict(9, {x:-280, y:45}));
        console.log("\tAdded as child of node 1 and dropped at (155, -90):");
        this._plotPrediction(graph1, manager.predict(1, {x:-155, y:-90}));

        console.log("\tAdded as child of node 5 and dropped at (375, 15):");
        var graph2 = manager.plot("testSymmetricPredict2", {width:1000, height:400});
        this._plotPrediction(graph2, manager.predict(5, {x:375, y:15}));
        console.log("\tAdded as child of node 5 and dropped at (375, 45):");
        this._plotPrediction(graph2, manager.predict(5, {x:375, y:45}));
        console.log("\tAdded as child of node 5 and dropped at (375, 45):");
        this._plotPrediction(graph2, manager.predict(5, {x:375, y:65}));
        console.log("\tAdded as child of node 5 and dropped at (380, -30):");
        this._plotPrediction(graph2, manager.predict(5, {x:380, y:-30}));

        console.log("\tAdded as child of node 3 and dropped at (280, 45):");
        var graph3 = manager.plot("testSymmetricPredict3", {width:1000, height:400});
        this._plotPrediction(graph3, manager.predict(3, {x:280, y:45}));
        console.log("\tAdded as child of node 3 and dropped at (255, 110):");
        this._plotPrediction(graph3, manager.predict(3, {x:255, y:110}));
        console.log("\tAdded as child of node 2 and dropped at (-260, 0):");

        var graph4 = manager.plot("testSymmetricPredict4", {width:1000, height:400});
        this._plotPrediction(graph4, manager.predict(2, {x:-260, y:0}));

        console.log("\tPredict nodes added with no position:");
        var graph5 = manager.plot("testSymmetricPredict5", {width:1000, height:400});
        this._plotPrediction(graph5, manager.predict(1, null));
        this._plotPrediction(graph5, manager.predict(2, null));
        this._plotPrediction(graph5, manager.predict(3, null));
        this._plotPrediction(graph5, manager.predict(10, null));
    }
});