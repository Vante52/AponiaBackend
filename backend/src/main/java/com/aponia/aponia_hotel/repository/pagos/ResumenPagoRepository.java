package com.aponia.aponia_hotel.repository.pagos;

import com.aponia.aponia_hotel.entities.pagos.ResumenPago;
import com.aponia.aponia_hotel.entities.reservas.Reserva;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ResumenPagoRepository extends JpaRepository<ResumenPago, String> {
    Optional<ResumenPago> findByReservaId(String reservaId);

    Optional<ResumenPago> findByReserva(Reserva reserva);
}