package com.biofarma.inventario.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Transactional
class GestionUsuariosIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private String obtenerTokenJwt(String codigoUsuario, String password) throws Exception {
        String loginBody = """
                {
                  "codigoUsuario": "%s",
                  "password": "%s"
                }
                """.formatted(codigoUsuario, password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    @Test
    void flujoCompleto_loginRealRolesYGestionDeUsuarios() throws Exception {
        // 1. Creamos una administradora de prueba (se revertirá con @Transactional) y hacemos login real
        mockMvc.perform(post("/api/v1/usuarios")
                        .with(user("setupAdmin").roles("ADMINISTRADORA"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigoUsuario": "admin.integracion",
                                  "nombres": "Ana",
                                  "apellidos": "Torres",
                                  "password": "AdminClave123",
                                  "rol": "ADMINISTRADORA"
                                }
                                """))
                .andExpect(status().isCreated());

        String tokenAdmin = obtenerTokenJwt("admin.integracion", "AdminClave123");

        // 2. ADMINISTRADORA registra una trabajadora nueva en SQL Server
        MvcResult creadoResult = mockMvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigoUsuario": "trab.integracion",
                                  "nombres": "Elena",
                                  "apellidos": "Vargas",
                                  "password": "clave123",
                                  "rol": "TRABAJADORA"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoUsuario").value("trab.integracion"))
                .andReturn();

        int idCreado = objectMapper.readTree(creadoResult.getResponse().getContentAsString())
                .get("idUsuario").asInt();

        // 3. La nueva TRABAJADORA inicia sesión y recibe su propio JWT real
        String tokenTrabNueva = obtenerTokenJwt("trab.integracion", "clave123");

        // 4. TRABAJADORA intenta entrar al panel de usuarios -> Bloqueada con 403
        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + tokenTrabNueva))
                .andExpect(status().isForbidden());

        // 5. ADMINISTRADORA desactiva a la trabajadora -> Ya no puede iniciar sesión
        mockMvc.perform(patch("/api/v1/usuarios/" + idCreado + "/estado")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigoUsuario": "trab.integracion",
                                  "password": "clave123"
                                }
                                """))
                .andExpect(status().is4xxClientError());

        // 6. ADMINISTRADORA la reactiva y luego la elimina físicamente
        mockMvc.perform(patch("/api/v1/usuarios/" + idCreado + "/estado")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));

        mockMvc.perform(delete("/api/v1/usuarios/" + idCreado)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());
    }
}