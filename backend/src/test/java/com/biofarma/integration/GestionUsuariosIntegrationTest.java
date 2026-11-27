package com.biofarma.inventario.integration;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.biofarma.inventario.infrastructure.persistence.entity.RolEntity;
import com.biofarma.inventario.infrastructure.persistence.entity.UsuarioEntity;
import com.biofarma.inventario.infrastructure.persistence.repository.SpringDataRolRepository;
import com.biofarma.inventario.infrastructure.persistence.repository.SpringDataUsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private SpringDataRolRepository springDataRolRepository;

    @Autowired
    private SpringDataUsuarioRepository springDataUsuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        if (springDataRolRepository.findByNombre("ADMINISTRADORA").isEmpty()) {
            springDataRolRepository.saveAndFlush(new RolEntity(null, "ADMINISTRADORA"));
        }
        if (springDataRolRepository.findByNombre("TRABAJADORA").isEmpty()) {
            springDataRolRepository.saveAndFlush(new RolEntity(null, "TRABAJADORA"));
        }
        if (springDataUsuarioRepository.findByCodigoUsuarioIgnoreCase("admin.integracion").isEmpty()) {
            RolEntity rolAdmin = springDataRolRepository.findByNombre("ADMINISTRADORA").orElseThrow();
            UsuarioEntity admin = new UsuarioEntity();
            admin.setCodigoUsuario("admin.integracion");
            admin.setNombres("Ana");
            admin.setApellidos("Torres");
            admin.setPasswordHash(passwordEncoder.encode("AdminClave123"));
            admin.setRol(rolAdmin);
            admin.setActivo(true);
            admin.setFechaCreacion(LocalDateTime.now());
            springDataUsuarioRepository.saveAndFlush(admin);
        }
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
        String tokenAdmin = obtenerTokenJwt("admin.integracion", "AdminClave123");

        // 1. ADMINISTRADORA registra una trabajadora nueva
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

        // 2. La nueva TRABAJADORA inicia sesión y recibe su propio JWT real
        String tokenTrabNueva = obtenerTokenJwt("trab.integracion", "clave123");

        // 3. TRABAJADORA intenta entrar al panel de usuarios -> Bloqueada con 403
        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + tokenTrabNueva))
                .andExpect(status().isForbidden());

        // 4. ADMINISTRADORA elimina físicamente a la trabajadora (0 ventas asociadas)
        mockMvc.perform(delete("/api/v1/usuarios/" + idCreado)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eliminadoFisicamente").value(true));
    }
}
