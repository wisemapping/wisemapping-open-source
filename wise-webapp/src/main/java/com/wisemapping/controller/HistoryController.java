/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* $Id: file 64488 2006-03-10 17:32:09Z paulo $
*/

package com.wisemapping.controller;

import com.wisemapping.model.MindMap;
import com.wisemapping.model.MindMapHistory;
import com.wisemapping.view.HistoryBean;
import com.wisemapping.view.MindMapBean;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryController
    extends BaseMultiActionController
{
    private static final String HISTORY_ID = "historyId";

    public ModelAndView list(HttpServletRequest request, HttpServletResponse response)
            throws java.lang.Exception {

        final MindMap map = getMindmapFromRequest(request);
        final List<HistoryBean> historyBeanList = createHistoryBeanList(map);

        final Map<String,Object> attr = new HashMap<String,Object>();
        attr.put("minmap",new MindMapBean(map));
        attr.put("goToMindmapList",request.getParameter("goToMindmapList"));
        attr.put("historyBeanList",historyBeanList);
        return new ModelAndView("mindmapHistory",attr);
    }

    public ModelAndView revert(HttpServletRequest request, HttpServletResponse response)
            throws java.lang.Exception {

        final MindMap map = getMindmapFromRequest(request);
        final int revertId = Integer.parseInt(request.getParameter(HISTORY_ID));
        getMindmapService().revertMapToHistory(map, revertId);
        final StringBuilder redirectionTo = new StringBuilder("redirect:");

        String goToMindmapList = request.getParameter("goToMindmapList");
        if (goToMindmapList != null)
        {
            redirectionTo.append("mymaps.htm");
        }
        else
        {
            redirectionTo.append("editor.htm?mapId=");
            redirectionTo.append(map.getId());
            redirectionTo.append("&action=open");
        }
        return new ModelAndView(redirectionTo.toString());
    }

    private List<HistoryBean> createHistoryBeanList(MindMap map) {
        final List<MindMapHistory> list = getMindmapService().getMindMapHistory(map.getId());
        final List<HistoryBean> historyBeanList = new ArrayList<HistoryBean>(list.size());
        for (MindMapHistory mindMapHistory : list) {
            historyBeanList.add(new HistoryBean(mindMapHistory.getMindmapId(),mindMapHistory.getId(),mindMapHistory.getCreator(),mindMapHistory.getCreationTime()));
        }
        return historyBeanList;
    }
}
