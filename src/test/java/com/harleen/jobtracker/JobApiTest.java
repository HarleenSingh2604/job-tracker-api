package com.harleen.jobtracker;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class JobApiTest {

    @Autowired MockMvc mvc;

    private String register(String user) throws Exception {
        String body = "{\"username\":\"" + user + "\",\"password\":\"password123\"}";
        String res = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(res, "$.token");
    }

    private static final String JOB = "{\"company\":\"Citi\",\"title\":\"Junior Developer\",\"location\":\"Mississauga\"}";

    @Test
    void rejectsRequestsWithoutToken() throws Exception {
        mvc.perform(get("/api/jobs")).andExpect(status().isUnauthorized());
    }

    @Test
    void createsAndListsJobsWithDefaults() throws Exception {
        String token = register("u-" + UUID.randomUUID());
        mvc.perform(post("/api/jobs").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(JOB))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APPLIED"))
                .andExpect(jsonPath("$.followUpDate").exists());
        mvc.perform(get("/api/jobs").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].company").value("Citi"));
    }

    @Test
    void usersCannotSeeEachOthersJobs() throws Exception {
        String a = register("a-" + UUID.randomUUID());
        String b = register("b-" + UUID.randomUUID());
        String res = mvc.perform(post("/api/jobs").header("Authorization", "Bearer " + a)
                        .contentType(MediaType.APPLICATION_JSON).content(JOB))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(res, "$.id");
        mvc.perform(get("/api/jobs/" + id).header("Authorization", "Bearer " + b))
                .andExpect(status().isNotFound());
    }

    @Test
    void validatesInput() throws Exception {
        String token = register("v-" + UUID.randomUUID());
        mvc.perform(post("/api/jobs").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"company\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
