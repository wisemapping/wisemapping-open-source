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
mindplot.Relationship = new Class({
    Extends: mindplot.ConnectionLine,
    Static: {
        getStrokeColor: function () {
            return '#9b74e6';
        },
        type: "Relationship"
    },
    initialize: function (sourceNode, targetNode, model) {
        $assert(sourceNode, "sourceNode can not be null");
        $assert(targetNode, "targetNode can not be null");

        this.parent(sourceNode, targetNode, model.getLineType());
        this.setModel(model);

        var strokeColor = mindplot.Relationship.getStrokeColor();

        this._line2d.setIsSrcControlPointCustom(false);
        this._line2d.setIsDestControlPointCustom(false);
        this._line2d.setCursor('pointer');
        this._line2d.setStroke(1, 'solid', strokeColor);
        this._line2d.setDashed(4, 2);
        this._focusShape = this._createLine(this.getLineType(), mindplot.ConnectionLine.SIMPLE_CURVED);
        this._focusShape.setStroke(2, "solid", "#3f96ff");

        var ctrlPoints = this._line2d.getControlPoints();
        this._focusShape.setSrcControlPoint(ctrlPoints[0]);
        this._focusShape.setDestControlPoint(ctrlPoints[1]);
        this._focusShape.setVisibility(false);
        this._onFocus = false;
        this._isInWorkspace = false;
        this._controlPointsController = new mindplot.ControlPoint();

        this._startArrow = new web2d.Arrow();
        this._startArrow.setStrokeColor(strokeColor);
        this._startArrow.setStrokeWidth(2);
        this.setShowStartArrow(true);

        // Share style is disable ...
        if (this._showEndArrow) {
            this._endArrow = new web2d.Arrow();
            this._endArrow.setStrokeColor(strokeColor);
            this._endArrow.setStrokeWidth(2);
        }

        // Position the line ...
        if ($defined(model.getSrcCtrlPoint())) {
            var srcPoint = model.getSrcCtrlPoint().clone();
            this.setSrcControlPoint(srcPoint);
        }
        if ($defined(model.getDestCtrlPoint())) {
            var destPoint = model.getDestCtrlPoint().clone();
            this.setDestControlPoint(destPoint);
        }
    },

    setStroke: function (color, style, opacity) {
        this.parent(color, style, opacity);
        this._startArrow.setStrokeColor(color);
    },

    redraw: function () {
        var line2d = this._line2d;
        var sourceTopic = this._sourceTopic;
        var sourcePosition = sourceTopic.getPosition();

        var targetTopic = this._targetTopic;
        var targetPosition = targetTopic.getPosition();
        if (targetTopic.getType() == mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
            targetPosition = mindplot.util.Shape.workoutIncomingConnectionPoint(targetTopic, sourcePosition);
        }

        var sPos, tPos;
        this._line2d.setStroke(2);
        var ctrlPoints = this._line2d.getControlPoints();
        if (!this._line2d.isDestControlPointCustom() && !this._line2d.isSrcControlPointCustom()) {

            var defaultPoints = mindplot.util.Shape.calculateDefaultControlPoints(sourcePosition, targetPosition);
            ctrlPoints[0].x = defaultPoints[0].x;
            ctrlPoints[0].y = defaultPoints[0].y;

            ctrlPoints[1].x = defaultPoints[1].x;
            ctrlPoints[1].y = defaultPoints[1].y;
        }
        var spoint = new core.Point();
        spoint.x = parseInt(ctrlPoints[0].x) + parseInt(sourcePosition.x);
        spoint.y = parseInt(ctrlPoints[0].y) + parseInt(sourcePosition.y);

        var tpoint = new core.Point();
        tpoint.x = parseInt(ctrlPoints[1].x) + parseInt(targetPosition.x);
        tpoint.y = parseInt(ctrlPoints[1].y) + parseInt(targetPosition.y);

        sPos = mindplot.util.Shape.calculateRelationShipPointCoordinates(sourceTopic, spoint);
        tPos = mindplot.util.Shape.calculateRelationShipPointCoordinates(targetTopic, tpoint);

        line2d.setFrom(sPos.x, sPos.y);
        line2d.setTo(tPos.x, tPos.y);

        line2d.moveToFront();

        //Positionate Arrows
        this._positionArrows();

        // Add connector ...
        this._positionateConnector(targetTopic);

        if (this.isOnFocus()) {
            this._refreshShape();
        }
        this._focusShape.moveToBack();
        this._controlPointsController.redraw();
    },

    _positionArrows: function () {
        var tpos = this._line2d.getTo();
        var spos = this._line2d.getFrom();

        this._startArrow.setFrom(spos.x, spos.y);
        this._startArrow.moveToBack();

        if (this._endArrow) {
            this._endArrow.setFrom(tpos.x, tpos.y);
            this._endArrow.moveToBack();
        }

        if (this._line2d.getType() == "CurvedLine") {
            var controlPoints = this._line2d.getControlPoints();
            this._startArrow.setControlPoint(controlPoints[0]);
            if (this._endArrow) {
                this._endArrow.setControlPoint(controlPoints[1]);
            }
        } else {
            this._startArrow.setControlPoint(this._line2d.getTo());
            if (this._endArrow) {
                this._endArrow.setControlPoint(this._line2d.getFrom());
            }
        }

        if (this._showEndArrow) {
            this._endArrow.setVisibility(this.isVisible());
        }
        this._startArrow.setVisibility(this.isVisible() && this._showStartArrow);
    },

    addToWorkspace: function (workspace) {
        workspace.append(this._focusShape);
        workspace.append(this._controlPointsController);

        this._controlPointControllerListener = this._initializeControlPointController.bind(this);
        this._line2d.addEvent('click', this._controlPointControllerListener);
        this._isInWorkspace = true;

        workspace.append(this._startArrow);
        if (this._endArrow)
            workspace.append(this._endArrow);

        this.parent(workspace);
        this._positionArrows();
        this.redraw();

    },

    _initializeControlPointController: function () {
        this.setOnFocus(true);
    },

    removeFromWorkspace: function (workspace) {
        workspace.removeChild(this._focusShape);
        workspace.removeChild(this._controlPointsController);
        this._line2d.removeEvent('click', this._controlPointControllerListener);
        this._isInWorkspace = false;
        workspace.removeChild(this._startArrow);
        if (this._endArrow)
            workspace.removeChild(this._endArrow);

        this.parent(workspace);
    },

    getType: function () {
        return mindplot.Relationship.type;
    },

    setOnFocus: function (focus) {
        // Change focus shape
        if (this.isOnFocus() != focus) {
            if (focus) {
                this._refreshShape();
                this._controlPointsController.setLine(this);
            }
            this._focusShape.setVisibility(focus);

            this._controlPointsController.setVisibility(focus);
            this._onFocus = focus;
            this.fireEvent(focus ? 'ontfocus' : 'ontblur', this);
        }
    },

    _refreshShape: function () {
        var sPos = this._line2d.getFrom();
        var tPos = this._line2d.getTo();
        var ctrlPoints = this._line2d.getControlPoints();
        this._focusShape.setFrom(sPos.x, sPos.y);
        this._focusShape.setTo(tPos.x, tPos.y);
        var shapeCtrlPoints = this._focusShape.getControlPoints();
        shapeCtrlPoints[0].x = ctrlPoints[0].x;
        shapeCtrlPoints[0].y = ctrlPoints[0].y;
        shapeCtrlPoints[1].x = ctrlPoints[1].x;
        shapeCtrlPoints[1].y = ctrlPoints[1].y;
        this._focusShape.updateLine();
    },

    addEvent: function (type, listener) {
        // Translate to web 2d events ...
        if (type == 'onfocus') {
            type = 'mousedown';
        }

        var line = this._line2d;
        line.addEvent(type, listener);
    },

    isOnFocus: function () {
        return this._onFocus;
    },

    isInWorkspace: function () {
        return this._isInWorkspace;
    },

    setVisibility: function (value) {
        this.parent(value);
        if (this._showEndArrow)
            this._endArrow.setVisibility(this._showEndArrow);
        this._startArrow.setVisibility(this._showStartArrow && value);
    },

    setOpacity: function (opacity) {
        this.parent(opacity);
        if (this._showEndArrow)
            this._endArrow.setOpacity(opacity);
        if (this._showStartArrow)
            this._startArrow.setOpacity(opacity);
    },

    setShowEndArrow: function (visible) {
        this._showEndArrow = visible;
        if (this._isInWorkspace)
            this.redraw();
    },

    setShowStartArrow: function (visible) {
        this._showStartArrow = visible;
        if (this._isInWorkspace)
            this.redraw();
    },

    setFrom: function (x, y) {
        $assert($defined(x), "x must be defined");
        $assert($defined(y), "y must be defined");

        this._line2d.setFrom(x, y);
        this._startArrow.setFrom(x, y);
    },

    setTo: function (x, y) {
        $assert($defined(x), "x must be defined");
        $assert($defined(y), "y must be defined");

        this._line2d.setTo(x, y);
        if (this._endArrow)
            this._endArrow.setFrom(x, y);
    },

    setSrcControlPoint: function (control) {
        this._line2d.setSrcControlPoint(control);
        this._startArrow.setControlPoint(control);
    },

    setDestControlPoint: function (control) {
        this._line2d.setDestControlPoint(control);
        if (this._showEndArrow)
            this._endArrow.setControlPoint(control);
    },

    getControlPoints: function () {
        return this._line2d.getControlPoints();
    },

    isSrcControlPointCustom: function () {
        return this._line2d.isSrcControlPointCustom();
    },

    isDestControlPointCustom: function () {
        return this._line2d.isDestControlPointCustom();
    },

    setIsSrcControlPointCustom: function (isCustom) {
        this._line2d.setIsSrcControlPointCustom(isCustom);
    },

    setIsDestControlPointCustom: function (isCustom) {
        this._line2d.setIsDestControlPointCustom(isCustom);
    },

    getId: function () {
        return this._model.getId();
    },

    fireEvent: function (type, event) {
        var elem = this._line2d;
        elem.trigger(type, event);
    }
});
