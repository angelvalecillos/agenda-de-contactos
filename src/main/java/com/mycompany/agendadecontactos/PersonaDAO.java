package com.mycompany.agendadecontactos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonaDAO {

    // insertar persona y setear id en el objeto Persona
    public int insertar(Persona persona) {
    String sql = "INSERT INTO persona (nombre, apellido, fecha_nac, empresa, puesto) VALUES (?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        
        stmt.setString(1, persona.getNombre());
        stmt.setString(2, persona.getApellido());
        stmt.setString(3, persona.getFechaNac());
        stmt.setString(4, persona.getEmpresa());
        stmt.setString(5, persona.getPuesto());
        
        stmt.executeUpdate();

        try (ResultSet rs = stmt.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1); // id_persona generado
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
       System.out.println("AAAAAAA "+e.toString());
    }
    return -1; // error
}
    public List<Persona> obtenerTodas() {
        List<Persona> lista = new ArrayList<>();
        String sql = "SELECT * FROM Persona";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Persona(
                        rs.getInt("id_persona"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("fecha_nac"),
                        rs.getString("empresa"),
                        rs.getString("puesto")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
    public void actualizar(Persona persona) {
        String sql = "UPDATE Persona SET nombre=?, apellido=?, fecha_nac=?, empresa=?, puesto=? WHERE id_persona=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, persona.getNombre());
            pstmt.setString(2, persona.getApellido());
            pstmt.setString(3, persona.getFechaNac());
            pstmt.setString(4, persona.getEmpresa());
            pstmt.setString(5, persona.getPuesto());
            pstmt.setInt(6, persona.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void eliminar(int id) {
        String sql = "DELETE FROM Persona WHERE id_persona=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public Persona buscarPersona(String nombre, String apellido) {
    String sql = "SELECT * FROM persona WHERE nombre = ? AND apellido = ?";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, nombre);
        stmt.setString(2, apellido);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return new Persona(
                        rs.getInt("id_persona"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("fecha_nac"),
                        rs.getString("empresa"),
                        rs.getString("puesto")
                );
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}

// telefono
public int insertarTelefono(int idPersona, String numero, String label) {
    String sql = "INSERT INTO telefono (id_persona, numero, label) VALUES (?, ?, ?)";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setInt(1, idPersona);
        stmt.setString(2, numero);
        stmt.setString(3, label);
        stmt.executeUpdate();
        try (ResultSet rs = stmt.getGeneratedKeys()) {
            if (rs.next()) return rs.getInt(1);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return -1;
}
public List<Telefono> obtenerTelefonos(int idPersona) {
    List<Telefono> telefonos = new ArrayList<>();
    String sql = "SELECT id_telefono, numero, label FROM telefono WHERE id_persona = ?";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idPersona);
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                telefonos.add(new Telefono(
                        rs.getInt("id_telefono"),
                        rs.getString("numero"),
                        rs.getString("label")
                ));
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return telefonos;
}
public void eliminarTelefonosDePersona(int idPersona) {
    String sql = "DELETE FROM telefono WHERE id_persona=?";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idPersona);
        stmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
public void updateTelefono(int idTelefono, String numero, String label) {
    String sql = "UPDATE telefono SET numero=?, label=? WHERE id_telefono=?";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, numero);
        stmt.setString(2, label);
        stmt.setInt(3, idTelefono);
        stmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
public void eliminarTelefonoPorId(int idTelefono) {
    String sql = "DELETE FROM telefono WHERE id_telefono = ?";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idTelefono);
        stmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
//correo
public int insertarCorreo(int idPersona, String correo, String label) {
    String sql = "INSERT INTO correo (id_persona, correo, label) VALUES (?, ?, ?)";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setInt(1, idPersona);
        stmt.setString(2, correo);
        stmt.setString(3, label);
        stmt.executeUpdate();
        try (ResultSet rs = stmt.getGeneratedKeys()) {
            if (rs.next()) return rs.getInt(1);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return -1;
}
public List<Correo> obtenerCorreos(int idPersona) {
    List<Correo> Correos = new ArrayList<>();
    String sql = "SELECT id_correo, correo, label FROM correo WHERE id_persona = ?";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idPersona);
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Correos.add(new Correo(
                        rs.getInt("id_correo"),
                        rs.getString("correo"),
                        rs.getString("label")
                ));
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return Correos;
}
public void eliminarCorreosDePersona(int idPersona) {
    String sql = "DELETE FROM correo WHERE id_persona=?";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idPersona);
        stmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
public void updateCorreo(int idCorreo, String correo, String label) {
    String sql = "UPDATE correo SET correo=?, label=? WHERE id_correo=?";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, correo);
        stmt.setString(2, label);
        stmt.setInt(3, idCorreo);
        stmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
public void eliminarCorreoPorId(int idCorreo) {
    String sql = "DELETE FROM correo WHERE id_correo = ?";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idCorreo);
        stmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
//direccion
public int insertarDir(int idPersona, Dir dir) {
    String sql = "INSERT INTO direccion (id_persona, Pais, direccion, direccion2, codigoPostal, ciudad, provincia, poBox, label) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setInt(1, idPersona);
        stmt.setString(2, dir.getPais());
        stmt.setString(3, dir.getDireccion1());
        stmt.setString(4, dir.getDireccion2());
        stmt.setString(5, dir.getCodigoPostal());
        stmt.setString(6, dir.getCiudad());
        stmt.setString(7, dir.getProvincia());
        stmt.setString(8, dir.getPoBox());
        stmt.setString(9, dir.getLabel());
        stmt.executeUpdate();
        try (ResultSet rs = stmt.getGeneratedKeys()) {
            if (rs.next()) return rs.getInt(1);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return -1;
}
public List<Dir> obtenerDirs(int idPersona) {
    List<Dir> dirs = new ArrayList<>();
    String sql = "SELECT id_direccion, Pais, direccion, direccion2, codigoPostal, ciudad, provincia, poBox, label " +
                 "FROM direccion WHERE id_persona = ?";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idPersona);
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                dirs.add(new Dir(
                        rs.getInt("id_direccion"),
                        rs.getString("Pais"),
                        rs.getString("direccion"),
                        rs.getString("direccion2"),
                        rs.getString("codigoPostal"),
                        rs.getString("ciudad"),
                        rs.getString("provincia"),
                        rs.getString("poBox"),
                        rs.getString("label")
                ));
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return dirs;
}
public void eliminarDirsDePersona(int idPersona) {
    String sql = "DELETE FROM direccion WHERE id_persona=?";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idPersona);
        stmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
public void updateDireccion(int idDireccion, Dir dir) {
    String sql = "UPDATE direccion SET Pais=?, direccion=?, direccion2=?, codigoPostal=?, ciudad=?, provincia=?, poBox=?, label=? " +
                 "WHERE id_direccion=?";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, dir.getPais());
        stmt.setString(2, dir.getDireccion1());
        stmt.setString(3, dir.getDireccion2());
        stmt.setString(4, dir.getCodigoPostal());
        stmt.setString(5, dir.getCiudad());
        stmt.setString(6, dir.getProvincia());
        stmt.setString(7, dir.getPoBox());
        stmt.setString(8, dir.getLabel());
        stmt.setInt(9, idDireccion);
        stmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
public void eliminarDirPorId(int idDireccion) {
    String sql = "DELETE FROM direccion WHERE id_direccion = ?";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idDireccion);
        stmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

}
