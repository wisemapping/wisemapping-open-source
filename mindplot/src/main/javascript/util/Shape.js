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

mindplot.util.Shape =
{
    isAtRight: function(sourcePoint, targetPoint)
    {
        core.assert(sourcePoint, "Source can not be null");
        core.assert(targetPoint, "Target can not be null");
        return sourcePoint.x < targetPoint.x;
    },
    workoutDistance: function(sourceNode, targetNode)
    {
        var sPos = sourceNode.getPosition();
        var tPos = targetNode.getPosition();

        var x = tPos.x - sPos.x;
        var y = tPos.y - sPos.y;

        var hip = y * y + x * x;
        return hip;
    },
    calculateRectConnectionPoint: function(rectCenterPoint, rectSize, isAtRight)
    {
        core.assert(rectCenterPoint, 'rectCenterPoint can  not be null');
        core.assert(rectSize, 'rectSize can  not be null');
        core.assert(core.Utils.isDefined(isAtRight), 'isRight can  not be null');

        // Node is placed at the right ?
        var result = new core.Point();

        // This is used fix a minor difference ...z
        var correctionHardcode = 2;
        if (isAtRight)
        {
            result.setValue(rectCenterPoint.x - (rectSize.width / 2) + correctionHardcode, rectCenterPoint.y);
        } else
        {
            result.setValue(parseFloat(rectCenterPoint.x) + (rectSize.width / 2) - correctionHardcode, rectCenterPoint.y);
        }

        return result;
    },
    _getRectShapeOffset : function(sourceTopic, targetTopic)
    {

        var tPos = targetTopic.getPosition();
        var sPos = sourceTopic.getPosition();

        var tSize = targetTopic.getSize();

        var x = sPos.x - tPos.x;
        var y = sPos.y - tPos.y;

        var gradient = 0;
        if (x)
        {
            gradient = y / x;
        }

        var area = this._getSector(gradient, x, y);
        var xOff = -1;
        var yOff = -1;
        if (area == 1 || area == 3)
        {
            xOff = tSize.width / 2;
            yOff = xOff * gradient;

            xOff = xOff * ((x < 0) ? -1 : 1);
            yOff = yOff * ((x < 0) ? -1 : 1);


        } else
        {
            yOff = tSize.height / 2;
            xOff = yOff / gradient;

            yOff = yOff * ((y < 0) ? -1 : 1);
            xOff = xOff * ((y < 0) ? -1 : 1);
        }


        // Controll boundaries.
        if (Math.abs(xOff) > tSize.width / 2)
        {
            xOff = ((tSize.width / 2) * Math.sign(xOff));
        }

        if (Math.abs(yOff) > tSize.height / 2)
        {
            yOff = ((tSize.height / 2) * Math.sign(yOff));
        }

        return {x:xOff,y:yOff};
    },

/**
 *  Sector are numered following the clockwise direction.
 */
    _getSector : function(gradient, x, y)
    {
        var result;
        if (gradient < 0.5 && gradient > -0.5)
        {
            // Sector 1 and 3
            if (x >= 0)
            {
                result = 1;
            } else
            {
                result = 3;
            }

        } else
        {
            // Sector 2 and 4
            if (y <= 0)
            {
                result = 4;
            } else
            {
                result = 2;
            }
        }

        return result;
    }
};

