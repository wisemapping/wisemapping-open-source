var TestClass = new Class({
    Extends: mindplot.Events,

    getEvents: function() {
        return this.$events;
    },

    removeEvents: function() {
        this.$events = {};
    }
});

// Test class and variables
var expectedChangeFn1 = function () {return 'change1';};
var expectedChangeFn2 = function () {return 'change2';};
var expectedLoadFn = function() {return 'loaded';};
var myTestClass = new TestClass();


describe("Events class suite", function() {

    afterEach(function() {
        myTestClass.removeEvents();
    });

    it("addEventTest", function() {
        expect(myTestClass.getEvents()).toEqual({});
        myTestClass.addEvent('change', expectedChangeFn1);
        expect(myTestClass.getEvents()).toEqual({change: [expectedChangeFn1]});
        myTestClass.addEvent('change', expectedChangeFn2);
        expect(myTestClass.getEvents()).toEqual({change: [expectedChangeFn1, expectedChangeFn2]});
        myTestClass.addEvent('load', expectedLoadFn);
        expect(myTestClass.getEvents()).toEqual({change: [expectedChangeFn1, expectedChangeFn2], load: [expectedLoadFn]});
    });
    it("removeEventTest", function() {
        expect(myTestClass.getEvents()).toEqual({});
        myTestClass.addEvent('change', expectedChangeFn1);
        myTestClass.addEvent('change', expectedChangeFn2);
        expect(myTestClass.getEvents()).toEqual({change: [expectedChangeFn1, expectedChangeFn2]});
        myTestClass.removeEvent('change', expectedChangeFn1);
        expect(myTestClass.getEvents()).toEqual({change: [expectedChangeFn2]});
    });
});
