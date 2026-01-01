package com.mycompany.agendadecontactos;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;
import javax.swing.JTextField;

public class Validaciones {

    // === EVENTOS DE TECLADO ===
    public void permitirSoloLetras(KeyEvent evt) {
        char c = evt.getKeyChar();
        if (!Character.isLetter(c) && c != ' ') {
            evt.consume();
            Toolkit.getDefaultToolkit().beep();
        }
    }

    public void permitirSoloNumeros(KeyEvent evt) {
        char c = evt.getKeyChar();
        if (!Character.isDigit(c)) {
            evt.consume();
            Toolkit.getDefaultToolkit().beep();
        }
    }

    // === VALIDACIONES DE TEXTO ===
    private static final Pattern PATRON_EMAIL = 
        Pattern.compile("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    private static final Pattern PATRON_TELEFONO = 
        Pattern.compile("^(\\+\\d{1,3}[- ]?)?\\d{6,15}$");

    public boolean validarEmail(JTextField campo) {
        return aplicarValidacion(campo, PATRON_EMAIL);
    }

    public boolean validarTelefono(JTextField campo) {
        return aplicarValidacion(campo, PATRON_TELEFONO);
    }

    public boolean validarEmailString(String email) {
        return PATRON_EMAIL.matcher(email.trim()).matches();
    }

    public boolean validarTelefonoString(String telefono) {
        return PATRON_TELEFONO.matcher(telefono.trim()).matches();
    }

    // === MÉTODO CENTRAL DE VALIDACIÓN ===
    private boolean aplicarValidacion(JTextField campo, Pattern patron) {
        String texto = campo.getText().trim();
        boolean valido = patron.matcher(texto).matches();
        campo.setBackground(valido ? Color.WHITE : Color.PINK);
        return valido;
    }
}
