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

web2d.peer.svg.CurvedLinePeer = function()
{
    var svgElement = window.document.createElementNS(this.svgNamespace, 'path');
    web2d.peer.svg.ElementPeer.call(this, svgElement);
    this._style={fill:'#495879'};
    this._updateStyle();
    this._customControlPoint_1 = false;
    this._customControlPoint_2 = false;
    this._control1=new core.Point();
    this._control2=new core.Point();
    this._lineStyle=true;
};

objects.extend(web2d.peer.svg.CurvedLinePeer, web2d.peer.svg.ElementPeer);

web2d.peer.svg.CurvedLinePeer.prototype.setSrcControlPoint = function(control){
    this._customControlPoint_1 = true;
    if(core.Utils.isDefined(control.x)){
        this._control1 = control;
        this._control1.x = parseInt(this._control1.x);
        this._control1.y = parseInt(this._control1.y)
    }
    this._updatePath();
};

web2d.peer.svg.CurvedLinePeer.prototype.setDestControlPoint = function(control){
    this._customControlPoint_2 = true;
    if(core.Utils.isDefined(control.x)){
        this._control2 = control;
        this._control2.x = parseInt(this._control2.x);
        this._control2.y = parseInt(this._control2.y)
    }
    this._updatePath();
};

web2d.peer.svg.CurvedLinePeer.prototype.isSrcControlPointCustom = function() {
    return this._customControlPoint_1;
};

web2d.peer.svg.CurvedLinePeer.prototype.isDestControlPointCustom = function() {
    return this._customControlPoint_2;
};

web2d.peer.svg.CurvedLinePeer.prototype.setIsSrcControlPointCustom = function(isCustom) {
    this._customControlPoint_1 = isCustom;
};

web2d.peer.svg.CurvedLinePeer.prototype.setIsDestControlPointCustom = function(isCustom) {
    this._customControlPoint_2 = isCustom;
};



web2d.peer.svg.CurvedLinePeer.prototype.getControlPoints = function(){
    return [this._control1, this._control2];
};

web2d.peer.svg.CurvedLinePeer.prototype.setFrom = function(x1, y1)
{
    this._x1 = parseInt(x1);
    this._y1 = parseInt(y1);
    this._updatePath();
};

web2d.peer.svg.CurvedLinePeer.prototype.setTo = function(x2, y2)
{
    this._x2 = parseInt(x2);
    this._y2 = parseInt(y2);
    this._updatePath();
};

web2d.peer.svg.CurvedLinePeer.prototype.getFrom = function()
{
    return new core.Point(this._x1,this._y1);
};

web2d.peer.svg.CurvedLinePeer.prototype.getTo = function()
{
    return new core.Point(this._x2,this._y2);
};

web2d.peer.svg.CurvedLinePeer.prototype.setStrokeWidth = function(width)
{
    this._style['stroke-width']= width;
    this._updateStyle();
};

web2d.peer.svg.CurvedLinePeer.prototype.setColor = function(color)
{
    this._style['stroke']= color;
    this._style['fill']=color;
    this._updateStyle();
};

web2d.peer.svg.CurvedLinePeer.prototype.updateLine = function(avoidControlPointFix){
    this._updatePath(avoidControlPointFix);
};

web2d.peer.svg.CurvedLinePeer.prototype.setLineStyle = function (style){
    this._lineStyle=style;
    if(this._lineStyle){
        this._style['fill']=this._fill;
    } else {
        this._fill = this._style['fill'];
        this._style['fill']='none';
    }
    this._updateStyle();
    this.updateLine();
};

web2d.peer.svg.CurvedLinePeer.prototype.getLineStyle = function (){
    return this._lineStyle;
};


web2d.peer.svg.CurvedLinePeer.prototype.setShowEndArrow = function(visible){
    this._showEndArrow =visible;
    this.updateLine();
};

web2d.peer.svg.CurvedLinePeer.prototype.isShowEndArrow = function(){
    return this._showEndArrow;
};

web2d.peer.svg.CurvedLinePeer.prototype.setShowStartArrow = function(visible){
    this._showStartArrow =visible;
    this.updateLine();
};

web2d.peer.svg.CurvedLinePeer.prototype.isShowStartArrow = function(){
    return this._showStartArrow;
};


web2d.peer.svg.CurvedLinePeer.prototype._updatePath = function(avoidControlPointFix)
{
    this._calculateAutoControlPoints(avoidControlPointFix);
    var path = "M"+this._x1+","+this._y1
                +" C"+(this._control1.x+this._x1)+","+(this._control1.y+this._y1)+" "
                  +(this._control2.x+this._x2)+","+(this._control2.y+this._y2)+" "
                  +this._x2+","+this._y2+
                  (this._lineStyle?" "
                    +(this._control2.x+this._x2)+","+(this._control2.y+this._y2+3)+" "
                    +(this._control1.x+this._x1)+","+(this._control1.y+this._y1+5)+" "
                    +this._x1+","+(this._y1+7)+" Z"
                    :""
                  );
    this._native.setAttribute("d",path);
};

web2d.peer.svg.CurvedLinePeer.prototype._updateStyle = function()
{
    var style = "";
    for(var key in this._style){
        style+=key+":"+this._style[key]+" ";
    }
    this._native.setAttribute("style",style);
};

web2d.peer.svg.CurvedLinePeer.prototype._calculateAutoControlPoints = function(avoidControlPointFix){
    if(core.Utils.isDefined(this._x1) && core.Utils.isDefined(this._x2)){
        //Both points available, calculate real points
        var defaultpoints = core.Utils.calculateDefaultControlPoints(new core.Point(this._x1, this._y1),new core.Point(this._x2,this._y2));
        if(!this._customControlPoint_1 && !(core.Utils.isDefined(avoidControlPointFix) && avoidControlPointFix==0)){
            this._control1.x = defaultpoints[0].x;
            this._control1.y = defaultpoints[0].y;
        }
        if(!this._customControlPoint_2 && !(core.Utils.isDefined(avoidControlPointFix) && avoidControlPointFix==1)){
            this._control2.x = defaultpoints[1].x;
            this._control2.y = defaultpoints[1].y;
        }
    }
};

web2d.peer.svg.CurvedLinePeer.prototype.setDashed = function(length,spacing){
    if(core.Utils.isDefined(length) && core.Utils.isDefined(spacing)){
        this._native.setAttribute("stroke-dasharray",length+","+spacing);
    } else {
        this._native.setAttribute("stroke-dasharray","");
    }

};
