mindplot.nlayout.ChangeEvent = new Class({
    initialize:function(id) {
        $assert(!isNaN(id), "id can not be null");
        this._id = id;
        this._position = null;
        this._order = null;
    },

    getId:function() {
        return this._id;
    },

    getOrder: function() {
        return this._order;
    },

    getPosition: function() {
        return this._position;
    },

    setOrder: function(value) {
        $assert(!isNaN(value), "value can not be null");
        this._order = value;
    },

    setPosition: function(value) {
        $assert(value, "value can not be null");
        this._position = value;
    },

    toString: function() {
        return "[order:" + this.getOrder() + ", position: {" + this.getPosition().x + "," + this.getPosition().y + "}]";
    }
});


