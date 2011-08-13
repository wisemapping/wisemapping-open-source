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

mindplot.Workspace = new Class({
    initialize: function(profile, screenManager, zoom) {
        // Create a suitable container ...
        $assert(screenManager, 'Div container can not be null');
        this._zoom = zoom;
        this._screenManager = screenManager;
        this._screenWidth = profile.width;
        this._screenHeight = profile.height;

        // Initalize web2d workspace.
        var workspace = this._createWorkspace(profile);
        this._workspace = workspace;

        var screenContainer = screenManager.getContainer();
        // Fix the height of the container ....
        screenContainer.style.height = this._screenHeight + "px";

        // Append to the workspace...
        workspace.addItAsChildTo(screenContainer);
        this.setZoom(zoom, true);

        // Register drag events ...
        this._registerDragEvents();

        this._eventsEnabled = true;

    },

    _updateScreenManager: function() {
        var zoom = this._zoom;
        this._screenManager.setScale(zoom);

        var coordOriginX = -((this._screenWidth * this._zoom) / 2);
        var coordOriginY = -((this._screenHeight * this._zoom) / 2);
        this._screenManager.setOffset(coordOriginX, coordOriginY);
    },

    _createWorkspace: function(profile) {
        // Initialize workspace ...
        var coordOriginX = -(this._screenWidth / 2);
        var coordOriginY = -(this._screenHeight / 2);

        var workspaceProfile = {
            width: this._screenWidth + "px",
            height: this._screenHeight + "px",
            coordSizeWidth:this._screenWidth,
            coordSizeHeight:this._screenHeight,
            coordOriginX:coordOriginX,
            coordOriginY:coordOriginY,
            fillColor:'transparent',
            strokeWidth:0
        };
        web2d.peer.Toolkit.init();
        return  new web2d.Workspace(workspaceProfile);
    },

    appendChild: function(shape) {
        if ($defined(shape.addToWorkspace)) {
            shape.addToWorkspace(this);
        } else {
            this._workspace.appendChild(shape);
        }
    },

    removeChild: function(shape) {
        // Element is a node, not a web2d element?
        if ($defined(shape.removeFromWorkspace)) {
            shape.removeFromWorkspace(this);
        } else {
            this._workspace.removeChild(shape);
        }
    },

    addEventListener: function(type, listener) {
        this._workspace.addEventListener(type, listener);
    },

    removeEventListener: function(type, listener) {
        this._workspace.removeEventListener(type, listener);
    },

    getSize: function() {
        return this._workspace.getCoordSize();
    },

    setZoom: function(zoom, center) {
        this._zoom = zoom;
        var workspace = this._workspace;

        // Update coord scale...
        var coordWidth = zoom * this._screenWidth;
        var coordHeight = zoom * this._screenHeight;
        workspace.setCoordSize(coordWidth, coordHeight);

        // Center topic....
        var coordOriginX;
        var coordOriginY;
        if (center) {
            coordOriginX = -(coordWidth / 2);
            coordOriginY = -(coordHeight / 2);
        } else {
            var coordOrigin = workspace.getCoordOrigin();
            coordOriginX = coordOrigin.x;
            coordOriginY = coordOrigin.y;
        }

        workspace.setCoordOrigin(coordOriginX, coordOriginY);

        // Update screen.
        this._screenManager.setOffset(coordOriginX, coordOriginY);
        this._screenManager.setScale(zoom);
    },

    getScreenManager: function() {
        return this._screenManager;
    },

    enableWorkspaceEvents: function(value) {
        this._eventsEnabled = value;
    },

    isWorkspaceEventsEnabled: function() {
        return this._eventsEnabled;
    },

    dumpNativeChart: function() {
        return this._workspace.dumpNativeChart();
    },

    _registerDragEvents: function() {
        var workspace = this._workspace;
        var screenManager = this._screenManager;
        var mWorkspace = this;
        var mouseDownListener = function(event) {
            if (!$defined(workspace._mouseMoveListener))
            {
                if (mWorkspace.isWorkspaceEventsEnabled())
                {
                    mWorkspace.enableWorkspaceEvents(false);

                    var mouseDownPosition = screenManager.getWorkspaceMousePosition(event);
                    var originalCoordOrigin = workspace.getCoordOrigin();

                    var wasDragged = false;
                    workspace._mouseMoveListener = function(event) {

                        var currentMousePosition = screenManager.getWorkspaceMousePosition(event);

                        var offsetX = currentMousePosition.x - mouseDownPosition.x;
                        var coordOriginX = -offsetX + originalCoordOrigin.x;

                        var offsetY = currentMousePosition.y - mouseDownPosition.y;
                        var coordOriginY = -offsetY + originalCoordOrigin.y;

                        workspace.setCoordOrigin(coordOriginX, coordOriginY);

                        // Change cursor.
                        if (Browser.firefox) {
                            window.document.body.style.cursor = "-moz-grabbing";
                        } else {
                            window.document.body.style.cursor = "move";
                        }
                        event.preventDefault();

                        // Fire drag event ...
                        screenManager.fireEvent('drag',new Event());
                        wasDragged = true;

                        
                    }.bind(this);
                    screenManager.addEvent('mousemove', workspace._mouseMoveListener);

                    // Register mouse up listeners ...
                    workspace._mouseUpListener = function(event) {

                        screenManager.removeEvent('mousemove', workspace._mouseMoveListener);
                        screenManager.removeEvent('mouseup', workspace._mouseUpListener);
                        workspace._mouseUpListener = null;
                        workspace._mouseMoveListener = null;
                        window.document.body.style.cursor = 'default';

                        // Update screen manager offset.
                        var coordOrigin = workspace.getCoordOrigin();
                        screenManager.setOffset(coordOrigin.x, coordOrigin.y);
                        mWorkspace.enableWorkspaceEvents(true);

                        if(!wasDragged)
                        {
                            screenManager.fireEvent('click',new Event());
                        }
                    };
                    screenManager.addEvent('mouseup', workspace._mouseUpListener);
                }
            } else {
                workspace._mouseUpListener();
            }
        };
        screenManager.addEvent('mousedown', mouseDownListener);
    }
});


