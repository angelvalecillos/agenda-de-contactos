package com.mycompany.agendadecontactos;

import java.util.ArrayList;
import java.util.List;

public class Persona {
    private int id;
    private String nombre;
    private String apellido;
    private String fechaNac;
    private String empresa;
    private String puesto;
    private List<Telefono> telefonos = new ArrayList<>();

    public Persona(int id, String nombre, String apellido, String fechaNac, String empresa, String puesto) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaNac = fechaNac;
        this.empresa = empresa;
        this.puesto = puesto;
    }

    public Persona(String nombre, String apellido, String fechaNac, String empresa, String puesto) {
        this(-1, nombre, apellido, fechaNac, empresa, puesto);
    }

    // getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getFechaNac() { return fechaNac; }
    public void setFechaNac(String fechaNac) { this.fechaNac = fechaNac; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public String getPuesto() { return puesto; }
    public void setPuesto(String puesto) { this.puesto = puesto; }

    public List<Telefono> getTelefonos() { return telefonos; }
    public void setTelefonos(List<Telefono> telefonos) { this.telefonos = telefonos; }
    public void addTelefono(Telefono t) { this.telefonos.add(t); }

    @Override
    public String toString() {
        return nombre + " " + apellido;
    }
}
