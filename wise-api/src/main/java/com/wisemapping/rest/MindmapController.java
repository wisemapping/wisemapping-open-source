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
import com.wisemapping.service.spam.SpamDetectionResult;
import com.wisemapping.service.spam.SpamContentExtractor;
import com.wisemapping.service.SpamDetectionService;
import com.wisemapping.validator.MapInfoValidator;
import com.wisemapping.validator.HtmlContentValidator;
import com.wisemapping.view.MindMapBean;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/restful/maps")
public class MindmapController extends BaseController {
    private final Logger logger = LogManager.getLogger();

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

    @Autowired
    private SpamDetectionService spamDetectionService;

    @Autowired
    private HtmlContentValidator htmlContentValidator;
    
    @Autowired
    private SpamContentExtractor spamContentExtractor;

    @Autowired
    private MetricsService metricsService;

    @Value("${app.accounts.max-inactive:20}")
    private int maxAccountsInactive;
    
    @Value("${app.mindmap.note.max-length:10000}")
    private int maxNoteLength;


    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = {"application/json"})
    @ResponseBody
    public RestMindmap retrieve(@PathVariable int id) throws WiseMappingException {
        final Account user = Utils.getUser(true);
        final Mindmap mindMap = findMindmapById(id);
        return new RestMindmap(mindMap, user);
    }

    @PreAuthorize("permitAll()")
    @RequestMapping(method = RequestMethod.GET, value = "/{id}/metadata", produces = {"application/json"})
    @ResponseBody
    public RestMindmapMetadata retrieveMetadata(@PathVariable int id) throws WiseMappingException {
        final Account user = Utils.getUser(false);
        final Mindmap mindmap = findMindmapById(id);

        // If it's not authenticated and map is spam-detected, block access
        if (user == null && mindmap.isSpamDetected()) {
            throw new SpamContentException();
        }

        // For private maps, check if user has permission to access
        if (!mindmap.isPublic() && !mindmapService.hasPermissions(user, mindmap, CollaborationRole.VIEWER)) {
            throw new AccessDeniedSecurityException("You do not have enough right access to see this map");
        }

        final MindMapBean mindMapBean = new MindMapBean(mindmap, user);

        // Is the mindmap locked ?.
        boolean isLocked = false;
        final LockManager lockManager = this.mindmapService.getLockManager();
        String lockFullName = null;
        if (lockManager.isLocked(mindmap) && !lockManager.isLockedBy(mindmap, user)) {
            final LockInfo lockInfo = lockManager.getLockInfo(mindmap);
            isLocked = true;
            lockFullName = lockInfo.getUser().getFullName();
        }

        return new RestMindmapMetadata(mindmap.getTitle(), mindMapBean.getProperties(), mindmap.getCreator().getFullName(), isLocked, lockFullName);
    }

    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.GET, value = "/", produces = {"application/json"})
    public RestMindmapList retrieveList(@RequestParam(required = false) String q) {
        final Account user = Utils.getUser(true);

        final MindmapFilter filter = MindmapFilter.parse(q);
        List<Mindmap> mindmaps = mindmapService.findMindmapsByUser(user);
        mindmaps = mindmaps
                .stream()
                .filter(m -> filter.accept(m, user)).toList();

        return new RestMindmapList(mindmaps, user);
    }

    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.GET, value = "/{id}/history/", produces = {"application/json"})
    public RestMindmapHistoryList fetchHistory(@PathVariable int id) {
        final List<MindMapHistory> histories = mindmapService.findMindmapHistory(id);
        final RestMindmapHistoryList result = new RestMindmapHistoryList();
        for (MindMapHistory history : histories) {
            result.addHistory(new RestMindmapHistory(history));
        }
        return result;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{id}/document", consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    public void updateDocument(@RequestBody RestMindmap restMindmap, @PathVariable int id, @RequestParam(required = false) boolean minor) throws WiseMappingException, IOException {

        final Mindmap mindmap = findMindmapById(id);
        final Account user = Utils.getUser(true);

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
        
        // Validate HTML content in notes
        htmlContentValidator.validateHtmlContent(mindmap);

        // Update map ...
        saveMindmapDocument(minor, mindmap, user);
    }

    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @RequestMapping(value = "/{id}/history/{hid}", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateRevertMindmap(@PathVariable int id, @PathVariable String hid) throws WiseMappingException, IOException {
        final Mindmap mindmap = findMindmapById(id);
        final Account user = Utils.getUser(true);

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

    @PreAuthorize("permitAll()")
    @RequestMapping(method = RequestMethod.GET, value = {"/{id}/document/xml", "/{id}/document/xml-pub"}, consumes = {"text/plain"}, produces = {"application/xml; charset=UTF-8"})
    @ResponseBody
    public byte[] retrieveDocument(@PathVariable int id, @NotNull HttpServletResponse response) throws WiseMappingException, IOException {
        final Account user = Utils.getUser(false);
        final Mindmap mindmap = findMindmapById(id);

        // If it's not authenticated and map is spam-detected, block access
        if (user == null && mindmap.isSpamDetected()) {
            throw new SpamContentException();
        }

        String xmlStr = mindmap.getXmlStr();
        return xmlStr.getBytes(StandardCharsets.UTF_8);
    }

    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.PUT, value = {"/{id}/document/xml"}, consumes = {"text/plain"})
    @ResponseBody
    public void updateDocument(@PathVariable int id, @RequestBody String xmlDoc) throws WiseMappingException {
        final Mindmap mindmap = findMindmapById(id);
        final Account user = Utils.getUser(true);
        mindmap.setXmlStr(xmlDoc);
        
        // Validate HTML content in notes
        htmlContentValidator.validateHtmlContent(mindmap);

        saveMindmapDocument(false, mindmap, user);
    }


    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.GET, value = {"/{id}/{hid}/document/xml"}, consumes = {"text/plain"}, produces = {"application/xml; charset=UTF-8"})
    @ResponseBody
    public byte[] retrieveDocument(@PathVariable int id, @PathVariable int hid, @NotNull HttpServletResponse response) throws WiseMappingException, IOException {
        final MindMapHistory mindmapHistory = mindmapService.findMindmapHistory(id, hid);
        return mindmapHistory.getUnzipXml();
    }

    /**
     * The intention of this method is the update of several properties at once ...
     */
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.PUT, value = "/{id}", consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateProperties(@RequestBody RestMindmap restMindmap, @PathVariable int id, @RequestParam(required = false) boolean minor) throws IOException, WiseMappingException {

        final Mindmap mindmap = findMindmapById(id);
        final Account user = Utils.getUser(true);

        final String xml = restMindmap.getXml();
        if (xml != null && !xml.isEmpty()) {
            mindmap.setXmlStr(xml);
        }

        // Update title  ...
        final String title = restMindmap.getTitle();
        if (title != null && !title.equals(mindmap.getTitle())) {
            if (mindmapService.getMindmapByTitle(title, user) != null) {
                throw buildValidationException("You already have a map with this title");
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
        // Use manager directly to bypass service security annotations
        final Mindmap result = mindmapService.findMindmapById(id);
        if (result == null) {
            throw new MapCouldNotFoundException("Map could not be found. Id:" + id);
        }
        return result;
    }

    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.PUT, value = "/{id}/title", consumes = {"text/plain"}, produces = {"application/json"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateTitle(@RequestBody String title, @PathVariable int id) throws WiseMappingException {

        final Mindmap mindMap = findMindmapById(id);
        final Account user = Utils.getUser(true);

        // Is there a map with the same name ?
        if (mindmapService.getMindmapByTitle(title, user) != null) {
            throw buildValidationException("You already have a mindmap with this title");
        }

        // Update map ...
        final Mindmap mindmap = findMindmapById(id);
        mindmap.setTitle(title);
        mindmapService.updateMindmap(mindMap, false);
    }

    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.POST, value = "/{id}/collabs/", consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateCollabs(@PathVariable int id, @NotNull @RequestBody RestCollaborationList restCollabs) throws CollaborationException, MapCouldNotFoundException, AccessDeniedSecurityException, InvalidEmailException, TooManyInactiveAccountsExceptions {
        final Mindmap mindMap = findMindmapById(id);

        // Only owner can change collaborators...
        final Account user = Utils.getUser();
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
                
                // Track mindmap sharing
                metricsService.trackMindmapShared(mindMap, restCollab.getEmail(), role.name(), user);
            }
        }

        // Remove all collaborations that no applies anymore ..
        for (final Collaboration collaboration : collabsToRemove) {
            mindmapService.removeCollaboration(mindMap, collaboration);
        }
    }

    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.PUT, value = "/{id}/collabs/", consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void addCollab(@PathVariable int id, @NotNull @RequestBody RestCollaborationList restCollabs) throws CollaborationException, MapCouldNotFoundException, AccessDeniedSecurityException, InvalidEmailException, TooManyInactiveAccountsExceptions, OwnerCannotChangeException {
        final Mindmap mindMap = findMindmapById(id);

        // Only owner can change collaborators...
        final Account user = Utils.getUser();
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
                
                // Track mindmap sharing (role change is also a sharing event)
                metricsService.trackMindmapShared(mindMap, collabEmail, newRole.name(), user);
            }
        }
    }


    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.GET, value = "/{id}/collabs", produces = {"application/json"})
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

    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.PUT, value = "/{id}/description", consumes = {"text/plain"}, produces = {"application/json"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateDescription(@RequestBody String description, @PathVariable int id) throws WiseMappingException {
        final Mindmap mindmap = findMindmapById(id);
        mindmap.setDescription(description);
        mindmapService.updateMindmap(mindmap, false);
    }

    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.PUT, value = "/{id}/publish", consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updatePublishState(@RequestBody Map<String, Boolean> request, @PathVariable int id) throws WiseMappingException {
        // application/json format: {"isPublic": true} or {"isPublic": false}
        Boolean isPublic = request.get("isPublic");
        if (isPublic == null) {
            throw new IllegalArgumentException("Map properties can not be null");
        }
        updatePublishStateInternal(isPublic, id);
    }

    private void updatePublishStateInternal(Boolean isPublic, int id) throws WiseMappingException {
        final Mindmap mindMap = findMindmapById(id);

        final Account user = Utils.getUser();
        if (!mindMap.hasPermissions(user, CollaborationRole.OWNER)) {
            throw new IllegalArgumentException("No enough to execute this operation");
        }

        // Check for spam content when trying to make public
        if (isPublic) {
            SpamDetectionResult spamResult = spamDetectionService.detectSpam(mindMap, "publish");
            if (spamResult.isSpam()) {
                // Mark the map as spam detected and throw exception
                mindMap.setSpamDetected(true);
                mindMap.setSpamDescription(spamResult.getDetails());
                mindMap.setSpamTypeCode(spamResult.getStrategyType());
                mindMap.setPublic(false);
                mindmapService.updateMindmap(mindMap, false);

                // Track spam prevention using MetricsService
                metricsService.trackSpamPrevention(mindMap, "publish");

                throw new SpamContentException();
            } else {
                // Making public and no spam detected - clear spam flag and make public
                mindMap.setSpamDetected(false);
                mindMap.setSpamDescription(null);
                mindMap.setPublic(true);
                
                // Track mindmap made public
                metricsService.trackMindmapMadePublic(mindMap, user);
            }
        } else {
            // Making private - only update public flag, preserve existing spam flag
            mindMap.setPublic(false);
        }

        // Update map status ...
        mindmapService.updateMindmap(mindMap, false);

    }

    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteMapById(@PathVariable int id) throws WiseMappingException {
        final Account user = Utils.getUser();
        final Mindmap mindmap = findMindmapById(id);
        mindmapService.removeMindmap(mindmap, user);
    }

    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}/collabs")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteCollabByEmail(@PathVariable int id, @RequestParam(required = false) String email) throws WiseMappingException {
        logger.debug("Deleting permission for email:" + email);

        // Is a valid email address ?
        final EmailValidator emailValidator = EmailValidator.getInstance();
        if (!emailValidator.isValid(email)) {
            throw new InvalidEmailException(email);
        }

        final Mindmap mindmap = findMindmapById(id);
        final Account user = Utils.getUser();

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

    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.PUT, value = "/{id}/starred", consumes = {"text/plain"}, produces = {"application/json"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateStarredState(@RequestBody String value, @PathVariable int id) throws WiseMappingException {

        logger.debug("Update starred:" + value);
        final Mindmap mindmap = findMindmapById(id);
        final Account user = Utils.getUser();

        // Update map status ...
        final boolean starred = Boolean.parseBoolean(value);
        final Optional<Collaboration> collaboration = mindmap.findCollaboration(user);
        if (collaboration.isEmpty()) {
            throw new WiseMappingException("No enough permissions.");
        }
        collaboration.get().getCollaborationProperties().setStarred(starred);
        mindmapService.updateCollaboration(user, collaboration.get());
    }

    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.GET, value = "/{id}/starred", produces = {"text/plain"})
    @ResponseBody
    public String fetchStarred(@PathVariable int id) throws WiseMappingException {
        final Mindmap mindmap = findMindmapById(id);
        final Account user = Utils.getUser();

        final Optional<Collaboration> collaboration = mindmap.findCollaboration(user);
        if (collaboration.isEmpty()) {
            throw new WiseMappingException("No enough permissions.");
        }
        boolean result = collaboration.get().getCollaborationProperties().getStarred();
        return Boolean.toString(result);
    }
    
    /**
     * Validates note content and provides character count information.
     * This endpoint helps users understand character limits for notes.
     */
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.POST, value = "/validate-note", consumes = {"text/plain"}, produces = {"application/json"})
    @ResponseBody
    public NoteValidationResponse validateNoteContent(@RequestBody String noteContent) {
        try {
            SpamContentExtractor.NoteCharacterCount characterCount = spamContentExtractor.getNoteCharacterCount(noteContent);
            
            return new NoteValidationResponse(
                characterCount.getRawLength(),
                characterCount.getTextLength(),
                characterCount.isHtml(),
                characterCount.getRemainingChars(),
                characterCount.isOverLimit(),
                characterCount.getUsagePercentage()
            );
        } catch (Exception e) {
            logger.warn("Error validating note content: {}", e.getMessage());
            return new NoteValidationResponse(0, 0, false, maxNoteLength, false, 0.0);
        }
    }

    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.DELETE, value = "/batch")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void batchDelete(@RequestParam() String ids) throws WiseMappingException {
        final Account user = Utils.getUser();
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

    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.POST, value = "", consumes = {"application/xml", "application/json"})
    @ResponseStatus(value = HttpStatus.CREATED)
    public void createMap(@RequestBody(required = false) String mapXml, @NotNull HttpServletResponse response, @RequestParam(required = false) String title, @RequestParam(required = false) String description) throws WiseMappingException {

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
        
        // Validate HTML content in notes
        htmlContentValidator.validateHtmlContent(mindmap);

        // Check for spam content during creation
        if (mindmap.isPublic()) {
            SpamDetectionResult spamResult = spamDetectionService.detectSpam(mindmap, "creation");
            if (spamResult.isSpam()) {
                mindmap.setSpamDetected(true);
                mindmap.setSpamDescription(spamResult.getDetails());
                // Get strategy name as enum
                mindmap.setSpamTypeCode(spamResult.getStrategyType());

                // Track spam detection during creation
                metricsService.trackSpamDetection(mindmap, spamResult, "creation");
            } else {
                mindmap.setSpamDetected(false);
                mindmap.setSpamDescription(null);
                mindmap.setSpamTypeCode(null);
            }
        }

        // Add new mindmap ...
        final Account user = Utils.getUser(true);
        mindmapService.addMindmap(mindmap, user);

        // Track mindmap creation
        metricsService.trackMindmapCreation(mindmap, user, "new");

        // Return the new created map ...
        response.setHeader("Location", "/api/restful/maps/" + mindmap.getId());
        response.setHeader("ResourceId", Integer.toString(mindmap.getId()));
    }

    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.POST, value = "/{id}", consumes = {"application/json"}, produces = {"application/json", "text/plain"})
    @ResponseStatus(value = HttpStatus.CREATED)
    public void createDuplicate(@RequestBody RestMindmapInfo restMindmap, @PathVariable int id, @NotNull HttpServletResponse response) throws WiseMappingException {
        // Validate ...
        final BindingResult result = new BeanPropertyBindingResult(restMindmap, "");
        new MapInfoValidator(mindmapService).validate(restMindmap.getDelegated(), result);
        if (result.hasErrors()) {
            throw new ValidationException(result);
        }

        // Some basic validations ...
        final Account user = Utils.getUser();

        // Create a shallowCopy of the map ...
        final Mindmap mindMap = findMindmapById(id);
        final Mindmap clonedMap = mindMap.shallowClone();
        clonedMap.setTitle(restMindmap.getTitle());
        clonedMap.setDescription(restMindmap.getDescription());

        // Check for spam content in the duplicated map
        SpamDetectionResult spamResult = spamDetectionService.detectSpam(clonedMap, "duplicate");
        if (spamResult.isSpam()) {
            clonedMap.setSpamDetected(true);
            clonedMap.setSpamDescription(spamResult.getDetails());
            // Get strategy name as enum
            clonedMap.setSpamTypeCode(spamResult.getStrategyType());
        } else {
            clonedMap.setSpamDetected(false);
            clonedMap.setSpamDescription(null);
            clonedMap.setSpamTypeCode(null);
        }

        // Add new mindmap ...
        mindmapService.addMindmap(clonedMap, user);

        // Track mindmap duplication
        metricsService.trackMindmapCreation(clonedMap, user, "duplicate");

        // Return the new created map ...
        response.setHeader("Location", "/api/restful/maps/" + clonedMap.getId());
        response.setHeader("ResourceId", Integer.toString(clonedMap.getId()));
    }


    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}/labels/{lid}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void removeLabelFromMap(@PathVariable int id, @PathVariable int lid) throws WiseMappingException {
        final Account user = Utils.getUser();
        final Mindmap mindmap = findMindmapById(id);
        final MindmapLabel label = labelService.findLabelById(lid, user);

        if (label == null) {
            throw new LabelCouldNotFoundException("Label could not be found. Id: " + lid);
        }

        mindmap.removeLabel(label);
        mindmapService.updateMindmap(mindmap, false);
    }

    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.POST, value = "/{id}/labels", consumes = {"application/json"})
    @ResponseStatus(value = HttpStatus.OK)
    public void updateLabel(@PathVariable int id, @RequestBody int lid) throws WiseMappingException {
        final Account user = Utils.getUser();
        final MindmapLabel label = labelService.findLabelById(lid, user);
        if (label == null) {
            throw new LabelCouldNotFoundException("Label could not be found. Id: " + lid);
        }

        final Mindmap mindmap = findMindmapById(id);
        mindmap.addLabel(label);
        mindmapService.updateMindmap(mindmap, false);
    }

    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @RequestMapping(method = RequestMethod.PUT, value = "/{id}/lock", consumes = {"text/plain"}, produces = {"application/json"})
    public ResponseEntity<RestLockInfo> lockMindmap(@RequestBody String value, @PathVariable int id) throws WiseMappingException {
        final Account user = Utils.getUser();
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


    private void saveMindmapDocument(boolean minor, @NotNull final Mindmap mindMap, @NotNull final Account user) throws WiseMappingException {
        final Calendar now = Calendar.getInstance();
        mindMap.setLastModificationTime(now);
        mindMap.setLastEditor(user);

        // Check for spam content during updates
        if (mindMap.isPublic()) {
            SpamDetectionResult spamResult = spamDetectionService.detectSpam(mindMap, "update");
            if (spamResult.isSpam()) {
                // If the map is currently public but now detected as spam, make it private
                mindMap.setPublic(false);
                mindMap.setSpamDetected(true);
                mindMap.setSpamDescription(spamResult.getDetails());
                // Get strategy name as enum
                mindMap.setSpamTypeCode(spamResult.getStrategyType());
            }
        }
        mindmapService.updateMindmap(mindMap, !minor);
    }

    private ValidationException buildValidationException(@NotNull String message) throws WiseMappingException {
        final BindingResult result = new BeanPropertyBindingResult(new RestMindmap(), "");
        result.rejectValue("title", "error.not-specified", null, message);
        return new ValidationException(result);
    }

    private void verifyActiveCollabs(@NotNull RestCollaborationList restCollabs, Account user) throws TooManyInactiveAccountsExceptions {
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
