package com.autenticacion.demo.controller;

import com.autenticacion.demo.dto.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de seguridad para AuthController
 * Valida autenticación y generación de tokens JWT
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Test: Login válido devuelve JWT
     * 
     * Valida:
     * - Autenticación correcta con credenciales válidas
     * - Generación de token JWT
     * - Respuesta HTTP 200 OK
     * - Body contiene el token
     */
    @Test
    void testLoginValidoDevuelveJWT() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(
                "admin@example.com",
                "password");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    /**
     * Test: Acceso sin token → 401
     * 
     * Valida:
     * - Filtro JWT rechaza peticiones sin token
     * - SecurityFilterChain protege recursos
     * - Respuesta HTTP 401 Unauthorized
     */
    @Test
    void testAccesoSinTokenDevuelve401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/recurso-protegido")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test: Acceso con token inválido → 401
     * 
     * Valida:
     * - Verificación criptográfica del token
     * - Rechazo correcto de tokens manipulados
     * - Respuesta HTTP 401 Unauthorized
     */
    @Test
    void testAccesoConTokenInvalidoDevuelve401() throws Exception {
        // Arrange - Token manipulado/inválido
        String tokenInvalido = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        // Act & Assert
        mockMvc.perform(get("/api/recurso-protegido")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", tokenInvalido))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test: Acceso con token malformado → 401
     * 
     * Valida:
     * - Rechazo de tokens con formato incorrecto
     * - Respuesta HTTP 401 Unauthorized
     */
    @Test
    void testAccesoConTokenMalformadoDevuelve401() throws Exception {
        // Arrange - Token malformado
        String tokenMalformado = "Bearer token_invalido_sin_estructura";

        // Act & Assert
        mockMvc.perform(get("/api/recurso-protegido")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", tokenMalformado))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test: Acceso con rol insuficiente → 403
     * 
     * Valida:
     * - Autorización basada en roles (no solo autenticación)
     * - Token USER → endpoint ADMIN
     * - Respuesta HTTP 403 Forbidden
     */
    @Test
    void testAccesoConRolInsuficienteDevuelve403() throws Exception {
        // Arrange - Obtener token de usuario con rol USER
        LoginRequest loginRequest = new LoginRequest(
                "user@example.com",
                "password");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("token").asText();

        // Act & Assert - Intentar acceder a endpoint de ADMIN con token de USER
        mockMvc.perform(get("/api/recurso-protegido/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
