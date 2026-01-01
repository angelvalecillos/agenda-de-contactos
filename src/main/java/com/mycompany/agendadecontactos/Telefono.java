package com.mycompany.agendadecontactos;

/**
 *
 * @author angel
 */
public class Telefono {
    private int id;
    private String numero;
    private String label;

    
    public Telefono(int id, String numero, String label) {
        this.id =id;
        this.numero = numero;
        this.label = label;
    }


    // Para crear un telefono nuevo sin id todavía
    public Telefono(String numero, String label) {
        this(0, numero, label);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    @Override
    public String toString() {
        return label + ": " + numero;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Telefono)) return false;
        Telefono t = (Telefono) o;
        return numero.equals(t.numero) && label.equals(t.label);
    }

    @Override
    public int hashCode() {
        return numero.hashCode() + label.hashCode();
    }
}