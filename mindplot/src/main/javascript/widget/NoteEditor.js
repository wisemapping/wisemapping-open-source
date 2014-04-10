mindplot.widget.NoteEditor = new Class({
    Extends:BootstrapDialog,
    initialize:function (model) {
        $assert(model, "model can not be null");
        this.parent($msg("Note"), {
            cancelButton: true,
            closeButton: true,
            acceptButton: true
        });
        var panel = this._buildPanel(model);
        this.setContent(panel);
    },


_buildPanel:function (model) {
        var result = $('<div></div>').css("padding-top", "5px");

        var form = $('<form></form>').attr({
            'action':'none',
            'id':'noteFormId'
        });
        // Add textarea
        var textArea = $('<textarea></textarea>').attr({
                'placeholder':$msg('WRITE_YOUR_TEXT_HERE'),
                'required':'true',
                'autofocus':'autofocus'
        });
        textArea.css({
            'width':'100%',
            'height':80,
            'resize':'none'
        });

        form.append(textArea);

    if (model.getValue() != null){
            textArea.val(model.getValue());
        }

        result.append(form);

        $(document).ready(function () {
            $(document).on('submit','#noteFormId',function () {
                event.stopPropagation();
                event.preventDefault();
                if (textArea.val()) {
                    model.setValue(textArea.val());
                }
                this.close();
            });

            $(document).on('click','#acceptBtn',function () {
                $("#noteFormId").submit();
            });
        });
        return result;
    },

    close:function () {
        this.close();
    }
});

/*
 *    Copyright [2012] [wisemapping]
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
