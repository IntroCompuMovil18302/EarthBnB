package com.gordos.earthbnb.modelo;

public class Reserva {

    private long fechaInicio;
    private long fechaFin;
    private String idUsuario;

    public Reserva(long fechaInicio, long fechaFin, String idUsuario) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.idUsuario= idUsuario;
    }

    public Reserva() {}

    public long getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(long fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public long getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(long fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }
}
