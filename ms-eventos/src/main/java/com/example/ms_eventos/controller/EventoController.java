package com.example.ms_eventos.controller;

import com.example.ms_eventos.service.EventoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/eventos")
@CrossOrigin(origins = "*")
public class EventoController {

    @Autowired
    private EventoService eventoService;

    // 1. POST /eventos/alta_evento
    @PostMapping("/alta_evento")
    public ResponseEntity<Map<String, Object>> altaEvento(@RequestBody Map<String, Object> body) {
        Map<String, Object> respuesta = new HashMap<>();
        try {
            eventoService.altaEvento(body);
            respuesta.put("mensaje", "OK");
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            respuesta.put("mensaje", "Error al dar de alta evento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
        }
    }

    // 2. GET /eventos/consulta_eventos
    @GetMapping("/consulta_eventos")
    public ResponseEntity<?> consultaEventos(
            @RequestParam(required = false, defaultValue = "") String palabra,
            @RequestParam(required = false) Integer id_usuario,
            @RequestParam(required = false) String token) {
        try {
            return ResponseEntity.ok(eventoService.consultarEventos(palabra, id_usuario, token));
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("mensaje", e.getMessage() != null && e.getMessage().contains("denegado") ? "Acceso denegado" : "Error al consultar eventos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
    }
}
