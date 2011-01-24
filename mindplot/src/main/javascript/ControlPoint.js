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

mindplot.ControlPoint = function()
{
    this._controlPointsController= [new web2d.Elipse({width:6, height:6, stroke:'1 solid #6589de',fillColor:'gray', visibility:false}),
                          new web2d.Elipse({width:6, height:6, stroke:'1 solid #6589de',fillColor:'gray', visibility:false})];
    this._controlLines=[new web2d.Line({strokeColor:"#6589de", strokeWidth:1, opacity:0.3}),
                        new web2d.Line({strokeColor:"#6589de", strokeWidth:1, opacity:0.3})];
    this._isBinded=false;
    this._controlPointsController[0].addEventListener('mousedown',this._mouseDown.bindWithEvent(this, mindplot.ControlPoint.FROM));
    this._controlPointsController[0].addEventListener('click',this._mouseClick.bindWithEvent(this));
    this._controlPointsController[0].addEventListener('dblclick',this._mouseClick.bindWithEvent(this));
    this._controlPointsController[1].addEventListener('mousedown',this._mouseDown.bindWithEvent(this,mindplot.ControlPoint.TO));
    this._controlPointsController[1].addEventListener('click',this._mouseClick.bindWithEvent(this));
    this._controlPointsController[1].addEventListener('dblclick',this._mouseClick.bindWithEvent(this));
};

mindplot.ControlPoint.prototype.setSide= function(side) {
    this._side = side;
};

mindplot.ControlPoint.prototype.setLine= function(line) {
    if(core.Utils.isDefined(this._line)){
        this._removeLine();
    }
    this._line= line;
    this._createControlPoint();
    this._endPoint = [];
    this._orignalCtrlPoint = [];
    this._orignalCtrlPoint[0] = this._controls[0].clone();
    this._orignalCtrlPoint[1] = this._controls[1].clone();
    this._endPoint[0] = this._line.getLine().getFrom().clone();
    this._endPoint[1] = this._line.getLine().getTo().clone();
};

mindplot.ControlPoint.prototype._createControlPoint = function() {
    this._controls= this._line.getLine().getControlPoints();
    var pos = this._line.getLine().getFrom();
    this._controlPointsController[0].setPosition(this._controls[mindplot.ControlPoint.FROM].x+pos.x, this._controls[mindplot.ControlPoint.FROM].y+pos.y-3);
    this._controlLines[0].setFrom(pos.x, pos.y);
    this._controlLines[0].setTo(this._controls[mindplot.ControlPoint.FROM].x+pos.x+3, this._controls[mindplot.ControlPoint.FROM].y+pos.y);
    pos = this._line.getLine().getTo();
    this._controlLines[1].setFrom(pos.x, pos.y);
    this._controlLines[1].setTo(this._controls[mindplot.ControlPoint.TO].x+pos.x+3, this._controls[mindplot.ControlPoint.TO].y+pos.y);
    this._controlPointsController[1].setPosition(this._controls[mindplot.ControlPoint.TO].x+pos.x, this._controls[mindplot.ControlPoint.TO].y+pos.y-3);

};

mindplot.ControlPoint.prototype._removeLine= function() {
    
};

mindplot.ControlPoint.prototype._mouseDown = function(event, point) {
    if(!this._isBinded){
        this._isBinded=true;
        this._mouseMoveFunction = this._mouseMove.bindWithEvent(this,point);
        this._workspace.getScreenManager().addEventListener('mousemove',this._mouseMoveFunction);
        this._mouseUpFunction = this._mouseUp.bindWithEvent(this,point);
        this._workspace.getScreenManager().addEventListener('mouseup',this._mouseUpFunction);
    }
    event.preventDefault();
    event.stop();
    return false;
};

mindplot.ControlPoint.prototype._mouseMove = function(event, point) {
    var screen = this._workspace.getScreenManager();
    var pos = screen.getWorkspaceMousePosition(event);
    var topic = null;
    if(point==0){
        var cords = core.Utils.calculateRelationShipPointCoordinates(this._line.getSourceTopic(),pos);
        this._line.getLine().setFrom(cords.x, cords.y);
    }else{
        var cords = core.Utils.calculateRelationShipPointCoordinates(this._line.getTargetTopic(),pos);
        this._line.getLine().setTo(cords.x, cords.y);
    }
    this._controls[point].x=(pos.x - cords.x);
    this._controls[point].y=(pos.y - cords.y);
    this._controlPointsController[point].setPosition(pos.x-5,pos.y-3);
    this._controlLines[point].setFrom(cords.x, cords.y);
    this._controlLines[point].setTo(pos.x-2,pos.y);
    this._line.getLine().updateLine(point);
    /*event.preventDefault();
    event.stop();
    return false;*/
};

mindplot.ControlPoint.prototype._mouseUp = function(event, point) {
    this._workspace.getScreenManager().removeEventListener('mousemove',this._mouseMoveFunction);
    this._workspace.getScreenManager().removeEventListener('mouseup',this._mouseUpFunction);
    var command = new mindplot.commands.MoveControlPointCommand(this,point);
    designer._actionRunner.execute(command); //todo:Uggly!! designer is global!!
    this._isBinded=false;
    /*event.preventDefault();
    event.stop();
    return false;*/
};

mindplot.ControlPoint.prototype._mouseClick = function(event){
    event.preventDefault();
    event.stop();
    return false;
};

mindplot.ControlPoint.prototype.setVisibility = function(visible){
    if(visible){
        this._controlLines[0].moveToFront();
        this._controlLines[1].moveToFront();
        this._controlPointsController[0].moveToFront();
        this._controlPointsController[1].moveToFront();
    }
    this._controlPointsController[0].setVisibility(visible);
    this._controlPointsController[1].setVisibility(visible);
    this._controlLines[0].setVisibility(visible);
    this._controlLines[1].setVisibility(visible);
};

mindplot.ControlPoint.prototype.addToWorkspace = function(workspace){
    this._workspace = workspace;
    workspace.appendChild(this._controlPointsController[0]);
    workspace.appendChild(this._controlPointsController[1]);
    workspace.appendChild(this._controlLines[0]);
    workspace.appendChild(this._controlLines[1]);
};

mindplot.ControlPoint.prototype.removeFromWorkspace = function(workspace){
    this._workspace = null;
    workspace.removeChild(this._controlPointsController[0]);
    workspace.removeChild(this._controlPointsController[1]);
    workspace.removeChild(this._controlLines[0]);
    workspace.removeChild(this._controlLines[1]);
};

mindplot.ControlPoint.prototype.getControlPoint = function(index){
    return this._controls[index];
};

mindplot.ControlPoint.prototype.getOriginalEndPoint = function(index){
    return this._endPoint[index];
};

mindplot.ControlPoint.prototype.getOriginalCtrlPoint = function(index){
    return this._orignalCtrlPoint[index];
};

mindplot.ControlPoint.FROM = 0;
mindplot.ControlPoint.TO = 1;
