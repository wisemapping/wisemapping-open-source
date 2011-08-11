TestCase("Mindplot test",{
    setUp:function(){
        /*:DOC += <div id="mindplot"></div> */
        var mapId = '1';
        var mapXml = '<map name="1" version="pela"><topic central="true" text="test" id="1"><topic position="103,-52" order="" id="2"/><topic position="-134,-75" order="" id="3"/><topic position="-126,5" order="" id="4"/><topic position="-115,53" order="" id="5"/><topic position="-136,-35" order="" id="6"/></topic></map>';
        var editorProperties = {"zoom":0.7};
        var isTryMode = false;

        afterMindpotLibraryLoading = function()
        {
            buildMindmapDesigner();

            // Register Events ...
            $(document).addEvent('keydown', designer.keyEventHandler.bindWithEvent(designer));

            /*// Autosave ...
            if (!isTryMode)
            {
                var autosave = function() {

                    if (designer.needsSave())
                    {
                        designer.save(function()
                        {
                            //                            var monitor = core.Monitor.getInstance();
                        }, false);
                    }
                };
                autosave.periodical(30000);

                // To prevent the user from leaving the page with changes ...
                window.onbeforeunload = function confirmExit()
                {
                    if (designer.needsSave())
                    {
                        designer.save(null, false)
                    }
                }
            }*/

        };

        var buildMindmapDesigner = function()
        {

            // Initialize message logger ...
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


            /*// Save map on load ....
            if (editorProperties.saveOnLoad)
            {
                var saveOnLoad = function() {
                    designer.save(function() {
                    }, false);
                }.delay(1000)
            }*/

        }
        afterMindpotLibraryLoading();
    },
    testWorkspaceBuild:function(){
        assertNotNull($('workspace'));
    },
    testCentralTopicPresent:function(){
        var centralTopic = designer.getCentralTopic();
        assertNotNull(centralTopic);
        var position = centralTopic.getPosition();
        assertEquals(0,position.x);
        assertEquals(0,position.y);
    },
    testMouseCreateMainTopic:function(){
        var centralTopic = designer.getCentralTopic();
        assertNotNull(centralTopic);
        var target = designer.getWorkSpace().getScreenManager().getContainer();
        var size = designer._getTopics().length;
        fireNativeEvent('dblclick',target,new core.Point(50,50));
        assertEquals(size+1, designer._getTopics().length);
    }

});

var fireNativeEvent = function(type, target, position){
    var event;
    if(Browser.ie){
        event = document.createEventObject();
        event.screenX = position.x;
        event.screenY = position.y;
        target.dispatchEvent(event);
    }else{
        var eventFamily;
        if(type == "click" || type == "mousedown" || type == "mousemove" || type == "mouseout" || type == "mouseover" ||
                type == "mouseup" || type == "dblclick")
            eventFamily = "MouseEvents";
        else if(type == "keydown" || type == "keypress" || type == "keyup" || type=="DOMActivate" || type == "DOMFocusIn" ||
                type == "DOMFocusOut")
            eventFamily = "UIEvents";
        else if(type == "abort" || type == "blur" || type == "change" || type == "error" || type == "focus" || type == "load"
                || type == "reset" || type == "resize" || type == "scroll" || type == "select" || type == "submit" || type == "unload")
            eventFamily = "HTMLEvents";
        else if(type == "DOMAttrModified"  || type == "DOMNodeInserted" || type == "DOMNodeRemoved"
                || type == "DOMCharacterDataModified" || type == "DOMNodeInsertedIntoDocument"  || type == "DOMNodeRemovedFromDocument"
                || type == "DOMSubtreeModified")
            eventFamily = "MutationEvents";
        else
            eventFamily = "Events";
        event = document.createEvent(eventFamily);
        event.initEvent(type,true,false, target, 0, position.x, position.y);
        target.fireEvent(type, event);
    }
    return event;
};
