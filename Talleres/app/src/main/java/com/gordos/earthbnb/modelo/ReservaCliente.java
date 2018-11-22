package com.gordos.earthbnb.modelo;

public class ReservaCliente {

    String idAlojamiento;
    String idReserva;
    String descripcion;
    String tipo;
    String fecha;

    public ReservaCliente() {}

    public ReservaCliente(String idAlojamiento, String idReserva, String descripcion, String tipo, String fecha) {
        this.idAlojamiento = idAlojamiento;
        this.idReserva = idReserva;
        this.tipo= tipo;
        this.descripcion= descripcion;
        this.fecha= fecha;
    }

    public String getIdAlojamiento() {
        return idAlojamiento;
    }

    public void setIdAlojamiento(String idAlojamiento) {
        this.idAlojamiento = idAlojamiento;
    }

    public String getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(String idReserva) {
        this.idReserva = idReserva;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
