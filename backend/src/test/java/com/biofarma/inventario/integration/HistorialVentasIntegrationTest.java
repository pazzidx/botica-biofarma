package com.biofarma.inventario.integration;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.biofarma.inventario.infrastructure.persistence.entity.DetalleVentaEntity;
import com.biofarma.inventario.infrastructure.persistence.entity.RolEntity;
import com.biofarma.inventario.infrastructure.persistence.entity.UsuarioEntity;
import com.biofarma.inventario.infrastructure.persistence.entity.VentaEntity;
import com.biofarma.inventario.infrastructure.persistence.repository.SpringDataRolRepository;
import com.biofarma.inventario.infrastructure.persistence.repository.SpringDataUsuarioRepository;
import com.biofarma.inventario.infrastructure.persistence.repository.SpringDataVentaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

/**
 * Pruebas de integración HU-03 cubriendo CA-01, CA-02, CA-03 y CA-04 con JWT real.
 */
@SpringBootTest
@Transactional
class HistorialVentasIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SpringDataRolRepository springDataRolRepository;

    @Autowired
    private SpringDataUsuarioRepository springDataUsuarioRepository;

    @Autowired
    private SpringDataVentaRepository springDataVentaRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    private String tokenAdmin;
    private String tokenTrabA;
    private String tokenTrabB;
    private Integer idVentaTrabA;
    private Integer idVentaTrabB;
    private Integer idTrabB;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        asegurarRol("ADMINISTRADORA");
        asegurarRol("TRABAJADORA");

        crearUsuario("admin.hu03", "Propietaria", "Biofarma", "Admin123*", "ADMINISTRADORA");
        crearUsuario("trab01.hu03", "Lucia", "Gomez", "Clave123*", "TRABAJADORA");
        crearUsuario("trab02.hu03", "Rosa", "Paredes", "Clave123*", "TRABAJADORA");

        tokenAdmin = obtenerTokenJwt("admin.hu03", "Admin123*");
        tokenTrabA = obtenerTokenJwt("trab01.hu03", "Clave123*");
        tokenTrabB = obtenerTokenJwt("trab02.hu03", "Clave123*");

        UsuarioEntity trabA = springDataUsuarioRepository.findByCodigoUsuarioIgnoreCase("trab01.hu03").orElseThrow();
        UsuarioEntity trabB = springDataUsuarioRepository.findByCodigoUsuarioIgnoreCase("trab02.hu03").orElseThrow();
        idTrabB = trabB.getIdUsuario();

        VentaEntity ventaA = crearVentaConDetalle(
                "T-HU03-0001",
                trabA,
                LocalDateTime.now().minusHours(2),
                "Paracetamol 500mg",
                "LOT-2026-A",
                2,
                new BigDecimal("12.50")
        );
        VentaEntity ventaB = crearVentaConDetalle(
                "T-HU03-0002",
                trabB,
                LocalDateTime.now().minusHours(1),
                "Amoxicilina 500mg",
                "LOT-2026-B",
                2,
                new BigDecimal("16.00")
        );

        idVentaTrabA = ventaA.getIdVenta();
        idVentaTrabB = ventaB.getIdVenta();
    }

    @Test
    void ca01_trabajadoraSoloVisualizaSusPropiasVentas() throws Exception {
        mockMvc.perform(get("/api/v1/ventas/historial")
                        .header("Authorization", "Bearer " + tokenTrabA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].numeroTicket").value("T-HU03-0001"))
                .andExpect(jsonPath("$[0].codigoUsuario").value("trab01.hu03"));

        mockMvc.perform(get("/api/v1/ventas/historial")
                        .header("Authorization", "Bearer " + tokenTrabB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].numeroTicket").value("T-HU03-0002"))
                .andExpect(jsonPath("$[0].codigoUsuario").value("trab02.hu03"));
    }

    @Test
    void ca02_trabajadoraNoPuedeConsultarDetalleDeVentaAjena() throws Exception {
        // Trabajadora A puede ver su propia venta
        mockMvc.perform(get("/api/v1/ventas/" + idVentaTrabA)
                        .header("Authorization", "Bearer " + tokenTrabA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroTicket").value("T-HU03-0001"))
                .andExpect(jsonPath("$.detalles", hasSize(1)));

        // Trabajadora A intenta ver venta de Trabajadora B -> 403 Forbidden
        mockMvc.perform(get("/api/v1/ventas/" + idVentaTrabB)
                        .header("Authorization", "Bearer " + tokenTrabA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void ca03_administradoraVisualizaHistorialGlobalYDetalleDeCualquierVenta() throws Exception {
        mockMvc.perform(get("/api/v1/ventas/historial")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].numeroTicket", hasItem("T-HU03-0001")))
                .andExpect(jsonPath("$[*].numeroTicket", hasItem("T-HU03-0002")));

        mockMvc.perform(get("/api/v1/ventas/" + idVentaTrabA)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoUsuario").value("trab01.hu03"));

        mockMvc.perform(get("/api/v1/ventas/" + idVentaTrabB)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoUsuario").value("trab02.hu03"));
    }

    @Test
    void ca04_servidorIgnoraParametrosDeUsuarioEnviadosPorElClienteYUsaTokenJwt() throws Exception {
        mockMvc.perform(get("/api/v1/ventas/historial")
                        .param("idUsuario", String.valueOf(idTrabB))
                        .param("codigoUsuario", "trab02.hu03")
                        .param("rol", "ADMINISTRADORA")
                        .header("Authorization", "Bearer " + tokenTrabA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].codigoUsuario", everyItem(is("trab01.hu03"))));
    }

    private void asegurarRol(String nombreRol) {
        if (springDataRolRepository.findByNombre(nombreRol).isEmpty()) {
            springDataRolRepository.saveAndFlush(new RolEntity(null, nombreRol));
        }
    }

    private void crearUsuario(String codigo, String nombres, String apellidos, String password, String rol)
            throws Exception {
        mockMvc.perform(post("/api/v1/usuarios")
                        .with(user("setupAdmin").roles("ADMINISTRADORA"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigoUsuario": "%s",
                                  "nombres": "%s",
                                  "apellidos": "%s",
                                  "password": "%s",
                                  "rol": "%s"
                                }
                                """.formatted(codigo, nombres, apellidos, password, rol)))
                .andExpect(status().isCreated());
    }

    private String obtenerTokenJwt(String codigoUsuario, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigoUsuario": "%s",
                                  "password": "%s"
                                }
                                """.formatted(codigoUsuario, password)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    private VentaEntity crearVentaConDetalle(
            String ticket,
            UsuarioEntity usuario,
            LocalDateTime fecha,
            String nombreProducto,
            String numeroLote,
            int cantidad,
            BigDecimal precioUnitario
    ) {
        BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));

        VentaEntity venta = new VentaEntity();
        venta.setNumeroTicket(ticket);
        venta.setUsuario(usuario);
        venta.setFechaVenta(fecha);
        venta.setTotal(subtotal);
        venta.setEstado("COMPLETADA");

        DetalleVentaEntity detalle = new DetalleVentaEntity();
        detalle.setIdLote(1);
        detalle.setNombreProducto(nombreProducto);
        detalle.setNumeroLote(numeroLote);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(precioUnitario);
        detalle.setSubtotal(subtotal);

        venta.agregarDetalle(detalle);
        return springDataVentaRepository.saveAndFlush(venta);
    }
}
