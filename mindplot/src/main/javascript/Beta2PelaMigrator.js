mindplot.Beta2PelaMigrator = function(betaSerializer){
    this._betaSerializer=betaSerializer;
    this._pelaSerializer = new mindplot.XMLMindmapSerializer_Pela();
};

mindplot.Beta2PelaMigrator.prototype.toXML = function(mindmap)
{
    return this._pelaSerializer.toXML(mindmap);
};


mindplot.Beta2PelaMigrator.prototype.loadFromDom = function(dom)
{
    var mindmap = this._betaSerializer.loadFromDom(dom);
    mindmap.setVersion(mindplot.ModelCodeName.PELA);
    return mindmap;
};
