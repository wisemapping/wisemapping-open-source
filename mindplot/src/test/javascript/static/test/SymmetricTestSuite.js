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
        console.log("testSymmetry:")
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
        $assert(manager.find(11).getPosition().y - manager.find(6).getPosition().y ==
            -(manager.find(12).getPosition().y - manager.find(6).getPosition().y), "Symmetry is not respected");
        $assert(manager.find(8).getPosition().y - manager.find(1).getPosition().y ==
            -(manager.find(11).getPosition().y - manager.find(1).getPosition().y), "Symmetry is not respected");
        $assert(manager.find(9).getPosition().y - manager.find(1).getPosition().y ==
            -(manager.find(11).getPosition().y - manager.find(1).getPosition().y), "Symmetry is not respected");

        console.log("OK!\n\n");
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

        // Graph
        var graph1 = manager.plot("testSymmetricPredict1", {width:1000, height:400});

        console.log("\tAdded as child of node 9 and dropped at (-280, 45):");
        var prediction1a = manager.predict(9, null, {x:-280, y:45});
        this._plotPrediction(graph1, prediction1a);
        $assert(prediction1a.position.x < manager.find(9).getPosition().x &&
            prediction1a.position.y == manager.find(9).getPosition().y, "Prediction incorrectly positioned");
        $assert(prediction1a.order == 0, "Prediction order should be 0");

        console.log("\tAdded as child of node 1 and dropped at (155, -90):");
        var prediction1b = manager.predict(1, null, {x:-155, y:-90});
        this._plotPrediction(graph1, prediction1b);
        $assert(prediction1b.position.x > manager.find(1).getPosition().x &&
            prediction1b.position.y == manager.find(1).getPosition().y, "Prediction is incorrectly positioned");
        $assert(prediction1b.order == 0, "Prediction order should be 0");

        // Graph 2
        var graph2 = manager.plot("testSymmetricPredict2", {width:1000, height:400});

        console.log("\tAdded as child of node 5 and dropped at (380, -30):");
        var prediction2d = manager.predict(5, null, {x:380, y:-30});
        this._plotPrediction(graph2, prediction2d);
        $assert(prediction2d.position.y < manager.find(7).getPosition().y &&
            prediction2d.position.x == manager.find(7).getPosition().x, "Prediction is incorrectly positioned" );
        $assert(prediction2d.order == 0, "Prediction order should be 0");

        console.log("\tAdded as child of node 5 and dropped at (375, 15):");
        var prediction2a = manager.predict(5, null, {x:375, y:15});
        this._plotPrediction(graph2, prediction2a);
        $assert(prediction2a.position.y > manager.find(7).getPosition().y &&
            prediction2a.position.y < manager.find(8).getPosition().y &&
            prediction2a.position.x == manager.find(7).getPosition().x, "Prediction is incorrectly positioned");
        $assert(prediction2a.order == 1, "Prediction order should be 1");

        console.log("\tAdded as child of node 5 and dropped at (375, 45):");
        var prediction2b = manager.predict(5, null, {x:375, y:45});
        this._plotPrediction(graph2, prediction2b);
        $assert(prediction2b.position.y > manager.find(8).getPosition().y &&
            prediction2b.position.y < manager.find(11).getPosition().y &&
            prediction2b.position.x == manager.find(7).getPosition().x, "Prediction is incorrectly positioned");
        $assert(prediction2b.order == 2, "Prediction order should be 2");

        console.log("\tAdded as child of node 5 and dropped at (375, 45):");
        var prediction2c = manager.predict(5, null, {x:375, y:65});
        this._plotPrediction(graph2, prediction2c);
        $assert(prediction2c.position.y > manager.find(11).getPosition().y &&
            prediction2c.position.x == manager.find(11).getPosition().x, "Prediction is incorrectly positioned");
        $assert(prediction2c.order == 3, "Prediction order should be 3");

        // Graph 3
        var graph3 = manager.plot("testSymmetricPredict3", {width:1000, height:400});

        console.log("\tAdded as child of node 3 and dropped at (280, 45):");
        var prediction3a = manager.predict(3, null, {x:280, y:45});
        this._plotPrediction(graph3, prediction3a);
        $assert(prediction3a.position.y > manager.find(5).getPosition().y &&
            prediction3a.position.y < manager.find(6).getPosition().y &&
            prediction3a.position.x == manager.find(5).getPosition().x, "Prediction is incorrectly positioned");
        $assert(prediction3a.order == 2, "Prediction order should be 2");

        console.log("\tAdded as child of node 3 and dropped at (255, 110):");
        var prediction3b = manager.predict(3, null, {x:255, y:110});
        this._plotPrediction(graph3, prediction3b);
        $assert(prediction3b.position.y > manager.find(6).getPosition().y &&
            prediction3b.position.x == manager.find(6).getPosition().x, "Prediction incorrectly positioned");
        $assert(prediction3b.order == 3, "Prediction order should be 3");

        // Graph 4
        console.log("\tAdded as child of node 2 and dropped at (-260, 0):");
        var graph4 = manager.plot("testSymmetricPredict4", {width:1000, height:400});
        var prediction4 = manager.predict(2, null, {x:-260, y:0});
        this._plotPrediction(graph4, prediction4);
        $assert(prediction4.position.y > manager.find(9).getPosition().y &&
            prediction4.position.y < manager.find(10).getPosition().y &&
            prediction4.position.x == manager.find(9).getPosition().x, "Prediction is incorrectly positioned");
        $assert(prediction4.order == 1, "Prediction order should be 1");

        // Graph 5
        console.log("\tPredict nodes added with no position:");
        var graph5 = manager.plot("testSymmetricPredict5", {width:1000, height:400});
        var prediction5a = manager.predict(1, null, null);
        this._plotPrediction(graph5, prediction5a);
        $assert(prediction5a.position.y == manager.find(1).getPosition().y &&
            prediction5a.position.x > manager.find(1).getPosition().x, "Prediction is incorrectly positioned");
        $assert(prediction5a.order == 0, "Prediction order should be 0");

        var prediction5b = manager.predict(2, null, null);
        this._plotPrediction(graph5, prediction5b);
        $assert(prediction5b.position.y > manager.find(10).getPosition().y &&
            prediction5b.position.x < manager.find(2).getPosition().x &&
            prediction5b.position.x == manager.find(10).getPosition().x, "Prediction is incorrectly positioned");
        $assert(prediction5b.order == 2, "Prediction order should be 2");

        var prediction5c = manager.predict(3, null, null);
        this._plotPrediction(graph5, prediction5c);
        $assert(prediction5c.position.y > manager.find(6).getPosition().y &&
            prediction5c.position.x > manager.find(3).getPosition().x &&
            prediction5c.position.x == manager.find(6).getPosition().x, "Prediction is incorrectly positioned");
        $assert(prediction5c.order == 3, "Prediction order should be 3");
        
        var prediction5d = manager.predict(10, null, null);
        this._plotPrediction(graph5, prediction5d);
        $assert(prediction5d.position.y == manager.find(10).getPosition().y &&
            prediction5d.position.x < manager.find(10).getPosition().x, "Prediction is incorrectly positioned");
        $assert(prediction5d.order == 0, "Prediction order should be 0");
    }
});