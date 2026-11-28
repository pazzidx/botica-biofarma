package com.biofarma.inventario.infrastructure.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.biofarma.inventario.domain.model.Medicamento;
import com.biofarma.inventario.domain.repository.MedicamentoRepository;
import com.biofarma.inventario.domain.repository.StockInventarioRepository;
import com.biofarma.inventario.infrastructure.persistence.entity.ProductoEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MedicamentoDisponibilidadRepositoryIntegrationTest {

    @Autowired
    private SpringDataProductoRepository springDataProductoRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private StockInventarioRepository stockInventarioRepository;

    private Integer idParacetamol;
    private Integer idAgotado;
    private Integer idInactivo;
    private Integer idEspecial;

    @BeforeEach
    void setUp() {
        idParacetamol = crearProducto("7501001", "Paracetamol 500mg", "Paracetamol", "Caja x 20", "Genfar", "12.50", 40, true).getIdProducto();
        idAgotado = crearProducto("MED-AGO", "Amoxicilina 500mg", "Amoxicilina", "Caja x 12", "Portugal", "16.00", 0, true).getIdProducto();
        idInactivo = crearProducto("MED-INA", "Paracetamol Antiguo", "Paracetamol", "Jarabe", "Genfar", "9.00", 25, false).getIdProducto();
        idEspecial = crearProducto("MED-100%", "Vitamina C 100%_Plus", "Ácido Ascórbico", "Frasco", "Medrock", "18.00", 12, true).getIdProducto();
    }

    @Test
    void deberiaBuscarPorCodigoNumericoYExcluirInactivos() {
        List<Medicamento> resultados = medicamentoRepository.buscarActivosPorTermino("7501001", 50);

        assertEquals(1, resultados.size());
        assertEquals(idParacetamol, resultados.get(0).getIdProducto());
        assertEquals("Paracetamol 500mg", resultados.get(0).getNombreComercial());
    }

    @Test
    void deberiaBuscarPorNombreParcialIgnorandoMayusculasYExcluyendoInactivos() {
        List<Medicamento> resultados = medicamentoRepository.buscarActivosPorTermino("parace", 50);

        assertEquals(1, resultados.size());
        assertEquals(idParacetamol, resultados.get(0).getIdProducto());
        assertFalse(resultados.stream().anyMatch(m -> m.getIdProducto().equals(idInactivo)));
    }

    @Test
    void deberiaEscaparComodinesLikeDeFormaSegura() {
        List<Medicamento> todosPorComodin = medicamentoRepository.buscarActivosPorTermino("%", 50);

        assertEquals(1, todosPorComodin.size());
        assertEquals(idEspecial, todosPorComodin.get(0).getIdProducto());
    }

    @Test
    void deberiaConsultarStockActualEnBloqueDesdeInventario() {
        Map<Integer, Integer> stockMap = stockInventarioRepository.obtenerStockActualPorProductos(
                List.of(idParacetamol, idAgotado)
        );

        assertEquals(2, stockMap.size());
        assertEquals(40, stockMap.get(idParacetamol));
        assertEquals(0, stockMap.get(idAgotado));
        assertTrue(stockInventarioRepository.obtenerStockActualPorProductos(List.of()).isEmpty());
    }

    private ProductoEntity crearProducto(
            String codigo,
            String nombre,
            String principio,
            String presentacion,
            String laboratorio,
            String precio,
            int stockActual,
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
        entity.setRequiereReceta(false);
        entity.setActivo(activo);
        entity.setFechaRegistro(LocalDateTime.now());
        return springDataProductoRepository.save(entity);
    }
}
