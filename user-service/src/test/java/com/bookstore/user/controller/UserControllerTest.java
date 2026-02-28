package com.bookstore.user.controller;

import com.bookstore.user.dto.UserRequest;
import com.bookstore.user.dto.UserResponse;
import com.bookstore.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserResponse buildResponse() {
        return UserResponse.builder()
                .id(1L)
                .username("john")
                .email("john@example.com")
                .fullName("John Doe")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private UserRequest buildRequest() {
        UserRequest req = new UserRequest();
        req.setUsername("john");
        req.setEmail("john@example.com");
        req.setFullName("John Doe");
        return req;
    }

    @Test
    void createUser_returns201() throws Exception {
        when(userService.createUser(any())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void createUser_invalidBody_returns400() throws Exception {
        UserRequest invalid = new UserRequest(); // sva polja su null — pada validacija

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_returns200() throws Exception {
        when(userService.getUserById(1L)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("john"));
    }

    @Test
    void getUserById_notFound_returns400() throws Exception {
        when(userService.getUserById(99L))
                .thenThrow(new IllegalArgumentException("User not found with id: 99"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User not found with id: 99"));
    }

    @Test
    void getAllUsers_returns200() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void deleteUser_returns204() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }
}
