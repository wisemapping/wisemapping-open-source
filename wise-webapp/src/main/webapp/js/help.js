/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under the Apache License, Version 2.0 (the "License") plus the
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

var Help = {
    buildHelp:function(panel){
        var container = new Element('div');
        container.setStyles({width:'100%', textAlign:'center'});
        var content1 = Help.buildContentIcon('../images/black-keyboard.png', 'Keyboard Shortcuts', function(){MOOdalBox.open('keyboard.htm','KeyBoard Shortcuts', '500px 400px', false);panel.hidePanel();});
        var content2 = Help.buildContentIcon('../images/firstSteps.png', 'Editor First Steps', function(){
           var wOpen;
           var sOptions;

           sOptions = 'status=yes,menubar=yes,scrollbars=yes,resizable=yes,toolbar=yes';
           sOptions = sOptions + ',width=' + (screen.availWidth - 10).toString();
           sOptions = sOptions + ',height=' + (screen.availHeight - 122).toString();
           sOptions = sOptions + ',screenX=0,screenY=0,left=0,top=0';

           wOpen = window.open("firststeps.htm", "WiseMapping", "width=100px, height=100px");
           wOpen.focus();
           wOpen.moveTo( 0, 0 );
           wOpen.resizeTo( screen.availWidth, screen.availHeight );
           panel.hidePanel();
        });

        container.addEvent('show', function(){
            content1.effect('opacity',{duration:800}).start(0,100);
            var eff = function(){content2.effect('opacity',{duration:800}).start(0,100);};
            eff.delay(150);
        });
        container.addEvent('hide', function(){
            content1.effect('opacity').set(0);
            content2.effect('opacity').set(0)
        });
        content1.inject(container);
        content2.inject(container);
        return container;
    },
    buildContentIcon:function(image, text, onClickFn){
        var container = new Element('div').setStyles({margin:'15px 0px 0px 0px', opacity:0, padding:'5px 0px', border: '1px solid transparent', cursor:'pointer'});

        var icon = new Element('div');
        icon.addEvent('click',onClickFn);
        var img = new Element('img');
        img.setProperty('src',image);
        img.inject(icon);
        icon.inject(container);

        var textContainer = new Element('div').setStyles({width:'100%', color:'white'});
        textContainer.innerHTML=text;
        textContainer.inject(container);

        container.addEvent('mouseover', function(event){
            $(this).setStyle('border-top', '1px solid #BBB4D6');
            $(this).setStyle('border-bottom', '1px solid #BBB4D6');
        }.bindWithEvent(container));
        container.addEvent('mouseout', function(event){
            $(this).setStyle('border-top', '1px solid transparent');
            $(this).setStyle('border-bottom', '1px solid transparent');

        }.bindWithEvent(container));
        return container;
    }
};
