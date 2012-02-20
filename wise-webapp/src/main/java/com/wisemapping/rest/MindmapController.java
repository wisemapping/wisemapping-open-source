package com.wisemapping.rest;


import com.wisemapping.model.MindMap;
import com.wisemapping.model.MindmapUser;
import com.wisemapping.model.User;
import com.wisemapping.rest.model.RestMindmap;
import com.wisemapping.service.MindmapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;

@Controller
public class MindmapController {
    @Autowired
    private MindmapService mindmapService;

    @RequestMapping(method = RequestMethod.GET, value = "/map/{id}", produces = {"text/xml", "application/json", "text/html"})
    @ResponseBody
    public ModelAndView getMindmap(@PathVariable int id) throws IOException {
        final MindMap mindMap = mindmapService.getMindmapById(id);
        final RestMindmap map = new RestMindmap(mindMap);
        return new ModelAndView("mapView", "map", map);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/maps")
    public ModelAndView getMindmaps() throws IOException {
        final User user = com.wisemapping.security.Utils.getUser();

        final List<MindmapUser> list = mindmapService.getMindmapUserByUser(user);
//        final RestMindMap map = new RestMindmap(mindMap);
//        return new ModelAndView("mapView", "map", map);
        return null;
    }
}
