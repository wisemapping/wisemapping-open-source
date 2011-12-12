mindplot.nlayout.TestSuite = new Class({
    Extends: mindplot.nlayout.ChildrenSorterStrategy,
    initialize:function() {

        this.testAligned();
        this.testEvents();
        this.testEventsComplex();
        this.testDisconnect();
        this.testRemoveNode();
    },

    testAligned: function() {

        var size = {width:30,height:30};
        var position = {x:0,y:0};
        var manager = new mindplot.nlayout.LayoutManager(0, size);

        manager.addNode(1, size, position);
        manager.connectNode(0, 1, 0);

        manager.layout();
        manager.dump();
    },

    testEvents: function() {
        var size = {width:10,height:10};
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

        // Ok, if a new node is added, this an event should be fired  ...
        console.log("---- Layout without changes should not affect the tree  ---");
        events.empty();
        manager.layout(true);

        $assert(events.length == 0, "Unnecessary tree updated.");
    },

    testEventsComplex: function() {
        var size = {width:10,height:10};
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

        // Add a new node and connect. Only children nodes should be affected.
        console.log("---- Connect a new node  ---");

        events.empty();
        manager.connectNode(1, 4, 2);
        manager.layout(true);
        manager.dump();

        // @todo: This seems no to be ok...
        $assert(events.length == 4, "Only 3 nodes should be repositioned.");
    },

    testDisconnect: function() {
        var size = {width:10,height:10};
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

        // Now, disconnect one node ...
        console.log("--- Disconnect a single node ---");
        events.empty();
        manager.disconnectNode(2);
        manager.layout(true);
        manager.dump();

        $assert(events.some(
            function(event) {
                return event.getId() == 2;
            }), "Event for disconnected node seems not to be propagated");

        // Great, let's disconnect a not with children.
        console.log("--- Disconnect a node with children ---");
        manager.disconnectNode(3);
        manager.layout(true);
        manager.dump();

        $assert(events.some(
            function(event) {
                return event.getId() == 2;
            }), "Event for disconnected node seems not to be propagated");
    },

    testRemoveNode: function() {
        var size = {width:10,height:10};
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

        // Test removal of a connected node ...
        console.log("--- Remove node 3  ---");
        manager.removeNode(3);
        manager.layout(true);
        manager.dump();
    }


});

