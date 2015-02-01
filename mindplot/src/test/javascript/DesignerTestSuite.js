var designer = null;
/*:DOC += <div id="mindplot"></div> */
var mapId = '1';
var mapXml = '<map name="1" version="pela"><topic central="true" text="test" id="1"><topic position="103,-52" order="" id="2"/><topic position="-134,-75" order="" id="3"/><topic position="-126,5" order="" id="4"/><topic position="-115,53" order="" id="5"/><topic position="-136,-35" order="" id="6"/></topic></map>';
var editorProperties = {"zoom":0.7, size: {width: "1366px", height:"768px"}};
var buildMindmapDesigner = function() {

    // Initialize message logger ...
    var container = $('<div id="mindplot"></div>');
    $("body").append(container);

    // Initialize Editor ...
    var window = $(window);
    var screenWidth = window.width();
    var screenHeight = window.height();

    // Positionate node ...
    // header - footer
    screenHeight = screenHeight - 90 - 61;

    // body margin ...
    editorProperties.width = screenWidth;
    editorProperties.height = screenHeight;

    designer = new mindplot.Designer(editorProperties, container);
    // Load map from XML file persisted on disk...
    var persistence = new mindplot.LocalStorageManager("src/test/resources/welcome.xml");
    var mindmap = persistence.load(mapId);
    designer.loadMap(mindmap);

    /*// Save map on load ....
     if (editorProperties.saveOnLoad)
     {
     var saveOnLoad = function() {
     designer.save(function() {
     }, false);
     }.delay(1000)
     }*/

}
buildMindmapDesigner();


describe("Designer test suite", function() {

    it("testWorkspaceBuild", function(){
        var mindplot = $(document).find('#mindplot');
        expect(mindplot).not.toBeNull();
        expect(mindplot).not.toBeUndefined();
    });
    it("testCentralTopicPresent", function(){
        var centralTopic = designer.getMindmap().getCentralTopic();
        expect($defined(centralTopic)).toBe(true);
        var position = centralTopic.getPosition();
        expect(position.x).toEqual(0);
        expect(position.y).toEqual(0);
    });
    /*it("testCentralTopicPresent", function(){
        var centralTopic = designer.getMindmap().getCentralTopic();
        expect($defined(centralTopic)).toBe(true);
        var target = designer.getWorkSpace().getScreenManager().getContainer();
        var size = designer.getModel().getTopics().length;
        fireNativeEvent('dblclick',target,new core.Point(50,50));
        assertEquals(size+1, designer.getModel().getTopics().length);
    });
    */

});