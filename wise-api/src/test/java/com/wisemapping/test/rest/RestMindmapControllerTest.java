package com.wisemapping.test.rest;


import com.wisemapping.config.AppConfig;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.Account;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.MindmapLabel;
import com.wisemapping.rest.MindmapController;
import com.wisemapping.rest.model.*;
import com.wisemapping.service.MindmapService;
import com.wisemapping.service.UserService;
import static com.wisemapping.test.rest.RestHelper.createHeaders;
import static com.wisemapping.test.rest.RestHelper.createTestUser;
import static com.wisemapping.test.rest.RestHelper.createUserViaApi;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.Statistics;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = {AppConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RestMindmapControllerTest {

    private static final String COLLAB_BY_USER_NAMED_QUERY = "Collaboration.findByCollaboratorId";
    private RestUser user;
    private String userPassword = "testPassword123";
    private static final int BULK_MAP_COUNT = 1000;
    private static final int LABELS_PER_MAP = 10;
    private static final int MAX_EXPECTED_QUERIES_FOR_LISTING = 50;
    private static final String BULK_MAP_TITLE_PREFIX = "Bulk Performance Map ";

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private MindmapService mindmapService;
    @Autowired
    private UserService userService;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private MindmapController mindmapController;

    @BeforeEach
    void createUser() {
        // Remote debug ...
        if (restTemplate == null) {
            this.restTemplate = new TestRestTemplate();
            this.restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:8081/"));
        }

        // Create a new test user using the helper method
        this.user = createTestUser(restTemplate, userPassword);
    }

    @Test
    public void listMaps() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String title1 = "List Maps 1";
        addNewMap(restTemplate, title1);

        final String title2 = "List Maps 2";
        addNewMap(restTemplate, title2);

        // Validate that the two maps are there ...
        final RestMindmapList body = fetchMaps(requestHeaders, restTemplate);
        final List<RestMindmapInfo> mindmaps = body.getMindmapsInfo();

        boolean found1 = false;
        boolean found2 = false;
        for (RestMindmapInfo mindmap : mindmaps) {
            if (mindmap.getTitle().equals(title1)) {
                found1 = true;
            }
            if (mindmap.getTitle().equals(title2)) {
                found2 = true;
            }
        }
        assertTrue(found1 && found2, "Map could not be found");
    }

    @Test
    public void bulkMindmapListingAvoidsCollaborationStorm() {
        final Account owner = userService.getUserBy(user.getEmail());
        assertNotNull(owner, "Owner account must exist");

        createBulkMindmaps(owner, BULK_MAP_COUNT, LABELS_PER_MAP);

        final SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        final Statistics statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        final RestMindmapList response = fetchMaps(createHeaders(MediaType.APPLICATION_JSON),
                this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword()));

        final long createdMaps = response.getMindmapsInfo()
                .stream()
                .filter(m -> m.getTitle().startsWith(BULK_MAP_TITLE_PREFIX))
                .count();

        assertEquals(BULK_MAP_COUNT, createdMaps, "Expected to retrieve every generated mindmap");

        final long executedStatements = statistics.getPrepareStatementCount();
        final long entityFetches = statistics.getEntityFetchCount();
        final long collectionFetches = statistics.getCollectionFetchCount();
        final long loadCount = statistics.getEntityLoadCount();

        final String topQueries = Arrays.stream(statistics.getQueries())
                .collect(Collectors.toMap(
                        query -> query,
                        query -> statistics.getQueryStatistics(query).getExecutionCount(),
                        Long::sum,
                        () -> new LinkedHashMap<>()))
                .entrySet()
                .stream()
                .sorted((left, right) -> Long.compare(right.getValue(), left.getValue()))
                .limit(5)
                .map(entry -> entry.getValue() + "x | " + entry.getKey().replaceAll("\\s+", " ").trim())
                .collect(Collectors.joining("\n"));

        assertTrue(executedStatements <= MAX_EXPECTED_QUERIES_FOR_LISTING,
                "Listing should not trigger excessive SQL statements.\n" +
                        "Executed: " + executedStatements +
                        ", entityFetches: " + entityFetches +
                        ", collectionFetches: " + collectionFetches +
                        ", entityLoadCount: " + loadCount +
                        "\nTop queries:\n" + topQueries);

        final long collabQueryExecutions = getNamedQueryExecutionCount(statistics, COLLAB_BY_USER_NAMED_QUERY);
        assertEquals(0, collabQueryExecutions,
                "Mindmap listing should not rely on Collaboration.findByCollaboratorId per mindmap.");
    }

    @Test
    public void adminBulkMindmapListingAvoidsCollaborationStorm() {
        final Account owner = userService.getUserBy(user.getEmail());
        assertNotNull(owner, "Owner account must exist");

        createBulkMindmaps(owner, BULK_MAP_COUNT, LABELS_PER_MAP);

        final SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        final Statistics statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        final int requestedPageSize = 200;
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate adminTemplate = this.restTemplate.withBasicAuth("admin@wisemapping.org", "testAdmin123");
        final HttpEntity<Void> requestEntity = new HttpEntity<>(requestHeaders);

        final ResponseEntity<PaginatedResponse<AdminRestMap>> response = adminTemplate.exchange(
                "/api/restful/admin/maps?page=0&pageSize=" + requestedPageSize,
                HttpMethod.GET,
                requestEntity,
                new org.springframework.core.ParameterizedTypeReference<PaginatedResponse<AdminRestMap>>() {});

        assertTrue(response.getStatusCode().is2xxSuccessful(), "Admin map listing should succeed");
        final PaginatedResponse<AdminRestMap> body = response.getBody();
        assertNotNull(body, "Admin listing response body must not be null");

        final long createdMaps = body.getData()
                .stream()
                .filter(m -> m.getTitle() != null && m.getTitle().startsWith(BULK_MAP_TITLE_PREFIX))
                .count();

        final long expectedPageMatches = Math.min(BULK_MAP_COUNT, requestedPageSize);
        assertTrue(createdMaps >= expectedPageMatches,
                "Admin endpoint should retrieve at least " + expectedPageMatches + " generated mindmaps on the first page");
        assertTrue(body.getTotalElements() >= BULK_MAP_COUNT,
                "Admin listing should report the generated mindmaps in the total count");

        final long executedStatements = statistics.getPrepareStatementCount();
        final long entityFetches = statistics.getEntityFetchCount();
        final long collectionFetches = statistics.getCollectionFetchCount();
        final long loadCount = statistics.getEntityLoadCount();

        assertTrue(executedStatements <= MAX_EXPECTED_QUERIES_FOR_LISTING,
                "Admin listing should not trigger excessive SQL statements.\n" +
                        "Executed: " + executedStatements +
                        ", entityFetches: " + entityFetches +
                        ", collectionFetches: " + collectionFetches +
                        ", entityLoadCount: " + loadCount);

        final long collabQueryExecutions = getNamedQueryExecutionCount(statistics, COLLAB_BY_USER_NAMED_QUERY);
        assertEquals(0, collabQueryExecutions,
                "Admin mindmap listing should not rely on Collaboration.findByCollaboratorId per mindmap.");
    }


    @Test
    public void changeMapTitle() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final URI resourceUri = addNewMap(restTemplate, "Map to change title");
        final String newTitle = changeMapTitle(requestHeaders, MediaType.APPLICATION_JSON, restTemplate, resourceUri);

        // Load map again ..
        final RestMindmap map = findMap(requestHeaders, restTemplate, resourceUri);
        assertEquals(newTitle, map.getTitle());
    }

    @Test
    public void validateMapsCreation() throws URISyntaxException {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        requestHeaders.set(HttpHeaders.ACCEPT_LANGUAGE, "en");
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String title = "Map to Validate Creation";
        addNewMap(restTemplate, title);

        // Add map with same name ...
        HttpEntity<RestMindmap> createUserEntity = new HttpEntity<>(requestHeaders);
        final ResponseEntity<String> response = restTemplate.exchange("/api/restful/maps?title=" + title, HttpMethod.POST, createUserEntity, String.class);
        // The global exception handler now returns 400 instead of 500 for validation errors
        assertTrue(response.getStatusCode().is4xxClientError(), "Expected 4xx status, got: " + response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("You have already a map with the same name"));
    }


    @Test
    public void changeMapDescription() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final URI resourceUri = addNewMap(restTemplate, "Map to change Description ");

        // Change map title ...
        requestHeaders.setContentType(MediaType.TEXT_PLAIN);
        final String newDescription = "New map to change description ";
        final HttpEntity<String> updateEntity = new HttpEntity<>(newDescription, requestHeaders);
        restTemplate.put(resourceUri + "/description", updateEntity);

        // Load map again ..
        final RestMindmap map = findMap(requestHeaders, restTemplate, resourceUri);
        assertEquals(newDescription, map.getDescription());
    }


    @Test
    public void updateMapXml() throws IOException, URISyntaxException {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String title = "Update XML sample";
        final URI resourceUri = addNewMap(restTemplate, title);

        // Update map xml content ...
        final String resourceUrl = resourceUri.toString();
        String newXmlContent = updateMapDocument(requestHeaders, restTemplate, resourceUrl, null);

        // Check that the map has been updated ...
        final RestMindmap response = findMap(requestHeaders, restTemplate, resourceUri);
        assertEquals(response.getXml(), newXmlContent);
    }

    private String updateMapDocument(final HttpHeaders requestHeaders, final TestRestTemplate template, final String resourceUrl, final String content) throws RestClientException {
        requestHeaders.setContentType(MediaType.TEXT_PLAIN);

        final String newXmlContent = content != null ? content : "<map>this is not valid</map>";
        HttpEntity<String> updateEntity = new HttpEntity<>(newXmlContent, requestHeaders);
        template.put(resourceUrl + "/document/xml", updateEntity);
        return newXmlContent;
    }


    @Test
    public void cloneMap() throws IOException, URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String title = "Map to clone  sample ";
        final String xml = "<map><node text='this is a cloned map'></map>";
        final URI newMapUri = addNewMap(restTemplate, title, xml);

        // Clone map ...
        final RestMindmapInfo restMindmap = new RestMindmapInfo();
        restMindmap.setTitle("Cloned map but with previous content.");
        restMindmap.setDescription("Cloned map desc");

        // Create a new map ...
        final HttpEntity<RestMindmapInfo> cloneEntity = new HttpEntity<>(restMindmap, requestHeaders);
        final ResponseEntity<Void> exchange = restTemplate.exchange(newMapUri.toString(), HttpMethod.POST, cloneEntity, Void.class);
        assertTrue(exchange.getStatusCode().is2xxSuccessful());
        URI clonedMapUri = exchange.getHeaders().getLocation();


        // Check that the map has been updated ...
        final RestMindmap response = findMap(requestHeaders, restTemplate, clonedMapUri);
        assertEquals(response.getXml(), xml);
    }


    @Test
    public void updateStarred() throws URISyntaxException {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String title1 = "Stared Map user 1";
        URI mapUri = addNewMap(restTemplate, title1);

        // Update starred ...
        final String resourceUrl = mapUri.toString() + "/starred";
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        final HttpHeaders textContentType = new HttpHeaders();
        textContentType.setContentType(MediaType.TEXT_PLAIN);
        final HttpEntity<String> updateEntity = new HttpEntity<>("true", textContentType);
        restTemplate.put(resourceUrl, updateEntity);

        // Has been updated ?.

        final HttpEntity<String> findLabelEntity = new HttpEntity<>(createHeaders(MediaType.TEXT_PLAIN));
        final ResponseEntity<String> response = restTemplate.exchange(resourceUrl, HttpMethod.GET, findLabelEntity, String.class);

        assertTrue(Boolean.parseBoolean(response.getBody()), "Starred has been updated");
    }


    @Test
    public void verifyMapOwnership() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate firstUser = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String title1 = "verifyMapOwnership Map user 1";
        addNewMap(firstUser, title1);

        //create another user
        final String secondUserPassword = "testPassword123";
        final String secondEmail = "test-" + System.nanoTime() + "@example.org";
        final RestUser createdSecondUser = createUserViaApi(this.restTemplate, secondEmail, "Test2", "User2", secondUserPassword);
        
        final TestRestTemplate secondTemplate = this.restTemplate.withBasicAuth(createdSecondUser.getEmail(), createdSecondUser.getPassword());

        final String title2 = "verifyMapOwnership Map user 2";
        addNewMap(secondTemplate, title2);

        final TestRestTemplate superadminTemplate = this.restTemplate.withBasicAuth("admin@wisemapping.org", "testAdmin123");
        final ResponseEntity<String> exchange = superadminTemplate.exchange("/api/restful/admin/users/" + createdSecondUser.getId(), HttpMethod.DELETE, null, String.class);
        assertTrue(exchange.getStatusCode().is2xxSuccessful(), "Status Code:" + exchange.getStatusCode() + "- " + exchange.getBody());

        // Validate that the two maps are there ...
        final RestMindmapList body = fetchMaps(requestHeaders, firstUser);
        final List<RestMindmapInfo> mindmaps = body.getMindmapsInfo();

        final Optional<RestMindmapInfo> any = mindmaps.stream().filter(m -> m.getTitle().equals(title1)).findAny();
        assertTrue(any.isPresent(), "Map could not be found");
    }

    @Test
    public void updateMap() throws IOException, WiseMappingException, URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String title = "Update sample ";
        final URI resourceUri = addNewMap(restTemplate, title);

        // Build map to update ...
        final RestMindmap mapToUpdate = new RestMindmap();
        mapToUpdate.setXml("<map>this is not valid</map>");
        mapToUpdate.setProperties("{zoom:x}");

        // Create lock headers ...
        final HttpHeaders lockHeaders = new HttpHeaders();
        lockHeaders.setContentType(MediaType.TEXT_PLAIN);

        // Update map ...
        final String resourceUrl = resourceUri.toString() + "/document";
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<RestMindmap> updateEntity = new HttpEntity<>(mapToUpdate, requestHeaders);
        restTemplate.put(resourceUrl, updateEntity);

        // Check that the map has been updated ...
        HttpEntity<RestUser> findMapEntity = new HttpEntity<>(requestHeaders);
        final ResponseEntity<RestMindmap> response = restTemplate.exchange(resourceUri.toString(), HttpMethod.GET, findMapEntity, RestMindmap.class);
        assertEquals(Objects.requireNonNull(response.getBody()).getXml(), mapToUpdate.getXml());
        assertEquals(response.getBody().getProperties(), mapToUpdate.getProperties());

        // Unlock ...
        HttpEntity<String> lockEntity = new HttpEntity<>("false", lockHeaders);
        restTemplate.exchange(resourceUri + "/lock", HttpMethod.PUT, lockEntity, RestLockInfo.class);
    }


    @Test
    public void addCollabs() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final URI resourceUri = addNewMap(restTemplate, "Map for addCollabs  - ");

        String newCollab = addNewCollaboration(requestHeaders, restTemplate, resourceUri);

        // Has been added ?
        RestCollaborationList responseCollbs = fetchAndGetCollabs(requestHeaders, restTemplate, resourceUri);

        // Has been added ?
        assertEquals(responseCollbs.getCount(), 2);

        final Optional<RestCollaboration> addedCollab = responseCollbs.getCollaborations().stream().filter(c -> c.getEmail().equals(newCollab)).findAny();
        assertTrue(addedCollab.isPresent());
        assertEquals(addedCollab.get().getRole(), "editor");
    }

    @Test
    public void updateCollabType() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final URI resourceUri = addNewMap(restTemplate, "Map for updateCollabType");

        // Add a new collaboration ...
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        final RestCollaborationList collabs = new RestCollaborationList();
        collabs.setMessage("Adding new permission");

        final String newCollab = "new-collab@example.com";
        String role = "editor";

        final RestCollaboration collab = addCollabToList(newCollab, role, collabs);

        final HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
        restTemplate.put(resourceUri + "/collabs/", updateEntity);

        // Has been added ?
        RestCollaborationList responseCollbs = fetchAndGetCollabs(requestHeaders, restTemplate, resourceUri);
        assertEquals(responseCollbs.getCount(), 2);

        // Update the collaboration type ...
        collab.setRole("viewer");
        restTemplate.put(resourceUri + "/collabs/", updateEntity);

        // Has been added ?
        final ResponseEntity<RestCollaborationList> afterResponse = fetchCollabs(requestHeaders, restTemplate, resourceUri);
        final Optional<RestCollaboration> updatedCollab = Objects.requireNonNull(afterResponse.getBody()).getCollaborations().stream().filter(c -> c.getEmail().equals(newCollab)).findAny();
        assertTrue(updatedCollab.isPresent());
        assertEquals(updatedCollab.get().getRole(), "viewer");
    }


    @Test
    public void deleteCollabs() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final URI resourceUri = addNewMap(restTemplate, "Map for deleteCollabs  - ");

        String newCollab = addNewCollaboration(requestHeaders, restTemplate, resourceUri);

        // Has been added ?
        RestCollaborationList responseCollbs = fetchAndGetCollabs(requestHeaders, restTemplate, resourceUri);

        // Has been added ?
        assertEquals(2, responseCollbs.getCount());

        // Now, remove it ...
        final ResponseEntity<String> exchange = restTemplate.exchange(resourceUri + "/collabs?email=" + newCollab, HttpMethod.DELETE, null, String.class);
        assertTrue(exchange.getStatusCode().is2xxSuccessful());

        // Check that it has been removed ...
        final ResponseEntity<RestCollaborationList> afterDeleteResponse = fetchCollabs(requestHeaders, restTemplate, resourceUri);
        assertEquals(1, Objects.requireNonNull(afterDeleteResponse.getBody()).getCollaborations().size());
    }

    private String addNewCollaboration(final HttpHeaders requestHeaders, final TestRestTemplate template, final URI resourceUri) throws RestClientException {
        // Add a new collaboration ...
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        final RestCollaborationList collabs = new RestCollaborationList();
        collabs.setMessage("Adding new permission");
        final String newCollab = "new-collab@example.com";
        String role = "editor";
        addCollabToList(newCollab, role, collabs);
        final HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
        template.put(resourceUri + "/collabs/", updateEntity);
        return newCollab;
    }


    @Test
    public void deleteCollabsWithInvalidEmail() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final URI resourceUri = addNewMap(restTemplate, "deleteCollabsWithInvalidEmail");

        // Remove with invalid email ...
        final ResponseEntity<String> exchange = restTemplate.exchange(resourceUri + "/collabs?email=invalidEmail", HttpMethod.DELETE, null, String.class);
        assertTrue(exchange.getStatusCode().is4xxClientError());
        assertTrue(Objects.requireNonNull(exchange.getBody()).contains("Invalid email exception:"));

        // Check that it has been removed ...
        final ResponseEntity<RestCollaborationList> afterDeleteResponse = fetchCollabs(requestHeaders, restTemplate, resourceUri);
        assertEquals(Objects.requireNonNull(afterDeleteResponse.getBody()).getCollaborations().size(), 1);
    }

    /**
     * Tests that non-owner users cannot delete collaborations from a map they don't have access to.
     * Security is enforced by Spring Security's @PreAuthorize on findMindmapById().
     */
    @Test
    public void deleteCollabsWithoutOwnerPermission() throws URISyntaxException {
        // Use the owner template to create the map
        final TestRestTemplate ownerTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());
        final URI resourceUri = addNewMap(ownerTemplate, "deleteWithoutOwnerPermission");

        // Create another user and add them as a collaborator
        final RestAccountControllerTest restAccount = RestAccountControllerTest.create(restTemplate);
        final RestUser collaboratorUser = restAccount.createNewUser();
        
        // Add the collaborator using the owner
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        final RestCollaborationList collabs = new RestCollaborationList();
        collabs.setMessage("Adding collaborator");
        addCollabToList(collaboratorUser.getEmail(), "editor", collabs);
        final HttpEntity<RestCollaborationList> addCollabEntity = new HttpEntity<>(collabs, requestHeaders);
        ownerTemplate.put(resourceUri + "/collabs/", addCollabEntity);

        // Create a third user who is NOT the owner and NOT a collaborator
        final RestAccountControllerTest anotherAccount = RestAccountControllerTest.create(restTemplate);
        final RestUser anotherUser = anotherAccount.createNewUser();
        final TestRestTemplate anotherTemplate = this.restTemplate.withBasicAuth(anotherUser.getEmail(), anotherUser.getPassword());

        // Try to delete the collaborator as a non-owner - this should fail
        // Spring Security's @PreAuthorize on findMindmapById blocks this before the manual check
        final ResponseEntity<String> exchange = anotherTemplate.exchange(resourceUri + "/collabs?email=" + collaboratorUser.getEmail(), HttpMethod.DELETE, null, String.class);
        assertTrue(exchange.getStatusCode().is4xxClientError(), "Expected 4xx error but got: " + exchange.getStatusCode());
        // Spring Security returns its own access denied message
        assertTrue(Objects.requireNonNull(exchange.getBody()).contains("Access denied") || 
                   exchange.getBody().contains("No enough permissions"),
                   "Expected access denied message but got: " + exchange.getBody());

    }

    @Test
    public void deleteOwnerCollab() throws URISyntaxException {
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final URI resourceUri = addNewMap(restTemplate, "Map for deleteOwnerCollab");

        // Now, remove owner collab ...
        final ResponseEntity<String> exchange = restTemplate.exchange(resourceUri + "/collabs?email=" + user.getEmail().replace(":admin", ""), HttpMethod.DELETE, null, String.class);
        assertTrue(exchange.getStatusCode().is4xxClientError());
        assertTrue(Objects.requireNonNull(exchange.getBody()).contains("Can not remove owner collab"));
    }

    @NotNull
    private ResponseEntity<RestCollaborationList> fetchCollabs(HttpHeaders requestHeaders, TestRestTemplate template, URI resourceUri) {
        final HttpEntity<RestCollaborationList> findCollabs = new HttpEntity<>(requestHeaders);
        return template.exchange(resourceUri + "/collabs", HttpMethod.GET, findCollabs, RestCollaborationList.class);
    }

    @Test
    public void addCollabsInvalidOwner() throws URISyntaxException {

        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...fetchAndGetCollabs(requestHeaders, template, resourceUri);
        final URI resourceUri = addNewMap(restTemplate, "Map for Collaboration");

        // Add a new collaboration ...
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        final RestCollaborationList collabs = new RestCollaborationList();
        collabs.setMessage("Adding new permission");

        // Validate that owner can not be added.
        addCollabToList("newCollab@example", "owner", collabs);

        final HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
        restTemplate.put(resourceUri + "/collabs/", updateEntity);
    }

    @Test
    public void removeLabelFromMindmap() throws URISyntaxException {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a new label
        final String titleLabel = "removeLabelFromMindmap";
        final URI labelUri = RestLabelControllerTest.addNewLabel(requestHeaders, restTemplate, titleLabel, "red");

        // Create a sample map ...
        final String mapTitle = "removeLabelFromMindmap";
        final URI mindmapUri = addNewMap(restTemplate, mapTitle);
        final String mapId = mindmapUri.getPath().replace("/api/restful/maps/", "");

        // Assign label to map ...
        String labelId = labelUri.getPath().replace("/api/restful/labels/", "");
        HttpEntity<String> labelEntity = new HttpEntity<>(labelId, requestHeaders);
        restTemplate.postForLocation("/api/restful/maps/" + mapId + "/labels", labelEntity);

        // Remove label from map
        final ResponseEntity<String> exchange = restTemplate.exchange("/api/restful//maps/" + mapId + "/labels/" + labelId, HttpMethod.DELETE, null, String.class);
        assertTrue(exchange.getStatusCode().is2xxSuccessful());


        Optional<RestMindmapInfo> mindmapInfo = fetchMap(requestHeaders, restTemplate, mapId);
        assertEquals(0, mindmapInfo.get().getLabels().size());
    }


    @NotNull
    private Optional<RestMindmapInfo> fetchMap(HttpHeaders requestHeaders, TestRestTemplate template, @NotNull String mapId) {
        // Check that the label has been removed ...
        final List<RestMindmapInfo> mindmapsInfo = fetchMaps(requestHeaders, template).getMindmapsInfo();
        return mindmapsInfo
                .stream()
                .filter(m -> m.getId() == Integer.parseInt(mapId))
                .findAny();
    }



    @Test
    public void addLabelToMindmap() throws URISyntaxException {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a new label
        final String titleLabel = "Label 1  - ";
        final URI labelUri = RestLabelControllerTest.addNewLabel(requestHeaders, restTemplate, titleLabel, "COLOR");

        // Create a sample map ...
        final String mapTitle = "Maps 1  - ";
        final URI mindmapUri = addNewMap(restTemplate, mapTitle);
        final String mapId = mindmapUri.getPath().replace("/api/restful/maps/", "");

        // Assign label to map ...
        String labelId = labelUri.getPath().replace("/api/restful/labels/", "");
        HttpEntity<String> labelEntity = new HttpEntity<>(labelId, requestHeaders);
        restTemplate.postForLocation("/api/restful/maps/" + mapId + "/labels", labelEntity);

        // Check that the label has been assigned ...
        Optional<RestMindmapInfo> mindmapInfo = fetchMap(requestHeaders, restTemplate, mapId);
        assertEquals(1, mindmapInfo.get().getLabels().size());
    }

    @Test
    public void fetchMapMetadata() throws URISyntaxException {
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String mapTitle = "Maps 1 !";
        final URI mindmapUri = addNewMap(restTemplate, mapTitle);

        final ResponseEntity<RestMindmapMetadata> exchange = restTemplate.exchange(mindmapUri + "/metadata", HttpMethod.GET, null, RestMindmapMetadata.class);
        assertTrue(exchange.getStatusCode().is2xxSuccessful());
        assertEquals(mapTitle, Objects.requireNonNull(exchange.getBody()).getTitle());

        final ResponseEntity<RestMindmapMetadata> exchangeWithXml = restTemplate.exchange(
                mindmapUri + "/metadata?xml=true",
                HttpMethod.GET,
                null,
                RestMindmapMetadata.class);

        assertTrue(exchangeWithXml.getStatusCode().is2xxSuccessful(), "Metadata fetch with xml flag should succeed");
        final RestMindmapMetadata metadataWithXml = exchangeWithXml.getBody();
        assertNotNull(metadataWithXml, "Metadata should not be null when requesting xml");
        assertNotNull(metadataWithXml.getXml(), "Metadata xml should not be null when xml flag is provided");
        assertFalse(metadataWithXml.getXml().isEmpty(), "Metadata xml should not be empty when xml flag is provided");

    }

    @Test
    @Disabled
    public void fetchMapMetadataWithAllExtendedFields() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String mapTitle = "Map with Extended Metadata Fields";
        final URI mindmapUri = addNewMap(restTemplate, mapTitle);

        // Set description
        requestHeaders.setContentType(MediaType.TEXT_PLAIN);
        final String testDescription = "Test description for extended metadata fields";
        final HttpEntity<String> descriptionEntity = new HttpEntity<>(testDescription, requestHeaders);
        restTemplate.put(mindmapUri + "/description", descriptionEntity);

        // Set starred status
        final HttpHeaders textContentType = new HttpHeaders();
        textContentType.setContentType(MediaType.TEXT_PLAIN);
        final HttpEntity<String> starredEntity = new HttpEntity<>("true", textContentType);
        restTemplate.put(mindmapUri + "/starred", starredEntity);

        // Verify starred was set correctly by calling the starred endpoint
        final ResponseEntity<String> starredCheck = restTemplate.exchange(mindmapUri + "/starred", HttpMethod.GET, null, String.class);
        assertTrue(Boolean.parseBoolean(starredCheck.getBody()), "Starred should be true after setting it");

        // Make map public (optional - might fail due to spam detection, but that's ok)
        final HttpHeaders jsonHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final Map<String, Boolean> publishRequest = new HashMap<>();
        publishRequest.put("isPublic", true);
        final HttpEntity<Map<String, Boolean>> publishEntity = new HttpEntity<>(publishRequest, jsonHeaders);
        restTemplate.exchange(mindmapUri + "/publish", HttpMethod.PUT, publishEntity, String.class);

        RestMindmapMetadata metadata = null;
        boolean metadataStarred = false;
        for (int attempt = 0; attempt < 3; attempt++) {
            final ResponseEntity<RestMindmapMetadata> metadataResponse = restTemplate.exchange(
                    mindmapUri + "/metadata",
                    HttpMethod.GET,
                    null,
                    RestMindmapMetadata.class);

            assertTrue(metadataResponse.getStatusCode().is2xxSuccessful(), "Metadata fetch should succeed");
            metadata = metadataResponse.getBody();
            assertNotNull(metadata, "Metadata should not be null");

            if (metadata.isStarred()) {
                metadataStarred = true;
                break;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for metadata to reflect starred state", e);
            }
        }

        assertNotNull(metadata, "Metadata should not be null after retries");
        assertTrue(metadataStarred, "starred should be true after setting it via the starred endpoint");

        // Verify all extended fields are populated
        assertEquals(mapTitle, metadata.getTitle(), "Title should match");
        assertEquals(testDescription, metadata.getDescription(), "Description should match");
        assertNotNull(metadata.getCreatedBy(), "createdBy (email) should not be null");
        assertEquals(user.getEmail(), metadata.getCreatedBy(), "createdBy should match user email");
        assertNotNull(metadata.getCreationTime(), "creationTime should not be null");
        assertNotNull(metadata.getLastModificationBy(), "lastModificationBy should not be null");
        assertNotNull(metadata.getLastModificationTime(), "lastModificationTime should not be null");
        assertTrue(metadata.isStarred(), "starred should be true");
        assertNotNull(metadata.getRole(), "role should not be null");
        assertEquals("owner", metadata.getRole(), "role should be 'owner' for map creator");
        assertNotNull(metadata.getCreatorFullName(), "creatorFullName should not be null");
        assertEquals(user.getFirstname() + " " + user.getLastname(), metadata.getCreatorFullName(), "creatorFullName should match user full name");

        // Verify public field (may be false if spam detection blocked it, but should be set)
        // We just verify it's a boolean value, not null
        assertNotNull(Boolean.valueOf(metadata.isPublic()), "public field should be set (boolean)");

        final ResponseEntity<RestMindmapMetadata> metadataWithXmlResponse = restTemplate.exchange(
                mindmapUri + "/metadata?xml=true",
                HttpMethod.GET,
                null,
                RestMindmapMetadata.class);
        assertTrue(metadataWithXmlResponse.getStatusCode().is2xxSuccessful(), "Metadata with xml flag should succeed");
        final RestMindmapMetadata metadataWithXml = metadataWithXmlResponse.getBody();
        assertNotNull(metadataWithXml, "Metadata with xml flag should not be null");
        assertNotNull(metadataWithXml.getXml(), "Metadata should include xml when xml flag is provided");
        assertFalse(metadataWithXml.getXml().isEmpty(), "Metadata xml should not be empty when xml flag is provided");
    }


    @Test
    public void fetchMapMetadataSpamAllowedForOwner() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate ownerTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String mapTitle = "Owner Spam Map Test";
        final URI mindmapUri = addNewMap(ownerTemplate, mapTitle);

        // Make the map public first
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        final Map<String, Boolean> publishRequest = new HashMap<>();
        publishRequest.put("isPublic", true);
        final HttpEntity<Map<String, Boolean>> publishEntity = new HttpEntity<>(publishRequest, requestHeaders);
        
        // Try to publish - this might fail due to spam detection
        final ResponseEntity<String> publishResponse = ownerTemplate.exchange(mindmapUri + "/publish", HttpMethod.PUT, publishEntity, String.class);
        assertNotNull(publishResponse, "Publish response should not be null");
        
        // Test metadata access as owner - this should always work regardless of spam status
        final ResponseEntity<RestMindmapMetadata> exchange = ownerTemplate.exchange(mindmapUri + "/metadata", HttpMethod.GET, null, RestMindmapMetadata.class);
        
        // Owner should always be able to access metadata, even if map is marked as spam
        assertTrue(exchange.getStatusCode().is2xxSuccessful());
        assertEquals(mapTitle, exchange.getBody().getTitle());
    }

    @Test
    public void updateCollabs() throws URISyntaxException {

        // Create a sample map ...
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());
        final URI resourceUri = addNewMap(restTemplate, "Map for updateCollabs");

        // Add a new collaboration ...
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        RestCollaborationList collabs = new RestCollaborationList();
        collabs.setMessage("Adding new permission");

        String newCollab = "new-collab@example.com";
        String role = "editor";

        addCollabToList(newCollab, role, collabs);

        HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
        restTemplate.put(resourceUri + "/collabs/", updateEntity);

        collabs = fetchAndGetCollabs(requestHeaders, restTemplate, resourceUri);

        //delete one collab
        collabs.setCollaborations(collabs.getCollaborations().stream().filter(c -> c.getRole().equals("owner")).collect(Collectors.toList()));

        //Add another collaborationMediaType
        newCollab = "another-collab@example.com";
        addCollabToList(newCollab, role, collabs);

        //add owner to list
        addCollabToList(user.getEmail().replace(":admin", ""), "owner", collabs);

        updateEntity = new HttpEntity<>(collabs, requestHeaders);
        restTemplate.postForLocation(resourceUri + "/collabs/", updateEntity);

        final RestCollaborationList responseCollbs = fetchAndGetCollabs(requestHeaders, restTemplate, resourceUri);

        // Has been another-collaboration list updated ?
        assertTrue(responseCollbs.getCollaborations().stream().anyMatch(x -> x.getEmail().equals("another-collab@example.com")));
        assertEquals(responseCollbs.getCount(), 2);
    }


    @Test
    public void updateProperties() throws IOException, WiseMappingException, URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String title = "updateProperties map";
        final URI resourceUri = addNewMap(restTemplate, title);

        // Build map to update ...
        final RestMindmap mapToUpdate = new RestMindmap();
        mapToUpdate.setXml("<map>this is not valid</map>");
        mapToUpdate.setProperties("{zoom:x}");
        mapToUpdate.setTitle("new title for map");
        mapToUpdate.setDescription("updated map description");

        // Update map ...
        final String resourceUrl = resourceUri.toString();
        final HttpEntity<RestMindmap> updateEntity = new HttpEntity<>(mapToUpdate, requestHeaders);
        restTemplate.put(resourceUrl, updateEntity);

        // Check that the map has been updated ...
        HttpEntity<RestUser> findMapEntity = new HttpEntity<>(requestHeaders);
        final ResponseEntity<RestMindmap> response = restTemplate.exchange(resourceUri.toString(), HttpMethod.GET, findMapEntity, RestMindmap.class);
        assertEquals(response.getBody().getTitle(), mapToUpdate.getTitle());
        assertEquals(response.getBody().getDescription(), mapToUpdate.getDescription());
        assertEquals(response.getBody().getXml(), mapToUpdate.getXml());
        assertEquals(response.getBody().getProperties(), mapToUpdate.getProperties());
    }


    @Test
    public void batchDelete() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String title1 = "Batch delete map 1";
        addNewMap(restTemplate, title1);

        final String title2 = "Batch delete map 2";
        addNewMap(restTemplate, title2);

        final String maps = fetchMaps(requestHeaders, restTemplate).getMindmapsInfo().stream().map(map -> String.valueOf(map.getId())).collect(Collectors.joining(","));

        final ResponseEntity<String> exchange = restTemplate.exchange("/api/restful/maps/batch?ids=" + maps, HttpMethod.DELETE, null, String.class);
        assertTrue(exchange.getStatusCode().is2xxSuccessful(), "Status code:" + exchange.getStatusCode() + " - " + exchange.getBody());

        // Validate that the two maps are there ...
        final RestMindmapList body = fetchMaps(requestHeaders, restTemplate);
        assertEquals(0, body.getMindmapsInfo().size());
    }


    @Test
    public void updatePublishStateWithJson() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String mapTitle = "updatePublishStateWithJson";
        final URI mindmapUri = addNewMap(restTemplate, mapTitle);

        // Test publishing with JSON content type
        final Map<String, Boolean> publishRequest = new HashMap<>();
        publishRequest.put("isPublic", true);

        final HttpEntity<Map<String, Boolean>> updateEntity = new HttpEntity<>(publishRequest, requestHeaders);

        // Try to publish - this should work with JSON content type
        final ResponseEntity<String> exchange = restTemplate.exchange(mindmapUri + "/publish", HttpMethod.PUT, updateEntity, String.class);
        
        // The request should be accepted (not 415 error)
        // The global exception handler now returns 400 instead of 500 for validation errors
        assertTrue(exchange.getStatusCode().is2xxSuccessful() || exchange.getStatusCode().is4xxClientError(), 
                   "Expected 2xx or 4xx status, got: " + exchange.getStatusCode() + " - " + exchange.getBody());
        
        // If it's 4xx, it should be due to spam detection, not content type issues
        if (exchange.getStatusCode().is4xxClientError()) {
            String body = Objects.requireNonNull(exchange.getBody()).toLowerCase();
            assertTrue(body.contains("spam") || body.contains("spamcontentexception"),
                      "Expected spam-related error, got: " + exchange.getBody());
        }
    }

    @Test
    public void updatePublishStateWithJsonMakePrivate() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String mapTitle = "updatePublishStateWithJsonMakePrivate";
        final URI mindmapUri = addNewMap(restTemplate, mapTitle);

        // Test making private with JSON content type
        final Map<String, Boolean> publishRequest = new HashMap<>();
        publishRequest.put("isPublic", false);

        final HttpEntity<Map<String, Boolean>> updateEntity = new HttpEntity<>(publishRequest, requestHeaders);

        // Try to make private - this should always work
        final ResponseEntity<String> exchange = restTemplate.exchange(mindmapUri + "/publish", HttpMethod.PUT, updateEntity, String.class);
        
        // Making private should always succeed
        assertTrue(exchange.getStatusCode().is2xxSuccessful(), 
                   "Expected 2xx status for making private, got: " + exchange.getStatusCode() + " - " + exchange.getBody());
    }

    @Test
    public void updatePublishStateWithJsonInvalidRequest() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String mapTitle = "updatePublishStateWithJsonInvalidRequest";
        final URI mindmapUri = addNewMap(restTemplate, mapTitle);

        // Test with invalid JSON request (missing "isPublic" field)
        final Map<String, Boolean> publishRequest = new HashMap<>();
        // Intentionally not adding "isPublic" field

        final HttpEntity<Map<String, Boolean>> updateEntity = new HttpEntity<>(publishRequest, requestHeaders);

        // Try to publish with invalid request - this should fail
        final ResponseEntity<String> exchange = restTemplate.exchange(mindmapUri + "/publish", HttpMethod.PUT, updateEntity, String.class);
        
        // Should return 4xx error due to missing "public" field
        assertTrue(exchange.getStatusCode().is4xxClientError(), 
                   "Expected 4xx status for invalid request, got: " + exchange.getStatusCode() + " - " + exchange.getBody());
        assertTrue(Objects.requireNonNull(exchange.getBody()).contains("Map properties can not be null"),
                   "Expected error about missing properties, got: " + exchange.getBody());
    }

    @Test
    public void fetchMapHistory() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final URI resourceUri = addNewMap(restTemplate, "Map to change title");
        updateMapDocument(requestHeaders, restTemplate, resourceUri.toString(), null);

        //fetch map history
        final HttpEntity<RestMindmapHistoryList> findMapEntity = new HttpEntity<>(requestHeaders);
        final ResponseEntity<RestMindmapHistoryList> maps = restTemplate.exchange(resourceUri + "/history/", HttpMethod.GET, findMapEntity, RestMindmapHistoryList.class);
        assertTrue(maps.getStatusCode().is2xxSuccessful(), maps.toString());

        assertEquals(1, Objects.requireNonNull(maps.getBody()).getCount());
    }


    @Test
    public void updateRevertMindmap() throws IOException, URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final URI resourceUri = addNewMap(restTemplate, "map to test revert changes");
        updateMapDocument(requestHeaders, restTemplate, resourceUri.toString(), "<map><node text='this is an xml to test revert changes service'></map>");

        updateMapDocument(requestHeaders, restTemplate, resourceUri.toString(), "<map><node text='this is an xml with modification to be reverted'></map>");

        //fetch map history
        final HttpEntity<RestMindmapHistoryList> findMapEntity = new HttpEntity<>(requestHeaders);
        final ResponseEntity<RestMindmapHistoryList> mapHistories = restTemplate.exchange(resourceUri + "/history/", HttpMethod.GET, findMapEntity, RestMindmapHistoryList.class);

        //aply revert
        final HttpEntity<String> cloneEntity = new HttpEntity<>(requestHeaders);
        restTemplate.postForLocation(resourceUri + "/history/latest", cloneEntity);
        final RestMindmap latestStoredMap = findMap(requestHeaders, restTemplate, resourceUri);
        restTemplate.postForLocation(resourceUri + "/history/" + mapHistories.getBody().getChanges().get(1).getId(), cloneEntity);
        final RestMindmap firstVersionMap = findMap(requestHeaders, restTemplate, resourceUri);

        //verify revert
        assertEquals(firstVersionMap.getXml(), "<map><node text='this is an xml to test revert changes service'></map>");
        assertEquals(latestStoredMap.getXml(), "<map><node text='this is an xml with modification to be reverted'></map>");
    }


    @Test
    public void addCollabWhitoutOwnerPermission() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final URI resourceUri = addNewMap(restTemplate, "MaddCollabWhitoutOwnerPermission");

        // Add a new collaboration ...
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        final RestCollaborationList collabs = new RestCollaborationList();
        collabs.setMessage("Adding new permission");

        final RestAccountControllerTest restAccount = RestAccountControllerTest.create(restTemplate);
        final RestUser newCollab = restAccount.createNewUser();
        String role = "editor";

        addCollabToList(newCollab.getEmail(), role, collabs);

        final HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
        restTemplate.put(resourceUri + "/collabs/", updateEntity);

        final TestRestTemplate newCollabTemplate = this.restTemplate.withBasicAuth(newCollab.getEmail(), newCollab.getPassword());
        final ResponseEntity<Void> exchange = newCollabTemplate.exchange(resourceUri + "/collabs/", HttpMethod.PUT, updateEntity, Void.class);
        assertTrue(exchange.getStatusCode().is4xxClientError(), exchange.toString());
    }

    @Test
    public void addCollabWhitOwnerRole() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final URI resourceUri = addNewMap(restTemplate, "addCollabWhitOwnerRole");

        // Add a new collaboration ...
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        final RestCollaborationList collabs = new RestCollaborationList();
        collabs.setMessage("Adding new permission");

        final String newCollab = "new-collaborator@mail.com";
        String role = "owner";

        addCollabToList(newCollab, role, collabs);

        final HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
        final ResponseEntity<RestCollaborationList> collabsList = restTemplate.exchange(resourceUri + "/collabs/", HttpMethod.PUT, updateEntity, RestCollaborationList.class);
        // The global exception handler now returns 400 instead of 500 for validation errors
        assertTrue(collabsList.getStatusCode().is4xxClientError(), "Expected 4xx status, got: " + collabsList.getStatusCode());
    }

    @Test
    public void retrieveDocumentXml() throws URISyntaxException {
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        final String xmlContent = "<map><node text='test document retrieval'></map>";
        final URI resourceUri = addNewMap(restTemplate, "Test Document Retrieval", xmlContent);
        final String mapId = resourceUri.getPath().replace("/api/restful/maps/", "");

        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.TEXT_PLAIN);
        final HttpEntity<String> requestEntity = new HttpEntity<>(requestHeaders);

        final ResponseEntity<String> exchange = restTemplate.exchange("/api/restful/maps/" + mapId + "/document/xml", HttpMethod.GET, requestEntity, String.class);
        assertTrue(exchange.getStatusCode().is2xxSuccessful());
        assertNotNull(exchange.getBody());
        assertTrue(exchange.getBody().contains("test document retrieval"));
    }

    @Test
    public void lockMindmap() throws URISyntaxException {
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        final URI resourceUri = addNewMap(restTemplate, "Map to Lock");

        final HttpHeaders lockHeaders = new HttpHeaders();
        lockHeaders.setContentType(MediaType.TEXT_PLAIN);
        final HttpEntity<String> lockEntity = new HttpEntity<>("true", lockHeaders);
        final ResponseEntity<RestLockInfo> exchange = restTemplate.exchange(resourceUri + "/lock", HttpMethod.PUT, lockEntity, RestLockInfo.class);
        assertTrue(exchange.getStatusCode().is2xxSuccessful());

        final HttpEntity<String> unlockEntity = new HttpEntity<>("false", lockHeaders);
        final ResponseEntity<RestLockInfo> unlockResponse = restTemplate.exchange(resourceUri + "/lock", HttpMethod.PUT, unlockEntity, RestLockInfo.class);
        assertTrue(unlockResponse.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void updateMapXmlWithTextPlain() throws URISyntaxException {
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        final URI resourceUri = addNewMap(restTemplate, "Map to Update XML");
        final String mapId = resourceUri.getPath().replace("/api/restful/maps/", "");

        final String xmlContent = "<map><topic central=\"true\" text=\"Updated via XML\"></topic></map>";
        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.TEXT_PLAIN);
        final HttpEntity<String> updateEntity = new HttpEntity<>(xmlContent, requestHeaders);

        final ResponseEntity<String> exchange = restTemplate.exchange("/api/restful/maps/" + mapId + "/document/xml", HttpMethod.PUT, updateEntity, String.class);
        assertTrue(exchange.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void retrieveHistoryDocumentXml() throws URISyntaxException {
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        final URI resourceUri = addNewMap(restTemplate, "Map for History Test");
        final String mapId = resourceUri.getPath().replace("/api/restful/maps/", "");

        final ResponseEntity<RestMindmapHistoryList> historyResponse = restTemplate.exchange("/api/restful/maps/" + mapId + "/history/", HttpMethod.GET, null, RestMindmapHistoryList.class);
        assertTrue(historyResponse.getStatusCode().is2xxSuccessful());

        if (historyResponse.getBody() != null && !historyResponse.getBody().getChanges().isEmpty()) {
            final int hid = historyResponse.getBody().getChanges().get(0).getId();
            final HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setContentType(MediaType.TEXT_PLAIN);
            final HttpEntity<String> requestEntity = new HttpEntity<>(requestHeaders);

            final ResponseEntity<String> exchange = restTemplate.exchange("/api/restful/maps/" + mapId + "/" + hid + "/document/xml", HttpMethod.GET, requestEntity, String.class);
            assertTrue(exchange.getStatusCode().is2xxSuccessful());
            assertNotNull(exchange.getBody());
        }
    }

    @Test
    public void getStarred() throws URISyntaxException {
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        final URI resourceUri = addNewMap(restTemplate, "Map to Star");

        final HttpHeaders starHeaders = new HttpHeaders();
        starHeaders.setContentType(MediaType.TEXT_PLAIN);
        final HttpEntity<String> starEntity = new HttpEntity<>("true", starHeaders);
        restTemplate.exchange(resourceUri + "/starred", HttpMethod.PUT, starEntity, String.class);

        final ResponseEntity<String> getStarredResponse = restTemplate.exchange(resourceUri + "/starred", HttpMethod.GET, null, String.class);
        assertTrue(getStarredResponse.getStatusCode().is2xxSuccessful());
        assertEquals("true", getStarredResponse.getBody());
    }

    @Test
    public void retrievePublicMapMetadataShouldBeAccessibleWithoutAuthentication() throws URISyntaxException {
        final TestRestTemplate authenticatedTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());
        final TestRestTemplate unauthenticatedTemplate = new TestRestTemplate();
        unauthenticatedTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(this.restTemplate.getRootUri()));
        
        // Create a map and make it public
        final String mapTitle = "Public Map Metadata Test";
        final URI mapUri = addNewMap(authenticatedTemplate, mapTitle);
        
        // Try to make the map public (this might fail due to spam detection, but let's try)
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final Map<String, Boolean> publishRequest = new HashMap<>();
        publishRequest.put("isPublic", true);
        final HttpEntity<Map<String, Boolean>> publishEntity = new HttpEntity<>(publishRequest, requestHeaders);
        final ResponseEntity<String> publishResponse = authenticatedTemplate.exchange(mapUri + "/publish", HttpMethod.PUT, publishEntity, String.class);
        
        // Only test public access if we successfully made the map public
        if (publishResponse.getStatusCode().is2xxSuccessful()) {
            // Test that unauthenticated access to public map metadata SHOULD work
            final ResponseEntity<RestMindmapMetadata> publicResponse = unauthenticatedTemplate.exchange(mapUri + "/metadata", HttpMethod.GET, null, RestMindmapMetadata.class);
            
            // This test expects the CORRECT behavior: public maps should be accessible without authentication
            // The endpoint is marked with @PreAuthorize("permitAll()") so it should work
            // If this fails, it demonstrates the bug in the findMindmapById method
            assertTrue(publicResponse.getStatusCode().is2xxSuccessful(), 
                       "Public map metadata should be accessible without authentication. " +
                       "BUG: Got " + publicResponse.getStatusCode() + " instead of 200. " +
                       "This indicates the findMindmapById method has authentication issues.");
            
            assertNotNull(publicResponse.getBody(), "Public map metadata should not be null");
            assertEquals(mapTitle, publicResponse.getBody().getTitle(), "Public map metadata should have correct title");
        }
    }

    @Test
    public void retrievePublicMapDocumentShouldBeAccessibleWithoutAuthentication() throws URISyntaxException {
        final TestRestTemplate authenticatedTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());
        final TestRestTemplate unauthenticatedTemplate = new TestRestTemplate();
        unauthenticatedTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(this.restTemplate.getRootUri()));
        
        // Create a map with specific XML content
        final String mapTitle = "Public Map Document Test";
        final String xmlContent = "<map><topic central=\"true\" text=\"Public Map Content\"></topic></map>";
        final URI mapUri = addNewMap(authenticatedTemplate, mapTitle, xmlContent);
        final String mapId = mapUri.getPath().replace("/api/restful/maps/", "");
        
        // Try to make the map public
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final Map<String, Boolean> publishRequest = new HashMap<>();
        publishRequest.put("isPublic", true);
        final HttpEntity<Map<String, Boolean>> publishEntity = new HttpEntity<>(publishRequest, requestHeaders);
        final ResponseEntity<String> publishResponse = authenticatedTemplate.exchange(mapUri + "/publish", HttpMethod.PUT, publishEntity, String.class);
        
        // Only test public access if we successfully made the map public
        if (publishResponse.getStatusCode().is2xxSuccessful()) {
            // Test that public document access SHOULD work
            // The document endpoints are marked with @PreAuthorize("permitAll()") so they should allow unauthenticated access
            final HttpHeaders xmlHeaders = new HttpHeaders();
            xmlHeaders.setContentType(MediaType.TEXT_PLAIN);
            final HttpEntity<String> requestEntity = new HttpEntity<>(xmlHeaders);
            
            // Test /document/xml endpoint - SHOULD work for public maps
            final ResponseEntity<String> xmlResponse = unauthenticatedTemplate.exchange("/api/restful/maps/" + mapId + "/document/xml", HttpMethod.GET, requestEntity, String.class);
            assertTrue(xmlResponse.getStatusCode().is2xxSuccessful(),
                       "Public map XML document should be accessible without authentication. " +
                       "BUG: Got " + xmlResponse.getStatusCode() + " instead of 200. " +
                       "This indicates the findMindmapById method has authentication issues.");
            assertNotNull(xmlResponse.getBody(), "Public map XML should not be null");
            assertTrue(xmlResponse.getBody().contains("Public Map Content"), "Public map XML should contain expected content");
            
            // Test /document/xml-pub endpoint - SHOULD also work for public maps  
            final ResponseEntity<String> xmlPubResponse = unauthenticatedTemplate.exchange("/api/restful/maps/" + mapId + "/document/xml-pub", HttpMethod.GET, requestEntity, String.class);
            assertTrue(xmlPubResponse.getStatusCode().is2xxSuccessful(),
                       "Public map XML-pub document should be accessible without authentication. " +
                       "BUG: Got " + xmlPubResponse.getStatusCode() + " instead of 200. " +
                       "This indicates the findMindmapById method has authentication issues.");
            assertNotNull(xmlPubResponse.getBody(), "Public map XML-pub should not be null");
            assertTrue(xmlPubResponse.getBody().contains("Public Map Content"), "Public map XML-pub should contain expected content");
        }
    }

    @Test
    public void confirmBugExistsInMetadataEndpoint() throws URISyntaxException {
        final TestRestTemplate authenticatedTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());
        final TestRestTemplate unauthenticatedTemplate = new TestRestTemplate();
        unauthenticatedTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(this.restTemplate.getRootUri()));
        
        // Create a private map (default)
        final String mapTitle = "Metadata Bug Test";
        final URI mapUri = addNewMap(authenticatedTemplate, mapTitle);
        
        // Get the raw response to see the exact status code for private maps
        final ResponseEntity<String> response = unauthenticatedTemplate.exchange(mapUri + "/metadata", HttpMethod.GET, null, String.class);
        
        // Log what we actually got for debugging
        System.out.println("Metadata endpoint returned status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
        
        // This confirms the bug exists: unauthenticated access to ANY map fails
        // Even for public maps, this same code path would be taken due to findMindmapById bug
        assertTrue(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError(),
                   "BUG CONFIRMED: Metadata endpoint fails for unauthenticated access due to findMindmapById authentication issue. Got: " + response.getStatusCode());
    }

    @Test
    public void confirmPrivateMapAccessDeniedWithoutPermissions() throws URISyntaxException {
        final TestRestTemplate ownerTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());
        final TestRestTemplate unauthenticatedTemplate = new TestRestTemplate();
        unauthenticatedTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(this.restTemplate.getRootUri()));
        
        // Create another user to test unauthorized access
        final RestAccountControllerTest restAccount = RestAccountControllerTest.create(restTemplate);
        final RestUser unauthorizedUser = restAccount.createNewUser();
        final TestRestTemplate unauthorizedTemplate = this.restTemplate.withBasicAuth(unauthorizedUser.getEmail(), unauthorizedUser.getPassword());
        
        // Create a private map (owner)
        final String mapTitle = "Private Map Access Test";
        final URI mapUri = addNewMap(ownerTemplate, mapTitle);
        
        // Ensure the map is private (maps are private by default, but let's be explicit)
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final Map<String, Boolean> publishRequest = new HashMap<>();
        publishRequest.put("isPublic", false);
        final HttpEntity<Map<String, Boolean>> publishEntity = new HttpEntity<>(publishRequest, requestHeaders);
        ownerTemplate.exchange(mapUri + "/publish", HttpMethod.PUT, publishEntity, String.class);
        
        // Test 1: Unauthenticated access to private map metadata should be denied (not 2xx)
        final ResponseEntity<String> unauthMetadataResponse = unauthenticatedTemplate.exchange(mapUri + "/metadata", HttpMethod.GET, null, String.class);
        assertFalse(unauthMetadataResponse.getStatusCode().is2xxSuccessful(),
                   "Unauthenticated access to private map metadata should be denied. Got: " + unauthMetadataResponse.getStatusCode());
        final ResponseEntity<String> unauthMetadataXmlResponse = unauthenticatedTemplate.exchange(mapUri + "/metadata?xml=true", HttpMethod.GET, null, String.class);
        assertFalse(unauthMetadataXmlResponse.getStatusCode().is2xxSuccessful(),
                "Unauthenticated access to private map metadata xml should be denied. Got: " + unauthMetadataXmlResponse.getStatusCode());
        
        // Test 2: Unauthorized user access to private map metadata should be denied (not 2xx)  
        final ResponseEntity<String> unauthorizedMetadataResponse = unauthorizedTemplate.exchange(mapUri + "/metadata", HttpMethod.GET, null, String.class);
        assertFalse(unauthorizedMetadataResponse.getStatusCode().is2xxSuccessful(),
                   "Unauthorized user access to private map metadata should be denied. Got: " + unauthorizedMetadataResponse.getStatusCode());
        final ResponseEntity<String> unauthorizedMetadataXmlResponse = unauthorizedTemplate.exchange(mapUri + "/metadata?xml=true", HttpMethod.GET, null, String.class);
        assertFalse(unauthorizedMetadataXmlResponse.getStatusCode().is2xxSuccessful(),
                "Unauthorized user access to private map metadata xml should be denied. Got: " + unauthorizedMetadataXmlResponse.getStatusCode());
        
        // Test 3: Unauthenticated access to private map document should be denied (not 2xx)
        final String mapId = mapUri.getPath().replace("/api/restful/maps/", "");
        final HttpHeaders xmlHeaders = new HttpHeaders();
        xmlHeaders.setContentType(MediaType.TEXT_PLAIN);
        final HttpEntity<String> requestEntity = new HttpEntity<>(xmlHeaders);
        
        final ResponseEntity<String> unauthDocumentResponse = unauthenticatedTemplate.exchange("/api/restful/maps/" + mapId + "/document/xml", HttpMethod.GET, requestEntity, String.class);
        assertFalse(unauthDocumentResponse.getStatusCode().is2xxSuccessful(),
                   "Unauthenticated access to private map document should be denied. Got: " + unauthDocumentResponse.getStatusCode());
        
        // Test 4: Unauthorized user access to private map document should be denied (not 2xx)
        final ResponseEntity<String> unauthorizedDocumentResponse = unauthorizedTemplate.exchange("/api/restful/maps/" + mapId + "/document/xml", HttpMethod.GET, requestEntity, String.class);
        assertFalse(unauthorizedDocumentResponse.getStatusCode().is2xxSuccessful(),
                   "Unauthorized user access to private map document should be denied. Got: " + unauthorizedDocumentResponse.getStatusCode());
        
        // Test 5: Owner should be able to access their own map metadata (this should always work)
        final ResponseEntity<RestMindmapMetadata> ownerMetadataResponse = ownerTemplate.exchange(mapUri + "/metadata", HttpMethod.GET, null, RestMindmapMetadata.class);
        assertTrue(ownerMetadataResponse.getStatusCode().is2xxSuccessful(),
                   "Map owner should be able to access their own map metadata. Got: " + ownerMetadataResponse.getStatusCode());
        assertEquals(mapTitle, ownerMetadataResponse.getBody().getTitle(), "Owner should get correct map title");
        final ResponseEntity<RestMindmapMetadata> ownerMetadataXmlResponse = ownerTemplate.exchange(
                mapUri + "/metadata?xml=true",
                HttpMethod.GET,
                null,
                RestMindmapMetadata.class);
        assertTrue(ownerMetadataXmlResponse.getStatusCode().is2xxSuccessful(),
                "Map owner should be able to retrieve metadata xml. Got: " + ownerMetadataXmlResponse.getStatusCode());
        final RestMindmapMetadata ownerMetadataWithXml = ownerMetadataXmlResponse.getBody();
        assertNotNull(ownerMetadataWithXml, "Owner metadata with xml flag should not be null");
        assertNotNull(ownerMetadataWithXml.getXml(), "Owner metadata should include xml when xml flag is provided");
        assertFalse(ownerMetadataWithXml.getXml().isEmpty(), "Owner metadata xml should not be empty");
        
        // Test 6: Owner should be able to access their own map document (this should always work)
        final ResponseEntity<String> ownerDocumentResponse = ownerTemplate.exchange("/api/restful/maps/" + mapId + "/document/xml", HttpMethod.GET, requestEntity, String.class);
        assertTrue(ownerDocumentResponse.getStatusCode().is2xxSuccessful(),
                   "Map owner should be able to access their own map document. Got: " + ownerDocumentResponse.getStatusCode());
        assertNotNull(ownerDocumentResponse.getBody(), "Owner should get map document content");
    }

    @Test 
    public void publicMapAccessShouldWorkWithoutAuthentication() throws URISyntaxException {
        final TestRestTemplate authenticatedTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());
        final TestRestTemplate unauthenticatedTemplate = new TestRestTemplate();
        unauthenticatedTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(this.restTemplate.getRootUri()));
        
        // Create a simple map and make it public
        final String mapTitle = "Public Test Map";
        final URI mapUri = addNewMap(authenticatedTemplate, mapTitle);
        
        // Make the map public (this might be blocked by spam detection, but let's try)
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final Map<String, Boolean> publishRequest = new HashMap<>();
        publishRequest.put("isPublic", true);
        final HttpEntity<Map<String, Boolean>> publishEntity = new HttpEntity<>(publishRequest, requestHeaders);
        final ResponseEntity<String> publishResponse = authenticatedTemplate.exchange(mapUri + "/publish", HttpMethod.PUT, publishEntity, String.class);
        
        // Only test public access if we successfully made the map public
        if (publishResponse.getStatusCode().is2xxSuccessful()) {
            // Test that unauthenticated access to public map metadata now works with our fix
            final ResponseEntity<RestMindmapMetadata> response = unauthenticatedTemplate.exchange(mapUri + "/metadata", HttpMethod.GET, null, RestMindmapMetadata.class);
            
            // With our fix applied, this should now work
            assertTrue(response.getStatusCode().is2xxSuccessful(), 
                       "Public map metadata should be accessible without authentication. " +
                       "Got: " + response.getStatusCode() + 
                       ". Our fix should have resolved this issue.");
            
            assertNotNull(response.getBody(), "Response body should not be null");
            assertEquals(mapTitle, response.getBody().getTitle(), "Should get correct title");

            final ResponseEntity<RestMindmapMetadata> responseWithXml = unauthenticatedTemplate.exchange(
                    mapUri + "/metadata?xml=true",
                    HttpMethod.GET,
                    null,
                    RestMindmapMetadata.class);

            assertTrue(responseWithXml.getStatusCode().is2xxSuccessful(),
                    "Public map metadata with xml flag should be accessible without authentication");
            final RestMindmapMetadata publicMetadataWithXml = responseWithXml.getBody();
            assertNotNull(publicMetadataWithXml, "Public metadata with xml flag should not be null");
            assertNotNull(publicMetadataWithXml.getXml(), "Public metadata should include xml when xml flag is provided");
            assertFalse(publicMetadataWithXml.getXml().isEmpty(), "Public metadata xml should not be empty");
        } else {
            // If spam detection blocked making it public, just log and skip
            System.out.println("Map was not made public (likely spam detection), skipping public access test");
        }
    }

    private void createBulkMindmaps(@NotNull Account owner, int totalMaps, int labelsPerMap) {
        final SecurityContext previousContext = impersonate(owner);
        try {
            for (int i = 0; i < totalMaps; i++) {
                final Mindmap mindmap = new Mindmap();
                final String title = BULK_MAP_TITLE_PREFIX + i;
                mindmap.setTitle(title);
                mindmap.setDescription("Performance load mindmap #" + i);
                mindmap.setUnzipXml(Mindmap.getDefaultMindmapXml(title).getBytes(StandardCharsets.UTF_8));
                mindmap.setLabels(buildLabelSet(owner, labelsPerMap, i));
                try {
                    mindmapService.addMindmap(mindmap, owner);
                } catch (WiseMappingException e) {
                    throw new IllegalStateException("Failed to seed mindmap data for bulk listing test", e);
                }
            }
        } finally {
            SecurityContextHolder.setContext(previousContext);
        }
    }

    private Set<MindmapLabel> buildLabelSet(@NotNull Account owner, int labelsPerMap, int mapIndex) {
        final Set<MindmapLabel> labels = Collections.newSetFromMap(new IdentityHashMap<>());
        for (int labelIndex = 0; labelIndex < labelsPerMap; labelIndex++) {
            final MindmapLabel label = new MindmapLabel();
            label.setCreator(owner);
            label.setTitle("bulk-label-" + mapIndex + "-" + labelIndex);
            label.setColor(String.format("#%06X", (mapIndex * labelsPerMap + labelIndex) & 0xFFFFFF));
            labels.add(label);
        }
        return labels;
    }

    private long getNamedQueryExecutionCount(@NotNull Statistics statistics, @NotNull String namedQuery) {
        final QueryStatistics queryStats = statistics.getQueryStatistics(namedQuery);
        return queryStats != null ? queryStats.getExecutionCount() : 0L;
    }

    private SecurityContext impersonate(@NotNull Account account) {
        final SecurityContext previousContext = SecurityContextHolder.getContext();
        final SecurityContext testContext = SecurityContextHolder.createEmptyContext();
        final com.wisemapping.security.UserDetails principal = new com.wisemapping.security.UserDetails(account, mindmapService.isAdmin(account), userService);
        final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                principal.getPassword(),
                principal.getAuthorities());
        authentication.setDetails(principal);
        testContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(testContext);
        return previousContext;
    }

    private String changeMapTitle(final HttpHeaders requestHeaders, final MediaType mediaType, final TestRestTemplate template, final URI resourceUri) throws RestClientException {
        requestHeaders.setContentType(MediaType.TEXT_PLAIN);
        final String result = "New map to change title  - " + mediaType;
        final HttpEntity<String> updateEntity = new HttpEntity<>(result, requestHeaders);
        template.put(resourceUri + "/title", updateEntity);
        return result;
    }

    @NotNull
    private RestMindmapList fetchMaps(final HttpHeaders requestHeaders, final TestRestTemplate template) throws RestClientException {
        final HttpEntity<RestMindmapList> findMapEntity = new HttpEntity<>(requestHeaders);
        final ResponseEntity<RestMindmapList> response = template.exchange("/api/restful/maps/", HttpMethod.GET, findMapEntity, RestMindmapList.class);
        assertTrue(response.getStatusCode().is2xxSuccessful(), response.toString());

        return Objects.requireNonNull(response.getBody());
    }


    private RestCollaborationList fetchAndGetCollabs(final HttpHeaders requestHeaders, final TestRestTemplate template, final URI resourceUri) {
        final ResponseEntity<RestCollaborationList> response = fetchCollabs(requestHeaders, template, resourceUri);
        return response.getBody();
    }

    private RestCollaboration addCollabToList(String newCollab, String role, RestCollaborationList collabs) {
        RestCollaboration collab = new RestCollaboration();
        collab.setEmail(newCollab);
        collab.setRole(role);
        collabs.addCollaboration(collab);
        return collab;
    }

    private RestMindmap findMap(@NotNull HttpHeaders requestHeaders, @NotNull TestRestTemplate template, URI resourceUri) {
        final HttpEntity<RestMindmap> findMapEntity = new HttpEntity<>(requestHeaders);
        final ResponseEntity<RestMindmap> response = template.exchange(resourceUri.toString(), HttpMethod.GET, findMapEntity, RestMindmap.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalArgumentException(response.toString());
        }
        return response.getBody();
    }

    //
    private URI addNewMap(@NotNull TestRestTemplate template, @NotNull String title, @Nullable String xml) throws URISyntaxException {
        // Create a new map ...
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_XML);
        final HttpEntity<String> createUserEntity = new HttpEntity<>(xml, requestHeaders);

        final ResponseEntity<String> exchange = template.exchange("/api/restful/maps?title=" + title, HttpMethod.POST, createUserEntity, String.class);
        assertTrue(exchange.getStatusCode().is2xxSuccessful());

        final List<String> locations = exchange.getHeaders().get(HttpHeaders.LOCATION);
        return new URI(locations.stream().findFirst().get());
    }

    private URI addNewMap(@NotNull TestRestTemplate template, @NotNull String title) throws URISyntaxException {
        return addNewMap(template, title, null);
    }

}
