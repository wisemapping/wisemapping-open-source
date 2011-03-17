/*
Class: BaseLayoutManager
	Base class for LayoutManagers

Arguments:
	element - the knob container
	knob - the handle
	options - see Options below

Options:
	steps - the number of steps for your slider.
	mode - either 'horizontal' or 'vertical'. defaults to horizontal.
	offset - relative offset for knob position. default to 0.

Events:
	onChange - a function to fire when the value changes.
	onComplete - a function to fire when you're done dragging.
	onTick - optionally, you can alter the onTick behavior, for example displaying an effect of the knob moving to the desired position.
		Passes as parameter the new position.
*/

mindplot.layoutManagers.BaseLayoutManager = new Class({

    options: {

    },

    initialize: function(designer, options) {
        this.setOptions(options);
        this._designer = designer;
    },
    addNode: function(node) {

    },
    getDesigner:function(){
        return this._designer;
    },
    getType:function(){
        return mindplot.layoutManagers.BaseLayoutManager.NAME;
    }
});

mindplot.layoutManagers.BaseLayoutManager.NAME ="BaseLayoutManager"; 

mindplot.layoutManagers.BaseLayoutManager.implement(new Events);
mindplot.layoutManagers.BaseLayoutManager.implement(new Options);