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
import com.wisemapping.importer.ImporterException;
import com.wisemapping.model.MindMap;
import com.wisemapping.model.User;
import com.wisemapping.security.Utils;
import com.wisemapping.view.TagBean;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TagsController
        extends BaseSimpleFormController {

    //~ Methods ..............................................................................................
    protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {

        final MindMap mindmap = null;
        final User user = Utils.getUser(httpServletRequest);
        final User dbUser = getUserService().getUserBy(user.getId());

        final TagBean tagBean = new TagBean();
        tagBean.setUserTags(dbUser.getTags());
        tagBean.setMindmapId(mindmap.getId());
        tagBean.setMindmapTags(mindmap.getTags());
        tagBean.setMindmapTitle(mindmap.getTitle());
        return tagBean;
    }

    public ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors)
            throws ServletException, WiseMappingException, ImporterException
    {
        final TagBean bean = (TagBean) command;
        final MindMap mindmap = getMindmapService().getMindmapById(bean.getMindmapId());
        getMindmapService().addTags(mindmap, bean.getMindmapTags());
        return new ModelAndView(getSuccessView());
    }
}
