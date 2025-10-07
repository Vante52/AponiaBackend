package com.aponia.aponia_hotel.service.reservas;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aponia.aponia_hotel.entities.reservas.ReservaServicio;
import com.aponia.aponia_hotel.repository.reservas.ReservaServicioRepository;

@Service
@Transactional
public class ReservaServicioServiceImpl implements ReservaServicioService {

    private final ReservaServicioRepository repository;

    public ReservaServicioServiceImpl(ReservaServicioRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaServicio> listar() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaServicio> findByReservaId(String reservaId) {
        return repository.findByReservaId(reservaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaServicio> findByServicioIdAndFecha(String servicioId, LocalDate fecha) {
        return repository.findByServicioIdAndFecha(servicioId, fecha);
    }

    @Override
    public void crear(ReservaServicio reservaServicio) {
        repository.save(reservaServicio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReservaServicio> obtener(String id) {
        return repository.findById(id);
    }

    @Override
    public void actualizar(ReservaServicio reservaServicio) {
        repository.save(reservaServicio);
    }

    @Override
    public void eliminar(String id) {
        repository.deleteById(id);
    }
}