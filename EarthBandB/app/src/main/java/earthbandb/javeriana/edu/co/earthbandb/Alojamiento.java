package earthbandb.javeriana.edu.co.earthbandb;

public class Alojamiento {
    private String valor;
    private String descripcion;
    private String tipo;
    private String ciudad;
    private String huespedes;
    private String idOwner;
    private String fecha;
    private double latitud;
    private double longitud;

    Alojamiento(String valor, String descripcion, String tipo, String ciudad, String huespedes, String idOwner, String fecha, double latitud, double longitud) {
        this.valor = valor;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.ciudad = ciudad;
        this.huespedes = huespedes;
        this.idOwner = idOwner;
        this.fecha = fecha;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public String getIdOwner() {
        return idOwner;
    }

    public void setIdOwner(String idOwner) {
        this.idOwner = idOwner;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
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

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getHuespedes() {
        return huespedes;
    }

    public void setHuespedes(String huespedes) {
        this.huespedes = huespedes;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }
}
