package com.app.planetaconsciente.controller;

import com.app.planetaconsciente.model.Capacitacion;
import com.app.planetaconsciente.service.CapacitacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Controller
@RequestMapping("/capacitaciones")
public class CapacitacionController {

    @Autowired
    private CapacitacionService capacitacionService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping
    public String listarCapacitaciones(Model model) {
        model.addAttribute("capacitaciones", capacitacionService.listarTodas());
        return "medioambiente/capacitaciones";
    }

    @PostMapping("/store")
    public String guardarCapacitacion(@RequestParam("titulo") String titulo,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("material") MultipartFile archivo) throws IOException {

        if (!archivo.isEmpty()) {
            String nombreArchivo = UUID.randomUUID() + "_" + archivo.getOriginalFilename();
            Path ruta = Paths.get(uploadDir).resolve(nombreArchivo);
            Files.copy(archivo.getInputStream(), ruta, StandardCopyOption.REPLACE_EXISTING);

            String tipo = archivo.getContentType();

            Capacitacion cap = new Capacitacion();
            cap.setTitulo(titulo);
            cap.setDescripcion(descripcion);
            cap.setUrlMaterial("/uploads/" + nombreArchivo);
            cap.setEsImagen(tipo != null && tipo.startsWith("image"));
            cap.setEsVideo(tipo != null && tipo.startsWith("video"));
            capacitacionService.guardar(cap);
        }

        return "redirect:/capacitaciones";
    }
}
