package com.gordos.earthbnb.modelo;

public class ReservaCliente {

    String idAlojamiento;
    String idReserva;

    public ReservaCliente() {}

    public ReservaCliente(String idAlojamiento, String idReserva) {
        this.idAlojamiento = idAlojamiento;
        this.idReserva = idReserva;
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
}
