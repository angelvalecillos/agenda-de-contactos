package com.mycompany.agendadecontactos;

/**
 *
 * @author angel
 */

public class Correo {
    private int id;         // id_correo
    private String correo;
    private String label;

    public Correo(int id, String correo, String label) {
        this.id = id;
        this.correo = correo;
        this.label = label;
    }

    // Constructor sin id (nuevo correo)
    public Correo(String correo, String label) {
        this(0, correo, label);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    @Override
    public String toString() {
        return label + ": " + correo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Correo)) return false;
        Correo c = (Correo) o;
        return correo.equals(c.correo) && label.equals(c.label);
    }

    @Override
    public int hashCode() {
        return correo.hashCode() + label.hashCode();
    }
}
