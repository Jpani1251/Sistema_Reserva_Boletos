package com.example.ms_eventos.repository.impl;

import com.example.ms_eventos.model.Evento;
import com.example.ms_eventos.repository.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class EventoRepositoryImpl implements EventoRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public int guardarEvento(Evento evento) {
        String sql = "INSERT INTO eventos (nombre, descripcion, id_lugar, fecha_hora_evento, precio_boleto) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, evento.getNombre());
            ps.setString(2, evento.getDescripcion());
            ps.setInt(3, evento.getIdLugar());
            ps.setTimestamp(4, evento.getFechaHoraEvento());
            ps.setDouble(5, evento.getPrecioBoleto());
            return ps;
        }, keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    @Override
    public void guardarFoto(byte[] foto, int idEvento) {
        String sql = "INSERT INTO fotos_eventos (foto, id_evento) VALUES (?, ?)";
        jdbcTemplate.update(sql, foto, idEvento);
    }

    @Override
    public List<Map<String, Object>> buscarEventosPorNombreODescripcion(String patron) {
        String sql = "SELECT e.id_evento, e.nombre, e.descripcion, e.id_lugar, e.fecha_hora_evento, e.precio_boleto, f.foto " +
                     "FROM eventos e LEFT JOIN fotos_eventos f ON e.id_evento = f.id_evento " +
                     "WHERE e.nombre LIKE ? OR e.descripcion LIKE ?";
        return jdbcTemplate.queryForList(sql, patron, patron);
    }
}
