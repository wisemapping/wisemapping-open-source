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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wisemapping.model.MindMap;

/**
 * Usage: http://localhost:8080/wisemapping/c/cooker?action=edit&mapId=12
 */
public class MindmapCooker extends BaseMultiActionController {

    public static final String MINDMAP_ID_PARAMETER = "mapId";
    public static final String MAP_XML_PARAM = "mapXml";

    public ModelAndView edit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {

        final String mindmapId = httpServletRequest.getParameter(MINDMAP_ID_PARAMETER);
        final int mapId = Integer.parseInt(mindmapId);
        final MindMap mindmap = getMindmapService().getMindmapById(mapId);

        // Mark as try mode...
        final ModelAndView view = new ModelAndView("mindmapCooker", "mindmap", mindmap);
        return view;
    }

    public ModelAndView save(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {

        final String mindmapId = httpServletRequest.getParameter(MINDMAP_ID_PARAMETER);
        final int mapId = Integer.parseInt(mindmapId);
        final MindMap mindmap = getMindmapService().getMindmapById(mapId);

        String xml = httpServletRequest.getParameter("xml");
        mindmap.setXmlStr(xml);

        getMindmapService().updateMindmap(mindmap, false);

        // Mark as try mode...
        final ModelAndView view = new ModelAndView("mindmapCooker", "mindmap", mindmap);
        return view;
    }

}
