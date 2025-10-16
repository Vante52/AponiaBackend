// package com.aponia.aponia_hotel.controller.pagos;

// import com.aponia.aponia_hotel.entities.pagos.ResumenPago;
// import com.aponia.aponia_hotel.entities.reservas.Reserva;
// import com.aponia.aponia_hotel.service.pagos.ResumenPagoService;
// import com.aponia.aponia_hotel.service.pagos.PagoService;
// import io.swagger.v3.oas.annotations.Operation;
// import org.springframework.web.bind.annotation.*;

// import java.math.BigDecimal;
// import java.util.List;

// @RestController
// @RequestMapping("/api/resumen-pagos")
// @CrossOrigin(origins = "http://localhost:4200")
// public class ResumenPagoRestController {

//     private final ResumenPagoService service;
//     private final PagoService pagoService;

//     public ResumenPagoRestController(ResumenPagoService service, PagoService pagoService) {
//         this.service = service;
//         this.pagoService = pagoService;
//     }

//     // ===== Lecturas =====

//     @GetMapping("/all")
//     @Operation(summary = "Lista todos los resúmenes de pago")
//     public List<ResumenPago> findAll() {
//         return service.listar();
//     }

//     @GetMapping("/find/{reservaId}")
//     @Operation(summary = "Obtiene el resumen de pago por reservaId")
//     public ResumenPago findOne(@PathVariable String reservaId) {
//         return service.obtenerPorReserva(reservaId).orElse(null);
//     }

//     // ===== Mutaciones CRUD =====

//     @PostMapping("/add")
//     @Operation(summary = "Crea un resumen de pago para una reserva")
//     public ResumenPago add(@RequestBody ResumenPago resumen, @RequestParam String reservaId) {
//         // maps-id: el id del resumen ES el id de la reserva
//         resumen.setReservaId(reservaId);
//         Reserva r = new Reserva(); r.setId(reservaId);
//         resumen.setReserva(r);
//         return service.crear(resumen);
//     }

//     @PutMapping("/update")
//     @Operation(summary = "Actualiza un resumen de pago existente")
//     public ResumenPago update(@RequestBody ResumenPago resumen, @RequestParam String reservaId) {
//         resumen.setReservaId(reservaId);
//         return service.actualizar(resumen);
//     }

//     @DeleteMapping("/delete/{reservaId}")
//     @Operation(summary = "Elimina el resumen de pago por reservaId")
//     public void delete(@PathVariable String reservaId) {
//         service.eliminar(reservaId);
//     }

//     // ===== Acción de negocio: recalcular =====

//     @PostMapping("/{reservaId}/recalcular")
//     @Operation(summary = "Recalcula el resumen: totalHabitaciones + totalServicios + totalPagado")
//     public void recalcular(@PathVariable String reservaId,
//                            @RequestParam BigDecimal totalHabitaciones,
//                            @RequestParam BigDecimal totalServicios) {
//         // totalPagado = suma de pagos COMPLETADOS
//         BigDecimal totalPagado = BigDecimal.valueOf(pagoService.calcularTotalPagosCompletados(reservaId));
//         service.actualizarResumen(reservaId, totalHabitaciones, totalServicios, totalPagado);
//     }
// }
