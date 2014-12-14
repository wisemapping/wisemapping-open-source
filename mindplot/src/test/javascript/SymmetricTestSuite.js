
describe("Balanced Test Suite", function() {

    it("symmetricTest", function() {
        var position = {x:0,y:0};
        var manager = new mindplot.layout.LayoutManager(0, TestSuite.ROOT_NODE_SIZE);

        manager.addNode(1, TestSuite.NODE_SIZE, position);
        manager.addNode(2, TestSuite.NODE_SIZE, position);
        manager.addNode(3, TestSuite.NODE_SIZE, position);
        manager.addNode(4, TestSuite.NODE_SIZE, position);
        manager.addNode(5, TestSuite.NODE_SIZE, position);
        manager.addNode(6, TestSuite.NODE_SIZE, position);
        manager.addNode(7, TestSuite.NODE_SIZE, position);
        manager.addNode(8, TestSuite.NODE_SIZE, position);
        manager.addNode(9, TestSuite.NODE_SIZE, position);
        manager.addNode(10, TestSuite.NODE_SIZE, position);
        manager.addNode(11, TestSuite.NODE_SIZE, position);
        manager.addNode(12, TestSuite.NODE_SIZE, position);
        manager.addNode(13, TestSuite.NODE_SIZE, position);
        manager.addNode(14, TestSuite.NODE_SIZE, position);
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

        // All nodes should be positioned symmetrically with respect to their common ancestors
        expect(manager.find(14).getPosition().y).toEqual(manager.find(13).getPosition().y);
        expect(manager.find(5).getPosition().y).toEqual(manager.find(10).getPosition().y);
        expect(manager.find(11).getPosition().y - manager.find(6).getPosition().y).toEqual(-(manager.find(12).getPosition().y - manager.find(6).getPosition().y));
        expect(manager.find(8).getPosition().y - manager.find(1).getPosition().y).toEqual(-(manager.find(11).getPosition().y - manager.find(1).getPosition().y));
        expect(manager.find(9).getPosition().y - manager.find(1).getPosition().y).toEqual(-(manager.find(11).getPosition().y - manager.find(1).getPosition().y));

    });

    it("symmetricDragPredictTest", function() {
        var position = {x:0,y:0};
        var manager = new mindplot.layout.LayoutManager(0, TestSuite.ROOT_NODE_SIZE);
        manager.addNode(1, TestSuite.NODE_SIZE, position).connectNode(0, 1, 1);
        manager.addNode(2, TestSuite.NODE_SIZE, position).connectNode(1, 2, 0);
        manager.layout();

        var prediction1a = manager.predict(1, 2, {x:-250, y:-20});
        expect(prediction1a.position.x).toEqual(manager.find(2).getPosition().x);
        expect(prediction1a.position.y).toEqual(manager.find(2).getPosition().y);
        expect(prediction1a.order).toEqual(manager.find(2).getOrder());

        var prediction1b = manager.predict(1, 2, {x:-250, y:20});
        expect(prediction1b.position.x).toEqual(manager.find(2).getPosition().x);
        expect(prediction1b.position.y).toEqual(manager.find(2).getPosition().y);
        expect(prediction1b.order).toEqual(manager.find(2).getOrder());

        var prediction1c = manager.predict(0, 2, {x:-100, y:-20});
        expect(prediction1c.position.x).toEqual(manager.find(1).getPosition().x);
        expect(prediction1c.position.y).toBeLessThan(manager.find(1).getPosition().y);
        expect(prediction1c.order).toEqual(manager.find(1).getOrder());

        var prediction1d = manager.predict(0, 2, {x:-100, y:20});
        expect(prediction1d.position.x).toEqual(manager.find(1).getPosition().x);
        expect(prediction1d.position.y).toBeGreaterThan(manager.find(1).getPosition().y);
        expect(prediction1d.order).toEqual(3);

        var prediction1e = manager.predict(1, 2, {x:-250, y:0});
        expect(prediction1e.position.x).toEqual(manager.find(2).getPosition().x);
        expect(prediction1e.position.y).toEqual(manager.find(2).getPosition().y);
        expect(prediction1e.order).toEqual(manager.find(2).getOrder());
    });

});
