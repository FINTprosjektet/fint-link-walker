package no.fint.linkwalker.dto

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.linkwalker.DiscoveredRelation
import spock.lang.Specification

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath
import static org.junit.Assert.assertThat

class TestCaseSpec extends Specification {
    private ObjectMapper objectMapper
    private TestCase testCase

    void setup() {
        objectMapper = new ObjectMapper()
        testCase = new TestCase(new TestRequest('baseUrl', 'endpoint', 'orgId', 'client'))
        testCase.addRelation(new DiscoveredRelation(rel: 'self', links: [new URL('http://localhost')]))
    }

    def "Serialize all fields"() {
        when:
        def json = objectMapper.writerWithView(TestCaseViews.Details.class).writeValueAsString(testCase)

        then:
        assertThat(json, hasJsonPath('$.id'))
        assertThat(json, hasJsonPath('$.remaining'))
        assertThat(json, hasJsonPath('$.relations'))
    }

    def "Serialize only results overview fields"() {
        when:
        def json = objectMapper.writerWithView(TestCaseViews.ResultsOverview.class).writeValueAsString(testCase)

        then:
        assertThat(json, hasJsonPath('$.id'))
        assertThat(json, hasNoJsonPath('$.remaining'))
        assertThat(json, hasNoJsonPath('$.relations'))
    }

}
