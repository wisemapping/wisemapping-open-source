/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/
mindplot.RelationshipModel = function(fromNode, toNode)
{
    $assert(fromNode, 'from node type can not be null');
    $assert(toNode, 'to node type can not be null');

    this._id = mindplot.RelationshipModel._nextUUID();
    this._fromNode = fromNode;
    this._toNode = toNode;
    this._lineType=mindplot.ConnectionLine.SIMPLE_CURVED;
    this._srcCtrlPoint=null;
    this._destCtrlPoint=null;
    this._endArrow=true;
    this._startArrow=false;
    this._ctrlPointRelative=false;

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

mindplot.RelationshipModel.prototype.getStartArrow= function(){
    return this._startArrow;
};

mindplot.RelationshipModel.prototype.setStartArrow= function(startArrow){
    this._startArrow = startArrow;
};

mindplot.RelationshipModel.prototype.clone = function(model){
    var result = new mindplot.RelationshipModel(this._fromNode, this._toNode);
    result._id = this._id;
    result._lineType = this._lineType;
    result._srcCtrlPoint = this._srcCtrlPoint;
    result._destCtrlPoint = this._destCtrlPoint;
    result._endArrow = this._endArrow;
    result._startArrow = this._startArrow;
    return result;
};

/**
 * @todo: This method must be implemented.
 */
mindplot.RelationshipModel._nextUUID = function()
{
    if (!$defined(this._uuid))
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
