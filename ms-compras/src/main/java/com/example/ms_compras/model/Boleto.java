package com.example.ms_compras.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Boleto {
    @JsonProperty("id_evento")
    private Integer idEvento;

    @JsonProperty("boletos_disponibles")
    private Integer boletosDisponibles;

    public Boleto() {
    }

    public Boleto(Integer idEvento, Integer boletosDisponibles) {
        this.idEvento = idEvento;
        this.boletosDisponibles = boletosDisponibles;
    }

    public Integer getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(Integer idEvento) {
        this.idEvento = idEvento;
    }

    public Integer getBoletosDisponibles() {
        return boletosDisponibles;
    }

    public void setBoletosDisponibles(Integer boletosDisponibles) {
        this.boletosDisponibles = boletosDisponibles;
    }
}
