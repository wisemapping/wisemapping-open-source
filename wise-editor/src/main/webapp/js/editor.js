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

var designer = null;


/*
 * Disclaimer: this global variable is a temporary workaround to Mootools' Browser class
 * We need to avoid browser detection and replace it with feature detection,
 * jquery recommends: http://www.modernizr.com/
 */

Browser = {
    firefox: window.globalStorage,
    ie: document.all && !window.opera,
    ie6: !window.XMLHttpRequest,
    ie7: document.all && window.XMLHttpRequest && !XDomainRequest && !window.opera,
    ie8: document.documentMode == 8,
    ie11: document.documentMode == 11,
    opera: Boolean(window.opera),
    chrome: Boolean(window.chrome),
    safari: window.getComputedStyle && !window.globalStorage && !window.opera,
    Platform: {
        mac: navigator.platform.indexOf('Mac') != -1
    }
};

function buildDesigner(options) {

    var container = $("#" + options.container);
    $assert(container, 'container could not be null');

    // Register load events ...
    designer = new mindplot.Designer(options, container);
    designer.addEvent('loadSuccess', function () {
        window.waitDialog.close();
        window.waitDialog = null;
        window.mindmapLoadReady = true;
    });

    var onerrorFn = function (message, url, lineNo) {

        // Ignore errors ...
        if (message === "Script error." && lineNo == 0) {
            // http://stackoverflow.com/questions/5913978/cryptic-script-error-reported-in-javascript-in-chrome-and-firefox
            return;
        }

        // Transform error ...
        var errorMsg = message;
        if (typeof(message) === 'object' && message.srcElement && message.target) {
            if (message.srcElement == '[object HTMLScriptElement]' && message.target == '[object HTMLScriptElement]') {
                errorMsg = 'Error loading script';
            } else {
                errorMsg = 'Event Error - target:' + message.target + ' srcElement:' + message.srcElement;
            }
        }
        errorMsg = errorMsg.toString();

        $.ajax({
            method: 'post',
            url: "/c/restful/logger/editor",
            headers: {"Content-Type": "application/json", "Accept": "application/json"},
            data: JSON.stringify({
                jsErrorMsg: "Message: '" + errorMsg + "', line:'" + lineNo + "', url: :" + url,
                jsStack: window.event.error.stack || window.errorStack,
                userAgent: navigator.userAgent,
                mapId: options.mapId
            })
        });

        // Close loading dialog ...
        if (window.waitDialog) {
            window.waitDialog.close();
            window.waitDialog = null;
        }

        // Open error dialog only in case of mindmap loading errors. The rest of the error are reported but not display the dialog.
        // Remove this in the near future.
        if (!window.mindmapLoadReady) {
            $notifyModal($msg('UNEXPECTED_ERROR_LOADING'));
        }
    };

    window.onerror = onerrorFn;

    // Configure default persistence manager ...
    var persistence;
    if (options.persistenceManager) {
        if (options.persistenceManager instanceof String) {
            persistence = eval("new " + options.persistenceManager + "()");
        }
        else {
            persistence = options.persistenceManager;
        }

    } else {
        persistence = new mindplot.LocalStorageManager("samples/{id}.xml");
    }
    mindplot.PersistenceManager.init(persistence);

    // Register toolbar event ...
    if ($('#toolbar')) {
        var menu = new mindplot.widget.Menu(designer, 'toolbar', options.mapId, "");

        //  If a node has focus, focus can be move to another node using the keys.
        designer._cleanScreen = function () {
            menu.clear();
        };
    }

    return designer;
}


function loadDesignerOptions(jsonConf) {
    // Load map options ...
    var result;
    var me = this;
    if (jsonConf) {
        $.ajax({
            url: jsonConf,
            dataType: 'json',
            async: false,
            method: 'get',
            success: function (options) {
                me.options = options;
            }
        });
        result = this.options;
    }
    else {
        // Set workspace screen size as default. In this way, resize issues are solved.
        var containerSize = {
            height: parseInt(screen.height),
            width: parseInt(screen.width)
        };

        var viewPort = {
            height: parseInt(window.innerHeight - 70), // Footer and Header
            width: parseInt(window.innerWidth)
        };
        result = {
            readOnly: false,
            zoom: 0.85,
            saveOnLoad: true,
            size: containerSize,
            viewPort: viewPort,
            container: 'mindplot',
            locale: 'en'
        };
    }
    return result;
}

editor = {};
editor.WaitDialog = new Class({
    initialize: function () {
        this.panel = this._buildPanel();
    },

    _buildPanel: function () {
        var result = $('#load');
        var content = result.find('.modal-content');
        var winH = $(window).height();
        //Set the popup window to center
        content.css('margin-top', winH / 2 - content.height() / 2);
        return result;
    },

    show: function () {
        this.panel.modal({
            backdrop: 'static'
        });
    },

    close: function () {
        this.panel.modal('hide');
    }
});

// Show loading dialog ...
waitDialog = new editor.WaitDialog();
waitDialog.show();

// Loading libraries ...
jQuery.getScript("js/mindplot-min.js");
