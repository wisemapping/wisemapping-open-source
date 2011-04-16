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

web2d.Workspace = function(attributes)
{
    this._htmlContainer = this._createDivContainer();

    var peer = web2d.peer.Toolkit.createWorkspace(this._htmlContainer);
    var defaultAttributes = {width:'200px',height:'200px',stroke:'1px solid #edf1be',
        fillColor:"white",coordOrigin:'0 0',coordSize:'200 200' };
    for (var key in attributes)
    {
        defaultAttributes[key] = attributes[key];
    }
    web2d.Element.call(this, peer, defaultAttributes);
    this._htmlContainer.appendChild(this._peer._native);

    this._disableTextSelection();
};

objects.extend(web2d.Workspace, web2d.Element);

/**
 * Avoid element selection. This remove some odd effect in IE when a element is draged.
 */
web2d.Workspace.prototype._disableTextSelection = function()
{
    var contaier = this._htmlContainer;

    function disabletext(e) {
        return false;
    }
    ;

    function reEnable() {
        return true;
    }
    ;

    //if the browser is IE4+
    contaier.onselectstart = new Function("return false");

    //if the browser is NS6
    if (core.Utils.isDefined(window.sidebar))
    {
        contaier.onmousedown = disabletext;
        contaier.onclick = reEnable;
    }
    ;
};

web2d.Workspace.prototype.getType = function()
{
    return "Workspace";
};
/**
 * Appends an element as a child to the object.
 */
web2d.Workspace.prototype.appendChild = function(element)
{
    if (!core.Utils.isDefined(element))
    {
        throw "Child element can not be null";
    }
    var elementType = element.getType();
    if (elementType == null)
    {
        throw "It seems not to be an element ->" + element;
    }

    if (elementType == "Workspace")
    {
        throw "A workspace can not have a workspace as a child";
    }

    this._peer.appendChild(element._peer);
};

/**
 * @todo: Write doc.
 */
web2d.Workspace.prototype.addItAsChildTo = function(element)
{
    if (!core.Utils.isDefined(element))
    {
        throw "Workspace div container can not be null";
    }
    element.appendChild(this._htmlContainer);
};

/**
 * Create a new div element that will be resposible for containing the workspace elements.
 */
web2d.Workspace.prototype._createDivContainer = function(domElement)
{
    var container = window.document.createElement("div");
    container.id = "workspaceContainer";
    container.style.overflow = "hidden";
    container.style.position = "relative";
    container.style.top = "0px";
    container.style.left = "0px";
    container.style.height = "688px";
    container.style.border = '1px solid red';

    return container;
};

/**
 *  Set the workspace area size. It can be defined using different units:
 * in (inches; 1in=2.54cm)
 * cm (centimeters; 1cm=10mm)
 * mm (millimeters)
 * pt (points; 1pt=1/72in)
 * pc (picas; 1pc=12pt)
 */
web2d.Workspace.prototype.setSize = function(width, height)
{
    // HTML container must have the size of the group element.
    if (core.Utils.isDefined(width))
    {
        this._htmlContainer.style.width = width;

    }

    if (core.Utils.isDefined(height))
    {
        this._htmlContainer.style.height = height;
    }
    this._peer.setSize(width, height);
};

/**
 * The workspace element is a containing blocks for this content - they define a CSS2 "block level box".
 * Inside the containing block a local coordinate system is defined for any sub-elements using the coordsize and coordorigin attributes.
 * All CSS2 positioning information is expressed in terms of this local coordinate space.
 * Consequently CSS2 position attributes (left, top, width, height and so on) have no unit specifier -
 * they are simple numbers, not CSS length quantities.
 */
web2d.Workspace.prototype.setCoordSize = function(width, height)
{
    this._peer.setCoordSize(width, height);
};

/**
 * @Todo: Complete Doc
 */
web2d.Workspace.prototype.setCoordOrigin = function(x, y)
{
    this._peer.setCoordOrigin(x, y);
};

/**
 * @Todo: Complete Doc
 */
web2d.Workspace.prototype.getCoordOrigin = function()
{
    return this._peer.getCoordOrigin();
};


// Private method declaration area
/**
 * All the SVG elements will be children of this HTML element.
 */
web2d.Workspace.prototype._getHtmlContainer = function()
{
    return this._htmlContainer;
};

web2d.Workspace.prototype.setFill = function(color, opacity)
{
    this._htmlContainer.style.backgroundColor = color;
    if (opacity || opacity === 0)
    {
        throw "Unsupported operation. Opacity not supported.";
    }
};

web2d.Workspace.prototype.getFill = function()
{
    var color = this._htmlContainer.style.backgroundColor;
    return {color:color};
};

/**
 * @Todo: Complete Doc
 */
web2d.Workspace.prototype.getSize = function()
{
    var width = this._htmlContainer.style.width;
    var height = this._htmlContainer.style.height;
    return {width:width,height:height};
};

web2d.Workspace.prototype.setStroke = function(width, style, color, opacity)
{
    if (style != 'solid')
    {
        throw 'Not supported style stroke style:' + style;
    }
    this._htmlContainer.style.border = width + ' ' + style + ' ' + color;

    if (opacity || opacity === 0)
    {
        throw "Unsupported operation. Opacity not supported.";
    }
};


web2d.Workspace.prototype.getCoordSize = function()
{
    return this._peer.getCoordSize();
};

/**
 * Remove an element as a child to the object.
 */
web2d.Workspace.prototype.removeChild = function(element)
{
    if (!core.Utils.isDefined(element))
    {
        throw "Child element can not be null";
    }

    if (element == this)
    {
        throw "It's not posible to add the group as a child of itself";
    }

    var elementType = element.getType();
    if (elementType == null)
    {
        throw "It seems not to be an element ->" + element;
    }

    this._peer.removeChild(element._peer);
};


web2d.Workspace.prototype.dumpNativeChart = function()
{
    var elem = this._htmlContainer
    return elem.innerHTML;
};