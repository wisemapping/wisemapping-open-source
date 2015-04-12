/*
 *    Copyright [2015] [wisemapping]
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

Grid = function (parent, colums, rows) {
    var cellSize = "10px";
    this._parent = parent;
    this._container = this._createContainer();
    var tbody = $(this._container.firstChild.firstChild);
    for (var i = 0; i < rows; i++) {
        var trElement = $('<tr></tr>');
        for (var j = 0; j < colums; j++) {
            var tdElement = $('<td></td>');
            tdElement.css({
                    width: cellSize,
                    height: cellSize,
                    borderWidth: "1px",
                    borderStyle: "dashed",
                    borderColor: "lightsteelblue"}
            );
            trElement.append(tdElement);
        }
        tbody.append(trElement);
    }
};

Grid.prototype.setPosition = function (x, y) {
    this._container.style.left = x;
    this._container.style.top = y;
};

Grid.prototype.render = function () {
    $(this._parent).append(this._container);
};

Grid.prototype._createContainer = function () {
    var result = window.document.createElement("div");
    result.style.tableLayout = "fixed";
    result.style.borderCollapse = "collapse";
    result.style.emptyCells = "show";
    result.style.position = "absolute";
    result.innerHTML = '<table style="table-layout:fixed;border-collapse:collapse;empty-cells:show;"><tbody id="tableBody"></tbody></table>';
    return  result;
};

