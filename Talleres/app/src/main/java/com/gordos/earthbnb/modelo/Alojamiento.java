package com.gordos.earthbnb.modelo;

public class Alojamiento {

    private double latitude;
    private double longitude;
    private String tipo;
    private int huespedes;
    private int habitaciones;
    private int camas;
    private int banos;
    private boolean banoPrivado;
    private boolean elementosBasicos;
    private boolean wifi;
    private boolean tv;
    private boolean armario;
    private boolean escritorio;
    private boolean desayuno;
    private boolean accesoCocina;
    private String idUsuario;
    private String urlFotos;
    private long fechaInicio;
    private long fechaFin;
    private String descripcion;
    private String idAlojamiento;
    private int precio;
    private double calificacion;

    public Alojamiento() {
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getHuespedes() {
        return huespedes;
    }

    public void setHuespedes(int huespedes) {
        this.huespedes = huespedes;
    }

    public int getHabitaciones() {
        return habitaciones;
    }

    public void setHabitaciones(int habitaciones) {
        this.habitaciones = habitaciones;
    }

    public int getCamas() {
        return camas;
    }

    public void setCamas(int camas) {
        this.camas = camas;
    }

    public int getBanos() {
        return banos;
    }

    public void setBanos(int banos) {
        this.banos = banos;
    }

    public boolean isBanoPrivado() {
        return banoPrivado;
    }

    public void setBanoPrivado(boolean banoPrivado) {
        this.banoPrivado = banoPrivado;
    }

    public boolean isElementosBasicos() {
        return elementosBasicos;
    }

    public void setElementosBasicos(boolean elementosBasicos) {
        this.elementosBasicos = elementosBasicos;
    }

    public boolean isWifi() {
        return wifi;
    }

    public void setWifi(boolean wifi) {
        this.wifi = wifi;
    }

    public boolean isTv() {
        return tv;
    }

    public void setTv(boolean tv) {
        this.tv = tv;
    }

    public boolean isArmario() {
        return armario;
    }

    public void setArmario(boolean armario) {
        this.armario = armario;
    }

    public boolean isEscritorio() {
        return escritorio;
    }

    public void setEscritorio(boolean escritorio) {
        this.escritorio = escritorio;
    }

    public boolean isDesayuno() {
        return desayuno;
    }

    public void setDesayuno(boolean desayuno) {
        this.desayuno = desayuno;
    }

    public boolean isAccesoCocina() {
        return accesoCocina;
    }

    public void setAccesoCocina(boolean accesoCocina) {
        this.accesoCocina = accesoCocina;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUrlFotos() {
        return urlFotos;
    }

    public void setUrlFotos(String urlFotos) {
        this.urlFotos = urlFotos;
    }

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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getPrecio() {
        return precio;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }
    
    public String getIdAlojamiento() {
        return idAlojamiento;
    }

    public void setIdAlojamiento(String idAlojamiento) {
        this.idAlojamiento = idAlojamiento;
    }

    public double getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(double calificacion) {
        this.calificacion = calificacion;
    }
}
