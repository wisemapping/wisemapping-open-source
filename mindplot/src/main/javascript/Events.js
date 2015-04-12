mindplot.Events = new Class({

    $events: {},

    _removeOn: function (string) {
        return string.replace(/^on([A-Z])/, function (full, first) {
            return first.toLowerCase();
        });
    },

    addEvent: function (type, fn, internal) {
        type = this._removeOn(type);

        this.$events[type] = (this.$events[type] || []).include(fn);
        if (internal) fn.internal = true;
        return this;
    },

    fireEvent: function (type, args, delay) {
        type = this._removeOn(type);
        var events = this.$events[type];
        if (!events) return this;
        args = Array.from(args);
        _.each(events, function (fn) {
            if (delay) fn.delay(delay, this, args);
            else fn.apply(this, args);
        }, this);
        return this;
    },

    removeEvent: function (type, fn) {
        type = this._removeOn(type);
        var events = this.$events[type];
        if (events && !fn.internal) {
            var index = events.indexOf(fn);
            if (index != -1) events.splice(index, 1);
        }
        return this;
    }

});
