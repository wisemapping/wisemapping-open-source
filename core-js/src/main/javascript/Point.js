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

core.Point = function(x, y)
{
    this.x = x;
    this.y = y;
};

core.Point.prototype.setValue = function(x, y)
{
    this.x = x;
    this.y = y;
};

core.Point.prototype.inspect = function()
{
    return "{x:" + this.x + ",y:" + this.y + "}";
};

core.Point.prototype.clone = function()
{
    return new core.Point(this.x, this.y);
};

core.Point.fromString = function(point) {
    var values = point.split(',');
    return new core.Point(values[0], values[1]);
};
