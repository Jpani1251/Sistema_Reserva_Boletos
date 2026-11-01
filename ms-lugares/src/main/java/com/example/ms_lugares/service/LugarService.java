package com.example.ms_lugares.service;

import com.example.ms_lugares.dto.AltaLugarRequest;
import com.example.ms_lugares.model.Lugar;

import java.util.List;

public interface LugarService {
    void registrarLugar(AltaLugarRequest request);
    List<Lugar> obtenerLugares();
}
