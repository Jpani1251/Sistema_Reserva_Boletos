package com.example.ms_compras.controller;

import com.example.ms_compras.model.CarritoItem;
import com.example.ms_compras.service.ComprasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/compras")
@CrossOrigin(origins = "*")
public class ComprasController {

    @Autowired
    private ComprasService comprasService;

    // 1. POST /compras/alta_boletos
    @PostMapping("/alta_boletos")
    public ResponseEntity<Map<String, Object>> altaBoletos(@RequestBody Map<String, Object> body) {
        Map<String, Object> respuesta = new HashMap<>();
        try {
            comprasService.altaBoletos(body);
            respuesta.put("mensaje", "OK");
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            respuesta.put("mensaje", "Error en alta_boletos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
        }
    }

    // 2. PUT /compras/compra_boletos
    @PutMapping("/compra_boletos")
    public ResponseEntity<Map<String, Object>> compraBoletos(@RequestBody Map<String, Object> body) {
        Map<String, Object> respuesta = new HashMap<>();
        try {
            comprasService.comprarBoletos(body);
            respuesta.put("mensaje", "OK");
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            respuesta.put("mensaje", "Error al comprar boletos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
        }
    }

    // 3. DELETE /compras/elimina_evento_carrito_compra
    @DeleteMapping("/elimina_evento_carrito_compra")
    public ResponseEntity<Map<String, Object>> eliminaEventoCarrito(
            @RequestParam(required = false) Integer id_usuario,
            @RequestParam(required = false) Integer id_evento,
            @RequestParam(required = false) String token,
            @RequestBody(required = false) Map<String, Object> body) {

        Map<String, Object> respuesta = new HashMap<>();
        try {
            comprasService.eliminarEventoDelCarrito(id_usuario, id_evento, token, body);
            respuesta.put("mensaje", "OK");
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            respuesta.put("mensaje", "Error al eliminar evento del carrito: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
        }
    }

    // 4. DELETE /compras/elimina_carrito_compra
    @DeleteMapping("/elimina_carrito_compra")
    public ResponseEntity<Map<String, Object>> eliminaCarrito(
            @RequestParam(required = false) Integer id_usuario,
            @RequestParam(required = false) String token,
            @RequestBody(required = false) Map<String, Object> body) {

        Map<String, Object> respuesta = new HashMap<>();
        try {
            comprasService.vaciarCarrito(id_usuario, token, body);
            respuesta.put("mensaje", "OK");
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            respuesta.put("mensaje", "Error al vaciar carrito: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
        }
    }

    // 5. GET /compras/obtiene_carrito
    @GetMapping("/obtiene_carrito")
    public ResponseEntity<?> obtieneCarrito(@RequestParam Integer id_usuario, @RequestParam String token) {
        try {
            List<CarritoItem> items = comprasService.obtenerCarrito(id_usuario, token);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("mensaje", e.getMessage() != null && e.getMessage().contains("denegado") ? "Acceso denegado" : "Error al obtener carrito: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
    }
}
