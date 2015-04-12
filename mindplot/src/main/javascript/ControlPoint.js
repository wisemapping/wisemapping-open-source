/*
 *    Copyright [2015] [wisemapping]
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

mindplot.ControlPoint = new Class({
    initialize: function () {
        var control1 = new web2d.Elipse({
            width: 6,
            height: 6,
            stroke: '1 solid #6589de',
            fillColor: 'gray',
            visibility: false
        });
        control1.setCursor('pointer');

        var control2 = new web2d.Elipse({
            width: 6,
            height: 6,
            stroke: '1 solid #6589de',
            fillColor: 'gray',
            visibility: false
        });
        control2.setCursor('pointer');

        this._controlPointsController = [control1, control2];
        this._controlLines = [new web2d.Line({strokeColor: "#6589de", strokeWidth: 1, opacity: 0.3}),
            new web2d.Line({strokeColor: "#6589de", strokeWidth: 1, opacity: 0.3})];

        this._isBinded = false;
        var me = this;
        this._controlPointsController[0].addEvent('mousedown', function (event) {
            (me._mouseDown)(event, mindplot.ControlPoint.FROM, me);
        });
        this._controlPointsController[0].addEvent('click', function (event) {
            (me._mouseClick)(event);
        });
        this._controlPointsController[0].addEvent('dblclick', function (event) {
            (me._mouseClick)(event);
        });

        this._controlPointsController[1].addEvent('mousedown', function (event) {
            (me._mouseDown)(event, mindplot.ControlPoint.TO, me);
        });
        this._controlPointsController[1].addEvent('click', function (event) {
            (me._mouseClick)(event);
        });
        this._controlPointsController[1].addEvent('dblclick', function (event) {
            (me._mouseClick)(event);
        });
    },

    setLine: function (line) {
        if ($defined(this._line)) {
            this._removeLine();
        }
        this._line = line;
        this._createControlPoint();
        this._endPoint = [];
        this._orignalCtrlPoint = [];
        this._orignalCtrlPoint[0] = this._controls[0].clone();
        this._orignalCtrlPoint[1] = this._controls[1].clone();
        this._endPoint[0] = this._line.getLine().getFrom().clone();
        this._endPoint[1] = this._line.getLine().getTo().clone();
    },

    redraw: function () {
        if ($defined(this._line))
            this._createControlPoint();
    },

    _createControlPoint: function () {
        this._controls = this._line.getLine().getControlPoints();
        var pos = this._line.getLine().getFrom();
        this._controlPointsController[0].setPosition(this._controls[mindplot.ControlPoint.FROM].x + pos.x, this._controls[mindplot.ControlPoint.FROM].y + pos.y - 3);
        this._controlLines[0].setFrom(pos.x, pos.y);
        this._controlLines[0].setTo(this._controls[mindplot.ControlPoint.FROM].x + pos.x + 3, this._controls[mindplot.ControlPoint.FROM].y + pos.y);
        pos = this._line.getLine().getTo();
        this._controlLines[1].setFrom(pos.x, pos.y);
        this._controlLines[1].setTo(this._controls[mindplot.ControlPoint.TO].x + pos.x + 3, this._controls[mindplot.ControlPoint.TO].y + pos.y);
        this._controlPointsController[1].setPosition(this._controls[mindplot.ControlPoint.TO].x + pos.x, this._controls[mindplot.ControlPoint.TO].y + pos.y - 3);

    },

    _removeLine: function () {

    },

    _mouseDown: function (event, point, me) {
        if (!this._isBinded) {
            this._isBinded = true;
            this._mouseMoveFunction = function (event) {
                (me._mouseMoveEvent)(event, point, me);
            };

            this._workspace.getScreenManager().addEvent('mousemove', this._mouseMoveFunction);
            this._mouseUpFunction = function (event) {
                (me._mouseUp)(event, point, me);
            };
            this._workspace.getScreenManager().addEvent('mouseup', this._mouseUpFunction);
        }
        event.preventDefault();
        event.stopPropagation();
        return false;
    },

    _mouseMoveEvent: function (event, point) {
        var screen = this._workspace.getScreenManager();
        var pos = screen.getWorkspaceMousePosition(event);

        var cords;
        if (point == 0) {
            cords = mindplot.util.Shape.calculateRelationShipPointCoordinates(this._line.getSourceTopic(), pos);
            this._line.setFrom(cords.x, cords.y);
            this._line.setSrcControlPoint(new core.Point(pos.x - cords.x, pos.y - cords.y));
        } else {
            cords = mindplot.util.Shape.calculateRelationShipPointCoordinates(this._line.getTargetTopic(), pos);
            this._line.setTo(cords.x, cords.y);
            this._line.setDestControlPoint(new core.Point(pos.x - cords.x, pos.y - cords.y));
        }

        this._controls[point].x = (pos.x - cords.x);
        this._controls[point].y = (pos.y - cords.y);
        this._controlPointsController[point].setPosition(pos.x - 5, pos.y - 3);
        this._controlLines[point].setFrom(cords.x, cords.y);
        this._controlLines[point].setTo(pos.x - 2, pos.y);
        this._line.getLine().updateLine(point);

    },

    _mouseUp: function (event, point) {
        this._workspace.getScreenManager().removeEvent('mousemove', this._mouseMoveFunction);
        this._workspace.getScreenManager().removeEvent('mouseup', this._mouseUpFunction);

        var actionDispatcher = mindplot.ActionDispatcher.getInstance();
        actionDispatcher.moveControlPoint(this, point);
        this._isBinded = false;
    },

    _mouseClick: function (event) {
        event.preventDefault();
        event.stopPropagation();
        return false;
    },

    setVisibility: function (visible) {
        if (visible) {
            this._controlLines[0].moveToFront();
            this._controlLines[1].moveToFront();
            this._controlPointsController[0].moveToFront();
            this._controlPointsController[1].moveToFront();
        }
        this._controlPointsController[0].setVisibility(visible);
        this._controlPointsController[1].setVisibility(visible);
        this._controlLines[0].setVisibility(visible);
        this._controlLines[1].setVisibility(visible);
    },

    addToWorkspace: function (workspace) {
        this._workspace = workspace;
        workspace.append(this._controlPointsController[0]);
        workspace.append(this._controlPointsController[1]);
        workspace.append(this._controlLines[0]);
        workspace.append(this._controlLines[1]);
    },

    removeFromWorkspace: function (workspace) {
        this._workspace = null;
        workspace.removeChild(this._controlPointsController[0]);
        workspace.removeChild(this._controlPointsController[1]);
        workspace.removeChild(this._controlLines[0]);
        workspace.removeChild(this._controlLines[1]);
    },

    getControlPoint: function (index) {
        return this._controls[index];
    },

    getOriginalEndPoint: function (index) {
        return this._endPoint[index];
    },

    getOriginalCtrlPoint: function (index) {
        return this._orignalCtrlPoint[index];
    }
});

mindplot.ControlPoint.FROM = 0;
mindplot.ControlPoint.TO = 1;
