mindplot.layoutManagers.boards.freeMindBoards.Entry = new Class({
    initialize:function(node){
        this._node = node;
        this._DEFAULT_X_GAP = 30;
        var pos = node.getPosition();
        if(!pos){
            var parent = node.getParent();
            pos = parent.getPosition().clone();
            var pwidth = parent.getSize().width;
            var width = node.getSize().width;
            pos.x = pos.x + Math.sign(pos.x) * (this._DEFAULT_X_GAP + pwidth/2 + width/2);
            node.setPosition(pos, false);
        }
        this._position = pos.y;
        var height = node.getSize().height;
        this._DEFAULT_GAP = 10;
        this._marginTop = this._DEFAULT_GAP + height/2;
        this._marginBottom = this._DEFAULT_GAP + height/2;
    },
    getNode:function(){
        return this._node;
    },
    getPosition:function(){
        return this._position;
    },
    setPosition:function(pos){
        var position = this._node.getPosition();
        position.y = pos;
        this._node.setPosition(position);
        this._position = pos;
    },
    getMarginTop:function(){
        return this._marginTop;
    },
    setMarginTop:function(value){
        if(value >= this._DEFAULT_GAP){
            this._marginTop = value;
        }
    },
    getMarginBottom:function(){
        return this._marginTop;
    }
});