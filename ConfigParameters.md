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
* zoom: how much the map should be zoomed.
* size: size of the map area.
* viewport
* persistenceManager: Classname of a class that extends mindplot.PersistenceManager (see ImplementingPersistence for more info.)
* mapId: The id of the map
* container: The id of the containing div.

