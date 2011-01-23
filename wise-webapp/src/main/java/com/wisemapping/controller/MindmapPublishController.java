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

package com.wisemapping.controller;

import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.MindMap;
import com.wisemapping.model.User;
import com.wisemapping.security.Utils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MindmapPublishController extends BaseMultiActionController {

    protected ModelAndView handleNoSuchRequestHandlingMethod(NoSuchRequestHandlingMethodException noSuchRequestHandlingMethodException, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {

        final MindMap mindmap = this.getMindmapFromRequest(httpServletRequest);
        if (mindmap == null) {
            throw new IllegalStateException("Map could not be found");
        }

        return new ModelAndView("mindmapPublish", "mindmap", mindmap);
    }

    public ModelAndView save(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws WiseMappingException {

        final MindMap mindmap = this.getMindmapFromRequest(httpServletRequest);
        if (mindmap == null) {
            throw new IllegalStateException("Map could not be found");
        }

        User user = Utils.getUser();
        if (!mindmap.getOwner().equals(user)) {
            throw new IllegalStateException("No enought right to execute this operation");
        }


        final String publicViewStr = httpServletRequest.getParameter("publicView");
        boolean publicView = Boolean.valueOf(publicViewStr);

        if (mindmap.isPublic() != publicView) {
            mindmap.setPublic(publicView);
            getMindmapService().updateMindmap(mindmap, false);
        }


        return new ModelAndView("closeDialog");
    }


}
