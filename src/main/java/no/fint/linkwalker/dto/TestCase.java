package no.fint.linkwalker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import no.fint.linkwalker.DiscoveredRelation;
import no.fint.linkwalker.TestedRelation;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * A TestCase gets the content of a URL, parses it, and follows any and all URLs
 * <p>
 * The TestCase is successful if the URL responds with 200 OK and all links contained in the response-object also pass their tests.
 */
@Slf4j
@Getter
@ToString(of = {"id", "status"})
public class TestCase {

    private final UUID id;
    private Status status;
    private String reason;
    private final String organisation;
    private final TestRequest testRequest;
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss")
    private final Date time;
    private final AtomicLong remaining = new AtomicLong();

    @JsonView(TestCaseViews.Details.class)
    private final Map<String, Collection<TestedRelation>> relations = new HashMap<>();

    private TestCase(TestCase testCase) {
        this.organisation = testCase.organisation;
        this.id = testCase.id;
        this.status = testCase.status;
        this.reason = testCase.reason;
        this.testRequest = testCase.testRequest;
        this.time = testCase.time;
        this.remaining.set(testCase.remaining.longValue());
    }

    public TestCase(String organisation, TestRequest testRequest) {
        this.organisation = organisation;
        this.id = UUID.randomUUID();
        this.testRequest = testRequest;
        this.time = new Date();
        status = Status.NOT_QUEUED;
    }

    private void transition(Status newStatus) {
        log.info("{}: {} {} -> {}", organisation, testRequest.getTarget(), status, newStatus);
        this.status = newStatus;
    }

    public void start() {
        transition(Status.RUNNING);
    }

    public void enqueued() {
        transition(Status.NOT_STARTED);
    }

    public void succeed() {
        transition(Status.OK);
    }

    public void partiallyFailed() {
        transition(Status.PARTIALLY_FAILED);
        reason = "There is no data to test.";
    }

    public void failed(Throwable t) {
        transition(Status.FAILED);
        reason = t.getClass() + ": " + t.getMessage();
    }

    public void failed(String reason) {
        transition(Status.FAILED);
        this.reason = reason;
    }

    public void addRelation(DiscoveredRelation discoveredRelation) {
        String rel = discoveredRelation.getRel();
        if (!relations.containsKey(rel)) {
            Set<TestedRelation> relationSet = new HashSet<>();
            relations.put(rel, relationSet);
        }
        Collection<TestedRelation> testedRelations = relations.get(rel);
        discoveredRelation.getLinks().forEach(link -> testedRelations.add(new TestedRelation(link, discoveredRelation.getParentUrl())));
    }

    public Map<String, Collection<TestedRelation>> getRelations() {
        return relations;
    }

    public TestCase filterAndCopyRelations(Status status) {
        Map<String, Collection<TestedRelation>> copiedRelations = new HashMap<>(relations);
        copiedRelations.entrySet().forEach(entry -> {
            List<TestedRelation> filteredStatuses = entry.getValue().stream().filter(val -> val.getStatus() == status).collect(Collectors.toList());
            entry.setValue(filteredStatuses);
        });

        TestCase copiedTestCase = new TestCase(this);
        copiedTestCase.relations.putAll(copiedRelations);
        return copiedTestCase;
    }

}
