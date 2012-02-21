package com.wisemapping.rest;


import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.MindMap;
import com.wisemapping.model.MindmapUser;
import com.wisemapping.model.User;
import com.wisemapping.rest.model.RestMindmap;
import com.wisemapping.rest.model.RestMindmapList;
import com.wisemapping.security.Utils;
import com.wisemapping.service.MindmapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class MindmapController extends BaseController{
    @Autowired
    private MindmapService mindmapService;

    @RequestMapping(method = RequestMethod.GET, value = "/maps/{id}", produces = {"application/json", "text/html", "application/xml"})
    @ResponseBody
    public ModelAndView getMindmap(@PathVariable int id) throws IOException {
        final MindMap mindMap = mindmapService.getMindmapById(id);
        final RestMindmap map = new RestMindmap(mindMap);
        return new ModelAndView("mapView", "map", map);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/maps", produces = {"application/json", "text/html", "application/xml"})
    public ModelAndView getMindmaps() throws IOException {
        final User user = com.wisemapping.security.Utils.getUser();

        final List<MindmapUser> mapsByUser = mindmapService.getMindmapUserByUser(user);
        final List<MindMap> mindmaps = new ArrayList<MindMap>();
        for (MindmapUser mindmapUser : mapsByUser) {
            mindmaps.add(mindmapUser.getMindMap());
        }

        final RestMindmapList restMindmapList = new RestMindmapList(mindmaps);
        return new ModelAndView("mapsView", "list", restMindmapList);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/maps/{id}", consumes = {"application/xml", "application/json"}, produces = {"application/json", "text/html", "application/xml"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateMap(@RequestBody RestMindmap restMindmap, @PathVariable int id, @RequestParam(required = false) boolean minor) throws IOException, WiseMappingException {

        final MindMap mindMap = mindmapService.getMindmapById(id);
        final User user = Utils.getUser();

        final String properties = restMindmap.getProperties();
        mindMap.setProperties(properties);

        final Calendar now = Calendar.getInstance();
        mindMap.setLastModificationTime(now);
        mindMap.setLastModifierUser(user.getUsername());

        final Calendar lastModification = Calendar.getInstance();
        lastModification.setTime(new Date());
        mindMap.setLastModificationTime(lastModification);

        final String xml = restMindmap.getXml();
        mindMap.setXmlStr(xml);
        mindmapService.updateMindmap(mindMap, minor);
    }

}
