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

web2d.CurvedLine = function(attributes)
{
    var peer = web2d.peer.Toolkit.createCurvedLine();
    var defaultAttributes = {strokeColor:'blue',strokeWidth:1,strokeStyle:'solid',strokeOpacity:1};
    for (var key in attributes)
    {
        defaultAttributes[key] = attributes[key];
    }
    web2d.Element.call(this, peer, defaultAttributes);
};
objects.extend(web2d.CurvedLine, web2d.Element);

web2d.CurvedLine.prototype.getType = function()
{
    return "CurvedLine";
};

web2d.CurvedLine.prototype.setFrom = function(x, y)
{
    this._peer.setFrom(x, y);
};

web2d.CurvedLine.prototype.setTo = function(x, y)
{
    this._peer.setTo(x, y);
};

web2d.CurvedLine.prototype.getFrom = function()
{
    return this._peer.getFrom();
};

web2d.CurvedLine.prototype.getTo = function()
{
    return this._peer.getTo();
};

web2d.CurvedLine.prototype.setShowEndArrow = function(visible){
    this._peer.setShowEndArrow(visible);
};

web2d.CurvedLine.prototype.isShowEndArrow = function(){
    return this._peer.isShowEndArrow();
};

web2d.CurvedLine.prototype.setShowStartArrow = function(visible){
    this._peer.setShowStartArrow(visible);
};

web2d.CurvedLine.prototype.isShowStartArrow = function(){
    return this._peer.isShowStartArrow();
};

web2d.CurvedLine.prototype.setSrcControlPoint = function(control){
    this._peer.setSrcControlPoint(control);
};

web2d.CurvedLine.prototype.setDestControlPoint = function(control){
    this._peer.setDestControlPoint(control);
};

web2d.CurvedLine.prototype.getControlPoints = function(){
    return this._peer.getControlPoints();
};

web2d.CurvedLine.prototype.isSrcControlPointCustom = function(){
    return this._peer.isSrcControlPointCustom();
};

web2d.CurvedLine.prototype.isDestControlPointCustom = function(){
    return this._peer.isDestControlPointCustom();
};

web2d.CurvedLine.prototype.setIsSrcControlPointCustom = function(isCustom){
    this._peer.setIsSrcControlPointCustom(isCustom);
};

web2d.CurvedLine.prototype.setIsDestControlPointCustom = function(isCustom){
    this._peer.setIsDestControlPointCustom(isCustom);
};

web2d.CurvedLine.prototype.updateLine= function(avoidControlPointFix){
    return this._peer.updateLine(avoidControlPointFix);
};

web2d.CurvedLine.prototype.setStyle = function(style){
    this._peer.setLineStyle(style);

};

web2d.CurvedLine.prototype.getStyle = function(){
    return this._peer.getLineStyle();
};

web2d.CurvedLine.prototype.setDashed = function(length,spacing){
    this._peer.setDashed(length, spacing);
};

web2d.CurvedLine.SIMPLE_LINE = false;
web2d.CurvedLine.NICE_LINE = true;

