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

mindplot.widget.LinkIconTooltip = new Class({
    Extends: mindplot.widget.FloatingTip,

    initialize : function(linkIcon) {
        $assert(linkIcon, "linkIcon can not be null");
        this.parent(linkIcon.getImage()._peer._native, {
            // Content can also be a function of the target element!
            content: this._buildContent.pass(linkIcon, this),
            html: true,
            position: 'bottom',
            arrowOffset : 10,
            center: true,
            arrowSize: 15,
            offset : {x:10,y:20},
            className: 'linkTip'
        });
    },

    _buildContent : function(linkIcon) {
        var result = new Element('div');
        result.setStyles({
            padding:'5px',
            width:'100%'
        });

        var title = new Element('div', {text:'Link'});
        title.setStyles({
            'font-weight':'bold',
            color:'black',
            'padding-bottom':'5px',
            width: '100px'
        });
        title.inject(result);

        var text = new Element('div', {text: "URL: " + linkIcon.getModel().getUrl()});
        text.setStyles({
                'white-space': 'pre-wrap',
                'word-wrap': 'break-word'
            }
        );
        text.inject(result);

        var imgContainer = new Element('div');
        imgContainer.setStyles({
            width: '100%',
            textAlign: 'center',
            'padding-bottom':'5px',
            'padding-top': '5px'
        });

        var img = new Element('img', {
                src: 'http://open.thumbshots.org/image.pxf?url=' + linkIcon.getModel().getUrl(),
                img : linkIcon.getModel().getUrl(),
                alt : linkIcon.getModel().getUrl()
            }
        );
        img.setStyles({
                padding: '5px'
            }
        );

        var link = new Element('a', {
            href : linkIcon.getModel().getUrl(),
            alt : 'Open in new window ...',
            target : '_blank'

        });
        img.inject(link);
        link.inject(imgContainer);
        imgContainer.inject(result);

        return result;
    }
});