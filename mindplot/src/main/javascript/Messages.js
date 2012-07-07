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

mindplot.Messages = new Class({
    Static:{
        init:function (locale) {
            locale = $defined(locale) ? locale : 'en';
            var bundle = mindplot.Messages.BUNDLES[locale];
            if (bundle == null && locale.indexOf("_") != -1) {
                // Try to locate without the specialization ...
                locale = locale.substring(0, locale.indexOf("_"));
                bundle = mindplot.Messages.BUNDLES[locale];
            }
            mindplot.Messages.__bundle = bundle;
        }
    }
});

$msg = function (key) {
    if (!mindplot.Messages.__bundle) {
        mindplot.Messages.init('en');
    }
    return mindplot.Messages.__bundle[key];
};

mindplot.Messages.BUNDLES = {
    'en':{

        ZOOM_IN:'Zoom In',
        ZOOM_OUT:'Zoom Out',
        TOPIC_SHAPE:'Topic Shape',
        TOPIC_ADD:'Add Topic',
        TOPIC_DELETE:'Delete Topic',
        TOPIC_ICON:'Add Icon',
        TOPIC_LINK:'Add Link',
        TOPIC_RELATIONSHIP:'Relationship',
        TOPIC_COLOR:'Topic Color',
        TOPIC_BORDER_COLOR:'Topic Border Color',
        TOPIC_NOTE:'Add Note',
        FONT_FAMILY:'Font Type',
        FONT_SIZE:'Text Size',
        FONT_BOLD:'Text Bold',
        FONT_ITALIC:'Text Italic',
        UNDO_EDITION:'Undo Edition',
        REDO_EDITION:'Redo Edition',
        UNDO:'Undo',
        REDO:'Redo',
        INSERT:'Insert',
        SAVE:'Save',
        NOTE:'Note',
        ADD_TOPIC:'Add Topic',
        LOADING:'Loading ...',
        EXPORT:'Export',
        PRINT:'Print',
        PUBLISH:'Publish',
        COLLABORATE:'Share',
        HISTORY:'History',
        DISCARD_CHANGES:'Discard Changes',
        FONT_COLOR:'Text Color',
        SAVING:'Saving ...',
        SAVE_COMPLETE:'Save Complete',
        ZOOM_IN_ERROR:'Zoom too high.',
        ZOOM_ERROR:'No more zoom can be applied.',
        ONLY_ONE_TOPIC_MUST_BE_SELECTED:'Could not create a topic. Only one topic must be selected.',
        ONE_TOPIC_MUST_BE_SELECTED:'Could not create a topic. One topic must be selected.',
        SAVE_COULD_NOT_BE_COMPLETED:'Save could not be completed. Try latter.',
        UNEXPECTED_ERROR_LOADING:"We're sorry, an unexpected error has occurred. Try again reloading the editor.\nIf the problem persists, contact us to support@wisemapping.com.",
        MAIN_TOPIC:'Main Topic',
        SUB_TOPIC:'Sub Topic',
        ISOLATED_TOPIC:'Isolated Topic',
        CENTRAL_TOPIC:'Central Topic',
        ONLY_ONE_TOPIC_MUST_BE_SELECTED_COLLAPSE:'Children can not be collapsed. One topic must be selected.',
        SHORTCUTS:'Keyboard Shortcuts',
        ENTITIES_COULD_NOT_BE_DELETED: 'Could not delete topic or relation. At least one map entity must be selected.',
        CENTRAL_TOPIC_CAN_NOT_BE_DELETED: 'Central topic can not be deleted.'
    },
    'es':{
        DISCARD_CHANGES:'Descartar Cambios',
        SAVE:'Guardar',
        INSERT:'Insertar',
        ZOOM_IN:'Acercar',
        ZOOM_OUT:'Alejar',
        TOPIC_BORDER_COLOR:'Color del Borde',
        TOPIC_SHAPE:'Forma del Tópico',
        TOPIC_ADD:'Agregar Tópico',
        TOPIC_DELETE:'Borrar Tópico',
        TOPIC_ICON:'Agregar Icono',
        TOPIC_LINK:'Agregar Enlace',
        TOPIC_NOTE:'Agregar Nota',
        TOPIC_COLOR:'Color Tópico',
        TOPIC_RELATIONSHIP:'Relación',
        FONT_FAMILY:'Tipo de Fuente',
        FONT_SIZE:'Tamaño de Texto',
        FONT_BOLD:'Negrita',
        FONT_ITALIC:'Italica',
        FONT_COLOR:'Color de Texto',
        UNDO_EDITION:'Undo Edition',
        REDO_EDITION:'Redo Edition',
        UNDO:'Rehacer',
        NOTE:'Nota',
        LOADING:'Cargando ...',
        PRINT:'Imprimir',
        PUBLISH:'Publicar',
        REDO:'Deshacer',
        ADD_TOPIC:'Agregar Tópico',
        COLLABORATE:'Compartir',
        EXPORT:'Exportar',
        HISTORY:'History',
        SAVE_COMPLETE:'Grabado Completo',
        SAVING:'Grabando ...',
        ONE_TOPIC_MUST_BE_SELECTED:'No ha sido posible crear un nuevo tópico. Al menos un tópico debe ser seleccionado.',
        ONLY_ONE_TOPIC_MUST_BE_SELECTED:'No ha sido posible crear un nuevo tópico. Solo un tópico debe ser seleccionado.',
        SAVE_COULD_NOT_BE_COMPLETED:'Grabación no pudo ser completada. Intentelo mas tarde.',
        UNEXPECTED_ERROR_LOADING:"Lo sentimos, un error inesperado ha ocurrido. Intentelo nuevamente recargando el editor.\n Si el problema persiste, contactenos a support@wisemapping.com.",
        ZOOM_ERROR:'No es posible aplicar mas zoom.',
        ZOOM_IN_ERROR:'El zoom es muy alto.',
        MAIN_TOPIC:'Tópico Principal',
        SUB_TOPIC:'Tópico Secundario',
        ISOLATED_TOPIC:'Tópico Aislado',
        CENTRAL_TOPIC:'Tópico Central',
        ONLY_ONE_TOPIC_MUST_BE_SELECTED_COLLAPSE:'Tópicos hijos no pueden ser colapsados. Solo un topic debe ser seleccionado.',
        SHORTCUTS:'Accesos directos'
    },
    zh_cn:{
        ZOOM_IN:'放大',
        ZOOM_OUT:'缩小',
        TOPIC_SHAPE:'节点外形',
        TOPIC_ADD:'添加节点',
        TOPIC_DELETE:'删除节点',
        TOPIC_ICON:'加入图标',
        TOPIC_LINK:'添加链接',
        TOPIC_RELATIONSHIP:'关系',
        TOPIC_COLOR:'节点颜色',
        TOPIC_BORDER_COLOR:'边框颜色',
        TOPIC_NOTE:'添加注释',
        FONT_FAMILY:'字体',
        FONT_SIZE:'文字大小',
        FONT_BOLD:'粗体',
        FONT_ITALIC:'斜体',
        UNDO:'撤销',
        REDO:'重做',
        INSERT:'插入',
        SAVE:'保存',
        NOTE:'注释',
        ADD_TOPIC:'添加节点',
        LOADING:'载入中……',
        EXPORT:'导出',
        PRINT:'打印',
        PUBLISH:'公开',
        COLLABORATE:'共享',
        HISTORY:'历史',
        DISCARD_CHANGES:'清除改变',
        FONT_COLOR:'文本颜色',
        SAVING:'保存中……',
        SAVE_COMPLETE:'完成保存',
        ZOOM_IN_ERROR:'缩放过多。',
        ZOOM_ERROR:'不能再缩放。',
        ONLY_ONE_TOPIC_MUST_BE_SELECTED:'不能创建节点。仅能选择一个节点。',
        ONE_TOPIC_MUST_BE_SELECTED:'不能创建节点。必须选择一个节点。',
        ONLY_ONE_TOPIC_MUST_BE_SELECTED_COLLAPSE:'子节点不能折叠。必须选择一个节点。',
        SAVE_COULD_NOT_BE_COMPLETED:'保存未完成。稍后再试。',
        UNEXPECTED_ERROR_LOADING:'抱歉，突遭错误，我们无法处理你的请求。\n尝试重新装载编辑器。如果问题依然存在请联系support@wisemapping.com。',
        MAIN_TOPIC:'主节点',
        SUB_TOPIC:'子节点',
        ISOLATED_TOPIC:'独立节点',
        CENTRAL_TOPIC:'中心节点',
        SHORTCUTS:'快捷键'
    },
    zh_tw:{
        ZOOM_IN:'放大',
        ZOOM_OUT:'縮小',
        TOPIC_SHAPE:'節點外形',
        TOPIC_ADD:'添加節點',
        TOPIC_DELETE:'刪除節點',
        TOPIC_ICON:'加入圖示',
        TOPIC_LINK:'添加鏈接',
        TOPIC_RELATIONSHIP:'關係',
        TOPIC_COLOR:'節點顏色',
        TOPIC_BORDER_COLOR:'邊框顏色',
        TOPIC_NOTE:'添加注釋',
        FONT_FAMILY:'字體',
        FONT_SIZE:'文字大小',
        FONT_BOLD:'粗體',
        FONT_ITALIC:'斜體',
        UNDO:'撤銷',
        REDO:'重做',
        INSERT:'插入',
        SAVE:'保存',
        NOTE:'注釋',
        ADD_TOPIC:'添加節點',
        LOADING:'載入中……',
        EXPORT:'導出',
        PRINT:'列印',
        PUBLISH:'公開',
        COLLABORATE:'共用',
        HISTORY:'歷史',
        DISCARD_CHANGES:'清除改變',
        FONT_COLOR:'文本顏色',
        SAVING:'保存中……',
        SAVE_COMPLETE:'完成保存',
        ZOOM_IN_ERROR:'縮放過多。',
        ZOOM_ERROR:'不能再縮放。',
        ONLY_ONE_TOPIC_MUST_BE_SELECTED:'不能創建節點。僅能選擇一個節點。',
        ONE_TOPIC_MUST_BE_SELECTED:'不能創建節點。必須選擇一個節點。',
        ONLY_ONE_TOPIC_MUST_BE_SELECTED_COLLAPSE:'子節點不能折疊。必須選擇一個節點。',
        SAVE_COULD_NOT_BE_COMPLETED:'保存未完成。稍後再試。',
        UNEXPECTED_ERROR_LOADING:'抱歉，突遭錯誤，我們無法處理你的請求。\n嘗試重新裝載編輯器。如果問題依然存在請聯繫support@wisemapping.com。',
        MAIN_TOPIC:'主節點',
        SUB_TOPIC:'子節點',
        ISOLATED_TOPIC:'獨立節點',
        CENTRAL_TOPIC:'中心節點',
        SHORTCUTS:'快捷鍵'
    }
};
mindplot.Messages.BUNDLES['zh'] = mindplot.Messages.zh_tw;

