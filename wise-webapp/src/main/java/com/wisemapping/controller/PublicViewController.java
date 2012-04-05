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

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wisemapping.model.MindMap;

public class PublicViewController extends BaseMultiActionController {

    protected ModelAndView handleNoSuchRequestHandlingMethod(NoSuchRequestHandlingMethodException noSuchRequestHandlingMethodException, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws Exception
    {

        final MindMap mindmap = super.getMindmapFromRequest(httpServletRequest);     

        final ModelAndView view = new ModelAndView("mindmapPublicView");
        view.addObject("mindmap", mindmap);
        view.addObject("viewTitle","'"+mindmap.getTitle()+"' Map View");
        return view;
    }
}
