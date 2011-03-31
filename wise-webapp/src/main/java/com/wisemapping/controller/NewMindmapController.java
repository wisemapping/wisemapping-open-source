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

package com.wisemapping.controller;

import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.MindMap;
import com.wisemapping.model.User;
import com.wisemapping.security.Utils;
import com.wisemapping.service.MindmapService;
import com.wisemapping.view.MindMapInfoBean;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class NewMindmapController
        extends BaseSimpleFormController {

    public ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors)
            throws ServletException, WiseMappingException, IOException {
        final MindMapInfoBean bean = (MindMapInfoBean) command;

        final String title = bean.getTitle();
        final String description = bean.getDescription();

        final User user = Utils.getUser();
        final MindmapService service = getMindmapService();

        // The map has not been created. Create a new one ...
        MindMap mindmap = new MindMap();
        mindmap.setDescription(description);
        mindmap.setTitle(title);
        mindmap.setOwner(user);

        final String defaultNativeMap = getDefaultMindmapXml(title);
        mindmap.setNativeXml(defaultNativeMap);

        final User dbUSer = getUserService().getUserBy(user.getId());

        service.addMindmap(mindmap, dbUSer);
      
        return new ModelAndView("redirect:editor.htm?mapId=" + mindmap.getId() + "&action=open");
    }


    private String getDefaultMindmapXml(final String title) {

        final StringBuffer map = new StringBuffer();
        map.append("<map>");
        map.append("<topic central=\"true\" text=\"");
        map.append(title);
        map.append("\"/></map>");
        return map.toString();
    }

}
