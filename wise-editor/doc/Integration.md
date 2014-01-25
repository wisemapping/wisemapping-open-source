JS Editor Integration
---------------------

## Running the JS only version

Start by creating the .zip file:

`mvn assembly:assembly -Dmaven.test.skip=true`

To test the javascript frontend you then do:

    ruby -rwebrick -e 'WEBrick::HTTPServer.new(:Port=>8000,:DocumentRoot=>".").start'

Now open a browser using the URL http://localhost:8000/wise-editor/src/main/webapp/

### Attaching drag and drop events.

1) Support for dragging TextNodes:

The following code is an example of how to add attach to the div dragImageNode the support for node dragging.

    $("dragTextNode").addEvent('mousedown', function(event) {
        event.preventDefault();

        // Create a image node ...
        var mindmap = designer.getMindmap();
        var node = mindmap.createNode();
        node.setText("Node Text !!!!");
        node.setMetadata("{'media':'test'}");
        node.setShapeType(mindplot.model.TopicShape.RECTANGLE);

        // Add link ...
        var link = node.createFeature(mindplot.TopicFeature.Link.id, {url:"http://www.wisemapping.com"});
        node.addFeature(link);

        // Add Note ...
        var note = node.createFeature(mindplot.TopicFeature.Note.id, {text:"This is a note"});
        node.addFeature(note);

        designer.addDraggedNode(event, node);
    });

In the example, a new node is created with text "Node Text !!!!" and a note and a link associated to it when the user drop the node. Something to pay attention is the node.setMetadata("{}"), this delegated will be persisted during the serialization. Here you can store all the data you need.

2) Support for dragging Images:  Similar to the point 1,drag support is registered to the div dragImageNode.

            $("dragImageNode").addEvent('mousedown', function(event) {
                event.preventDefault();

                // Create a image node ...
                var mindmap = designer.getMindmap();
                var node = mindmap.createNode();
                node.setImageSize(80, 43);
                node.setMetadata("{'media':'video,'url':'http://www.youtube.com/watch?v=P3FrXftyuzw&feature=g-vrec&context=G2b4ab69RVAAAAAAAAAA'}");
                node.setImageUrl("images/logo-small.png");
                node.setShapeType(mindplot.model.TopicShape.IMAGE);

                designer.addDraggedNode(event, node);
            });

The  node.setShapeType(mindplot.model.TopicShape.IMAGE) defines a image node. This makes mandatory the set of setImageUrl and setImageSize properties in the node.

3) An event registration mechanism for Image nodes edit events: The next snipped show how to register a custom edition handler.

            designer.addEvent("editnode", function(event) {
                var node = event.model;

                alert("Node Id:" + node.getId());
                alert("Node Metadata:" + node.getMetadata());
                alert("Is Read Only:" + event.readOnly);
       } });