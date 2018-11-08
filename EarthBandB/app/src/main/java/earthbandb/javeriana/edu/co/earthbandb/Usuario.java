package earthbandb.javeriana.edu.co.earthbandb;

public class Usuario {
    private String nombre;
    private String apellido;
    private String pais;
    private String ciudad;
    private String username;
    private String email;
    private String idUser;
    private String tipo;

    Usuario(String nombre, String apellido, String email, String idUser,String username,String tipo) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.idUser = idUser;
        this.username=username;
        this.tipo=tipo;
    }
    Usuario(){

    }

    public String getTipo() { return tipo; }

    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

}

