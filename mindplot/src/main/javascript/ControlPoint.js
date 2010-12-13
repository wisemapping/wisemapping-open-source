/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: file 64488 2006-03-10 17:32:09Z paulo $
 */

mindplot.ControlPoint = function()
{
    this._controlPoints= [new web2d.Elipse({width:6, height:6, stroke:'1 solid #6589de',fillColor:'gray', visibility:false}),
                          new web2d.Elipse({width:6, height:6, stroke:'1 solid #6589de',fillColor:'gray', visibility:false})];
    this._controlLines=[new web2d.Line({strokeColor:"#6589de", strokeWidth:1, opacity:0.3}),
                        new web2d.Line({strokeColor:"#6589de", strokeWidth:1, opacity:0.3})];
    this._isBinded=false;
    this._controlPoints[0].addEventListener('mousedown',this._mouseDown.bindWithEvent(this, mindplot.ControlPoint.FROM));
    this._controlPoints[0].addEventListener('click',this._mouseClick.bindWithEvent(this));
    this._controlPoints[0].addEventListener('dblclick',this._mouseClick.bindWithEvent(this));
    this._controlPoints[1].addEventListener('mousedown',this._mouseDown.bindWithEvent(this,mindplot.ControlPoint.TO));
    this._controlPoints[1].addEventListener('click',this._mouseClick.bindWithEvent(this));
    this._controlPoints[1].addEventListener('dblclick',this._mouseClick.bindWithEvent(this));
    this._mouseClickOnBackgroundFunction = this._mouseClickOnBackground.bind(this);
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
};

mindplot.ControlPoint.prototype._createControlPoint = function() {
    this._controls= this._line.getLine().getControlPoints();
    this._controlPoints[0].setPosition(this._controls[mindplot.ControlPoint.FROM].x, this._controls[mindplot.ControlPoint.FROM].y-3);
    this._controlPoints[1].setPosition(this._controls[mindplot.ControlPoint.TO].x, this._controls[mindplot.ControlPoint.TO].y-3);
    var pos = this._line.getLine().getFrom();
    this._controlLines[0].setFrom(pos.x, pos.y);
    this._controlLines[0].setTo(this._controls[mindplot.ControlPoint.FROM].x+3, this._controls[mindplot.ControlPoint.FROM].y);
    pos = this._line.getLine().getTo();
    this._controlLines[1].setFrom(pos.x, pos.y);
    this._controlLines[1].setTo(this._controls[mindplot.ControlPoint.TO].x+3, this._controls[mindplot.ControlPoint.TO].y);

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
    this._controls[point].x=pos.x;
    this._controls[point].y=pos.y;
    this._controlPoints[point].setPosition(pos.x-5,pos.y-3);
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

mindplot.ControlPoint.prototype._mouseClickOnBackground = function(event){
    this.setVisibility(false);
};

mindplot.ControlPoint.prototype.setVisibility = function(visible){
    if(visible){
        this._workspace.getScreenManager().addEventListener('mousedown',this._mouseClickOnBackgroundFunction);
        this._controlLines[0].moveToFront();
        this._controlLines[1].moveToFront();
        this._controlPoints[0].moveToFront();
        this._controlPoints[1].moveToFront();
    }
    else{
        this._workspace.getScreenManager().removeEventListener('mousedown',this._mouseClickOnBackgroundFunction);
    }
    this._controlPoints[0].setVisibility(visible);
    this._controlPoints[1].setVisibility(visible);
    this._controlLines[0].setVisibility(visible);
    this._controlLines[1].setVisibility(visible);
};

mindplot.ControlPoint.prototype.addToWorkspace = function(workspace){
    this._workspace = workspace;
    workspace.appendChild(this._controlPoints[0]);
    workspace.appendChild(this._controlPoints[1]);
    workspace.appendChild(this._controlLines[0]);
    workspace.appendChild(this._controlLines[1]);
};

mindplot.ControlPoint.prototype.removeFromWorkspace = function(workspace){
    this._workspace = null;
    workspace.removeChild(this._controlPoints[0]);
    workspace.removeChild(this._controlPoints[1]);
    workspace.removeChild(this._controlLines[0]);
    workspace.removeChild(this._controlLines[1]);
};

mindplot.ControlPoint.FROM = 0;
mindplot.ControlPoint.TO = 1;
