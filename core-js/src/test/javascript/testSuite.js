describe("Functions suite test", function() {
    it("$defined() test spec", function() {
        var testVariable = undefined;
        expect($defined(testVariable)).toBe(false);
        testVariable = 1;
        expect($defined(testVariable)).toBe(true);
        testVariable = null;
        expect($defined(testVariable)).toBe(false);
    });
});
