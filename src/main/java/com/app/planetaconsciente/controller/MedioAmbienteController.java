package com.app.planetaconsciente.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MedioAmbienteController {

    @GetMapping("/medio_ambiente")
    public String mostrarIndexConForoYCapacitaciones() {
        return "medioambiente/medio_ambiente";
    }
}
