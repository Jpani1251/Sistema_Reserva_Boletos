package com.example.ms_lugares.repository.impl;

import com.example.ms_lugares.model.Lugar;
import com.example.ms_lugares.repository.LugarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LugarRepositoryImpl implements LugarRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Lugar> lugarRowMapper = (rs, rowNum) -> {
        Lugar lugar = new Lugar();
        lugar.setIdLugar(rs.getInt("id_lugar"));
        lugar.setNombre(rs.getString("nombre"));
        lugar.setDireccion(rs.getString("direccion"));
        return lugar;
    };

    @Override
    public void guardar(Lugar lugar) {
        String sql = "INSERT INTO lugares (nombre, direccion) VALUES (?, ?)";
        jdbcTemplate.update(sql, lugar.getNombre(), lugar.getDireccion());
    }

    @Override
    public List<Lugar> obtenerTodos() {
        String sql = "SELECT id_lugar, nombre, direccion FROM lugares";
        return jdbcTemplate.query(sql, lugarRowMapper);
    }
}
