
mindplot.RelationshipLine = function(sourceNode, targetNode, lineType)
{
    mindplot.ConnectionLine.call(this,sourceNode, targetNode, lineType);
    this._isOnfocus = false;
    this._focusShape = this._createLine(this.getLineType(), mindplot.ConnectionLine.SIMPLE_CURVED);
    this._focusShape.setStroke(2, "solid", "#3f96ff");
    var ctrlPoints = this._line2d.getControlPoints();
    this._focusShape.setSrcControlPoint(ctrlPoints[0]);
    this._focusShape.setDestControlPoint(ctrlPoints[1]);
    this._focusShape.setVisibility(false);
    this._onFocus = false;
    this._isInWorkspace = false;
    this._controlPointsController = new mindplot.ControlPoint();

};

objects.extend(mindplot.RelationshipLine, mindplot.ConnectionLine);

mindplot.RelationshipLine.prototype.redraw = function()
{
    var line2d = this._line2d;
    var sourceTopic = this._sourceTopic;
    var sourcePosition = sourceTopic.getPosition();

    var targetTopic = this._targetTopic;
    var targetPosition = targetTopic.getPosition();

    var sPos,tPos;
    this._line2d.setStroke(2);
    var ctrlPoints = this._line2d.getControlPoints();
    if(!core.Utils.isDefined(ctrlPoints[0].x) || !core.Utils.isDefined(ctrlPoints[1].x)){
        var defaultPoints = core.Utils.calculateDefaultControlPoints(sourceTopic.getPosition(), targetTopic.getPosition());
        ctrlPoints[0].x=defaultPoints[0].x;
        ctrlPoints[0].y=defaultPoints[0].y;
        ctrlPoints[1].x=defaultPoints[1].x;
        ctrlPoints[1].y=defaultPoints[1].y;
    }
    sPos = core.Utils.calculateRelationShipPointCoordinates(sourceTopic,ctrlPoints[0]);
    tPos = core.Utils.calculateRelationShipPointCoordinates(targetTopic,ctrlPoints[1]);

    line2d.setFrom(sPos.x, sPos.y);
    line2d.setTo(tPos.x, tPos.y);

    line2d.moveToBack();

    // Add connector ...
    this._positionateConnector(targetTopic);

    if(this.isOnFocus()){
        this._refreshSelectedShape();
    }
    this._focusShape.moveToBack();

};

mindplot.RelationshipLine.prototype.addToWorkspace = function(workspace)
{
    workspace.appendChild(this._focusShape);
    workspace.appendChild(this._controlPointsController);
    this._controlPointControllerListener =this._initializeControlPointController.bindWithEvent(this,workspace);
    this._line2d.addEventListener('click', this._controlPointControllerListener);
    this._isInWorkspace = true;

    mindplot.RelationshipLine.superClass.addToWorkspace.call(this, workspace);
};

mindplot.RelationshipLine.prototype._initializeControlPointController = function(event,workspace){
        this.setOnFocus(true);
};

mindplot.RelationshipLine.prototype.removeFromWorkspace = function(workspace){
    workspace.removeChild(this._focusShape);
    workspace.removeChild(this._controlPointsController);
    this._line2d.removeEventListener('click',this._controlPointControllerListener);
    this._isInWorkspace = false;
    mindplot.RelationshipLine.superClass.removeFromWorkspace.call(this,workspace);
};

mindplot.RelationshipLine.prototype.getType = function(){
    return mindplot.RelationshipLine.type;
};

mindplot.RelationshipLine.prototype.setOnFocus = function(focus){
    // Change focus shape
    if(focus){
        this._refreshSelectedShape();
        this._controlPointsController.setLine(this);
    }
    this._focusShape.setVisibility(focus);

    this._controlPointsController.setVisibility(focus);
    this._onFocus = focus;
};

mindplot.RelationshipLine.prototype._refreshSelectedShape = function () {
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
    //this._focusShape.setSrcControlPoint(ctrlPoints[0]);
    //this._focusShape.setDestControlPoint(ctrlPoints[1]);
};

mindplot.RelationshipLine.prototype.addEventListener = function(type, listener){
    // Translate to web 2d events ...
    if (type == 'onfocus')
    {
        type = 'mousedown';
    }

    var line = this._line2d;
    line.addEventListener(type, listener);
};

mindplot.RelationshipLine.prototype.isOnFocus = function()
{
    return this._onFocus;
};

mindplot.RelationshipLine.prototype.isInWorkspace = function(){
    return this._isInWorkspace;
};


mindplot.RelationshipLine.type = "RelationshipLine";