package com.mycompany.agendadecontactos;

import java.sql.*;

/**
 *
 * @author angel
 */

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:agenda.db";

    public static Connection getConnection() throws SQLException {
        try {
            // Forzamos a cargar el driver
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver JDBC de SQLite", e);
        }
        return DriverManager.getConnection(URL);
    }

    // Crear tabla si no existe
    public static void initDatabase() {
    try (Connection conn = getConnection();
         Statement stmt = conn.createStatement()) {

        // Crear tabla Persona
        String sqlPersona = """
            CREATE TABLE IF NOT EXISTS Persona (
                id_persona INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                apellido TEXT NOT NULL,
                fecha_nac DATE,
                empresa TEXT,
                puesto TEXT
            )
        """;

        stmt.execute(sqlPersona);
        
        String sqlTelefono = """
        CREATE TABLE IF NOT EXISTS telefono (
                id_telefono INTEGER PRIMARY KEY AUTOINCREMENT,
                id_persona  INTEGER,
                numero      VARCHAR(20),
                label       VARCHAR(20),
                FOREIGN KEY (id_persona) REFERENCES persona(id_persona) ON DELETE CASCADE
            )
        """;

        stmt.execute(sqlTelefono);

                String sqlCorreo = """
        CREATE TABLE IF NOT EXISTS correo (
                id_correo INTEGER PRIMARY KEY AUTOINCREMENT,
                id_persona  INTEGER,
                correo      VARCHAR(30),
                label       VARCHAR(20),
                FOREIGN KEY (id_persona) REFERENCES persona(id_persona) ON DELETE CASCADE
            )
        """;

        stmt.execute(sqlCorreo);
        
        String sqlDireccion = """
        CREATE TABLE IF NOT EXISTS direccion (
                id_direccion INTEGER PRIMARY KEY AUTOINCREMENT,
                id_persona  INTEGER,
                Pais      VARCHAR(20),
                direccion      VARCHAR(40),
                direccion2      VARCHAR(40),
                codigoPostal      VARCHAR(10),
                ciudad      VARCHAR(20),
                provincia      VARCHAR(20),
                poBox      VARCHAR(40),
                label       VARCHAR(20),
                FOREIGN KEY (id_persona) REFERENCES persona(id_persona) ON DELETE CASCADE
            )
        """;

        stmt.execute(sqlDireccion);

        
        System.out.println("✅ Tabla Persona lista y conexión funcionando");
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

}