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

public class PublicPagesController extends BaseMultiActionController {

    public ModelAndView handleNoSuchRequestHandlingMethod(NoSuchRequestHandlingMethodException noSuchRequestHandlingMethodException, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        return this.home(httpServletRequest, httpServletResponse);
    }

    public ModelAndView faq(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        return new ModelAndView("faq");
    }

    public ModelAndView aboutUs(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        return new ModelAndView("aboutUs");
    }

    public ModelAndView video(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        return new ModelAndView("video");
    }

    public ModelAndView crew(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        return new ModelAndView("crew");
    }

    public ModelAndView privacyPolicy(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        return new ModelAndView("privacyPolicy");
    }

    public ModelAndView installCFG(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        return new ModelAndView("installCFG");
    }

    public ModelAndView home(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        return new ModelAndView("homepage");
    }

    public ModelAndView tryEditor(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {

        final MindMap mindmap = getMindmapService().getMindmapById(TRY_EXAMPLE_MINDMAP_ID);

        ModelAndView view = new ModelAndView("mindmapEditor", "mindmap", mindmap);
        final String xmlMap = mindmap.getXmlAsJsLiteral();
        view.addObject(MAP_XML_PARAM, xmlMap);
        view.addObject("editorTryMode", true);
        view.addObject("showHelp", true);
        return view;
    }

    public ModelAndView termsOfUse(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        return new ModelAndView("termsOfUse");
    }

    public static final int TRY_EXAMPLE_MINDMAP_ID = 3;
    public static final String MAP_XML_PARAM = "mapXml";

}
