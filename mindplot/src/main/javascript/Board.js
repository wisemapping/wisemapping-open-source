/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under the Apache License, Version 2.0 (the "License") plus the
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

mindplot.Board = function(defaultHeight, referencePoint)
{
    this.initialize(defaultHeight, referencePoint);
};

mindplot.Board.prototype.initialize = function(defaultHeight, referencePoint)
{
    core.assert(referencePoint, "referencePoint can not be null");
    this._defaultWidth = defaultHeight;
    this._entries = new mindplot.BidirectionalArray();
    this._referencePoint = referencePoint;
};

mindplot.Board.prototype.getReferencePoint = function()
{
    return this._referencePoint;
};

/**
 * ---------------------------------------
 */
mindplot.BidirectionalArray = function()
{
    this._leftElem = [];
    this._rightElem = [];
};

mindplot.BidirectionalArray.prototype.get = function(index, sign)
{
    core.assert(core.Utils.isDefined(index), 'Illegal argument, index must be passed.');
    if (core.Utils.isDefined(sign))
    {
        core.assert(index >= 0, 'Illegal absIndex value');
        index = index * sign;
    }

    var result = null;
    if (index >= 0 && index < this._rightElem.length)
    {
        result = this._rightElem[index];
    } else if (index < 0 && Math.abs(index) < this._leftElem.length)
    {
        result = this._leftElem[Math.abs(index)];
    }
    return result;
};

mindplot.BidirectionalArray.prototype.set = function(index, elem)
{
    core.assert(core.Utils.isDefined(index), 'Illegal index value');

    var array = (index >= 0) ? this._rightElem : this._leftElem;
    array[Math.abs(index)] = elem;
};

mindplot.BidirectionalArray.prototype.length = function(index)
{
    core.assert(core.Utils.isDefined(index), 'Illegal index value');
    return (index >= 0) ? this._rightElem.length : this._leftElem.length;
};

mindplot.BidirectionalArray.prototype.upperLength = function()
{
    return this.length(1);
};

mindplot.BidirectionalArray.prototype.lowerLength = function()
{
    return this.length(-1);
};

mindplot.BidirectionalArray.prototype.inspect = function()
{
    var result = '{';
    var lenght = this._leftElem.length;
    for (var i = 0; i < lenght; i++)
    {
        var entry = this._leftElem[lenght - i - 1];
        if (entry != null)
        {
            if (i != 0)
            {
                result += ', ';
            }
            result += entry.inspect();
        }
    }

    lenght = this._rightElem.length;
    for (var i = 0; i < lenght; i++)
    {
        var entry = this._rightElem[i];
        if (entry != null)
        {
            if (i != 0)
            {
                result += ', ';
            }
            result += entry.inspect();
        }
    }
    result += '}';

    return result;

};