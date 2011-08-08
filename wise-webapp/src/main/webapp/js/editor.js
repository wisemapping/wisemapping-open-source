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
Asset.javascript('../js/mindplot.svg.js', {
    id: 'MindplotSVGLib',
    onLoad: function() {
        afterMindpotLibraryLoading();
    }
});

var designer = null;
/* JavaScript tabs changer */

// CSS helper functions
CSS = {
    // Adds a class to an element.
    AddClass: function (e, c) {
        if (!e.className.match(new RegExp("\\b" + c + "\\b", "i")))
            e.className += (e.className ? " " : "") + c;
    },

    // Removes a class from an element.
    RemoveClass: function (e, c) {
        e.className = e.className.replace(new RegExp(" \\b" + c + "\\b|\\b" + c + "\\b ?", "gi"), "");
    }
};

// Functions for handling tabs.
Tabs = {
    // Changes to the tab with the specified ID.
    GoTo: function (contentId, skipReplace) {
        // This variable will be true if a tab for the specified
        // content ID was found.
        var foundTab = false;

        // Get the TOC element.
        var toc = $("toc");
        if (toc) {
            var lis = toc.getElementsByTagName("li");
            for (var j = 0; j < lis.length; j++) {
                var li = lis[j];

                // Give the current tab link the class "current" and
                // remove the class from any other TOC links.
                var anchors = li.getElementsByTagName("a");
                var anchors = li.getElementsByTagName("a");
                for (var k = 0; k < anchors.length; k++) {
                    if (anchors[k].hash == "#" + contentId) {
                        CSS.AddClass(li, "current");
                        foundTab = true;
                        break;
                    } else {
                        CSS.RemoveClass(li, "current");
                    }
                }
            }
        }

        // Show the content with the specified ID.
        var divsToHide = [];
        var divs = document.getElementsByTagName("div");
        for (var i = 0; i < divs.length; i++) {
            var div = divs[i];

            if (div.className.match(/\btabContent\b/i)) {
                if (div.id == "_" + contentId)
                    div.style.display = "block";
                else {
                    divsToHide.push(div);
                }
            }
        }

        // Hide the other content boxes.
        for (var i = 0; i < divsToHide.length; i++)
            divsToHide[i].style.display = "none";

        // Change the address bar.
        if (!skipReplace) window.location.replace("#" + contentId);
    },

    OnClickHandler: function (e) {
        // Stop the event (to stop it from scrolling or
        // making an entry in the history).
        if (!e) e = window.event;
        if (e.preventDefault) e.preventDefault(); else e.returnValue = false;

        // Get the name of the anchor of the link that was clicked.
        Tabs.GoTo(this.hash.substring(1));
    },

    Init: function () {
        if (!document.getElementsByTagName) {
            return;
        }

        // Attach an onclick event to all the anchor links on the page.
        var anchors = document.getElementsByTagName("a");
        for (var i = 0; i < anchors.length; i++) {
            var a = anchors[i];
            if (a.hash) {
                a.onclick = Tabs.OnClickHandler;
            }
        }

        var contentId;
        if (window.location.hash)
            contentId = window.location.hash.substring(1);

        var divs = document.getElementsByTagName("div");
        for (var i = 0; i < divs.length; i++) {
            var div = divs[i];

            if (div.className.match(/\btabContent\b/i)) {
                if (!contentId) contentId = div.id;
                div.id = "_" + div.id;
            }
        }

        if (contentId)
            Tabs.GoTo(contentId, true);
    }
};

if (document.createStyleSheet) {
    var style = document.createStyleSheet();
    style.addRule("div.tabContent", "display: none;");
    style.addRule("div" + contentId, "display: block;");
} else {
    var head = document.getElementsByTagName("head")[0];
    if (head) {
        var style = document.createElement("style");
        style.setAttribute("type", "text/css");
        style.appendChild(document.createTextNode("div.tabContent { display: none; }"));
        style.appendChild(document.createTextNode("div" + contentId + " { display: block; }"));
        head.appendChild(style);
    }
}

// Hook up the OnLoad event to the tab initialization function.
Tabs.Init();

// Hide the content while waiting for the onload event to trigger.
var contentId = window.location.hash || "#Introduction";

function afterMindpotLibraryLoading() {
    buildMindmapDesigner();

    if ($('helpButton') != null) {
        var helpPanel = new Panel({panelButton:$('helpButton'), backgroundColor:'black'});
        helpPanel.setContent(Help.buildHelp(helpPanel));
    }

    if ($('helpButtonFirstSteps') != null) {
        var firstStepsPanel = $('helpButtonFirstSteps')
        firstStepsPanel.addEvent('click', function(event) {
            var firstStepWindow = window.open("firststeps.htm", "WiseMapping", "width=100px, height=100px");
            firstStepWindow.focus();
            firstStepWindow.moveTo(0, 0);
            firstStepWindow.resizeTo(screen.availWidth, screen.availHeight);
        });
    }

    if ($('helpButtonKeyboard') != null) {
        var keyboardPanel = $('helpButtonKeyboard')
        keyboardPanel.addEvent('click', function(event) {
            MOOdalBox.open('keyboard.htm', 'KeyBoard Shortcuts', '500px 400px', false)
        });
    }

    // Register Events ...
    $(document).addEvent('keydown', designer.keyEventHandler.bindWithEvent(designer));
    $("ffoxWorkarroundInput").addEvent('keydown', designer.keyEventHandler.bindWithEvent(designer));



    // Save event handler ....
    var saveButton = $('saveButton');
    saveButton.addEvent('click', function(event) {

        if (!isTryMode) {
            saveButton.setStyle('cursor', 'wait');
            var saveFunc = function() {
                designer.save(function() {
                    var monitor = core.Monitor.getInstance();
                    monitor.logMessage('Save completed successfully');
                    saveButton.setStyle('cursor', 'pointer');
                }, true);
            }
            saveFunc.delay(1);
        } else {
            new Windoo.Confirm('This option is not enabled in try mode. You must by signed in order to execute this action.<br/> to create an account click <a href="userRegistration.htm">here</a>',
                {
                    'window': {theme:Windoo.Themes.wise,
                        title:''
                    }
                });
        }
    });

    var discardButton = $('discardButton');
    discardButton.addEvent('click', function(event) {

        if (!isTryMode) {
            displayLoading();
            window.document.location = "mymaps.htm";
        } else {
            displayLoading();
            window.document.location = "home.htm";
        }
    });

    if (isTryMode) {
        $('tagIt').addEvent('click', function(event) {
            new Windoo.Confirm('This option is not enabled in try mode. You must by signed in order to execute this action.',
                {
                    'window': {theme:Windoo.Themes.wise,
                        title:''
                    }
                });

        });

        $('shareIt').addEvent('click', function(event) {
            new Windoo.Confirm('This option is not enabled in try mode. You must by signed in order to execute this action.',
                {
                    'window': {theme:Windoo.Themes.wise,
                        title:''
                    }
                });

        });

        $('publishIt').addEvent('click', function(event) {
            new Windoo.Confirm('This option is not enabled in try mode. You must by signed in order to execute this action.',
                {
                    'window': {theme:Windoo.Themes.wise,
                        title:''
                    }
                });

        });

        $('history').addEvent('click', function(event) {
            new Windoo.Confirm('This option is not enabled in try mode. You must by signed in order to execute this action.',
                {
                    'window': {theme:Windoo.Themes.wise,
                        title:''
                    }
                });

        });

    }

    // Autosave ...
    if (!isTryMode) {
        var autosave = function() {

            if (designer.needsSave()) {
                designer.save(function() {
                    var monitor = core.Monitor.getInstance();
                }, false);
            }
        };
        autosave.periodical(30000);

        // To prevent the user from leaving the page with changes ...
        window.onbeforeunload = function() {
            if (designer.needsSave()) {
                designer.save(null, false)
            }
        }
    }

    var menu = new mindplot.widget.Menu(designer);

    //  If a node has focus, focus can be move to another node using the keys.
    designer._cleanScreen = function(){menu.clear()};

    // If not problem has occured, I close the dialod ...
    var closeDialog = function() {

        if (!window.hasUnexpectedErrors) {
            waitDialog.deactivate();
        }
    }.delay(500);
}

function buildMindmapDesigner() {

    // Initialize message logger ...
    var monitor = new core.Monitor($('msgLoggerContainer'), $('msgLogger'));
    core.Monitor.setInstance(monitor);

    var container = $('mindplot');

    // Initialize Editor ...

    var screenWidth = window.getWidth();
    var screenHeight = window.getHeight();

    // Positionate node ...
    // header - footer
    screenHeight = screenHeight - 90 - 61;

    // body margin ...
    editorProperties.width = screenWidth;
    editorProperties.height = screenHeight;

    designer = new mindplot.MindmapDesigner(editorProperties, container);
    designer.loadFromXML(mapId, mapXml);

    // Save map on load ....
    if (editorProperties.saveOnLoad) {
        var saveOnLoad = function() {
            designer.save(function() {
            }, false);
        }.delay(1000)
    }

};

