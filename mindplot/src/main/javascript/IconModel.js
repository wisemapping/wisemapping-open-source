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

mindplot.IconModel = function(iconType, topic)
{
    $assert(iconType, 'Icon id can not be null');
    $assert(topic, 'topic can not be null');
    this._iconType = iconType;
    this._id = mindplot.IconModel._nextUUID();
    this._topic = topic;
};

mindplot.IconModel.prototype.getId = function()
{
    return this._id;
};

mindplot.IconModel.prototype.getIconType = function()
{
    return this._iconType;
};


mindplot.IconModel.prototype.setIconType = function(iconType)
{
    this._iconType = iconType;
};

mindplot.IconModel.prototype.getTopic = function()
{
    return this._topic;
};

mindplot.IconModel.prototype.isIconModel = function()
{
    return true;
};


/**
 * @todo: This method must be implemented.
 */
mindplot.IconModel._nextUUID = function()
{
    if (!$defined(this._uuid))
    {
        this._uuid = 0;
    }

    this._uuid = this._uuid + 1;
    return this._uuid;
};

