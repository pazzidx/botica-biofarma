package com.biofarma.inventario.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.biofarma.inventario.infrastructure.persistence.entity.ProductoEntity;
import com.biofarma.inventario.infrastructure.persistence.entity.RolEntity;
import com.biofarma.inventario.infrastructure.persistence.entity.UsuarioEntity;
import com.biofarma.inventario.infrastructure.persistence.repository.SpringDataProductoRepository;
import com.biofarma.inventario.infrastructure.persistence.repository.SpringDataRolRepository;
import com.biofarma.inventario.infrastructure.persistence.repository.SpringDataUsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

/**
 * Pruebas de integración completas para HU-05 cubriendo CA-01 a CA-06 con JWT real,
 * persistencia JPA/H2 y medición reproducible de tiempo de respuesta.
 */
@SpringBootTest
@Transactional
class ConsultaDisponibilidadMedicamentosIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SpringDataRolRepository springDataRolRepository;

    @Autowired
    private SpringDataUsuarioRepository springDataUsuarioRepository;

    @Autowired
    private SpringDataProductoRepository springDataProductoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    private String tokenTrabajadora;
    private String tokenAdministradora;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        asegurarRol("ADMINISTRADORA");
        asegurarRol("TRABAJADORA");

        crearUsuario("admin.hu05", "Propietaria", "Biofarma", "Admin123*", "ADMINISTRADORA");
        crearUsuario("trab.hu05", "Lucia", "Gomez", "Clave123*", "TRABAJADORA");

        tokenAdministradora = obtenerTokenJwt("admin.hu05", "Admin123*");
        tokenTrabajadora = obtenerTokenJwt("trab.hu05", "Clave123*");

        crearProducto("7501001", "Paracetamol 500mg", "Paracetamol", "Caja x 20 tabletas", "Genfar", "12.50", 45, false, true);
        crearProducto("MED-AMX-1", "Amoxicilina 500mg", "Amoxicilina", "Caja x 12 cápsulas", "Portugal", "16.00", 18, true, true);
        crearProducto("MED-AMX-2", "Amoxicilina Pediátrica", "Amoxicilina", "Frasco 60ml", "Medrock", "21.90", 0, true, true);
        crearProducto("MED-INA-1", "Amoxicilina Descontinuada", "Amoxicilina", "Caja x 8", "Genfar", "10.00", 50, false, false);
    }

    @Test
    void deberiaConsultarPorCodigoNumericoConStockYPrecioExacto() throws Exception {
        mockMvc.perform(get("/api/v1/medicamentos/disponibilidad")
                        .header("Authorization", "Bearer " + tokenTrabajadora)
                        .param("termino", "7501001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigoProducto").value("7501001"))
                .andExpect(jsonPath("$[0].nombreComercial").value("Paracetamol 500mg"))
                .andExpect(jsonPath("$[0].presentacion").value("Caja x 20 tabletas"))
                .andExpect(jsonPath("$[0].precioVenta").value(12.50))
                .andExpect(jsonPath("$[0].stockActual").value(45))
                .andExpect(jsonPath("$[0].estadoDisponibilidad").value("Disponible"))
                .andExpect(jsonPath("$[0].disponible").value(true));
    }

    @Test
    void deberiaBuscarPorNombreParcialMostrandoDisponiblesYAgotadosExcluyendoInactivos() throws Exception {
        mockMvc.perform(get("/api/v1/medicamentos/disponibilidad")
                        .header("Authorization", "Bearer " + tokenTrabajadora)
                        .param("termino", "Amoxi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].codigoProducto", hasItem("MED-AMX-1")))
                .andExpect(jsonPath("$[*].codigoProducto", hasItem("MED-AMX-2")))
                .andExpect(jsonPath("$[*].codigoProducto", not(hasItem("MED-INA-1"))))
                .andExpect(jsonPath("$[?(@.codigoProducto=='MED-AMX-2')].stockActual", hasItem(0)))
                .andExpect(jsonPath("$[?(@.codigoProducto=='MED-AMX-2')].estadoDisponibilidad", hasItem("Agotado")))
                .andExpect(jsonPath("$[?(@.codigoProducto=='MED-AMX-2')].disponible", hasItem(false)));
    }

    @Test
    void deberiaRetornarListaVaciaCuandoNoHayCoincidencias() throws Exception {
        mockMvc.perform(get("/api/v1/medicamentos/disponibilidad")
                        .header("Authorization", "Bearer " + tokenAdministradora)
                        .param("termino", "MedicamentoInexistente999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deberiaControlarStockNegativoSinOcultarloComoCeroNiExponerTrazas() throws Exception {
        crearProducto("MED-NEG-1", "Jarabe Inconsistente", "Dextrometorfano", "Frasco", "Genfar", "15.00", -4, false, true);

        mockMvc.perform(get("/api/v1/medicamentos/disponibilidad")
                        .header("Authorization", "Bearer " + tokenTrabajadora)
                        .param("termino", "MED-NEG-1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Inconsistencia de inventario"))
                .andExpect(jsonPath("$.message", containsString("MED-NEG-1")));
    }

    @Test
    void deberiaRechazarAccesoSinTokenYTerminoVacio() throws Exception {
        mockMvc.perform(get("/api/v1/medicamentos/disponibilidad")
                        .param("termino", "Paracetamol"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/medicamentos/disponibilidad")
                        .header("Authorization", "Bearer " + tokenTrabajadora)
                        .param("termino", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deberiaResponderEnMenosDeDosSegundosConCatalogoPoblado() throws Exception {
        List<ProductoEntity> loteCatalogo = new ArrayList<>(500);
        for (int i = 1; i <= 500; i++) {
            ProductoEntity p = new ProductoEntity();
            p.setCodigoProducto(String.format("PERF-%04d", i));
            p.setNombreComercial(i % 25 == 0 ? "Paracetamol Compuesto " + i : "Producto Generico " + i);
            p.setPrincipioActivo(i % 25 == 0 ? "Paracetamol" : "Principio " + i);
            p.setPresentacion("Caja x 10");
            p.setLaboratorio("LabBio");
            p.setPrecioVenta(new BigDecimal("9.90"));
            p.setStockActual(i % 2 == 0 ? 15 : 0);
            p.setStockMinimo(5);
            p.setRequiereReceta(false);
            p.setActivo(true);
            p.setFechaRegistro(LocalDateTime.now());
            loteCatalogo.add(p);
        }
        springDataProductoRepository.saveAll(loteCatalogo);
        springDataProductoRepository.flush();

        long inicioNs = System.nanoTime();
        mockMvc.perform(get("/api/v1/medicamentos/disponibilidad")
                        .header("Authorization", "Bearer " + tokenTrabajadora)
                        .param("termino", "Paracetamol"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoProducto").exists());
        long duracionMs = (System.nanoTime() - inicioNs) / 1_000_000;

        System.out.println("[HU-05 PERF] Tiempo medido de consulta sobre >500 productos: " + duracionMs + " ms");
        assertTrue(duracionMs < 2000, "La consulta superó el límite de 2000 ms: " + duracionMs + " ms");
    }

    private void asegurarRol(String nombreRol) {
        if (springDataRolRepository.findByNombre(nombreRol).isEmpty()) {
            RolEntity rol = new RolEntity();
            rol.setNombre(nombreRol);
            springDataRolRepository.save(rol);
        }
    }

    private void crearUsuario(String codigo, String nombres, String apellidos, String clave, String nombreRol) {
        if (springDataUsuarioRepository.findByCodigoUsuarioIgnoreCase(codigo).isPresent()) {
            return;
        }
        RolEntity rol = springDataRolRepository.findByNombre(nombreRol).orElseThrow();
        UsuarioEntity entity = new UsuarioEntity();
        entity.setCodigoUsuario(codigo);
        entity.setNombres(nombres);
        entity.setApellidos(apellidos);
        entity.setPasswordHash(passwordEncoder.encode(clave));
        entity.setRol(rol);
        entity.setActivo(true);
        entity.setFechaCreacion(LocalDateTime.now());
        springDataUsuarioRepository.save(entity);
    }

    private ProductoEntity crearProducto(
            String codigo,
            String nombre,
            String principio,
            String presentacion,
            String laboratorio,
            String precio,
            int stockActual,
            boolean requiereReceta,
            boolean activo
    ) {
        ProductoEntity entity = new ProductoEntity();
        entity.setCodigoProducto(codigo);
        entity.setNombreComercial(nombre);
        entity.setPrincipioActivo(principio);
        entity.setPresentacion(presentacion);
        entity.setLaboratorio(laboratorio);
        entity.setPrecioVenta(new BigDecimal(precio));
        entity.setStockActual(stockActual);
        entity.setStockMinimo(5);
        entity.setRequiereReceta(requiereReceta);
        entity.setActivo(activo);
        entity.setFechaRegistro(LocalDateTime.now());
        return springDataProductoRepository.save(entity);
    }

    private String obtenerTokenJwt(String codigoUsuario, String password) throws Exception {
        String body = objectMapper.writeValueAsString(
                java.util.Map.of("codigoUsuario", codigoUsuario, "password", password)
        );
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }
}
