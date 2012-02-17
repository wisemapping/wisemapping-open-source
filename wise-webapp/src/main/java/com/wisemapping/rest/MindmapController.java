package com.wisemapping.rest;


import com.wisemapping.model.MindMap;
import com.wisemapping.rest.model.RestMindMap;
import com.wisemapping.service.MindmapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/map")
public class MindmapController {
    @Autowired
    private MindmapService mindmapService;


    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    @ResponseBody
    public ModelAndView getMindmap(@PathVariable int id) throws IOException {
        final MindMap mindMap = mindmapService.getMindmapById(id);
        final RestMindMap map = new RestMindMap(mindMap);
        return new ModelAndView("mapView", "map", map);
    }
}
