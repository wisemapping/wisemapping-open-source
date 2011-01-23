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

web2d.EventDispatcher = function(element)
{
    this._listeners = [];
    var dispatcher = this;

    this.eventListener = function(event)
    {
        for (var i = 0; i < dispatcher._listeners.length; i++)
        {
            if (dispatcher._listeners[i] != null)
            {
                dispatcher._listeners[i].call(element, event || window.event);
            }
        }
    };
};

web2d.EventDispatcher.prototype.addListener = function(type, listener)
{
    if (!listener)
    {
        throw "Listener can not be null.";
    }
    this._listeners.include(listener);
};

web2d.EventDispatcher.prototype.removeListener = function(type, listener)
{
    if (!listener)
    {
        throw "Listener can not be null.";
    }

    //    var found = false;
    var length = this._listeners.length;

    this._listeners.remove(listener);

    var newLength = this._listeners.length;

    if (newLength >= length)
    {
        throw "There is not listener to remove";
    }
    /*this._listeners = this._listeners.reject(function(iter)
    {
        if (iter == listener)
        {
            found = true;
        }
        return iter == listener;
    });

    // Could I remove any listener ?
    if (!found)
    {
        throw "There is not listener to remove";
    }*/

};

web2d.EventDispatcher.prototype.getListenersCount = function()
{
    return this._listeners.length;
};
