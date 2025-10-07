package com.aponia.aponia_hotel.repository.pagos;

import com.aponia.aponia_hotel.entities.pagos.Pago;
import com.aponia.aponia_hotel.entities.pagos.Pago.EstadoPago;
import com.aponia.aponia_hotel.entities.pagos.Pago.TipoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, String> {
    List<Pago> findByReservaId(String reservaId);

    List<Pago> findByReservaIdAndEstado(String reservaId, EstadoPago estado);

    List<Pago> findByTipo(TipoPago tipo);

    List<Pago> findByEstado(EstadoPago estado);

    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.reserva.id = :reservaId AND p.estado = :estado")
    BigDecimal calcularTotalPagosPorEstado(@Param("reservaId") String reservaId, @Param("estado") EstadoPago estado);

    @Query("SELECT p FROM Pago p WHERE p.reserva.id = :reservaId AND p.estado = :estado ORDER BY p.fecha DESC")
    List<Pago> findByReservaIdAndEstadoOrderByFechaDesc(@Param("reservaId") String reservaId, @Param("estado") EstadoPago estado);
}