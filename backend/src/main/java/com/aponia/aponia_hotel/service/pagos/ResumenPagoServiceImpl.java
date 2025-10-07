package com.aponia.aponia_hotel.service.pagos;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aponia.aponia_hotel.entities.pagos.ResumenPago;
import com.aponia.aponia_hotel.repository.pagos.ResumenPagoRepository;

@Service
@Transactional
public class ResumenPagoServiceImpl implements ResumenPagoService {

    private final ResumenPagoRepository repository;

    public ResumenPagoServiceImpl(ResumenPagoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumenPago> listar() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResumenPago> obtenerPorReserva(String reservaId) {
        return repository.findByReservaId(reservaId);
    }

    @Override
    public ResumenPago crear(ResumenPago resumenPago) {
        return repository.save(resumenPago);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResumenPago> obtener(String id) {
        return repository.findById(id);
    }

    @Override
    public ResumenPago actualizar(ResumenPago resumenPago) {
        return repository.save(resumenPago);
    }

    @Override
    public void eliminar(String id) {
        repository.deleteById(id);
    }
    
    @Override
    public void actualizarResumen(String reservaId, BigDecimal totalHabitaciones, BigDecimal totalServicios, BigDecimal totalPagado){

    }

}