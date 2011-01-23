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

import com.wisemapping.model.MindMap;
import com.wisemapping.model.MindMapCriteria;
import com.wisemapping.security.Utils;
import com.wisemapping.view.MindMapBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchController extends BaseMultiActionController {
    private static final int MAX_RESULT = 20;

    public ModelAndView handleNoSuchRequestHandlingMethod(NoSuchRequestHandlingMethodException noSuchRequestHandlingMethodException, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        return searchPage(httpServletRequest, httpServletResponse);
    }

    public ModelAndView searchPage(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Search");
        return new ModelAndView("search");
    }

    public ModelAndView showAll(HttpServletRequest request, HttpServletResponse response)
    {
        final Map<String, Object> viewAttrMap = getRequestAttributes(request);
        
        viewAttrMap.put("emptyCriteria", false);
        com.wisemapping.model.User user = Utils.getUser();
        viewAttrMap.put("user",user);

        final List<MindMap> searchResult = getMindmapService().getPublicMaps(MAX_RESULT);
        final List<MindMapBean> result = new ArrayList<MindMapBean>();
        for (MindMap mindMap : searchResult) {
            result.add(new MindMapBean(mindMap));
        }
        viewAttrMap.put("wisemapsList", result);


        return new ModelAndView("searchResult", viewAttrMap);
    }

    public ModelAndView search(HttpServletRequest request, HttpServletResponse response) {

        logger.info("Search Result");

        final Map<String, Object> viewAttrMap = getRequestAttributes(request);

        final MindMapCriteria criteria = getMindMapCriteriaFromRequest(request);
        viewAttrMap.put("emptyCriteria", criteria.isEmpty());
        com.wisemapping.model.User user = Utils.getUser();
        viewAttrMap.put("user",user);
        if (!criteria.isEmpty()) {
            final List<MindMap> searchResult = getMindmapService().search(criteria);
            final List<MindMapBean> result = new ArrayList<MindMapBean>();
            for (MindMap mindMap : searchResult) {
                result.add(new MindMapBean(mindMap));
            }
            viewAttrMap.put("wisemapsList", result);
        }

        return new ModelAndView("searchResult", viewAttrMap);
    }   

     private Map<String, Object> getRequestAttributes(HttpServletRequest request) {
        final Map<String, Object> viewAttrMap = new HashMap<String, Object>();
        viewAttrMap.put("titleOrTags", request.getParameter("titleOrTags"));
        final String name = request.getParameter("name");
        viewAttrMap.put("name", name);
        viewAttrMap.put("description", request.getParameter("description"));
        viewAttrMap.put("tags", request.getParameter("tags"));
        viewAttrMap.put("advanceSearch", request.getParameter("advanceSearch"));
        return viewAttrMap;
    }
}
