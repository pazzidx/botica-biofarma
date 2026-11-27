package com.biofarma.inventario.infrastructure.persistence.repository;

import com.biofarma.inventario.infrastructure.persistence.entity.VentaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio Spring Data JPA para la entidad VentaEntity.
 */
public interface SpringDataVentaRepository extends JpaRepository<VentaEntity, Integer> {

    @Query("""
            SELECT DISTINCT v
            FROM VentaEntity v
            JOIN FETCH v.usuario
            LEFT JOIN FETCH v.detalles
            ORDER BY v.fechaVenta DESC, v.idVenta DESC
            """)
    List<VentaEntity> findAllWithUsuarioAndDetallesOrderByFechaVentaDesc();

    @Query("""
            SELECT DISTINCT v
            FROM VentaEntity v
            JOIN FETCH v.usuario
            LEFT JOIN FETCH v.detalles
            WHERE v.usuario.idUsuario = :idUsuario
            ORDER BY v.fechaVenta DESC, v.idVenta DESC
            """)
    List<VentaEntity> findByUsuarioIdWithDetallesOrderByFechaVentaDesc(@Param("idUsuario") Integer idUsuario);

    @Query("""
            SELECT v
            FROM VentaEntity v
            JOIN FETCH v.usuario
            LEFT JOIN FETCH v.detalles
            WHERE v.idVenta = :idVenta
            """)
    Optional<VentaEntity> findByIdWithUsuarioAndDetalles(@Param("idVenta") Integer idVenta);
}
