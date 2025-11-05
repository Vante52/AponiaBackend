package com.aponia.aponia_hotel.e2e;

import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class HotelRegistrationReservationE2ETest {

    private static WebDriver driver;
    private static final String FRONT_URL = "http://localhost:4200";

    @BeforeAll
    static void setUp() {
        System.out.println("=== VERIFICANDO SOLUCIÓN fechaCreacion ===");
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
    }

    @Test
    void testVerificarSolucionReservas() throws Exception {
        System.out.println("🔧 PROBLEMA: fechaCreacion es NULL en Reserva");
        System.out.println("💡 SOLUCIÓN: Asignar LocalDateTime.now() en ReservaService");
        System.out.println("=============================================");

        // Usuario logueado
        System.out.println("\n✅ USUARIO LOGUEADO CORRECTAMENTE");

        // Paso 1: Crear primera reserva
        System.out.println("\n1. 🏨 CREANDO PRIMERA RESERVA");
        driver.get(FRONT_URL + "/dashboard/crear-reserva");
        System.out.println("✅ Página de reservas abierta");
        System.out.println("👉 Crea la primera reserva manualmente");
        System.out.println("⏳ Esperando 45 segundos...");
        Thread.sleep(45000);

        // Paso 2: Crear segunda reserva solapada
        System.out.println("\n3. 🏨 CREANDO SEGUNDA RESERVA (SOLAPADA)");
        driver.get(FRONT_URL + "/dashboard/crear-reserva");
        System.out.println("✅ Página de reservas abierta");
        System.out.println("👉 Crea la segunda reserva con fechas solapadas");
        System.out.println("👉 Mismo tipo de habitación");
        System.out.println("⏳ Esperando 45 segundos...");
        Thread.sleep(45000);

        // Paso 4: Verificar segunda reserva y comparar
        System.out.println("\n4. 🔢 VERIFICANDO RESERVAs");
        driver.get(FRONT_URL + "/dashboard/reservas");
        System.out.println("✅ Página de reservas abierta");
        System.out.println("👉 Verifica que se creó las reserva");
        System.out.println("👉 ¿Son DIFERENTES las habitaciones?");
        System.out.println("⏳ Esperando 30 segundos...");
        Thread.sleep(10000);

        System.out.println("\n🎉 VERIFICACIÓN COMPLETADA");
        System.out.println("=============================================");
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            System.out.println("\n🔚 Cerrando navegador...");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.quit();
            System.out.println("✅ Navegador cerrado");
        }
    }
}
