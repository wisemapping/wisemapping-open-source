# WiseMapping: a Web based mindmapping application

## Project Information

The goal of this project is to provide a high quality product that can be deployed by educational and academic institutions, private and public companies and anyone who needs to have a mindmapping application. WiseMapping is based on the same code source supporting WiseMapping.com. More info: www.wisemapping.org

## Compiling and Running

### Prerequisites

The following products must be installed:
    * Java Development Kit 7 or higher (http://java.sun.com/javase/downloads/index.jsp)
    * Maven 2.2.1 or higher (http://maven.apache.org/)

### Compiling

WiseMapping uses Maven as packaging and project management. The project is composed of 4 maven sub-modules:
    * core-js: Utilities JavaScript libraries
    * web2d: JavaScript 2D SVG abstraction library used by the mind map editor
    * mindplot: JavaScript mind map designer core
    * wise-webapp: J2EE web application 

Full compilation of the project can be done executing within <project-dir>:

`mvn clean install`

Once this command is execute, the file <project-dir>/wise-webapp/target/wisemapping.war will be generated.

### Testing

The previously generated war can be deployed locally executing within the directory <project-dir>/wise-webapp the following command:

`mvn jetty:run`

This will start the application on the URL: http://localhost:8080. Additionally, a file based database is automatically populated with a test user.

User: test@wisemapping.org
Pass: test


## Running the JS only version

Start by creating the .zip file: 

`mvn assembly:assembly -Dmaven.test.skip=true`

To test the javascript frontend you then do:

    unzip target/wisemapping-3.0-SNAPSHOT-editor.zip
    cd target/wisemapping-3.0-SNAPSHOT-editor
    ruby -rwebrick -e 'WEBrick::HTTPServer.new(:Port=>8000,:DocumentRoot=>".").start' 

Now open a browser using the URL http://localhost:8000/

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



## Author

   * Pablo Luna
   * Paulo Veiga
   * Ignacio Manzano
   * Nicolas Damonte

## License

The source code is Licensed under the WiseMapping Open License, Version 1.0 (the “License”);

You may obtain a copy of the License at: http://www.wisemapping.org/license
