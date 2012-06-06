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

package com.wisemapping.rest;


import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.importer.ImportFormat;
import com.wisemapping.importer.Importer;
import com.wisemapping.importer.ImporterException;
import com.wisemapping.importer.ImporterFactory;
import com.wisemapping.model.MindMap;
import com.wisemapping.model.MindmapUser;
import com.wisemapping.model.User;
import com.wisemapping.rest.model.RestMindmap;
import com.wisemapping.rest.model.RestMindmapInfo;
import com.wisemapping.rest.model.RestMindmapList;
import com.wisemapping.security.Utils;
import com.wisemapping.service.MindmapService;
import com.wisemapping.validator.MapInfoValidator;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;


@Controller
public class MindmapController extends BaseController {
    @Autowired
    private MindmapService mindmapService;


    @RequestMapping(method = RequestMethod.GET, value = "/maps/{id}", produces = {"application/json", "application/xml", "text/html"})
    @ResponseBody
    public ModelAndView retrieve(@PathVariable int id) throws IOException {
        final User user = com.wisemapping.security.Utils.getUser();
        final MindMap mindMap = mindmapService.getMindmapById(id);
        final RestMindmap map = new RestMindmap(mindMap, user);

        return new ModelAndView("mapView", "map", map);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/maps/{id}", produces = {"application/wisemapping+xml"}, params = {"download=wxml"})
    @ResponseBody
    public ModelAndView retrieveAsWise(@PathVariable int id) throws IOException {
        final MindMap mindMap = mindmapService.getMindmapById(id);
        final Map<String, Object> values = new HashMap<String, Object>();

        final User user = com.wisemapping.security.Utils.getUser();
        values.put("mindmap", new RestMindmap(mindMap, user));
        values.put("filename", mindMap.getTitle());
        return new ModelAndView("transformViewWise", values);
    }


    @RequestMapping(method = RequestMethod.GET, value = "/maps/{id}", produces = {"application/freemind"}, params = {"download=mm"})
    @ResponseBody
    public ModelAndView retrieveDocumentAsFreemind(@PathVariable int id) throws IOException {
        final MindMap mindMap = mindmapService.getMindmapById(id);
        final Map<String, Object> values = new HashMap<String, Object>();
        values.put("content", mindMap.getXmlStr());
        values.put("filename", mindMap.getTitle());
        return new ModelAndView("transformViewFreemind", values);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/maps/", produces = {"application/json", "text/html", "application/xml"})
    public ModelAndView retrieveList(@RequestParam(required = false) String q) throws IOException {
        final User user = com.wisemapping.security.Utils.getUser();

        final MindmapFilter filter = MindmapFilter.parse(q);

        final List<MindmapUser> mapsByUser = mindmapService.getMindmapUserByUser(user);
        final List<MindMap> mindmaps = new ArrayList<MindMap>();
        for (MindmapUser mindmapUser : mapsByUser) {
            final MindMap mindmap = mindmapUser.getMindMap();
            if (filter.accept(mindmap, user)) {
                mindmaps.add(mindmap);
            }
        }
        final RestMindmapList restMindmapList = new RestMindmapList(mindmaps, user);
        return new ModelAndView("mapsView", "list", restMindmapList);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/maps/{id}/document", consumes = {"application/xml", "application/json"}, produces = {"application/json", "text/html", "application/xml"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateDocument(@RequestBody RestMindmap restMindmap, @PathVariable int id, @RequestParam(required = false) boolean minor) throws IOException, WiseMappingException {

        final MindMap mindMap = mindmapService.getMindmapById(id);
        final User user = Utils.getUser();

        // Validate arguments ...
        final String properties = restMindmap.getProperties();
        if (properties == null) {
            throw new IllegalArgumentException("Map properties can not be null");
        }
        mindMap.setProperties(properties);

        // Validate content ...
        final String xml = restMindmap.getXml();
        if (xml == null) {
            throw new IllegalArgumentException("Map xml can not be null");
        }
        mindMap.setXmlStr(xml);

        // Update map ...
        saveMindmap(minor, mindMap, user);
    }

    /**
     * The intention of this method is the update of several properties at once ...
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/maps/{id}", consumes = {"application/xml", "application/json"}, produces = {"application/json", "text/html", "application/xml"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void update(@RequestBody RestMindmap restMindmap, @PathVariable int id, @RequestParam(required = false) boolean minor) throws IOException, WiseMappingException {

        final MindMap mindMap = mindmapService.getMindmapById(id);
        final User user = Utils.getUser();

        // Update document properties ...
        final String properties = restMindmap.getProperties();
        if (properties != null) {
            mindMap.setProperties(properties);
        }
        final String xml = restMindmap.getXml();
        if (xml != null) {
            mindMap.setXmlStr(xml);
        }

        // Update title  ...
        final String title = restMindmap.getTitle();
        if (title != null && !title.equals(mindMap.getTitle())) {
            if (mindmapService.getMindmapByTitle(title, user) != null) {
                throw buildValidationException("title", "You already have a map with this title");
            }
            mindMap.setTitle(title);
        }

        // Update description ...
        final String description = restMindmap.getDescription();
        if (description != null) {
            mindMap.setDescription(description);
        }

        final String tags = restMindmap.getTags();
        if (tags != null) {
            mindMap.setTags(tags);
        }

        // Update map ...
        saveMindmap(minor, mindMap, user);
    }


    @RequestMapping(method = RequestMethod.PUT, value = "/maps/{id}/title", consumes = {"text/plain"}, produces = {"application/json", "text/html", "application/xml"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateTitle(@RequestBody String title, @PathVariable int id) throws WiseMappingException {

        final MindMap mindMap = mindmapService.getMindmapById(id);
        final User user = Utils.getUser();

        // Is there a map with the same name ?
        if (mindmapService.getMindmapByTitle(title, user) != null) {

            throw buildValidationException("title", "You already have a mindmap with this title");
        }

        // Update map ...
        final MindMap mindmap = mindmapService.getMindmapById(id);
        mindmap.setTitle(title);
        saveMindmap(true, mindMap, user);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/maps/{id}/description", consumes = {"text/plain"}, produces = {"application/json", "text/html", "application/xml"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateDescription(@RequestBody String description, @PathVariable int id) throws WiseMappingException {

        final MindMap mindMap = mindmapService.getMindmapById(id);
        final User user = Utils.getUser();

        // Update map ...
        final MindMap mindmap = mindmapService.getMindmapById(id);
        mindmap.setDescription(description);
        saveMindmap(true, mindMap, user);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/maps/{id}/publish", consumes = {"text/plain"}, produces = {"application/json", "text/html", "application/xml"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updatePublishState(@RequestBody String value, @PathVariable int id) throws WiseMappingException {

        final MindMap mindMap = mindmapService.getMindmapById(id);
        final User user = Utils.getUser();

        if (!mindMap.getOwner().equals(user)) {
            throw new IllegalArgumentException("No enough to execute this operation");
        }

        // Update map status ...
        mindMap.setPublic(Boolean.parseBoolean(value));
        saveMindmap(true, mindMap, user);

    }

    @RequestMapping(method = RequestMethod.PUT, value = "/maps/{id}/starred", consumes = {"text/plain"}, produces = {"application/json", "text/html", "application/xml"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateStarredState(@RequestBody String value, @PathVariable int id) throws WiseMappingException {

        final MindMap mindMap = mindmapService.getMindmapById(id);
        final User user = Utils.getUser();

        // Update map status ...
        mindMap.setStarred(user, Boolean.parseBoolean(value));
        saveMindmap(true, mindMap, user);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/maps/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateMap(@PathVariable int id) throws IOException, WiseMappingException {
        final User user = Utils.getUser();
        final MindMap mindmap = mindmapService.getMindmapById(id);
        mindmapService.removeMindmap(mindmap, user);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/maps/batch")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void batchDelete(@RequestParam(required = true) String ids) throws IOException, WiseMappingException {
        final User user = Utils.getUser();
        final String[] mapsIds = ids.split(",");
        for (final String mapId : mapsIds) {
            final MindMap mindmap = mindmapService.getMindmapById(Integer.parseInt(mapId));
            mindmapService.removeMindmap(mindmap, user);
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/maps", consumes = {"application/xml", "application/json", "application/wisemapping+xml"})
    @ResponseStatus(value = HttpStatus.CREATED)
    public void createMap(@RequestBody RestMindmap restMindmap, @NotNull HttpServletResponse response, @RequestParam(required = false) String title, @RequestParam(required = false) String description) throws IOException, WiseMappingException {

        // Overwrite title and description if they where specified by parameter.
        if (title != null && !title.isEmpty()) {
            restMindmap.setTitle(title);
        }
        if (description != null && !description.isEmpty()) {
            restMindmap.setDescription(description);
        }

        // Validate ...
        final BindingResult result = new BeanPropertyBindingResult(restMindmap, "");
        new MapInfoValidator(mindmapService).validate(restMindmap.getDelegated(), result);
        if (result.hasErrors()) {
            throw new ValidationException(result);
        }

        // If the user has not specified the xml content, add one ...
        final MindMap delegated = restMindmap.getDelegated();
        String xml = restMindmap.getXml();
        if (xml == null || xml.isEmpty()) {
            xml = MindMap.getDefaultMindmapXml(restMindmap.getTitle());
        }
        delegated.setXmlStr(xml);

        // Add new mindmap ...
        final User user = Utils.getUser();
        mindmapService.addMindmap(delegated, user);

        // Return the new created map ...
        response.setHeader("Location", "/service/maps/" + delegated.getId());
        response.setHeader("ResourceId", Integer.toString(delegated.getId()));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/maps", consumes = {"application/freemind"})
    @ResponseStatus(value = HttpStatus.CREATED)
    public void createMapFromFreemind(@RequestBody byte[] freemindXml, @RequestParam(required = true) String title, @RequestParam(required = false) String description, @NotNull HttpServletResponse response) throws IOException, WiseMappingException, ImporterException {

        // Convert map ...
        final Importer importer = ImporterFactory.getInstance().getImporter(ImportFormat.FREEMIND);
        final ByteArrayInputStream stream = new ByteArrayInputStream(freemindXml);
        final MindMap mindMap = importer.importMap(title, "", stream);

        // Save new map ...
        final User user = Utils.getUser();
        createMap(new RestMindmap(mindMap, user), response, title, description);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/maps/{id}", consumes = {"application/xml", "application/json"})
    @ResponseStatus(value = HttpStatus.CREATED)
    public void createDuplicate(@RequestBody RestMindmapInfo restMindmap, @PathVariable int id, @NotNull HttpServletResponse response) throws IOException, WiseMappingException {
        // Validate ...
        final BindingResult result = new BeanPropertyBindingResult(restMindmap, "");
        new MapInfoValidator(mindmapService).validate(restMindmap.getDelegated(), result);
        if (result.hasErrors()) {
            throw new ValidationException(result);
        }

        // Some basic validations ...
        final User user = Utils.getUser();

        // Create a shallowCopy of the map ...
        final MindMap mindMap = mindmapService.getMindmapById(id);
        final MindMap clonedMap = mindMap.shallowClone();
        clonedMap.setTitle(restMindmap.getTitle());
        clonedMap.setDescription(restMindmap.getDescription());
        clonedMap.setOwner(user);

        // Add new mindmap ...
        mindmapService.addMindmap(clonedMap, user);

        // Return the new created map ...
        response.setHeader("Location", "/service/maps/" + clonedMap.getId());
        response.setHeader("ResourceId", Integer.toString(clonedMap.getId()));
    }

    private void saveMindmap(boolean minor, @NotNull final MindMap mindMap, @NotNull final User user) throws WiseMappingException {
        final Calendar now = Calendar.getInstance();
        mindMap.setLastModificationTime(now);
        mindMap.setLastModifierUser(user.getUsername());
        mindmapService.updateMindmap(mindMap, minor);
    }

    private ValidationException buildValidationException(@NotNull String fieldName, @NotNull String message) throws ValidationException {
        final BindingResult result = new BeanPropertyBindingResult(new RestMindmap(), "");
        result.rejectValue(fieldName, "error.not-specified", null, message);
        return new ValidationException(result);
    }
}
