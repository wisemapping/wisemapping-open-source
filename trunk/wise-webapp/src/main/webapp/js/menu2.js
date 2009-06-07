/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* $Id: file 64488 2006-03-10 17:32:09Z paulo $
*/

Window.onDomReady(function() {
    // variable for the list status
    var listStatus = 'closed';
    // setup var that holds our opened list's id
    var listOpen;
    // show list function
    var showList = function(lid) {
        var listId = lid.replace(/For/g, '');
        // need to check if there is an open list
        if (listStatus == "open") {
            // check if the open list is the same
            // as toggled list. If not, then we hide it
            if (listId != listOpen) {
                hideList();
            }
        }
        if (listStatus == "closed") {
            // set our list status
            listStatus = 'open';
            // set the curent open list id
            listOpen = listId;
            // show our list with a little effects
            new fx.Opacity($(listOpen), {duration: 500}).custom(0, 1);
            new fx.Height($(listOpen), {duration: 300}).custom(20, 40);
            new fx.Width($(listOpen), {duration: 300}).custom(20, 131);
            // we add a timeout so the sublist goes away
            // if the user doesn't click/mouseover another
            // menu item
            (hideList).delay(15000);
        }
    };
    // hide list function
    var hideList = function() {
        if (listOpen) {
            // check if our list is shown already - if so run the effects to hide list
            if ($(listOpen).getStyle('visibility') == "visible") {
                new fx.Opacity($(listOpen), {duration: 500}).custom(1, 0);
                new fx.Height($(listOpen), {duration: 300}).custom(40, 20);
                new fx.Width($(listOpen), {duration: 300}).custom(131, 20);
            }
            // set our list status
            listStatus = 'closed';
            // reset open list id
            listOpen = '';
        }
    };
    $ES('a.navbutton').action({
    // initialize the submenu - gets general data in order to attempt to position
    // the submenu in relation to the image/anchor tag that opens it
        initialize: function() {
            // check if element has our flag for having a drop menu
            if (this.hasClass('hasSubNav')) {
                var listId = this.name.replace(/For/g, '');
                // have to do it this way.
                // for some reason this.firstChild.getTag() won't work
                if ($(this.firstChild).getTag() == 'img') {
                    // attempt to set offset to be a little taller
                    // than your image
                    var yOffset = this.firstChild.height + 1;
                } else {
                    // set your default offset here
                    var yOffset = 20;
                }
                // set the styles of your list
                // to position it (relatively) correctly
                $(listId).setStyles({ top: yOffset + 'px', left: this.getLeft() + 'px' });
            }
        },
        onmouseover: function() {
            // add mouseover action to change image
            this.firstChild.src = this.firstChild.src.replace(/off/g, 'on');
            // optional effect for mouseover
            this.effect('opacity').custom(.3, 1);
            // check if element has our flag for having a drop menu
            if (this.hasClass('hasSubNav')) {
                // pass the id of the mouseover, so we can determine
                // which list to show
                showList(this.id);
            } else {
                // if the button moused over does not have a list
                // then we close the list since we are obviously
                // on another button now
                if (listStatus == 'open') {
                    hideList();
                }
            }
        },
        onmouseout: function() {
            // switch mouseout button
            this.firstChild.src = this.firstChild.src.replace(/on/g, 'off');
            // optional effect for mouseout
            this.effect('opacity').custom(.3, 1);
        }
    });
});