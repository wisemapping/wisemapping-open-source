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

var Menu = {
    init: function ()
    {
        $ES('.subMenu2', $('mydocs')).each(function(el) {
            var parent = el.getParent();
            var button = $E('.button', parent);
            if (button.getTag() == 'div')
            {
                el.setStyle('top', '25px');
                el.setStyle('left', '7px');
            }
            button.addEvent('click', this.updateSubMenu.bindWithEvent(this, [button, el]));
            button.state = "closed";
        }, this);

        this.currentElement = null;
        this.currentItem = null;
        $(document).addEvent('click', this.hide.bindWithEvent(this));
    },
    updateSubMenu: function(event, src, el)
    {
        if (src.state == "open")
        {
            el.setStyle("visibility", "hidden");
            src.state = "closed";
            this.currentElement = null;
            this.currentItem = null;
        }
        else
        {
            if (this.currentElement != null)
            {
                this.hide.attempt(null, this);
            }
            this.currentElement = el;
            this.currentItem = src;
            el.setStyle("visibility", "visible");
            src.state = "open";
        }
        if (event != null)
        {
            var evt = new Event(event);
            evt.stopPropagation();
        }
    },
    hide: function(event)
    {
        if (this.currentElement != null)
        {
            this.updateSubMenu.attempt([event, this.currentItem, this.currentElement], this);
        }
    }
};

Window.onDomReady(Menu.init.bind(Menu));