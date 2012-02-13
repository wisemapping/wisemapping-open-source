package com.wisemapping.rest;


import com.wisemapping.model.MindMap;
import com.wisemapping.service.MindmapService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class MindmapController {
    private MindmapService mindmapService;

    public void setMindmapService(MindmapService mindmapService) {
        this.mindmapService = mindmapService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/map/{id}")
    public
    @ResponseBody
    Map<String, Object> getMindmap(@PathVariable int id) throws IOException {
        final Map<String, Object> result = new HashMap<String, Object>();
        final MindMap mindMap = mindmapService.getMindmapById(id);
        result.put("xml", mindMap.getNativeXml());
        result.put("creationTime", mindMap.getCreationTime());
        result.put("description", mindMap.getDescription());
        result.put("lastModification", mindMap.getLastModificationDate());
        result.put("owner", mindMap.getOwner().getUsername());
        return result;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/map/{id}")
    public void updateMindmap(@PathVariable int id) throws IOException {

    }


}
