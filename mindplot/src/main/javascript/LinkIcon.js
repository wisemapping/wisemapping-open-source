/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

mindplot.LinkIcon = function(urlModel, topic, designer) {
    var divContainer=designer.getWorkSpace().getScreenManager().getContainer();
    var bubbleTip = mindplot.BubbleTip.getInstance(divContainer);
    mindplot.Icon.call(this, mindplot.LinkIcon.IMAGE_URL);
    this._linkModel = urlModel;
    this._topic = topic;
    this._designer = designer;
    var image = this.getImage();
    var imgContainer = new Element('div').setStyles({textAlign:'center', cursor:'pointer'});
    this._img = new Element('img');
    var url = urlModel.getUrl();
    this._img.src = 'http://open.thumbshots.org/image.pxf?url=' + url;

    if (url.indexOf('http:') == -1)
    {
        url = 'http://' + url;
    }
    this._img.alt = url;
    this._url=url;
    var openWindow = function() {
        var wOpen;
        var sOptions;

        sOptions = 'status=yes,menubar=yes,scrollbars=yes,resizable=yes,toolbar=yes';
        sOptions = sOptions + ',width=' + (screen.availWidth - 10).toString();
        sOptions = sOptions + ',height=' + (screen.availHeight - 122).toString();
        sOptions = sOptions + ',screenX=0,screenY=0,left=0,top=0';
        var url = this._img.alt;
        wOpen = window.open(url, "link", "width=100px, height=100px");
        wOpen.focus();
        wOpen.moveTo(0, 0);
        wOpen.resizeTo(screen.availWidth, screen.availHeight);
    };
    this._img.addEvent('click', openWindow.bindWithEvent(this));
    this._img.inject(imgContainer);

    var attribution = new Element('div').setStyles({fontSize:10, textAlign:"center"});
    attribution.innerHTML = "<a href='http://www.thumbshots.org' target='_blank' title='About Thumbshots thumbnails' style='color:#08468F'>About Thumbshots thumbnails</a>";

    var container = new Element('div');
    var element = new Element('div').setStyles({borderBottom:'1px solid #e5e5e5'});

    var title = new Element('div').setStyles({fontSize:12, textAlign:'center'});
    this._link = new Element('span');
    this._link.href = url;
    this._link.innerHTML = url;
    this._link.setStyle("text-decoration", "underline");
    this._link.setStyle("cursor", "pointer");
    this._link.inject(title);
    this._link.addEvent('click', openWindow.bindWithEvent(this));
    title.inject(element);

    imgContainer.inject(element);
    attribution.inject(element);
    element.inject(container);
    
    if(!designer._viewMode){
        var buttonContainer = new Element('div').setStyles({paddingTop:5, textAlign:'center'});
        var editBtn = new Element('input', {type:'button', 'class':'btn-primary', value:'Edit','class':'btn-primary'}).addClass('button').inject(buttonContainer);
        var removeBtn = new Element('input', {type:'button', value:'Remove','class':'btn-primary'}).addClass('button').inject(buttonContainer);

        editBtn.setStyle("margin-right", "3px");
        removeBtn.setStyle("margin-left", "3px");

        removeBtn.addEvent('click', function(event) {
            var command = new mindplot.commands.RemoveLinkFromTopicCommand(this._topic.getId());
            designer._actionRunner.execute(command);
            bubbleTip.forceClose();
        }.bindWithEvent(this));

        var okButtonId = 'okLinkButtonId'
        editBtn.addEvent('click', function(event) {
            var topic = this._topic;
            var designer = this._designer;
            var link = this;
            var okFunction = function(e) {
                var result = false;
                var url = urlInput.value;
                if ("" != url.trim())
                {
                    link._img.src = 'http://open.thumbshots.org/image.pxf?url=' + url;
                    link._img.alt = url;
                    link._link.href = url;
                    link._link.innerHTML = url;
                    this._linkModel.setUrl(url);
                    result = true;
                }
                return result;
            };
            var msg = new Element('div');
            var urlText = new Element('div').inject(msg);
            urlText.innerHTML = "URL:"

            var formElem = new Element('form', {'action': 'none', 'id':'linkFormId'});
            var urlInput = new Element('input', {'type': 'text', 'size':30,'value':url});
            urlInput.inject(formElem);
            formElem.inject(msg)

            formElem.addEvent('submit', function(e)
            {
                $(okButtonId).fireEvent('click', e);
                e = new Event(e);
                e.stop();
            });


            var dialog = mindplot.LinkIcon.buildDialog(designer, okFunction, okButtonId);
            dialog.adopt(msg).show();

        }.bindWithEvent(this));
        buttonContainer.inject(container);
    }


    var linkIcon = this;
    image.addEventListener('mouseover', function(event) {
        bubbleTip.open(event, container, linkIcon);
    });
    image.addEventListener('mousemove', function(event) {
        bubbleTip.updatePosition(event);
    });
    image.addEventListener('mouseout', function(event) {
        bubbleTip.close(event);
    });
};

objects.extend(mindplot.LinkIcon, mindplot.Icon);

mindplot.LinkIcon.prototype.initialize = function() {

};

mindplot.LinkIcon.prototype.getUrl=function(){
    return this._url;
};

mindplot.LinkIcon.prototype.getModel=function(){
    return this._linkModel;
};

mindplot.LinkIcon.buildDialog = function(designer, okFunction, okButtonId) {
    var windoo = new Windoo({
        title: 'Write link URL',
        theme: Windoo.Themes.wise,
        modal:true,
        buttons:{'menu':false, 'close':false, 'minimize':false, 'roll':false, 'maximize':false},
        destroyOnClose:true,
        height:130
    });

    var cancel = new Element('input', {'type': 'button', 'class':'btn-primary', 'value': 'Cancel','class':'btn-primary'}).setStyle('margin-right', "5px");
    cancel.setStyle('margin-left', "5px");
    cancel.addEvent('click', function(event) {
        $(document).addEvent('keydown', designer.keyEventHandler.bindWithEvent(designer));
        windoo.close();
    }.bindWithEvent(this));

    var ok = new Element('input', {'type': 'button', 'class':'btn-primary','value': 'Ok','class':'btn-primary','id':okButtonId}).setStyle('marginRight', 10);
    ok.addEvent('click', function(event) {
        var couldBeUpdated = okFunction.attempt();
        if (couldBeUpdated)
        {
            $(document).addEvent('keydown', designer.keyEventHandler.bindWithEvent(designer));
            windoo.close();
        }
    }.bindWithEvent(this));

    var panel = new Element('div', {'styles': {'padding-top': 10, 'text-align': 'right'}}).adopt(ok, cancel);

    windoo.addPanel(panel);
    $(document).removeEvents('keydown');
    return windoo;
};

mindplot.LinkIcon.IMAGE_URL = "../images/world_link.png";

 