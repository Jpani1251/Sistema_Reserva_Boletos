package com.example.ms_lugares.controller;

import com.example.ms_lugares.dto.AltaLugarRequest;
import com.example.ms_lugares.model.Lugar;
import com.example.ms_lugares.service.LugarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/lugares")
@CrossOrigin(origins = "*")
public class LugarController {

    @Autowired
    private LugarService lugarService;

    // 1. POST /lugares/alta_lugar (Solo administrador)
    @PostMapping("/alta_lugar")
    public ResponseEntity<Map<String, Object>> altaLugar(@RequestBody Map<String, Object> body) {
        Map<String, Object> respuesta = new HashMap<>();
        try {
            AltaLugarRequest request = new AltaLugarRequest();
            if (body.get("id_usuario") != null) {
                request.setIdUsuario(Integer.parseInt(body.get("id_usuario").toString()));
            }
            request.setToken((String) body.get("token"));
            request.setNombre((String) body.get("nombre"));
            request.setDireccion((String) body.get("direccion"));

            lugarService.registrarLugar(request);
            respuesta.put("mensaje", "OK");
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            respuesta.put("mensaje", "Error al dar de alta lugar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
        }
    }

    // 2. GET /lugares/obtiene_lugares
    @GetMapping("/obtiene_lugares")
    public ResponseEntity<?> obtieneLugares() {
        try {
            List<Lugar> lugares = lugarService.obtenerLugares();
            return ResponseEntity.ok(lugares);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("mensaje", "Error al obtener lugares: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}
