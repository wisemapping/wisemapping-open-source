/*
 *    Copyright [2022] [wisemapping]
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

import com.wisemapping.exceptions.*;
import com.wisemapping.model.*;
import com.wisemapping.rest.model.*;
import com.wisemapping.security.Utils;
import com.wisemapping.service.*;
import com.wisemapping.validator.MapInfoValidator;
import jakarta.transaction.Transactional;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@Transactional
@PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
public class MindmapController extends BaseController {
    final Logger logger = LogManager.getLogger();

    private static final String LATEST_HISTORY_REVISION = "latest";

    @Qualifier("mindmapService")
    @Autowired
    private MindmapService mindmapService;

    @Qualifier("labelService")
    @Autowired
    private LabelService labelService;

    @Qualifier("userService")
    @Autowired
    private UserService userService;

    @Value("${accounts.maxInactive:20}")
    private int maxAccountsInactive;

    @RequestMapping(method = RequestMethod.GET, value = "/maps/{id}", produces = {"application/json"})
    @ResponseBody
    public RestMindmap retrieve(@PathVariable int id) throws WiseMappingException {
        final User user = Utils.getUser();
        final Mindmap mindMap = findMindmapById(id);
        return new RestMindmap(mindMap, user);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/maps/", produces = {"application/json"})
    public RestMindmapList retrieveList(@RequestParam(required = false) String q) {
        final User user = Utils.getUser();

        final MindmapFilter filter = MindmapFilter.parse(q);
        List<Mindmap> mindmaps = mindmapService.findMindmapsByUser(user);
        mindmaps = mindmaps
                .stream()
                .filter(m -> filter.accept(m, user)).toList();

        return new RestMindmapList(mindmaps, user);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/maps/{id}/history/", produces = {"application/json"})
    public RestMindmapHistoryList fetchHistory(@PathVariable int id) {
        final List<MindMapHistory> histories = mindmapService.findMindmapHistory(id);
        final RestMindmapHistoryList result = new RestMindmapHistoryList();
        for (MindMapHistory history : histories) {
            result.addHistory(new RestMindmapHistory(history));
        }
        return result;
    }

    @RequestMapping(value = "/maps/{id}/history/{hid}", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateRevertMindmap(@PathVariable int id, @PathVariable String hid) throws WiseMappingException, IOException {
        final Mindmap mindmap = findMindmapById(id);
        final User user = Utils.getUser();

        if (LATEST_HISTORY_REVISION.equals(hid)) {
            // Revert to the latest stored version ...
            List<MindMapHistory> mindmapHistory = mindmapService.findMindmapHistory(id);
            if (mindmapHistory.size() > 0) {
                final MindMapHistory mindMapHistory = mindmapHistory.get(0);
                mindmap.setZippedXml(mindMapHistory.getZippedXml());
                saveMindmapDocument(true, mindmap, user);
            }
        } else {
            mindmapService.revertChange(mindmap, Integer.parseInt(hid));
        }
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/maps/{id}/document", consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateDocument(@RequestBody RestMindmap restMindmap, @PathVariable int id, @RequestParam(required = false) boolean minor) throws WiseMappingException, IOException {

        final Mindmap mindmap = findMindmapById(id);
        final User user = Utils.getUser();

        // Validate arguments ...
        final String properties = restMindmap.getProperties();
        if (properties == null) {
            throw new IllegalArgumentException("Map properties can not be null");
        }

        // Have permissions ?
        final LockManager lockManager = mindmapService.getLockManager();
        lockManager.lock(mindmap, user);

        // Update collaboration properties ...
        final CollaborationProperties collaborationProperties = mindmap.findCollaborationProperties(user);
        collaborationProperties.setMindmapProperties(properties);

        // Validate content ...
        final String xml = restMindmap.getXml();
        mindmap.setXmlStr(xml);

        // Update map ...
        saveMindmapDocument(minor, mindmap, user);
    }

    @PreAuthorize("permitAll()")
    @RequestMapping(method = RequestMethod.GET, value = {"/maps/{id}/document/xml", "/maps/{id}/document/xml-pub"}, consumes = {"text/plain"}, produces = {"application/xml; charset=UTF-8"})
    @ResponseBody
    public byte[] retrieveDocument(@PathVariable int id, @NotNull HttpServletResponse response) throws WiseMappingException, IOException {
        final Mindmap mindmap = findMindmapById(id);

        String xmlStr = mindmap.getXmlStr();
        return xmlStr.getBytes(StandardCharsets.UTF_8);
    }

    @RequestMapping(method = RequestMethod.PUT, value = {"/maps/{id}/document/xml"}, consumes = {"text/plain"})
    @ResponseBody
    public void updateDocument(@PathVariable int id, @RequestBody String xmlDoc) throws WiseMappingException, IOException {
        final Mindmap mindmap = findMindmapById(id);
        final User user = Utils.getUser();
        mindmap.setXmlStr(xmlDoc);

        saveMindmapDocument(false, mindmap, user);
    }


    @RequestMapping(method = RequestMethod.GET, value = {"/maps/{id}/{hid}/document/xml"}, consumes = {"text/plain"}, produces = {"application/xml; charset=UTF-8"})
    @ResponseBody
    public byte[] retrieveDocument(@PathVariable int id, @PathVariable int hid, @NotNull HttpServletResponse response) throws WiseMappingException, IOException {
        final MindMapHistory mindmapHistory = mindmapService.findMindmapHistory(id, hid);
        return mindmapHistory.getUnzipXml();
    }


    /**
     * The intention of this method is the update of several properties at once ...
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/maps/{id}", consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateProperties(@RequestBody RestMindmap restMindmap, @PathVariable int id, @RequestParam(required = false) boolean minor) throws IOException, WiseMappingException {

        final Mindmap mindmap = findMindmapById(id);
        final User user = Utils.getUser();

        final String xml = restMindmap.getXml();
        if (xml != null && !xml.isEmpty()) {
            mindmap.setXmlStr(xml);
        }

        // Update title  ...
        final String title = restMindmap.getTitle();
        if (title != null && !title.equals(mindmap.getTitle())) {
            if (mindmapService.getMindmapByTitle(title, user) != null) {
                throw buildValidationException("title", "You already have a map with this title");
            }
            mindmap.setTitle(title);
        }

        // Update description ...
        final String description = restMindmap.getDescription();
        if (description != null) {
            mindmap.setDescription(description);
        }

        // Update document properties ...
        final String properties = restMindmap.getProperties();
        if (properties != null) {
            final CollaborationProperties collaborationProperties = mindmap.findCollaborationProperties(user);
            collaborationProperties.setMindmapProperties(properties);
        }

        // Update map ...
        saveMindmapDocument(minor, mindmap, user);
    }

    @NotNull
    private Mindmap findMindmapById(int id) throws MapCouldNotFoundException, AccessDeniedSecurityException {
        // Has enough permissions ?
        final User user = Utils.getUser();
        if (!mindmapService.hasPermissions(user, id, CollaborationRole.VIEWER)) {
            throw new AccessDeniedSecurityException(id, user);
        }

        // Does the map exists ?
        final Mindmap result = mindmapService.findMindmapById(id);
        if (result == null) {
            throw new MapCouldNotFoundException("Map could not be found. Id:" + id);
        }
        return result;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/maps/{id}/title", consumes = {"text/plain"}, produces = {"application/json"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateTitle(@RequestBody String title, @PathVariable int id) throws WiseMappingException {

        final Mindmap mindMap = findMindmapById(id);
        final User user = Utils.getUser();

        // Is there a map with the same name ?
        if (mindmapService.getMindmapByTitle(title, user) != null) {
            throw buildValidationException("title", "You already have a mindmap with this title");
        }

        // Update map ...
        final Mindmap mindmap = findMindmapById(id);
        mindmap.setTitle(title);
        mindmapService.updateMindmap(mindMap, false);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/maps/{id}/collabs/", consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateCollabs(@PathVariable int id, @NotNull @RequestBody RestCollaborationList restCollabs) throws CollaborationException, MapCouldNotFoundException, AccessDeniedSecurityException, InvalidEmailException, TooManyInactiveAccountsExceptions {
        final Mindmap mindMap = findMindmapById(id);

        // Only owner can change collaborators...
        final User user = Utils.getUser();
        if (!mindMap.hasPermissions(user, CollaborationRole.OWNER)) {
            throw new IllegalArgumentException("No enough permissions");
        }

        // Do not allow more than 20 collabs not active
        verifyActiveCollabs(restCollabs, user);

        // Compare one by one if some of the elements has been changed ....
        final Set<Collaboration> collabsToRemove = new HashSet<>(mindMap.getCollaborations());
        for (RestCollaboration restCollab : restCollabs.getCollaborations()) {
            final String email = restCollab.getEmail();

            // Is a valid email address ?
            if (!EmailValidator.getInstance().isValid(email)) {
                throw new InvalidEmailException(email);
            }

            final Collaboration collaboration = mindMap.findCollaboration(email);
            // Validate role format ...
            String roleStr = restCollab.getRole();
            if (roleStr == null) {
                throw new IllegalArgumentException(roleStr + " is not a valid role");
            }

            // Remove from the list of pending to remove ...
            if (collaboration != null) {
                collabsToRemove.remove(collaboration);
            }

            // Is owner ?
            final CollaborationRole role = CollaborationRole.valueOf(roleStr.toUpperCase());
            if (role != CollaborationRole.OWNER) {
                mindmapService.addCollaboration(mindMap, restCollab.getEmail(), role, restCollabs.getMessage());
            }
        }

        // Remove all collaborations that no applies anymore ..
        for (final Collaboration collaboration : collabsToRemove) {
            mindmapService.removeCollaboration(mindMap, collaboration);
        }
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/maps/{id}/collabs/", consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void addCollab(@PathVariable int id, @NotNull @RequestBody RestCollaborationList restCollabs) throws CollaborationException, MapCouldNotFoundException, AccessDeniedSecurityException, InvalidEmailException, TooManyInactiveAccountsExceptions, OwnerCannotChangeException {
        final Mindmap mindMap = findMindmapById(id);

        // Only owner can change collaborators...
        final User user = Utils.getUser();
        if (!mindMap.hasPermissions(user, CollaborationRole.OWNER)) {
            throw new AccessDeniedSecurityException("User must be owner to share mindmap");
        }

        // Do not allow more than 20 collabs not active
        verifyActiveCollabs(restCollabs, user);

        // Is valid email address ?
        final EmailValidator emailValidator = EmailValidator.getInstance();
        final Set<String> invalidEmails = restCollabs
                .getCollaborations()
                .stream()
                .map(RestCollaboration::getEmail)
                .filter(e -> !emailValidator.isValid(e)).collect(Collectors.toSet());

        if (!invalidEmails.isEmpty()) {
            throw new InvalidEmailException(String.join(", ", invalidEmails));
        }

        // Has any role changed ?. Just removed it.
        final Map<String, Collaboration> collabByEmail = mindMap
                .getCollaborations()
                .stream()
                .collect(Collectors.toMap(collaboration -> collaboration.getCollaborator().getEmail(), collaboration -> collaboration));


        // Great, let's add all the collabs again ...
        for (RestCollaboration restCollab : restCollabs.getCollaborations()) {
            // Validate newRole format ...
            final String roleStr = restCollab.getRole();
            if (roleStr == null) {
                throw new IllegalArgumentException(roleStr + " is not a valid newRole");
            }

            // Had the newRole changed ?. Otherwise, don't touch it.
            final CollaborationRole newRole = CollaborationRole.valueOf(roleStr.toUpperCase());
            final String collabEmail = restCollab.getEmail();
            final Collaboration currentCollab = collabByEmail.get(collabEmail);
            if (currentCollab == null || currentCollab.getRole() != newRole) {

                // Are we trying to change the owner ...
                if (currentCollab != null && currentCollab.getRole() == CollaborationRole.OWNER) {
                    throw new OwnerCannotChangeException(collabEmail);
                }

                // Role can not be changed ...
                if (newRole == CollaborationRole.OWNER) {
                    throw new OwnerCannotChangeException(collabEmail);
                }

                // This is collaboration that with different newRole, try to change it ...
                if (currentCollab != null) {
                    mindmapService.removeCollaboration(mindMap, currentCollab);
                }
                mindmapService.addCollaboration(mindMap, collabEmail, newRole, restCollabs.getMessage());
            }
        }
    }


    @RequestMapping(method = RequestMethod.GET, value = "/maps/{id}/collabs", produces = {"application/json"})
    public RestCollaborationList retrieveList(@PathVariable int id) throws MapCouldNotFoundException, AccessDeniedSecurityException {
        final Mindmap mindMap = findMindmapById(id);

        final Set<Collaboration> collaborations = mindMap.getCollaborations();
        final List<RestCollaboration> collabs = new ArrayList<>();
        for (Collaboration collaboration : collaborations) {
            collabs.add(new RestCollaboration(collaboration));
        }

        final RestCollaborationList result = new RestCollaborationList();
        result.setCollaborations(collabs);

        return result;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/maps/{id}/description", consumes = {"text/plain"}, produces = {"application/json"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateDescription(@RequestBody String description, @PathVariable int id) throws WiseMappingException {
        final Mindmap mindmap = findMindmapById(id);
        mindmap.setDescription(description);
        mindmapService.updateMindmap(mindmap, false);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/maps/{id}/publish", consumes = {"text/plain"}, produces = {"application/json"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updatePublishState(@RequestBody String value, @PathVariable int id) throws WiseMappingException {

        final Mindmap mindMap = findMindmapById(id);

        final User user = Utils.getUser();
        if (!mindMap.hasPermissions(user, CollaborationRole.OWNER)) {
            throw new IllegalArgumentException("No enough to execute this operation");
        }

        // Update map status ...
        mindMap.setPublic(Boolean.parseBoolean(value));
        mindmapService.updateMindmap(mindMap, false);

    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/maps/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteMapById(@PathVariable int id) throws IOException, WiseMappingException {
        final User user = Utils.getUser();
        final Mindmap mindmap = findMindmapById(id);
        mindmapService.removeMindmap(mindmap, user);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/maps/{id}/collabs")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteCollabByEmail(@PathVariable int id, @RequestParam(required = false) String email) throws IOException, WiseMappingException {
        logger.debug("Deleting permission for email:" + email);

        // Is a valid email address ?
        final EmailValidator emailValidator = EmailValidator.getInstance();
        if (!emailValidator.isValid(email)) {
            throw new InvalidEmailException(email);
        }

        final Mindmap mindmap = findMindmapById(id);
        final User user = Utils.getUser();

        // Only owner can change collaborators...
        if (!mindmap.hasPermissions(user, CollaborationRole.OWNER)) {
            throw new IllegalArgumentException("No enough permissions");
        }

        final Collaboration collab = mindmap.findCollaboration(email);
        if (collab != null) {
            CollaborationRole role = collab.getRole();

            // Owner collab can not be removed ...
            if (role == CollaborationRole.OWNER) {
                throw new IllegalArgumentException("Can not remove owner collab");
            }
            mindmapService.removeCollaboration(mindmap, collab);
        }
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/maps/{id}/starred", consumes = {"text/plain"}, produces = {"application/json"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateStarredState(@RequestBody String value, @PathVariable int id) throws WiseMappingException {

        logger.debug("Update starred:" + value);
        final Mindmap mindmap = findMindmapById(id);
        final User user = Utils.getUser();

        // Update map status ...
        final boolean starred = Boolean.parseBoolean(value);
        final Optional<Collaboration> collaboration = mindmap.findCollaboration(user);
        if (!collaboration.isPresent()) {
            throw new WiseMappingException("No enough permissions.");
        }
        collaboration.get().getCollaborationProperties().setStarred(starred);
        mindmapService.updateCollaboration(user, collaboration.get());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/maps/{id}/starred", produces = {"text/plain"})
    @ResponseBody
    public String fetchStarred(@PathVariable int id) throws WiseMappingException {
        final Mindmap mindmap = findMindmapById(id);
        final User user = Utils.getUser();

        final Optional<Collaboration> collaboration = mindmap.findCollaboration(user);
        if (!collaboration.isPresent()) {
            throw new WiseMappingException("No enough permissions.");
        }
        boolean result = collaboration.get().getCollaborationProperties().getStarred();
        return Boolean.toString(result);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/maps/batch")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void batchDelete(@RequestParam() String ids) throws IOException, WiseMappingException {
        final User user = Utils.getUser();
        final String[] mapsIds = ids.split(",");
        try {
            for (final String mapId : mapsIds) {
                final Mindmap mindmap = findMindmapById(Integer.parseInt(mapId));
                mindmapService.removeMindmap(mindmap, user);
            }
        } catch (Exception e) {
            final AccessDeniedSecurityException accessDenied = new AccessDeniedSecurityException("Map could not be deleted. Maps to be deleted:" + ids);
            accessDenied.initCause(e);
            throw accessDenied;
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/maps", consumes = {"application/xml", "application/json"})
    @ResponseStatus(value = HttpStatus.CREATED)
    public void createMap(@RequestBody(required = false) String mapXml, @NotNull HttpServletResponse response, @RequestParam(required = false) String title, @RequestParam(required = false) String description) throws IOException, WiseMappingException {

        final Mindmap mindmap = new Mindmap();
        if (title != null && !title.isEmpty()) {
            mindmap.setTitle(title);
        }

        if (description != null && !description.isEmpty()) {
            mindmap.setDescription(description);
        }

        // Validate ...
        final BindingResult result = new BeanPropertyBindingResult(mindmap, "");
        new MapInfoValidator(mindmapService).validate(mindmap, result);
        if (result.hasErrors()) {
            throw new ValidationException(result);
        }

        // If the user has not specified the xml content, add one ...
        if (mapXml == null || mapXml.isEmpty()) {
            mapXml = Mindmap.getDefaultMindmapXml(mindmap.getTitle());
        }
        mindmap.setXmlStr(mapXml);

        // Add new mindmap ...
        final User user = Utils.getUser(true);
        mindmapService.addMindmap(mindmap, user);

        // Return the new created map ...
        response.setHeader("Location", "/service/maps/" + mindmap.getId());
        response.setHeader("ResourceId", Integer.toString(mindmap.getId()));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/maps/{id}", consumes = {"application/json"}, produces = {"application/json", "text/plain"})
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
        final Mindmap mindMap = findMindmapById(id);
        final Mindmap clonedMap = mindMap.shallowClone();
        clonedMap.setTitle(restMindmap.getTitle());
        clonedMap.setDescription(restMindmap.getDescription());

        // Add new mindmap ...
        mindmapService.addMindmap(clonedMap, user);

        // Return the new created map ...
        response.setHeader("Location", "/service/maps/" + clonedMap.getId());
        response.setHeader("ResourceId", Integer.toString(clonedMap.getId()));
    }

    private void saveMindmapDocument(boolean minor, @NotNull final Mindmap mindMap, @NotNull final User user) throws WiseMappingException {
        final Calendar now = Calendar.getInstance();
        mindMap.setLastModificationTime(now);
        mindMap.setLastEditor(user);
        mindmapService.updateMindmap(mindMap, !minor);
    }

    private ValidationException buildValidationException(@NotNull String fieldName, @NotNull String message) throws WiseMappingException {
        final BindingResult result = new BeanPropertyBindingResult(new RestMindmap(), "");
        result.rejectValue(fieldName, "error.not-specified", null, message);
        return new ValidationException(result);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/maps/{id}/labels/{lid}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void removeLabelFromMap(@PathVariable int id, @PathVariable int lid) throws WiseMappingException {
        final User user = Utils.getUser();
        final Mindmap mindmap = findMindmapById(id);
        final Label label = labelService.findLabelById(lid, user);

        if (label == null) {
            throw new LabelCouldNotFoundException("Label could not be found. Id: " + lid);
        }

        mindmap.removeLabel(label);
        mindmapService.updateMindmap(mindmap, false);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/maps/{id}/labels", consumes = {"application/json"})
    @ResponseStatus(value = HttpStatus.OK)
    public void updateLabel(@PathVariable int id, @RequestBody int lid) throws WiseMappingException {
        final User user = Utils.getUser();
        final Label label = labelService.findLabelById(lid, user);
        if (label == null) {
            throw new LabelCouldNotFoundException("Label could not be found. Id: " + lid);
        }

        final Mindmap mindmap = findMindmapById(id);
        mindmap.addLabel(label);
        mindmapService.updateMindmap(mindmap, false);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/maps/{id}/lock", consumes = {"text/plain"}, produces = {"application/json"})
    public ResponseEntity<RestLockInfo> lockMindmap(@RequestBody String value, @PathVariable int id) throws WiseMappingException {
        final User user = Utils.getUser();
        final LockManager lockManager = mindmapService.getLockManager();
        final Mindmap mindmap = findMindmapById(id);

        ResponseEntity<RestLockInfo> result = new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        if (Boolean.parseBoolean(value)) {
            final LockInfo lockInfo = lockManager.lock(mindmap, user);
            final RestLockInfo restLockInfo = new RestLockInfo(lockInfo, user);
            result = new ResponseEntity<>(restLockInfo, HttpStatus.OK);
        } else {
            lockManager.unlock(mindmap, user);
        }
        return result;
    }

    private void verifyActiveCollabs(@NotNull RestCollaborationList restCollabs, User user) throws TooManyInactiveAccountsExceptions {
        // Do not allow more than 20 new accounts per mindmap...
        final List<Mindmap> userMindmaps = mindmapService.findMindmapsByUser(user);
        final Set<String> allEmails = userMindmaps
                .stream()
                .filter(m -> m.hasPermissions(user, CollaborationRole.OWNER))
                .map(Mindmap::getCollaborations)
                .flatMap(Collection::stream)
                .map(c -> c.getCollaborator().getEmail())
                .collect(Collectors.toSet());
        allEmails.addAll(restCollabs
                .getCollaborations().stream()
                .map(RestCollaboration::getEmail)
                .collect(Collectors.toSet()));

        long inactiveAccounts = allEmails.stream().filter(e -> userService.getUserBy(e) == null).count();
        if (inactiveAccounts > maxAccountsInactive) {
            throw new TooManyInactiveAccountsExceptions(inactiveAccounts);
        }
    }
}
