# Configuration parameters for embedded minmaps

The mindmap designer object takse a set of configuration options.

An example config may look like this::

    {
        "readOnly":false,
        "zoom":1.3,
        "size":{
            "width":800,
            "height":400
        },
        "viewPort":
        {
            "width":800,
            "height":400
        },
        "persistenceManager": "mindplot.LocalStorageManager",
        "mapId": "welcome",
        "container":"mindplot"

    }

The options are:

* readOnly: Set to true if the viewer should not be able to edit the map.
* zoom: how much the map should be zoomed. Range: 0.3 - 1.9. 0.3 = largest text.
* size: size of the map area.
* viewPort: set this to the same as the size
* persistenceManager: Classname of a class that extends mindplot.PersistenceManager (see ImplementingPersistence for more info.)
* mapId: The id of the map
* container: The id of the containing div.

Viewport and size should be set like this::

        var containerSize = {
            height: parseInt(screen.height),
            width:  parseInt(screen.width)
        };

        var viewPort = {
            height: parseInt(window.innerHeight - 70), // Footer and Header
            width:  parseInt(window.innerWidth)
        };

