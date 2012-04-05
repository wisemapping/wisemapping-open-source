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

var designer = null;

function buildDesigner(options) {

    var container = $(options.container);
    $assert(container, 'container could not be null');

    // Register load events ...
    designer = new mindplot.Designer(options, container);
    designer.addEvent('loadSuccess', function() {
        window.waitDialog.close.delay(1000, window.waitDialog);
        window.waitDialog = null;
    });

    window.onerror = function(e) {
        if (window.waitDialog) {
            window.waitDialog.close.delay(1000, window.waitDialog);
            window.waitDialog = null;
        }
        errorDialog.show();
        console.log(e);
    };

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
        persistence = new mindplot.LocalStorageManager();
    }
    mindplot.PersistenceManager.init(persistence);

    // Register toolbar event ...
    if ($('toolbar')) {
        var menu = new mindplot.widget.Menu(designer, 'toolbar', options.mapId, "");

        //  If a node has focus, focus can be move to another node using the keys.
        designer._cleanScreen = function() {
            menu.clear()
        };
    }

    return designer;
}


function loadDesignerOptions(jsonConf) {
    // Load map options ...
    var result;
    if (jsonConf) {
        var request = new Request.JSON({
                url: jsonConf,
                async:false,
                onSuccess:
                    function(options) {
                        this.options = options;

                    }.bind(this)
            }
        );
        request.get();
        result = this.options;
    }
    else {
        // Set workspace screen size as default. In this way, resize issues are solved.
        var containerSize = {
            height: parseInt(screen.height),
            width:  parseInt(screen.width)
        };

        var viewPort = {
            height: parseInt(window.innerHeight - 70), // Footer and Header
            width:  parseInt(window.innerWidth)
        };
        result = {readOnly:false,zoom:0.85,saveOnLoad:true,size:containerSize,viewPort:viewPort,container:'mindplot'};
    }
    return result;
}

editor = {};
editor.WaitDialog = new Class({
    Extends:MooDialog,
    initialize : function() {
        var panel = this._buildPanel();
        this.parent({
                closeButton:false,
                destroyOnClose:true,
                autoOpen:false,
                useEscKey:false,
                title:'Loading ...',
                onInitialize: function(wrapper) {
                    wrapper.setStyle('opacity', 0);
                    this.wrapper.setStyle('display', 'none');
                    this.fx = new Fx.Morph(wrapper, {
                        duration: 100,
                        transition: Fx.Transitions.Bounce.easeOut
                    });
                },

                onBeforeOpen: function() {
                    this.overlay = new Overlay(this.options.inject, {
                        duration: this.options.duration
                    });
                    this.overlay.open();
                    this.fx.start({
                        'margin-top': [-200, -100],
                        opacity: [0, 1]
                    }).chain(function() {
                        this.fireEvent('show');
                        this.wrapper.setStyle('display', 'block');

                    }.bind(this));
                },

                onBeforeClose: function() {
                    this.fx.start({
                        'margin-top': [-100, 0],
                        opacity: 0,
                        duration: 200
                    }).chain(function() {
                        this.fireEvent('hide');
                        this.wrapper.setStyle('display', 'none');

                    }.bind(this));
                }}
        );
        this.setContent(panel);
    },

    _buildPanel : function () {
        var result = new Element('div');
        result.setStyles({
            'text-align':'center',
            width: '400px'
        });
        var img = new Element('img', {'src': 'images/ajax-loader.gif'});
        img.inject(result);
        return result;
    },

    show : function() {
        this.open();
    },

    destroy: function() {
        this.parent();
        this.overlay.destroy();
    }

});


editor.FatalErrorDialog = new Class({
    Extends:MooDialog,
    initialize : function() {
        var panel = this._buildPanel();
        this.parent({
                closeButton:false,
                destroyOnClose:true,
                autoOpen:true,
                useEscKey:false,
                title:'Outch!!. An unexpected error has occurred',
                onInitialize: function(wrapper) {
                    wrapper.setStyle('opacity', 0);
                    this.wrapper.setStyle('display', 'none');
                    this.fx = new Fx.Morph(wrapper, {
                        duration: 100,
                        transition: Fx.Transitions.Bounce.easeOut
                    });
                },

                onBeforeOpen: function() {
                    this.overlay = new Overlay(this.options.inject, {
                        duration: this.options.duration
                    });
                    if (this.options.closeOnOverlayClick)
                        this.overlay.addEvent('click', this.close.bind(this));
                    this.overlay.open();
                    this.fx.start({
                        'margin-top': [-200, -100],
                        opacity: [0, 1]
                    }).chain(function() {
                        this.fireEvent('show');
                        this.wrapper.setStyle('display', 'block');
                    }.bind(this));
                },

                onBeforeClose: function() {
                    this.fx.start({
                        'margin-top': [-100, 0],
                        opacity: 0,
                        duration: 200
                    }).chain(function() {
                        this.wrapper.setStyle('display', 'none');
                        this.fireEvent('hide');

                    }.bind(this));
                }}
        );
        this.setContent(panel);
    },

    destroy: function() {
        this.parent();
        this.overlay.destroy();
    },

    _buildPanel : function () {
        var result = new Element('div');
        result.setStyles({
            'text-align':'center',
            width: '400px'
        });
        var p = new Element('p', {'text': 'We\'re sorry, an error has occurred and we can not process your request. Please try again, or go to the home page.'});
        p.inject(result);

        var img = new Element('img', {'src': 'images/alert-sign.png'});
        img.inject(result);

        return result;
    },

    show : function() {
        this.open();
    }

});


editor.Help = {
    buildHelp:function(panel) {
        var container = new Element('div');
        container.setStyles({width:'100%', textAlign:'center'});
        var content1 = Help.buildContentIcon('images/black-keyboard.png', 'Keyboard Shortcuts', function() {
            MOOdalBox.open('keyboard.htm', 'KeyBoard Shortcuts', '500px 400px', false);
            panel.hidePanel();
        });
        var content2 = Help.buildContentIcon('images/firstSteps.png', 'Editor First Steps', function() {
            var wOpen;
            var sOptions;

            sOptions = 'status=yes,menubar=yes,scrollbars=yes,resizable=yes,toolbar=yes';
            sOptions = sOptions + ',width=' + (screen.availWidth - 10).toString();
            sOptions = sOptions + ',height=' + (screen.availHeight - 122).toString();
            sOptions = sOptions + ',screenX=0,screenY=0,left=0,top=0';

            wOpen = window.open("firststeps.htm", "WiseMapping", "width=100px, height=100px");
            wOpen.focus();
            wOpen.moveTo(0, 0);
            wOpen.resizeTo(screen.availWidth, screen.availHeight);
            panel.hidePanel();
        });

        container.addEvent('show', function() {
            content1.effect('opacity', {duration:800}).start(0, 100);
            var eff = function() {
                content2.effect('opacity', {duration:800}).start(0, 100);
            };
            eff.delay(150);
        });
        container.addEvent('hide', function() {
            content1.effect('opacity').set(0);
            content2.effect('opacity').set(0)
        });
        content1.inject(container);
        content2.inject(container);
        return container;
    },
    buildContentIcon:function(image, text, onClickFn) {
        var container = new Element('div').setStyles({margin:'15px 0px 0px 0px', opacity:0, padding:'5px 0px', border: '1px solid transparent', cursor:'pointer'});

        var icon = new Element('div');
        icon.addEvent('click', onClickFn);
        var img = new Element('img');
        img.setProperty('src', image);
        img.inject(icon);
        icon.inject(container);

        var textContainer = new Element('div').setStyles({width:'100%', color:'white'});
        textContainer.innerHTML = text;
        textContainer.inject(container);

        container.addEvent('mouseover', function() {
            $(this).setStyle('border-top', '1px solid #BBB4D6');
            $(this).setStyle('border-bottom', '1px solid #BBB4D6');
        }.bindWithEvent(container));
        container.addEvent('mouseout', function() {
            $(this).setStyle('border-top', '1px solid transparent');
            $(this).setStyle('border-bottom', '1px solid transparent');

        }.bindWithEvent(container));
        return container;
    }
};


// Show loading dialog ...
waitDialog = new editor.WaitDialog();
waitDialog.show();
errorDialog = new editor.FatalErrorDialog();

// Loading libraries ...
Asset.javascript("js/mindplot-min.js");
