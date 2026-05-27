package com.natived.spock.demo.nativedspocktestdemo.spock

import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
class StudentControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    // -----------------------------------------------------------------------
    // Parameterised test — Spock runs once per row in the where: table.
    //
    // Two scenarios both expect a 400 Bad Request with a standard error body:
    //   1. id field is explicitly null   → '{"id": null}'
    //   2. id field is missing entirely  → '{}'
    //
    // The error JSON must contain:
    //   status  : 400
    //   error   : "Bad Request"
    //   message : description of the problem
    //   path    : the endpoint that was called
    // -----------------------------------------------------------------------

    def "getStudent returns 400 when id is #scenario"() {
        when: "POST /api/v1/spock/getStudent is called with #scenario id"
        def result = mockMvc.perform(
                post("/api/v1/spock/getStudent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        ).andReturn()

        def json = new JsonSlurper().parseText(result.response.contentAsString)

        then: "response is 400 Bad Request with a standard error body"
        result.response.status == 400
        json.status           == 400
        json.error            == "Bad Request"
        json.message          != null
        json.path             == "/api/v1/spock/getStudent"

        where:
        scenario    | requestBody
        "null"      | '{"id": null}'
        "missing"   | '{}'
    }
}
