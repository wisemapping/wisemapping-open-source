mindplot.RelationshipModel = function(fromNode, toNode)
{
    core.assert(fromNode, 'from node type can not be null');
    core.assert(toNode, 'to node type can not be null');

    this._id = mindplot.RelationshipModel._nextUUID();
    this._fromNode = fromNode;
    this._toNode = toNode;
    this._lineType=mindplot.ConnectionLine.SIMPLE_CURVED;
    this._srcCtrlPoint=null;
    this._destCtrlPoint=null;
    this._endArrow=true;

};

mindplot.RelationshipModel.prototype.getFromNode=function(){
    return this._fromNode;
};

mindplot.RelationshipModel.prototype.getToNode=function(){
    return this._toNode;
};

mindplot.RelationshipModel.prototype.getId=function(){
    return this._id;
};

mindplot.RelationshipModel.prototype.getLineType = function(){
    return this._lineType;
};

mindplot.RelationshipModel.prototype.setLineType = function(lineType){
    this._lineType = lineType;
};

mindplot.RelationshipModel.prototype.getSrcCtrlPoint= function(){
    return this._srcCtrlPoint;
};

mindplot.RelationshipModel.prototype.setSrcCtrlPoint= function(srcCtrlPoint){
    this._srcCtrlPoint = srcCtrlPoint;
};

mindplot.RelationshipModel.prototype.getDestCtrlPoint= function(){
    return this._destCtrlPoint;
};

mindplot.RelationshipModel.prototype.setDestCtrlPoint= function(destCtrlPoint){
    this._destCtrlPoint = destCtrlPoint;
};

mindplot.RelationshipModel.prototype.getEndArrow= function(){
    return this._endArrow;
};

mindplot.RelationshipModel.prototype.setEndArrow= function(endArrow){
    this._endArrow = endArrow;
};

mindplot.RelationshipModel.prototype.clone = function(model){
    var result = new mindplot.RelationshipModel(this._fromNode, this._toNode);
    result._id = this._id;
    result._lineType = this._lineType;
    result._srcCtrlPoint = this._srcCtrlPoint;
    result._destCtrlPoint = this._destCtrlPoint;
    result._endArrow = this._endArrow;
    return result;
};

/**
 * @todo: This method must be implemented.
 */
mindplot.RelationshipModel._nextUUID = function()
{
    if (!this._uuid)
    {
        this._uuid = 0;
    }

    this._uuid = this._uuid + 1;
    return this._uuid;
};


mindplot.RelationshipModel.prototype.inspect = function()
{
    return '(fromNode:' + this.getFromNode().getId() + ' , toNode: ' + this.getToNode().getId() + ')';
};
