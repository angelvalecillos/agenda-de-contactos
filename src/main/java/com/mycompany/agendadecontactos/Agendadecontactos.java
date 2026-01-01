package com.mycompany.agendadecontactos;

/**
 *
 * @author angel
 */
public class Agendadecontactos {
public static void main(String[] args) {
        DatabaseManager.initDatabase();
        java.awt.EventQueue.invokeLater(() -> {
            new AgendaGUI().setVisible(true);
        });
    }
}

