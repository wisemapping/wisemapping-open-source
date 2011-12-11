mindplot.nlayout.Node = new Class({
    initialize:function(id, size, position, sorter) {
        $assert(!isNaN(id), "id can not be null");
        $assert(size, "size can not be null");
        $assert(position, "position can not be null");
        $assert(sorter, "sorter can not be null");

        this._id = id;
        this._sorter = sorter;
        this._properties = {};

        this.setSize(size);
        this.setPosition(position);
    },

    getId:function() {
        return this._id;
    },

    setOrder: function(order) {
        $assert(!isNaN(order), "Order can not be null");
        this._setProperty('order', order, false);
    },

    resetPositionState : function() {
        var prop = this._properties['position'];
        if (prop) {
            prop.hasChanded = false;
        }
    },

    resetOrderState : function() {
        var prop = this._properties['order'];
        if (prop) {
            prop.hasChanded = false;
        }
    },

    getOrder: function() {
        return this._getProperty('order');
    },

    hasOrderChanged: function() {
        return this._isPropertyChanged('order');
    },

    hasPositionChanged: function() {
        return this._isPropertyChanged('position');

    },

    getPosition: function() {
        return this._getProperty('position');
    },

    setSize : function(size) {
        $assert($defined(size), "Size can not be null");
        this._setProperty('size', Object.clone(size));
    },

    getSize: function() {
        return this._getProperty('size');
    },

    setPosition : function(position) {
        $assert($defined(position), "Position can not be null");
        $assert(!isNaN(position.x), "x can not be null");
        $assert(!isNaN(position.y), "y can not be null");

        this._setProperty('position', Object.clone(position));
    },

    _setProperty: function(key, value) {
        var prop = this._properties[key];
        if (!prop) {
            prop = {
                hasChanded:false,
                value: null,
                oldValue : null
            };
        }

        prop.oldValue = prop.value;
        prop.value = value;
        prop.hasChanded = true;

        this._properties[key] = prop;
    },

    _getProperty: function(key) {
        var prop = this._properties[key];
        return $defined(prop) ? prop.value : null;
    },

    _isPropertyChanged: function(key) {
        var prop = this._properties[key];
        return prop ? prop.hasChanded : false;
    },

    _setPropertyUpdated : function(key) {
        var prop = this._properties[key];
        if (prop) {
            this._properties[key] = true;
        }
    },

    getSorter: function() {
        return this._sorter;
    },


    toString: function() {
        return "[order:" + this.getOrder() + ", position: {" + this.getPosition().x + "," + this.getPosition().y + "}]";
    }

});

