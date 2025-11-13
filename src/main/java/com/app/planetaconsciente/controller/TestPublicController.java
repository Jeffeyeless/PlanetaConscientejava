package com.app.planetaconsciente.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
public class TestPublicController {

    @GetMapping("/clasificacion")
    public String testClasificacion(@RequestParam double huella) {
        // Copia exacta de tu método determinarClasificacion
        if (huella < 3000)
            return "Baja (Ecológica)";
        if (huella <= 6000)
            return "Media (Promedio)";
        if (huella <= 10000)
            return "Alta (Necesita mejorar)";
        return "Muy Alta (Impacto significativo)";
    }
    
    @GetMapping("/health")
    public String healthCheck() {
        return "TestPublicController funcionando correctamente";
    }
}