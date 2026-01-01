package com.mycompany.agendadecontactos;

/**
 *
 * @author angel
 */

public class Dir {
    private int id;                //  id_direccion
    private String pais;
    private String direccion1;
    private String direccion2;
    private String codigoPostal;
    private String ciudad;
    private String provincia;
    private String poBox;
    private String label;

    public Dir(int id, String pais, String direccion1, String direccion2,
               String codigoPostal, String ciudad, String provincia,
               String poBox, String label) {
        this.id = id;
        this.pais = pais;
        this.direccion1 = direccion1;
        this.direccion2 = direccion2;
        this.codigoPostal = codigoPostal;
        this.ciudad = ciudad;
        this.provincia = provincia;
        this.poBox = poBox;
        this.label = label;
    }

    // Constructor sin id (nuevo registro)
    public Dir(String pais, String direccion1, String direccion2,
               String codigoPostal, String ciudad, String provincia,
               String poBox, String label) {
        this(0, pais, direccion1, direccion2, codigoPostal, ciudad, provincia, poBox, label);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public String getDireccion1() { return direccion1; }
    public void setDireccion1(String direccion1) { this.direccion1 = direccion1; }

    public String getDireccion2() { return direccion2; }
    public void setDireccion2(String direccion2) { this.direccion2 = direccion2; }

    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }

    public String getPoBox() { return poBox; }
    public void setPoBox(String poBox) { this.poBox = poBox; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    @Override
    public String toString() {
        return label + ": " + direccion1 +
                (direccion2.isEmpty() ? "" : ", " + direccion2) +
                " - " + ciudad + " (" + pais + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dir)) return false;
        Dir d = (Dir) o;
        return direccion1.equals(d.direccion1) &&
               ciudad.equals(d.ciudad) &&
               pais.equals(d.pais) &&
               label.equals(d.label);
    }

    @Override
    public int hashCode() {
        return direccion1.hashCode() + ciudad.hashCode() + pais.hashCode();
    }
}
