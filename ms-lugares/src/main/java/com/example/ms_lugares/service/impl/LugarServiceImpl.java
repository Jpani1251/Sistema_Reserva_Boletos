package com.example.ms_lugares.service.impl;

import com.example.ms_lugares.client.UsuarioAuthClient;
import com.example.ms_lugares.dto.AltaLugarRequest;
import com.example.ms_lugares.model.Lugar;
import com.example.ms_lugares.repository.LugarRepository;
import com.example.ms_lugares.service.LugarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class LugarServiceImpl implements LugarService {

    @Autowired
    private LugarRepository lugarRepository;

    @Autowired
    private UsuarioAuthClient authClient;

    @Override
    public void registrarLugar(AltaLugarRequest request) {
        Map<String, Object> auth = authClient.verificarAcceso(request.getIdUsuario(), request.getToken());
        if (auth == null || !Boolean.TRUE.equals(auth.get("acceso")) || !"administrador".equals(auth.get("rol"))) {
            throw new SecurityException("Acceso no autorizado: se requiere rol administrador");
        }

        if (request.getNombre() == null || request.getDireccion() == null) {
            throw new IllegalArgumentException("Nombre y dirección son campos obligatorios");
        }

        Lugar lugar = new Lugar();
        lugar.setNombre(request.getNombre());
        lugar.setDireccion(request.getDireccion());

        lugarRepository.guardar(lugar);
    }

    @Override
    public List<Lugar> obtenerLugares() {
        return lugarRepository.obtenerTodos();
    }
}
