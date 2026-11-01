package com.example.ms_eventos.service.impl;

import com.example.ms_eventos.client.ComprasClient;
import com.example.ms_eventos.client.LugaresClient;
import com.example.ms_eventos.client.UsuarioClient;
import com.example.ms_eventos.model.Evento;
import com.example.ms_eventos.repository.EventoRepository;
import com.example.ms_eventos.service.EventoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

@Service
public class EventoServiceImpl implements EventoService {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UsuarioClient usuarioClient;

    @Autowired
    private LugaresClient lugaresClient;

    @Autowired
    private ComprasClient comprasClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void altaEvento(Map<String, Object> body) {
        Object idUsuario = body.get("id_usuario");
        Object token = body.get("token");

        Map<String, Object> auth = usuarioClient.verificarAcceso(idUsuario, token);
        if (auth == null || !Boolean.TRUE.equals(auth.get("acceso")) || !"administrador".equals(auth.get("rol"))) {
            throw new SecurityException("Acceso no autorizado: se requiere rol administrador");
        }

        String nombre = (String) body.get("nombre");
        String descripcion = (String) body.get("descripcion");
        int idLugar = Integer.parseInt(body.get("id_lugar").toString());
        double precioBoleto = Double.parseDouble(body.get("precio_boleto").toString());
        int cantidadBoletos = Integer.parseInt(body.get("cantidad_boletos").toString());
        String fotoBase64 = (String) body.get("foto");

        String fechaHoraStr = (String) body.get("fecha_hora_evento");
        if (fechaHoraStr == null && body.get("fecha") != null && body.get("hora") != null) {
            String hora = body.get("hora").toString();
            if (hora.length() == 5) hora += ":00";
            fechaHoraStr = body.get("fecha").toString() + " " + hora;
        }
        if (fechaHoraStr != null) {
            fechaHoraStr = fechaHoraStr.replace("T", " ");
            if (fechaHoraStr.length() == 16) fechaHoraStr += ":00";
        }
        Timestamp fechaHora = Timestamp.valueOf(fechaHoraStr);

        if (fotoBase64 != null && fotoBase64.contains(",")) {
            fotoBase64 = fotoBase64.substring(fotoBase64.indexOf(",") + 1);
        }
        byte[] fotoBytes = fotoBase64 != null ? Base64.getDecoder().decode(fotoBase64) : new byte[0];

        // 1er INSERT: tabla eventos
        Evento evento = new Evento(null, nombre, descripcion, idLugar, fechaHora, precioBoleto);
        int idEvento = eventoRepository.guardarEvento(evento);

        // 2do INSERT: tabla fotos_eventos
        eventoRepository.guardarFoto(fotoBytes, idEvento);

        // 3er paso: Invocar a compras/alta_boletos
        comprasClient.darAltaBoletos(idEvento, cantidadBoletos, idUsuario, token);
    }

    @Override
    public List<Map<String, Object>> consultarEventos(String palabra, Integer idUsuario, String token) {
        Map<String, Object> auth = usuarioClient.verificarAcceso(idUsuario, token);
        if (auth == null || !Boolean.TRUE.equals(auth.get("acceso"))) {
            throw new SecurityException("Acceso denegado");
        }

        List<Map<String, Object>> listaLugares = lugaresClient.obtenerLugares();
        Map<Integer, Map<String, Object>> mapaLugares = new HashMap<>();
        for (Map<String, Object> l : listaLugares) {
            if (l.get("id_lugar") != null) {
                int idL = ((Number) l.get("id_lugar")).intValue();
                mapaLugares.put(idL, l);
            }
        }

        String patron = "%" + (palabra != null ? palabra : "") + "%";
        List<Map<String, Object>> filas = eventoRepository.buscarEventosPorNombreODescripcion(patron);
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Map<String, Object> fila : filas) {
            Map<String, Object> item = new HashMap<>();
            int idLugar = ((Number) fila.get("id_lugar")).intValue();
            Map<String, Object> infoLugar = mapaLugares.getOrDefault(idLugar, new HashMap<>());

            String fechaHora = fila.get("fecha_hora_evento") != null ? fila.get("fecha_hora_evento").toString() : "";
            String[] partes = fechaHora.split(" ");
            String fecha = partes.length > 0 ? partes[0] : "";
            String hora = partes.length > 1 ? partes[1] : "";

            byte[] fotoBytes = (byte[]) fila.get("foto");
            String fotoBase64 = fotoBytes != null ? Base64.getEncoder().encodeToString(fotoBytes) : "";

            item.put("id_evento", fila.get("id_evento"));
            item.put("foto", fotoBase64);
            item.put("nombre", fila.get("nombre"));
            item.put("descripcion", fila.get("descripcion"));
            item.put("lugar", infoLugar.getOrDefault("nombre", "Desconocido"));
            item.put("direccion", infoLugar.getOrDefault("direccion", "Desconocida"));
            item.put("fecha", fecha);
            item.put("hora", hora);
            item.put("precio_boleto", fila.get("precio_boleto"));

            resultado.add(item);
        }

        return resultado;
    }
}
