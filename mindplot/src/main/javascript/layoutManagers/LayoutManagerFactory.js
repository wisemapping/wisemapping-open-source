mindplot.layoutManagers.LayoutManagerFactory = {};
mindplot.layoutManagers.LayoutManagerFactory.managers = {
    OriginalLayoutManager:mindplot.layoutManagers.OriginalLayoutManager
//    FreeLayoutManager:mindplot.layoutManagers.FreeLayoutManager
};
mindplot.layoutManagers.LayoutManagerFactory.getManagerByName = function(name){
    var manager = mindplot.layoutManagers.LayoutManagerFactory.managers[name+"Manager"];
    if(manager){
        return manager;
    }
    else{
        return mindplot.layoutManagers.LayoutManagerFactory.managers["OriginalLayoutManager"]; 
    }
};


