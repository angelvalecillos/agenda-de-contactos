package com.mycompany.agendadecontactos;

/**
 *
 * @author angel
 */
public class Contacto {
private int id;
    private String nombre;
    private String telefono;
    private String email;

    public Contacto(int id, String nombre, String telefono, String email) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
    }

    public Contacto(String nombre, String telefono, String email) {
        this(-1, nombre, telefono, email); // -1 cuando aún no está en BD
    }

    // Getters y Setters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public void setId(int id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return nombre + " - " + telefono + " - " + email;
    }
}