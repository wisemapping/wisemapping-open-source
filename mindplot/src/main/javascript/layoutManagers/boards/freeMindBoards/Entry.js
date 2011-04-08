mindplot.layoutManagers.boards.freeMindBoards.Entry = new Class({
    initialize:function(node, useFinalPosition){
        this._node = node;
        this._DEFAULT_X_GAP = 30;
        var pos = node.getModel().getFinalPosition();
        if(useFinalPosition && pos){
            this.setPosition(pos.x, pos.y);
        }
        else{
            pos = node.getPosition();
            if(!pos){
                var parent = node.getParent();
                pos = parent.getPosition().clone();
                var pwidth = parent.getSize().width;
                var width = node.getSize().width;
                pos.x = pos.x + Math.sign(pos.x) * (this._DEFAULT_X_GAP + pwidth/2 + width/2);
                node.setPosition(pos, false);

            }
        }
        this._position = pos.y;
        this._DEFAULT_GAP = 10;
        var height = this.getNode().getSize().height;
        this._minimalMargin = this._DEFAULT_GAP + height/2;
        this._marginTop = this._minimalMargin;
        this._marginBottom = this._minimalMargin;
        this._marginTopChildren=0;
        this._marginBottomChildren=0;
    },
    getNode:function(){
        return this._node;
    },
    getId:function(){
        return this.getNode().getId();
    },
    getPosition:function(){
        return this._position;
    },
    setPosition:function(x,y){
        var position = this._node.getPosition().clone();
        position.y = y;
        if(null != x){
            position.x = x;
        }

        this._node.setPosition(position, false);
        this._position = y;
    },
    getMarginTop:function(){
        return this._marginTop;
    },
    setMarginTop:function(value){
        if(value >= this._minimalMargin){
            this._marginTop = value;
        }
    },
    setMarginBottom:function(value){
        if(value >= this._minimalMargin){
            this._marginBottom = value;
        }
    },
    getMarginBottom:function(){
        return this._marginBottom;
    },
    getChildrenMarginTop:function(){
        return this._marginTopChildren;
    },
    setChildrenMarginTop:function(value){
        if(value >= this._minimalMargin){
            this._marginTopChildren = value - this._minimalMargin;
        }else{
            this._marginTopChildren=0;
        }
    },
    setChildrenMarginBottom:function(value){
        if(value >= this._minimalMargin){
            this._marginBottomChildren = value - this._minimalMargin;
        }else{
            this._marginBottomChildren=0;
        }
    },
    getChildrenMarginBottom:function(){
        return this._marginBottomChildren;
    },
    getTotalMarginTop:function(){
        return this._marginTopChildren+this._marginTop;
    },
    getTotalMarginBottom:function(){
        return this._marginBottomChildren + this._marginBottom;
    }
});