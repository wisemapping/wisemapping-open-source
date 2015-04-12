Options = new Class({

    setOptions: function () {
        var options = this.options = Object.merge.apply(null, [{}, this.options].append(arguments));
        if (this.addEvent) for (var option in options) {
            if (typeOf(options[option]) != 'function' || !(/^on[A-Z]/).test(option)) continue;
            this.addEvent(option, options[option]);
            delete options[option];
        }
        return this;
    }

});