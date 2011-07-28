mindplot.layout.LayoutManagerFactory = {};
mindplot.layout.LayoutManagerFactory.managers = {
    OriginalLayoutManager:mindplot.layout.OriginalLayoutManager,
    FreeMindLayoutManager:mindplot.layout.FreeMindLayoutManager
};
mindplot.layout.LayoutManagerFactory.getManagerByName = function(name) {
    var manager = mindplot.layout.LayoutManagerFactory.managers[name + "Manager"];
    if ($defined(manager)) {
        return manager;
    }
    else {
        return mindplot.layout.LayoutManagerFactory.managers["OriginalLayoutManager"];
    }
};


