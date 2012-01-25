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
mindplot.layout.BalancedTestSuite = new Class({
    Extends: mindplot.layout.TestSuite,

    initialize:function() {
        $("balancedTest").setStyle("display","block");

        this.testBalanced();
        this.testBalancedPredict();
    },

    testBalanced: function() {
        console.log("testBalanced:");
        var position = {x:0, y:0};
        var plotsize = {width:1000, height:200};
        var manager = new mindplot.layout.LayoutManager(0, mindplot.layout.TestSuite.ROOT_NODE_SIZE);

        manager.addNode(1, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.connectNode(0, 1, 0);
        manager.layout();
        manager.plot("testBalanced1", plotsize);

        manager.addNode(2, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.connectNode(0, 2, 1);
        manager.layout();
        manager.plot("testBalanced2", plotsize);

        manager.addNode(3, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.connectNode(0, 3, 2);
        manager.layout();
        manager.plot("testBalanced3", plotsize);

        manager.addNode(4, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.connectNode(0, 4, 3);
        manager.layout();
        manager.plot("testBalanced4", plotsize);

        manager.addNode(5, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.connectNode(0, 5, 4);
        manager.layout();
        manager.plot("testBalanced5", plotsize);

        manager.addNode(6, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.connectNode(0, 6, 5);
        manager.layout();
        manager.plot("testBalanced6", plotsize);

        manager.addNode(7, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(8, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(9, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.connectNode(3, 7, 0)
        manager.connectNode(7, 8, 0)
        manager.connectNode(7, 9, 1);
        manager.layout();
        manager.plot("testBalanced7", plotsize);

        manager.addNode(10, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(11, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(12, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.connectNode(6, 10, 0)
        manager.connectNode(10, 11, 0)
        manager.connectNode(10, 12, 1);
        manager.layout();
        manager.plot("testBalanced8", plotsize);

        manager.addNode(13, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.connectNode(0, 13, 4);
        manager.layout();
        manager.plot("testBalanced9", {width:1000, height:400});

        // Check orders have shifted accordingly
        $assert(manager.find(5).getOrder() == 6, "Node 5 should have order 6");

        manager.addNode(14, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.connectNode(0, 14, 5);
        manager.layout();
        manager.plot("testBalanced10", {width:1000, height:400});

        // Check orders have shifted accordingly
        $assert(manager.find(6).getOrder() == 7, "Node 6 should have order 7");

        manager.addNode(15, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.connectNode(0, 15, 4);
        manager.layout();
        manager.plot("testBalanced11", {width:1000, height:400});

        // Check orders have shifted accordingly
        $assert(manager.find(13).getOrder() == 6, "Node 13 should have order 6");
        $assert(manager.find(5).getOrder() == 8, "Node 5 should have order 8");

        manager.addNode(16, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.connectNode(0, 16, 25);
        manager.layout();
        manager.plot("testBalanced12", {width:1000, height:400});

        // Check orders have shifted accordingly
        $assert(manager.find(16).getOrder() == 9, "Node 16 should have order 9");

        manager.addNode(17, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(18, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(19, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.connectNode(0, 17, 11);
        manager.connectNode(0, 18, 13);
        manager.connectNode(0, 19, 10);
        manager.layout();
        manager.plot("testBalanced13", {width:1000, height:400});

        // Check that everything is ok
        $assert(manager.find(1).getPosition().x > manager.find(0).getPosition().x, "even order nodes must be at right of central topic");
        $assert(manager.find(3).getPosition().x > manager.find(0).getPosition().x, "even order nodes must be at right of central topic");
        $assert(manager.find(5).getPosition().x > manager.find(0).getPosition().x, "even order nodes must be at right of central topic");
        $assert(manager.find(2).getPosition().x < manager.find(0).getPosition().x, "odd order nodes must be at right of central topic");
        $assert(manager.find(4).getPosition().x < manager.find(0).getPosition().x, "odd order nodes must be at right of central topic");
        $assert(manager.find(6).getPosition().x < manager.find(0).getPosition().x, "odd order nodes must be at right of central topic");
        $assert(manager.find(7).getPosition().x > manager.find(3).getPosition().x, "children of 1st level even order nodes must be to the right");
        $assert(manager.find(8).getPosition().x > manager.find(7).getPosition().x, "children of 1st level even order nodes must be to the right");
        $assert(manager.find(9).getPosition().x > manager.find(7).getPosition().x, "children of 1st level even order nodes must be to the right");
        $assert(manager.find(10).getPosition().x < manager.find(6).getPosition().x, "children of 1st level odd order nodes must be to the left");
        $assert(manager.find(11).getPosition().x < manager.find(10).getPosition().x, "children of 1st level odd order nodes must be to the left");
        $assert(manager.find(12).getPosition().x < manager.find(10).getPosition().x, "children of 1st level odd order nodes must be to the left");

        console.log("\n");
    },

    testBalancedPredict: function() {
        console.log("testBalancedPredict");
        var position = {x:0, y:0};
        var manager = new mindplot.layout.LayoutManager(0, mindplot.layout.TestSuite.ROOT_NODE_SIZE);

        manager.addNode(1, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(2, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(3, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(4, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(5, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(7, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(8, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(9, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(10, mindplot.layout.TestSuite.NODE_SIZE, position);
        manager.addNode(11, mindplot.layout.TestSuite.NODE_SIZE, position);

        manager.connectNode(0,1,0);
        manager.connectNode(0,2,1);
        manager.connectNode(0,3,2);
        manager.connectNode(0,4,3);
        manager.connectNode(0,5,4);
        manager.connectNode(4,7,0);
        manager.connectNode(4,8,1);
        manager.connectNode(8,9,0);
        manager.connectNode(3,10,0);
        manager.connectNode(3,11,1);

        manager.layout();

        // Graph 1
        var graph1 = manager.plot("testBalancedPredict1", {width:1000, height:400});

        console.log("\tAdded as child of node 0 and dropped at (165, -70):");
        var prediction1a = manager.predict(0, {x:165, y:-70});
        this._plotPrediction(graph1, prediction1a);
        $assert(prediction1a.position.x == 130 && prediction1a.position.y == -100, "Prediction is incorrectly positioned");
        $assert(prediction1a.order == 0, "Prediction order should be 0");

        console.log("\tAdded as child of node 0 and dropped at (165, -10):");
        var prediction1b = manager.predict(0, {x:165, y:-10});
        this._plotPrediction(graph1, prediction1b);
        $assert(prediction1b.position.x == 130 && prediction1b.position.y == -30, "Prediction is incorrectly positioned");
        $assert(prediction1b.order == 2, "Prediction order should be 2");

        console.log("\tAdded as child of node 0 and dropped at (145, 15):");
        var prediction1c = manager.predict(0, {x:145, y:15});
        this._plotPrediction(graph1, prediction1c);
        $assert(prediction1c.position.x == 130 && prediction1c.position.y == 30, "Prediction is incorrectly positioned");
        $assert(prediction1c.order == 4, "Prediction order should be 4");

        console.log("\tAdded as child of node 0 and dropped at (145, 70):");
        var prediction1d = manager.predict(0, {x:145, y:70});
        this._plotPrediction(graph1, prediction1d);
        $assert(prediction1d.position.x == 130 && prediction1d.position.y == 100, "Prediction is incorrectly positioned");
        $assert(prediction1d.order == 6, "Prediction order should be 6");

        // Graph 2
        var graph2 = manager.plot("testBalancedPredict2", {width:1000, height:400});

        console.log("\tAdded as child of node 0 and dropped at (-145, -50):");
        var prediction2a = manager.predict(0, {x:-145, y:-50});
        this._plotPrediction(graph2, prediction2a);
        $assert(prediction2a.position.x == -130 && prediction2a.position.y == -80, "Prediction is incorrectly positioned");
        $assert(prediction2a.order == 1, "Prediction order should be 1");

        console.log("\tAdded as child of node 0 and dropped at (-145, -10):");
        var prediction2b = manager.predict(0, {x:-145, y:-10});
        this._plotPrediction(graph2, prediction2b);
        $assert(prediction2b.position.x == -130 && prediction2b.position.y == -10, "Prediction is incorrectly positioned");
        $assert(prediction2b.order == 3, "Prediction order should be 1");

        console.log("\tAdded as child of node 0 and dropped at (-145, 40):");
        var prediction2c = manager.predict(0, {x:-145, y:400});
        this._plotPrediction(graph2, prediction2c);
        $assert(prediction2c.position.x == -130 && prediction2c.position.y == 60, "Prediction is incorrectly positioned");
        $assert(prediction2c.order == 5, "Prediction order should be 1");

        // Graph 3
        console.log("\tPredict nodes added with no position:");
        var graph3 = manager.plot("testBalancedPredict3", {width:1000, height:400});
        var prediction3 = manager.predict(0, null);
        this._plotPrediction(graph3, prediction3);
        $assert(prediction3.position.x < manager.find(0).getPosition().x, "Prediction is incorrectly positioned");
        $assert(prediction3.order == 5, "Prediction order should be 5");

        console.log("\tPredict nodes added with no position:");
        manager.addNode(6, mindplot.layout.TestSuite.NODE_SIZE, prediction3.position);
        manager.connectNode(0,6,prediction3.order);
        manager.layout();
        var graph4 = manager.plot("testBalancedPredict4", {width:1000, height:400});
        var prediction4 = manager.predict(0, null);
        this._plotPrediction(graph4, prediction4);
        $assert(prediction4.position.x > manager.find(0).getPosition().x, "Prediction is incorrectly positioned");
        $assert(prediction4.order == 6);

        console.log("\tPredict nodes added only a root node:");
        manager.removeNode(1).removeNode(2).removeNode(3).removeNode(4).removeNode(5);
        manager.layout();
        var graph5 = manager.plot("testBalancedPredict5", {width:1000, height:400});
        var prediction5a = manager.predict(0, null);
        var prediction5b = manager.predict(0, {x: 40, y: 100});
        this._plotPrediction(graph5, prediction5a);
        this._plotPrediction(graph5, prediction5b);
        $assert(prediction5a.position.x > manager.find(0).getPosition().x && prediction5a.position.y == manager.find(0).getPosition().y, "Prediction is incorrectly positioned");
        $assert(prediction5a.order == 0, "Prediction order should be 0");
        $assert(prediction5a.position.x == prediction5b.position.x && prediction5a.position.y == prediction5b.position.y, "Both predictions should be the same");
        $assert(prediction5a.order == prediction5b.order, "Both predictions should be the same");
    }
});