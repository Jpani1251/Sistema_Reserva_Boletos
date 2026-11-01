package com.example.ms_compras.service.impl;

import com.example.ms_compras.client.UsuarioClient;
import com.example.ms_compras.model.CarritoItem;
import com.example.ms_compras.repository.ComprasRepository;
import com.example.ms_compras.service.ComprasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ComprasServiceImpl implements ComprasService {

    @Autowired
    private ComprasRepository comprasRepository;

    @Autowired
    private UsuarioClient usuarioClient;

    private void validarAcceso(Object idUsuario, Object token) {
        Map<String, Object> auth = usuarioClient.verificarAcceso(idUsuario, token);
        if (auth == null || !Boolean.TRUE.equals(auth.get("acceso"))) {
            throw new SecurityException("Acceso denegado");
        }
    }

    private void validarAccesoAdmin(Object idUsuario, Object token) {
        Map<String, Object> auth = usuarioClient.verificarAcceso(idUsuario, token);
        if (auth == null || !Boolean.TRUE.equals(auth.get("acceso")) || !"administrador".equals(auth.get("rol"))) {
            throw new SecurityException("Acceso no autorizado: se requiere rol administrador");
        }
    }

    @Override
    public void altaBoletos(Map<String, Object> body) {
        Object idUsuario = body.get("id_usuario");
        Object token = body.get("token");
        int idEvento = Integer.parseInt(body.get("id_evento").toString());
        Object cantObj = body.get("cantidad_boletos") != null ? body.get("cantidad_boletos") : body.get("boletos_disponibles");
        int cantidadBoletos = Integer.parseInt(cantObj.toString());

        validarAccesoAdmin(idUsuario, token);
        comprasRepository.guardarBoletos(idEvento, cantidadBoletos);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void comprarBoletos(Map<String, Object> body) {
        int idUsuario = Integer.parseInt(body.get("id_usuario").toString());
        String token = (String) body.get("token");
        int idEvento = Integer.parseInt(body.get("id_evento").toString());
        int cantidadComprar = Integer.parseInt(body.get("cantidad_boletos").toString());

        validarAcceso(idUsuario, token);

        Optional<Integer> dispOpt = comprasRepository.obtenerBoletosDisponibles(idEvento);
        if (dispOpt.isEmpty()) {
            throw new IllegalArgumentException("El evento no tiene boletos registrados");
        }

        int disponibles = dispOpt.get();
        if (cantidadComprar > disponibles) {
            throw new IllegalArgumentException("No hay suficientes boletos");
        }

        Optional<Integer> enCarritoOpt = comprasRepository.obtenerCantidadEnCarrito(idUsuario, idEvento);
        if (enCarritoOpt.isEmpty()) {
            comprasRepository.agregarAlCarrito(idUsuario, idEvento, cantidadComprar);
        } else {
            comprasRepository.actualizarCarrito(idUsuario, idEvento, cantidadComprar);
        }

        comprasRepository.decrementarBoletos(idEvento, cantidadComprar);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void eliminarEventoDelCarrito(Integer idUsuario, Integer idEvento, String token, Map<String, Object> body) {
        if (body != null) {
            if (idUsuario == null && body.get("id_usuario") != null) idUsuario = Integer.parseInt(body.get("id_usuario").toString());
            if (idEvento == null && body.get("id_evento") != null) idEvento = Integer.parseInt(body.get("id_evento").toString());
            if (token == null) token = (String) body.get("token");
        }

        validarAcceso(idUsuario, token);

        Optional<Integer> enCarritoOpt = comprasRepository.obtenerCantidadEnCarrito(idUsuario, idEvento);
        if (enCarritoOpt.isPresent()) {
            int cant = enCarritoOpt.get();
            comprasRepository.incrementarBoletos(idEvento, cant);
            comprasRepository.eliminarEventoDelCarrito(idUsuario, idEvento);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void vaciarCarrito(Integer idUsuario, String token, Map<String, Object> body) {
        if (body != null) {
            if (idUsuario == null && body.get("id_usuario") != null) idUsuario = Integer.parseInt(body.get("id_usuario").toString());
            if (token == null) token = (String) body.get("token");
        }

        validarAcceso(idUsuario, token);

        List<CarritoItem> items = comprasRepository.obtenerCarritoPorUsuario(idUsuario);
        for (CarritoItem item : items) {
            comprasRepository.incrementarBoletos(item.getIdEvento(), item.getCantidadBoletos());
        }

        comprasRepository.vaciarCarrito(idUsuario);
    }

    @Override
    public List<CarritoItem> obtenerCarrito(Integer idUsuario, String token) {
        validarAcceso(idUsuario, token);
        return comprasRepository.obtenerCarritoPorUsuario(idUsuario);
    }
}
