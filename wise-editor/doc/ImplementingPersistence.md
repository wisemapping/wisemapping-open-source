# Implementing custom storage

To implement a custom backend, you need to create your own implementation of the mindplot.PersistenceManager class. 

Here is an example skeleton::

function createStorageManager(mindplot) {
    mindplot.RestStorageManager = new Class({
        Extends:mindplot.PersistenceManager,
        initialize: function(url) {
            this.parent();
            this.backendUrl = url;
        },

        saveMapXml : function(mapId, mapXml, pref, saveHistory, events) {
            var url = this.backendUrl  + mapId;
            $.ajax({
                url: url,
                method: 'post',
                async: false,
                success: function(responseText) {
                    events.onSuccess();
                },
                onError: function (text, error) {
                    console.log("Error saving mindmap to: " + url, text, error);
                    events.onError();
                }
            });
        },

        loadMapDom : function(mapId) {
                var xml;
                $.ajax({
                    url: this.backendUrl  + mapId,
                    method: 'get',
                    async: false,
                    success: function(responseText) {
                        xml = responseText;
                    }
                });

                // If I could not load it from a file, hard code one.
                if (xml == null) {
                    throw "Map could not be loaded";
                }
                var parser = new DOMParser();
                return parser.parseFromString(xml,  "text/xml");
           }

    }
    );
    return new mindplot.RestStorageManager(url);
}


In your script for loading the mindmap you add a call to the callback into the loadcomplete method::

     $(document).on('loadcomplete', function(resource) {
               //Asset.javascript("{{ asset('bundles/fpgadmin/js/FpgMindmapPersistence.js')}}");
               // Options has been defined in by a external ile ?
               var options = {
                    'persistenceManager' : createStorageManager(mindplot, "http://localhost/my/rest/interface");    
                    viewPort: {
                        height: parseInt(window.innerHeight - 70), // Footer and Header
                        width:  parseInt(window.innerWidth)
                    },
                    size : {
                    height: parseInt(screen.height),
                    width:  parseInt(screen.width)
                },
                "readOnly":false,
                "zoom":1.3,
                "container":"mindplot"
                "mapId" : "myMapId"
               };
               var designer = buildDesigner(options);
               var persistence = mindplot.PersistenceManager.getInstance();
               var mindmap;
               try {
                   mindmap = persistence.load(mapId);
               } catch(e) {
                   // If the map could not be loaded, create a new empty map...
                   mindmap = mindplot.model.Mindmap.buildEmpty(mapId);
               }
               designer.loadMap(mindmap);
           });
