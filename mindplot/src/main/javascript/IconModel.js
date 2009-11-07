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

mindplot.IconModel = function(iconType, topic)
{
    core.assert(iconType, 'Icon id can not be null');
    core.assert(topic, 'topic can not be null');
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
    if (!this._uuid)
    {
        this._uuid = 0;
    }

    this._uuid = this._uuid + 1;
    return this._uuid;
};

