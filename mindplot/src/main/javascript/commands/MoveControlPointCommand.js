mindplot.commands.MoveControlPointCommand = mindplot.Command.extend(
{
    initialize: function(ctrlPointControler, point)
    {
        core.assert(ctrlPointControler, 'line can not be null');
        this._ctrlPointControler = ctrlPointControler;
        this._id = mindplot.Command._nextUUID();
        this._wasCustom=false;
        this._point = point;
        this._controlPoint = null;
    },
    execute: function(commandContext)
    {
        var line = this._ctrlPointControler._line;
        var ctrlPoints = line.getLine().getControlPoints();
        var model = line.getModel();
        var point = null;
        switch (this._point){
            case 0:
                if(core.Utils.isDefined(model.getSrcCtrlPoint())){
                    this._controlPoint= model.getSrcCtrlPoint().clone();
                }
                model.setSrcCtrlPoint(ctrlPoints[0].clone());
                this._wasCustom = line.getLine().isSrcControlPointCustom();
                line.getLine().setIsSrcControlPointCustom(true);
                break;
            case 1:
                if(core.Utils.isDefined(model.getDestCtrlPoint())){
                    this._controlPoint = model.getDestCtrlPoint().clone();
                }
                model.setDestCtrlPoint(ctrlPoints[1].clone());
                this._wasCustom = line.getLine().isDestControlPointCustom();
                line.getLine().setIsDestControlPointCustom(true);
                break;
        }
    },
    undoExecute: function(commandContext)
    {
        var line = this._ctrlPointControler._line;
        var model = line.getModel();
        switch (this._point){
            case 0:
                if(core.Utils.isDefined(this._controlPoint)){
                    model.setSrcCtrlPoint(this._controlPoint.clone());
                    line.getLine().setSrcControlPoint(this._controlPoint.clone());
                    line.getLine().setIsSrcControlPointCustom(this._wasCustom);
                }
            break;
            case 1:
                if(core.Utils.isDefined(this._controlPoint)){
                    model.setDestCtrlPoint(this._controlPoint.clone());
                    line.getLine().setDestControlPoint(this._controlPoint.clone());
                    line.getLine().setIsDestControlPointCustom(this._wasCustom);
                }
            break;
        }
        this._ctrlPointControler.setLine(line);
    }
});