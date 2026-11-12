package com.example.usermanagement.controller;

import com.example.usermanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepository;

    @BeforeEach
    void clean() {
        userRepository.deleteAll();
    }

    private String body(String name, String email) {
        return """
                {"fullName":"%s","email":"%s","phone":"0912345678","role":"USER"}
                """.formatted(name, email);
    }

    private long createUser(String name, String email) throws Exception {
        MvcResult res = mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON).content(body(name, email)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/users/")))
                .andReturn();
        String location = res.getResponse().getHeader("Location");
        return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
    }

    @Test
    void fullCrudFlow() throws Exception {
        long id = createUser("Nguyen Van A", "a@example.com");

        mvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("a@example.com"))
                .andExpect(jsonPath("$.active").value(true));

        mvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON).content(body("Nguyen Van B", "b@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Nguyen Van B"));

        mvc.perform(get("/api/users").param("keyword", "van b"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        mvc.perform(delete("/api/users/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/users/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void createWithInvalidDataReturns400() throws Exception {
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void duplicateEmailReturns409() throws Exception {
        createUser("User 1", "dup@example.com");
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(body("User 2", "DUP@example.com")))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteMissingUserReturns404() throws Exception {
        mvc.perform(delete("/api/users/{id}", 9999)).andExpect(status().isNotFound());
    }
}
