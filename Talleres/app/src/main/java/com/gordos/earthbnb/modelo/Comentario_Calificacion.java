package com.gordos.earthbnb.modelo;

public class Comentario_Calificacion {

    private String comentario;
    private double calificacion;
    private String nombre;

    public Comentario_Calificacion(){

    }

    public Comentario_Calificacion(String comentario, double calificacion, String nombre) {
        this.comentario = comentario;
        this.calificacion = calificacion;
        this.nombre = nombre;
    }

    public Comentario_Calificacion(double calificacion) {
        this.calificacion = calificacion;
    }

    public double getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(double calificacion) {
        this.calificacion = calificacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
