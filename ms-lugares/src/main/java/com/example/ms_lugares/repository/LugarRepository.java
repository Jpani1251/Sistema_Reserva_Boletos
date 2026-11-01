package com.example.ms_lugares.repository;

import com.example.ms_lugares.model.Lugar;
import java.util.List;

public interface LugarRepository {
    void guardar(Lugar lugar);
    List<Lugar> obtenerTodos();
}
