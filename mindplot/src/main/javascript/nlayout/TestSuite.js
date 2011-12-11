mindplot.nlayout.TestSuite = new Class({
    Extends: mindplot.nlayout.ChildrenSorterStrategy,
    initialize:function() {

//        this.testAligned();
        this.testEvents();

        // @ Agregar tests que garantice que no se reposicional cosan inecesariamente 2 veces...
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

        // Reposition ...
        manager.layout();
        console.log("Updated tree:");
        manager.dump();

        // Listen for changes ...
        console.log("Updated nodes ...");
        var events = [];
        manager.addEvent('change', function(event) {
            console.log("Updated nodes: {id:" + event.getId() + ", order: " + event.getOrder() + ",position: {" + event.getPosition().x + "," + event.getPosition().y + "}");
            events.push(event);
        });
        manager.flushEvents();

        // Second flush must not fire events ...
        console.log("---- Test Flush ---");

        events.empty();
        manager.flushEvents();
        $assert(events.length == 0, "Event should not be fire twice.");

        // Ok, if a new node is added, this an event should be fired  ...
        console.log("---- Layout without changes should not affect the tree  ---");
        events.empty();
        manager.layout(true);

        $assert(events.length == 0, "Unnecessary tree updated.");
    }

});

