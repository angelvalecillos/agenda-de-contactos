package com.mycompany.agendadecontactos;

/**
 *
 * @author angel
 */
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class TextPrompt extends JLabel implements FocusListener, DocumentListener {
    private final JTextField textField;

    public TextPrompt(String text, JTextField textField) {
        this.textField = textField;
        setText(text);
        setFont(textField.getFont());
        setForeground(Color.GRAY);
        setBorder(BorderFactory.createEmptyBorder(textField.getInsets().top, 4, 0, 0));
        setHorizontalAlignment(SwingConstants.LEADING);
        textField.addFocusListener(this);
        textField.getDocument().addDocumentListener(this);
        textField.setLayout(new BorderLayout());
        textField.add(this);
        checkForPrompt();
    }

    private void checkForPrompt() {
        if (textField.getText().length() > 0) {
            setVisible(false);
        } else {
            setVisible(true);
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        checkForPrompt();
    }

    @Override
    public void focusLost(FocusEvent e) {
        checkForPrompt();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        checkForPrompt();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        checkForPrompt();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {}
}


