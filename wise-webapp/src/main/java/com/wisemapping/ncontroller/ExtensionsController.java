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

package com.wisemapping.ncontroller;

import com.wisemapping.model.MindMap;
import com.wisemapping.service.MindmapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@Controller
public class ExtensionsController {
    @Qualifier("mindmapService")
    @Autowired
    private MindmapService mindmapService;

    @RequestMapping(value = "try")
    public ModelAndView tryEditor() throws IOException {

        final MindMap mindmap = mindmapService.getMindmapById(TRY_EXAMPLE_MINDMAP_ID);

        ModelAndView view = new ModelAndView("mindmapEditor", "mindmap", mindmap);
        final String xmlMap = mindmap.getXmlAsJsLiteral();
        view.addObject(MAP_XML_PARAM, xmlMap);
        view.addObject("editorTryMode", true);
        view.addObject("showHelp", true);
        return view;
    }

    @RequestMapping(value = "privacyPolicy")
    public ModelAndView privacyPolicy() {
        return new ModelAndView("privacyPolicy");
    }

    @RequestMapping(value = "termsOfUse")
    public ModelAndView termsOfUse() {
        return new ModelAndView("termsOfUse");
    }

    @RequestMapping(value = "faq")
    public ModelAndView faq() {
        return new ModelAndView("faq");
    }

    public static final int TRY_EXAMPLE_MINDMAP_ID = 3;
    public static final String MAP_XML_PARAM = "mapXml";

}
