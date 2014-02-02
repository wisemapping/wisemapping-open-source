package com.wisemapping.rest;


import com.wisemapping.exceptions.LabelCouldNotFoundException;
import com.wisemapping.exceptions.LabelMindmapRelationshipNotFoundException;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.LabelMindmap;
import com.wisemapping.rest.model.RestLabelMindmap;
import com.wisemapping.service.LabelMindmapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
public class LabelMindmapController extends BaseController {

    @Qualifier("labelMindmapService")
    @Autowired
    private LabelMindmapService labelMindmapService;

    @RequestMapping(method = RequestMethod.DELETE, value = "/labels/maps")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void removeLabelFromMindmap(@RequestBody RestLabelMindmap restLabelMindmap) throws WiseMappingException {
        final int labelId = restLabelMindmap.getLabelId();
        final int mindmapId = restLabelMindmap.getMindmapId();
        final LabelMindmap relationship = labelMindmapService.getLabelMindmap(labelId, mindmapId);
        if (relationship == null) {
            throw new LabelMindmapRelationshipNotFoundException("Label Map relation could not be found. Label Id: " + labelId + ", Map Id: " + mindmapId);
        }
        labelMindmapService.removeLabelFromMindmap(relationship);
    }


}
