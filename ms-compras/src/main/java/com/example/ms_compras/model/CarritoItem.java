package com.example.ms_compras.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CarritoItem {
    @JsonProperty("id_usuario")
    private Integer idUsuario;

    @JsonProperty("id_evento")
    private Integer idEvento;

    @JsonProperty("cantidad_boletos")
    private Integer cantidadBoletos;

    public CarritoItem() {
    }

    public CarritoItem(Integer idUsuario, Integer idEvento, Integer cantidadBoletos) {
        this.idUsuario = idUsuario;
        this.idEvento = idEvento;
        this.cantidadBoletos = cantidadBoletos;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(Integer idEvento) {
        this.idEvento = idEvento;
    }

    public Integer getCantidadBoletos() {
        return cantidadBoletos;
    }

    public void setCantidadBoletos(Integer cantidadBoletos) {
        this.cantidadBoletos = cantidadBoletos;
    }
}
