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

core.ColorPicker = function()
{
    this.palette = "7x10";
    this._palettes = {"7x10": [["fff", "fcc", "fc9", "ff9", "ffc", "9f9", "9ff", "cff", "ccf", "fcf"],
            ["ccc", "f66", "f96", "ff6", "ff3", "6f9", "3ff", "6ff", "99f", "f9f"],
            ["c0c0c0", "f00", "f90", "fc6", "ff0", "3f3", "6cc", "3cf", "66c", "c6c"],
            ["999", "c00", "f60", "fc3", "fc0", "3c0", "0cc", "36f", "63f", "c3c"],
            ["666", "900", "c60", "c93", "990", "090", "399", "33f", "60c", "939"],
            ["333", "600", "930", "963", "660", "060", "366", "009", "339", "636"],
            ["000", "300", "630", "633", "330", "030", "033", "006", "309", "303"]],

        "3x4": [["ffffff"/*white*/, "00ff00"/*lime*/, "008000"/*green*/, "0000ff"/*blue*/],
                ["c0c0c0"/*silver*/, "ffff00"/*yellow*/, "ff00ff"/*fuchsia*/, "000080"/*navy*/],
                ["808080"/*gray*/, "ff0000"/*red*/, "800080"/*purple*/, "000000"/*black*/]]
        //["00ffff"/*aqua*/, "808000"/*olive*/, "800000"/*maroon*/, "008080"/*teal*/]];
    };
};

core.ColorPicker.buildRendering = function ()
{
    this.domNode = document.createElement("table");
    //		dojo.html.disableSelection(this.domNode);
    //		dojo.event.connect(this.domNode, "onmousedown", function (e) {
    //			e.preventDefault();
    //		});
    with (this.domNode) { // set the table's properties
        cellPadding = "0";
        cellSpacing = "1";
        border = "1";
        style.backgroundColor = "white";
    }
    var colors = this._palettes[this.palette];
    for (var i = 0; i < colors.length; i++) {
        var tr = this.domNode.insertRow(-1);
        for (var j = 0; j < colors[i].length; j++) {
            if (colors[i][j].length == 3) {
                colors[i][j] = colors[i][j].replace(/(.)(.)(.)/, "$1$1$2$2$3$3");
            }

            var td = tr.insertCell(-1);
            with (td.style) {
                backgroundColor = "#" + colors[i][j];
                border = "1px solid gray";
                width = height = "15px";
                fontSize = "1px";
            }

            td.color = "#" + colors[i][j];

            td.onmouseover = function (e) {
                this.style.borderColor = "white";
            };
            td.onmouseout = function (e) {
                this.style.borderColor = "gray";
            };
            //				dojo.event.connect(td, "onmousedown", this, "onClick");

            td.innerHTML = "&nbsp;";
        }
    }
};

core.ColorPicker.onClick = function(/*Event*/ e)
{
    this.onColorSelect(e.currentTarget.color);
    e.currentTarget.style.borderColor = "gray";
};

core.ColorPicker.onColorSelect = function(color)
{
    // summary:
    //		Callback when a color is selected.
    // color: String
    //		Hex value corresponding to color.
};

