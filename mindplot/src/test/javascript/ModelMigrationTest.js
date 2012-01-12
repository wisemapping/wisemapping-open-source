TestCase("Model Migration Tests",{
    setUp:function(){
        mapXml = '<map name="1"><topic central="true" text="test"><topic position="-127,-100" fontStyle="Verdana;;#038f39;;italic;" brColor="#db770b"><topic order="0"/><topic order="1"/><topic order="2"><topic order="0"/><topic order="1"/><topic order="2"/></topic></topic><topic position="-168,50" shape="line"><icon id="conn_disconnect"/><icon id="chart_curve"/></topic><topic position="166,-100" shape="elipse"><note text="this%20is%20a%20note"/><topic order="0"/><topic order="1"/><topic order="2"/></topic><topic position="173,0" shape="rectagle" bgColor="#f2a2b5"><link url="www.google.com"/></topic></topic><topic position="-391,-2" text="im alone"/></map>';
    },
    testModelMigration:function(){
        ids=[];
        var domDocument = core.Utils.createDocumentFromText(mapXml);

        var betaSerializer = new mindplot.XMLMindmapSerializer_Beta();
        var betaMap = betaSerializer.loadFromDom(domDocument);

        var serializer = mindplot.XMLMindmapSerializerFactory.getSerializerFromDocument(domDocument);
        var mindmap = serializer.loadFromDom(domDocument);

        //Assert that the new model is Pela
        assertEquals(mindplot.ModelCodeName.PELA, mindmap.getVersion());

        //Assert same number of branches
        var betaBranches = betaMap.getBranches();
        var branches = mindmap.getBranches();
        assertEquals(betaBranches.length, branches.length);

        //Assert same nodes recursively
        //Since Id can change let's assume the order is the same
        for(var i = 0; i<betaBranches.length; i++){
            var branch = betaBranches[i];
            this._findAndCompareNodes(branch, branches[i]);

        }
        
    },
    _findAndCompareNodes:function(betaNode, node){
        this._compareNodes(betaNode, node);
        //Assert same nodes recursively
        //Since Id can change let's assume the order is the same
        for(var i = 0; i<betaNode.getChildren().length; i++){
            var betaChild = betaNode.getChildren()[i];
            var child = node.getChildren()[i];
            this._findAndCompareNodes(betaChild, child);

        }
    },
    _compareNodes:function(node1, node2){
        assertNotNull(node1);
        assertNotNull(node2);

        //In Pela Version every id is different
        var pelaId = node2.getId();
        assertTrue(ids[pelaId]==undefined);
        ids.push(pelaId);

        var children1 = node1.getChildren();
        var children2 = node2.getChildren();
        assertEquals(children1.length, children2.length);

        var position1 = node1.getPosition();
        var position2 = node2.getPosition();
        if(position1==null){
            assertNull(position2);
        }else{
            assertEquals(position1.x, position2.x);
            assertEquals(position1.y, position2.y);
        }
        assertEquals(node1.areChildrenShrunken(), node2.areChildrenShrunken());
        assertEquals(node1.getType(), node2.getType());
        assertEquals(node1.getText(), node2.getText());
        assertEquals(node1.isConnected(), node2.isConnected());
        assertEquals(node1.getSize().width, node2.getSize().width);
        assertEquals(node1.getSize().height, node2.getSize().height);
        this._compareIcons(node1.getIcons(), node2.getIcons());
        this._compareLinks(node1.getLinks(), node2.getLinks());
        this._compareNotes(node1.getNotes(),node2.getNotes());

        var order1 = node1.getOrder();
        var order2 = node2.getOrder();
        if(order1==null){
            assertNull(order2);
        }else{
            assertEquals(order1, order2);
        }
        assertEquals(node1.getShapeType(), node2.getShapeType());
        assertEquals(node1.getFontFamily(), node2.getFontFamily());
        assertEquals(node1.getFontStyle(), node2.getFontStyle());
        assertEquals(node1.getFontWeight(), node2.getFontWeight());
        assertEquals(node1.getFontSize(), node2.getFontSize());
        assertEquals(node1.getBorderColor(), node2.getBorderColor());
        assertEquals(node1.getBackgroundColor(), node2.getBackgroundColor());
    },
    _compareLinks:function(links1, links2){
        assertEquals(links1.length, links2.length);
        for(var i=0; i<links1.length; i++){
            var link1 = links1[i];
            var link2 = links2[i];
            assertEquals(link1.getUrl(), link2.getUrl());

        }
    },
    _compareIcons:function(icons1, icons2){
        assertEquals(icons1.length, icons2.length);
        for(var i=0; i<icons1.length; i++){
            var icon1 = icons1[i];
            var icon2 = icons2[i];
            assertEquals(icon1.getIconType(), icon2.getIconType());

        }
    },
    _compareNotes:function(notes1, notes2){
        assertEquals(notes1.length, notes2.length);
        for(var i=0; i<notes1.length; i++){
            var note1 = notes1[i];
            var note2 = notes2[i];
            assertEquals(note1.getText(), note2.getText());

        }
    }

});