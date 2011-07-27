mindplot.EventBus = new Class({
    Extends:Options,
    Implements:Events,
    options: {

    },
    initialize: function(options) {
        this.setOptions(options);
    }

});

mindplot.EventBus.events = {
    NodeResizeEvent:'NodeResizeEvent',
    NodeMoveEvent:'NodeMoveEvent',
    NodeDisconnectEvent:'NodeDisconnectEvent',
    NodeConnectEvent:'NodeConnectEvent',
    NodeRepositionateEvent:'NodeRepositionateEvent',
    NodeShrinkEvent:'NodeShrinkEvent',
    NodeMouseOverEvent:'NodeMouseOverEvent',
    NodeMouseOutEvent:'NodeMouseOutEvent'
};

mindplot.EventBus.instance = new mindplot.EventBus();