mindplot.XMLMindmapSerializerFactory = {};

mindplot.XMLMindmapSerializerFactory.getSerializerFromMindmap = function(mindmap){
    return mindplot.XMLMindmapSerializerFactory.getSerializer(mindmap.getVersion());
};

mindplot.XMLMindmapSerializerFactory.getSerializerFromDocument = function(domDocument){
    var rootElem = domDocument.documentElement;
    return mindplot.XMLMindmapSerializerFactory.getSerializer(rootElem.getAttribute("version"))
};


mindplot.XMLMindmapSerializerFactory.getSerializer = function(version){
    if(!core.Utils.isDefined(version)){
        version = mindplot.ModelCodeName.BETA;
    }
    var codeNames = mindplot.XMLMindmapSerializerFactory._codeNames;
    var found = false;
    var serializer = null;
    for(var i=0; i<codeNames.length; i++){
        if(!found){
            found = codeNames[i].codeName==version;
            if(found)
                serializer = new (codeNames[i].serializer)();
        } else{
            var migrator = codeNames[i].migrator;
            serializer = new migrator(serializer);
        }
    }

    return serializer;
};

mindplot.XMLMindmapSerializerFactory._codeNames =
[{
    codeName:mindplot.ModelCodeName.BETA,
    serializer: mindplot.XMLMindmapSerializer_Beta,
    migrator:function(){//todo:error
    }
 },
 {
    codeName:mindplot.ModelCodeName.PELA,
    serializer:mindplot.XMLMindmapSerializer_Pela,
    migrator:mindplot.Beta2PelaMigrator
 }
];