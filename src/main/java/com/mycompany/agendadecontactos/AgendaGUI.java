package com.mycompany.agendadecontactos;

/**
 *
 * @author angel
 */

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.*;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import java.awt.image.BufferedImage;
import java.io.Reader;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;




public class AgendaGUI extends javax.swing.JFrame {
    
    private final DefaultListModel<Persona> modeloLista;
    private DefaultListModel<Telefono> modeloTelefonos;
    private DefaultListModel<Correo> modeloCorreos;
    private DefaultListModel<Dir> modeloDir;   
    private List<Telefono> telefonosTemporales = new ArrayList<>();    
    private List<Correo> correosTemporales = new ArrayList<>();    
    private List<Dir> dirTemporales = new ArrayList<>();    
    private PersonaDAO dao;
    private Validaciones val = new Validaciones();
    private int index=0;
    private int selectedPersonaId = -1;
    private ResourceBundle textos;
    private Locale idiomaActual = new Locale("es"); // español por defecto
    // === ATRIBUTOS DE IDIOMA ===
    private final String CONFIG_FILE = "config.txt";
    
    
    public AgendaGUI() {
        initComponents();
        
        // === MENÚ PRINCIPAL ===
        Locale idiomaGuardado = cargarIdiomaConfig();
        cambiarIdioma(idiomaGuardado); // inicializa textos aquí

        // luego construyes el menú
        setJMenuBar(crearMenu());

        JMenu menuConfig = new JMenu("☰ " + (textos != null ? textos.getString("label.configuracion") : "Configuración"));
        JMenu menuIdioma = new JMenu("🌐 " + (textos != null ? textos.getString("label.idioma") : "Idioma"));

        // === OPCIONES DE IDIOMA ===
        JMenuItem itemEs = new JMenuItem("Español");
        JMenuItem itemEn = new JMenuItem("English");
        JMenuItem itemFr = new JMenuItem("Français");
        JMenuItem itemPt = new JMenuItem("Português");

        // Asignar acciones
        itemEs.addActionListener(e -> { cambiarIdioma(new Locale("es")); guardarIdiomaConfig(); });
        itemEn.addActionListener(e -> { cambiarIdioma(new Locale("en")); guardarIdiomaConfig(); });
        itemFr.addActionListener(e -> { cambiarIdioma(new Locale("fr")); guardarIdiomaConfig(); });
        itemPt.addActionListener(e -> { cambiarIdioma(new Locale("pt")); guardarIdiomaConfig(); });

        // Añadir ítems al submenú
        menuIdioma.add(itemEs);
        menuIdioma.add(itemEn);
        menuIdioma.add(itemFr);
        menuIdioma.add(itemPt);

        

        // Construcción del menú
        menuConfig.add(menuIdioma);
        menuConfig.addSeparator();
        
        JMenuItem itemImportar = new JMenuItem("Importar contactos (.csv)");
        itemImportar.addActionListener(e -> importarContactosDesdeCSV());

        JMenuItem itemExportar = new JMenuItem("Exportar contactos (.csv)");
        itemExportar.addActionListener(e -> exportarContactosAGoogleCSV());

        JMenuItem itemQR = new JMenuItem("Generar QR del contacto");
        itemQR.addActionListener(e -> {
            Persona seleccionada = listaPersonas.getSelectedValue();
            generarQRDeContacto(seleccionada);
        });

        menuConfig.addSeparator();
        menuConfig.add(itemImportar);
        menuConfig.add(itemExportar);
        menuConfig.add(itemQR);

// === OTROS SUBMENÚS (espacio futuro) ===
        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.addActionListener(e -> dispose());
        menuConfig.add(itemSalir);
        
        
        // Detecta idioma del sistema y carga interfaz
        Locale localeSistema = Locale.getDefault();
        cambiarIdioma(localeSistema);
        cambiarIdioma(idiomaGuardado);

        setLocationRelativeTo(null);
        
        if (listaTelefonos == null) {
            listaTelefonos = new javax.swing.JList<>();
            jScrollPane2.setViewportView(listaTelefonos);
        }
        if (listaCorreos == null) {
            listaCorreos = new javax.swing.JList<>();
            jScrollPane3.setViewportView(listaCorreos);
        }
        if (listaDir == null) {
            listaDir = new javax.swing.JList<>();
            jScrollPane4.setViewportView(listaDir);
        }


        dao = new PersonaDAO();
        modeloLista = new DefaultListModel<>();
        listaPersonas.setModel(modeloLista);
        modeloTelefonos = new DefaultListModel<>();
        listaTelefonos.setModel(modeloTelefonos);
        modeloCorreos = new DefaultListModel<>();
        listaCorreos.setModel(modeloCorreos);
        modeloDir = new DefaultListModel<>();
        listaDir.setModel(modeloDir);
        
        cargarPersonas();
        cargarTelefonos(null);
        cargarCorreos(null);
        cargarDir(null);
        
        // listener para que al seleccionar un elemento se carguen los campos
        listaPersonas.addListSelectionListener(e -> {
    if (!e.getValueIsAdjusting()) {
        Persona seleccionada = listaPersonas.getSelectedValue();
        if (seleccionada != null) {
            // Guardamos la PK globalmente
            selectedPersonaId = seleccionada.getId();

            txtNombre.setText(seleccionada.getNombre());
            txtApellido.setText(seleccionada.getApellido());
            txtFechaNac.setText(formatearFechaParaMostrar(seleccionada.getFechaNac(), idiomaActual));
            txtEmpresa.setText(seleccionada.getEmpresa());
            txtPuesto.setText(seleccionada.getPuesto());

            // cargar telefonos/correos/dirs desde BD
            modeloTelefonos.clear();
            for (Telefono t : dao.obtenerTelefonos(seleccionada.getId())) modeloTelefonos.addElement(t);

            modeloCorreos.clear();
            for (Correo c : dao.obtenerCorreos(seleccionada.getId())) modeloCorreos.addElement(c);

            modeloDir.clear();
            for (Dir d : dao.obtenerDirs(seleccionada.getId())) modeloDir.addElement(d);

            // limpiar campos de entrada auxiliares
            txtTelefono.setText("");
            txtTelefonoLabel.setText("");
            btnAgregarTelefono.setText("+");

            txtCorreo.setText("");
            txtCorreoLabel.setText("");
            btnAgregarCorreo.setText("+");

            txtDir1.setText("");
            txtDirLabel.setText("");
            btnAgregarDireccion.setText("+");

        } else {
            // cuando no hay selección
            selectedPersonaId = -1;
            limpiarFormulario();
        }
    }
});

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelLast = new javax.swing.JLabel();
        labelName = new javax.swing.JLabel();
        labelFechaNac = new javax.swing.JLabel();
        labelEmpresa = new javax.swing.JLabel();
        labelPuesto = new javax.swing.JLabel();
        labelTelefono = new javax.swing.JLabel();
        labelCorreo = new javax.swing.JLabel();
        labelDireccion = new javax.swing.JLabel();
        labelPais = new javax.swing.JLabel();
        labelDir1 = new javax.swing.JLabel();
        labelDir2 = new javax.swing.JLabel();
        labelPostal = new javax.swing.JLabel();
        labelCiudad = new javax.swing.JLabel();
        labelProvincia = new javax.swing.JLabel();
        labelPOBox = new javax.swing.JLabel();
        labelEtiqueta1 = new javax.swing.JLabel();
        labelEtiqueta2 = new javax.swing.JLabel();
        labelEtiqueta3 = new javax.swing.JLabel();
        btnGuardar = new javax.swing.JButton();
        btnActualizar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        btnAgregarTelefono = new javax.swing.JButton();
        btnAgregarCorreo = new javax.swing.JButton();
        btnAgregarDireccion = new javax.swing.JButton();
        scrollLista = new javax.swing.JScrollPane();
        listaPersonas = new javax.swing.JList<>(new DefaultListModel<>());
        jScrollPane2 = new javax.swing.JScrollPane();
        listaTelefonos = new javax.swing.JList<>(new DefaultListModel<Telefono>());
        jScrollPane3 = new javax.swing.JScrollPane();
        listaCorreos = new javax.swing.JList<>(new DefaultListModel<Correo>())
        ;
        jScrollPane4 = new javax.swing.JScrollPane();
        listaDir = new javax.swing.JList<>(new DefaultListModel<Dir>())
        ;
        txtNombre = new javax.swing.JTextField();
        txtApellido = new javax.swing.JTextField();
        txtFechaNac = new javax.swing.JTextField();
        txtEmpresa = new javax.swing.JTextField();
        txtPuesto = new javax.swing.JTextField();
        txtTelefono = new javax.swing.JTextField();
        txtTelefonoLabel = new javax.swing.JTextField();
        txtCorreo = new javax.swing.JTextField();
        txtCorreoLabel = new javax.swing.JTextField();
        txtPais = new javax.swing.JTextField();
        txtDir1 = new javax.swing.JTextField();
        txtDir2 = new javax.swing.JTextField();
        txtCodigoPostal = new javax.swing.JTextField();
        txtCiudad = new javax.swing.JTextField();
        txtProvincia = new javax.swing.JTextField();
        txtPOBox = new javax.swing.JTextField();
        txtDirLabel = new javax.swing.JTextField();
        jSeparator5 = new javax.swing.JSeparator();
        jSeparator6 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        txtBuscar = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(580, 820));
        setResizable(false);

        labelLast.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelLast.setText("Apellido");
        labelLast.setAlignmentX(0.5F);
        labelLast.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        labelLast.setInheritsPopupMenu(false);
        labelLast.setMaximumSize(new java.awt.Dimension(70, 16));
        labelLast.setMinimumSize(new java.awt.Dimension(70, 16));
        labelLast.setPreferredSize(new java.awt.Dimension(70, 16));

        labelName.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelName.setText("nombre");
        labelName.setAlignmentX(0.5F);
        labelName.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        labelName.setInheritsPopupMenu(false);
        labelName.setMaximumSize(new java.awt.Dimension(70, 16));
        labelName.setMinimumSize(new java.awt.Dimension(70, 16));
        labelName.setPreferredSize(new java.awt.Dimension(70, 16));

        labelFechaNac.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelFechaNac.setText("fecha de nac");
        labelFechaNac.setAlignmentX(0.5F);
        labelFechaNac.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        labelFechaNac.setInheritsPopupMenu(false);
        labelFechaNac.setMaximumSize(new java.awt.Dimension(70, 16));
        labelFechaNac.setMinimumSize(new java.awt.Dimension(70, 16));
        labelFechaNac.setPreferredSize(new java.awt.Dimension(70, 16));

        labelEmpresa.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelEmpresa.setText("empresa");

        labelPuesto.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelPuesto.setText("Puesto");
        labelPuesto.setAlignmentX(0.5F);
        labelPuesto.setMaximumSize(new java.awt.Dimension(70, 16));
        labelPuesto.setMinimumSize(new java.awt.Dimension(70, 16));
        labelPuesto.setPreferredSize(new java.awt.Dimension(70, 16));

        labelTelefono.setText("TELEFONOS");
        labelTelefono.setAlignmentX(0.5F);
        labelTelefono.setMaximumSize(new java.awt.Dimension(100, 16));
        labelTelefono.setMinimumSize(new java.awt.Dimension(100, 16));
        labelTelefono.setPreferredSize(new java.awt.Dimension(100, 16));

        labelCorreo.setText("CORREOS");
        labelCorreo.setAlignmentX(0.5F);
        labelCorreo.setMaximumSize(new java.awt.Dimension(172, 16));
        labelCorreo.setMinimumSize(new java.awt.Dimension(172, 16));
        labelCorreo.setPreferredSize(new java.awt.Dimension(172, 16));

        labelDireccion.setText("DIRECCIONES");
        labelDireccion.setAlignmentX(0.5F);
        labelDireccion.setMaximumSize(new java.awt.Dimension(172, 16));
        labelDireccion.setMinimumSize(new java.awt.Dimension(172, 16));
        labelDireccion.setPreferredSize(new java.awt.Dimension(172, 16));

        labelPais.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelPais.setText("pais");
        labelPais.setAlignmentX(0.5F);
        labelPais.setMaximumSize(new java.awt.Dimension(70, 16));
        labelPais.setMinimumSize(new java.awt.Dimension(70, 16));
        labelPais.setPreferredSize(new java.awt.Dimension(70, 16));

        labelDir1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelDir1.setText("dirreccion 1");
        labelDir1.setAlignmentX(0.5F);
        labelDir1.setMaximumSize(new java.awt.Dimension(70, 16));
        labelDir1.setMinimumSize(new java.awt.Dimension(70, 16));
        labelDir1.setPreferredSize(new java.awt.Dimension(70, 16));

        labelDir2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelDir2.setText("dirreccion 2");
        labelDir2.setAlignmentX(0.5F);
        labelDir2.setMaximumSize(new java.awt.Dimension(70, 16));
        labelDir2.setMinimumSize(new java.awt.Dimension(70, 16));
        labelDir2.setPreferredSize(new java.awt.Dimension(70, 16));

        labelPostal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelPostal.setText("codigo postal");
        labelPostal.setAlignmentX(0.5F);
        labelPostal.setMaximumSize(new java.awt.Dimension(70, 16));
        labelPostal.setMinimumSize(new java.awt.Dimension(70, 16));
        labelPostal.setPreferredSize(new java.awt.Dimension(70, 16));

        labelCiudad.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelCiudad.setText("ciudad");
        labelCiudad.setAlignmentX(0.5F);
        labelCiudad.setMaximumSize(new java.awt.Dimension(70, 16));
        labelCiudad.setMinimumSize(new java.awt.Dimension(70, 16));
        labelCiudad.setPreferredSize(new java.awt.Dimension(70, 16));

        labelProvincia.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelProvincia.setText("provincia");
        labelProvincia.setAlignmentX(0.5F);
        labelProvincia.setMaximumSize(new java.awt.Dimension(70, 16));
        labelProvincia.setMinimumSize(new java.awt.Dimension(70, 16));
        labelProvincia.setPreferredSize(new java.awt.Dimension(70, 16));

        labelPOBox.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelPOBox.setText("po Box");
        labelPOBox.setAlignmentX(0.5F);
        labelPOBox.setMaximumSize(new java.awt.Dimension(70, 16));
        labelPOBox.setMinimumSize(new java.awt.Dimension(70, 16));
        labelPOBox.setPreferredSize(new java.awt.Dimension(70, 16));

        labelEtiqueta1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelEtiqueta1.setText("etiqueta");
        labelEtiqueta1.setAlignmentX(0.5F);
        labelEtiqueta1.setMaximumSize(new java.awt.Dimension(70, 16));
        labelEtiqueta1.setMinimumSize(new java.awt.Dimension(70, 16));
        labelEtiqueta1.setPreferredSize(new java.awt.Dimension(70, 16));

        labelEtiqueta2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelEtiqueta2.setText("etiqueta");

        labelEtiqueta3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelEtiqueta3.setText("etiqueta");
        labelEtiqueta3.setAlignmentX(0.5F);
        labelEtiqueta3.setMaximumSize(new java.awt.Dimension(100, 16));
        labelEtiqueta3.setMinimumSize(new java.awt.Dimension(100, 16));
        labelEtiqueta3.setPreferredSize(new java.awt.Dimension(100, 16));

        btnGuardar.setText("Guardar");
        btnGuardar.setAlignmentX(0.5F);
        btnGuardar.setMaximumSize(new java.awt.Dimension(100, 23));
        btnGuardar.setMinimumSize(new java.awt.Dimension(100, 23));
        btnGuardar.setPreferredSize(new java.awt.Dimension(100, 23));
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        btnActualizar.setText("Actualizar");
        btnActualizar.setAlignmentX(0.5F);
        btnActualizar.setMaximumSize(new java.awt.Dimension(100, 23));
        btnActualizar.setMinimumSize(new java.awt.Dimension(100, 23));
        btnActualizar.setPreferredSize(new java.awt.Dimension(100, 23));
        btnActualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarActionPerformed(evt);
            }
        });

        btnEliminar.setText("Eliminar");
        btnEliminar.setAlignmentX(0.5F);
        btnEliminar.setMaximumSize(new java.awt.Dimension(100, 23));
        btnEliminar.setMinimumSize(new java.awt.Dimension(100, 23));
        btnEliminar.setName(""); // NOI18N
        btnEliminar.setPreferredSize(new java.awt.Dimension(100, 23));
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });

        btnLimpiar.setText("Limpiar");
        btnLimpiar.setAlignmentX(0.5F);
        btnLimpiar.setMaximumSize(new java.awt.Dimension(100, 23));
        btnLimpiar.setMinimumSize(new java.awt.Dimension(100, 23));
        btnLimpiar.setPreferredSize(new java.awt.Dimension(100, 23));
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
            }
        });

        btnAgregarTelefono.setText("+");
        btnAgregarTelefono.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarTelefonoActionPerformed(evt);
            }
        });

        btnAgregarCorreo.setText("+");
        btnAgregarCorreo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarCorreoActionPerformed(evt);
            }
        });

        btnAgregarDireccion.setText("+");
        btnAgregarDireccion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarDireccionActionPerformed(evt);
            }
        });

        scrollLista.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollLista.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollLista.setMaximumSize(new java.awt.Dimension(32767, 32767000));

        listaPersonas.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        listaPersonas.setMaximumSize(new java.awt.Dimension(198, 600000));
        listaPersonas.setMinimumSize(new java.awt.Dimension(198, 250));
        scrollLista.setViewportView(listaPersonas);

        listaTelefonos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listaTelefonosMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(listaTelefonos);

        listaCorreos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listaCorreosMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(listaCorreos);

        listaDir.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listaDirMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(listaDir);

        txtNombre.setActionCommand("<Not Set>");
        txtNombre.setAutoscrolls(false);
        txtNombre.setMaximumSize(new java.awt.Dimension(116, 22));
        txtNombre.setMinimumSize(new java.awt.Dimension(116, 22));
        txtNombre.setPreferredSize(new java.awt.Dimension(116, 22));
        txtNombre.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtNombreKeyTyped(evt);
            }
        });

        txtApellido.setActionCommand("<Not Set>");
        txtApellido.setMaximumSize(new java.awt.Dimension(116, 22));
        txtApellido.setMinimumSize(new java.awt.Dimension(116, 22));
        txtApellido.setPreferredSize(new java.awt.Dimension(116, 22));
        txtApellido.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtApellidoKeyTyped(evt);
            }
        });

        txtFechaNac.setMaximumSize(new java.awt.Dimension(116, 22));
        txtFechaNac.setMinimumSize(new java.awt.Dimension(116, 22));
        txtFechaNac.setPreferredSize(new java.awt.Dimension(116, 22));

        txtEmpresa.setMaximumSize(new java.awt.Dimension(116, 22));
        txtEmpresa.setMinimumSize(new java.awt.Dimension(116, 22));
        txtEmpresa.setPreferredSize(new java.awt.Dimension(116, 22));

        txtPuesto.setMaximumSize(new java.awt.Dimension(116, 22));
        txtPuesto.setMinimumSize(new java.awt.Dimension(116, 22));
        txtPuesto.setPreferredSize(new java.awt.Dimension(116, 22));

        txtTelefono.setMaximumSize(new java.awt.Dimension(120, 22));
        txtTelefono.setMinimumSize(new java.awt.Dimension(120, 22));
        txtTelefono.setPreferredSize(new java.awt.Dimension(120, 22));
        txtTelefono.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtTelefonoKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtTelefonoKeyTyped(evt);
            }
        });

        txtTelefonoLabel.setMaximumSize(new java.awt.Dimension(70, 22));
        txtTelefonoLabel.setMinimumSize(new java.awt.Dimension(70, 22));
        txtTelefonoLabel.setPreferredSize(new java.awt.Dimension(70, 22));

        txtCorreo.setMaximumSize(new java.awt.Dimension(200, 22));
        txtCorreo.setMinimumSize(new java.awt.Dimension(200, 22));
        txtCorreo.setPreferredSize(new java.awt.Dimension(200, 22));
        txtCorreo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCorreoKeyTyped(evt);
            }
        });

        txtCorreoLabel.setMaximumSize(new java.awt.Dimension(70, 22));
        txtCorreoLabel.setMinimumSize(new java.awt.Dimension(70, 22));
        txtCorreoLabel.setPreferredSize(new java.awt.Dimension(70, 22));

        txtPais.setActionCommand("<Not Set>");
        txtPais.setMaximumSize(new java.awt.Dimension(116, 22));
        txtPais.setMinimumSize(new java.awt.Dimension(116, 22));
        txtPais.setPreferredSize(new java.awt.Dimension(116, 22));
        txtPais.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtPaisKeyTyped(evt);
            }
        });

        txtCodigoPostal.setMaximumSize(new java.awt.Dimension(116, 22));
        txtCodigoPostal.setMinimumSize(new java.awt.Dimension(116, 22));
        txtCodigoPostal.setPreferredSize(new java.awt.Dimension(116, 22));

        txtCiudad.setMaximumSize(new java.awt.Dimension(116, 22));
        txtCiudad.setMinimumSize(new java.awt.Dimension(116, 22));
        txtCiudad.setPreferredSize(new java.awt.Dimension(116, 22));

        txtProvincia.setMaximumSize(new java.awt.Dimension(116, 22));
        txtProvincia.setMinimumSize(new java.awt.Dimension(116, 22));
        txtProvincia.setPreferredSize(new java.awt.Dimension(116, 22));

        txtPOBox.setMaximumSize(new java.awt.Dimension(116, 22));
        txtPOBox.setMinimumSize(new java.awt.Dimension(116, 22));
        txtPOBox.setPreferredSize(new java.awt.Dimension(116, 22));

        txtDirLabel.setMaximumSize(new java.awt.Dimension(116, 22));
        txtDirLabel.setMinimumSize(new java.awt.Dimension(116, 22));
        txtDirLabel.setPreferredSize(new java.awt.Dimension(116, 22));

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);

        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtBuscarKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator2)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addGap(6, 6, 6)
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                            .addComponent(labelEmpresa, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(labelPuesto, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                            .addComponent(txtEmpresa, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(txtPuesto, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                        .addComponent(labelFechaNac, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(txtFechaNac, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                    .addComponent(labelLast, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(txtApellido, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                    .addComponent(labelName, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                            .addComponent(scrollLista, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtBuscar))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jSeparator4, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jSeparator5, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(labelTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(labelEtiqueta3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(txtTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(txtTelefonoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnAgregarTelefono))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jScrollPane3)
                                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(labelCorreo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(txtCorreo, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(txtCorreoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(btnAgregarCorreo))
                                                    .addComponent(labelEtiqueta2, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(labelDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnAgregarDireccion))))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(labelProvincia, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtProvincia, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(labelCiudad, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(labelPais, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                    .addComponent(labelDir1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(labelDir2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(labelPostal, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtDir1, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtDir2, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtPais, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addComponent(txtCiudad, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(txtCodigoPostal, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(btnActualizar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(labelPOBox, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtPOBox, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(labelEtiqueta1, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtDirLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(scrollLista, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtApellido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelLast, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtFechaNac, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelFechaNac, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtEmpresa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelEmpresa, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtPuesto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelPuesto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelEtiqueta3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtTelefonoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnAgregarTelefono))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelCorreo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelEtiqueta2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtCorreoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnAgregarCorreo))
                            .addComponent(txtCorreo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnAgregarDireccion))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSeparator3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelPais, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPais, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelDir1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDir1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelDir2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDir2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelPostal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCodigoPostal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelCiudad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCiudad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelProvincia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtProvincia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelPOBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPOBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelEtiqueta1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDirLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 7, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnActualizar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
    String nombre = txtNombre.getText().trim();
    String apellido = txtApellido.getText().trim();
    String fechaTexto = txtFechaNac.getText();
    
    String fechaISO="";
    // siempre guardar en formato ISO
    if(!"".equals(fechaTexto)){
        LocalDate fechaValida = validarFecha(fechaTexto, idiomaActual);
        fechaISO = fechaValida.toString(); // yyyy-MM-dd
    }
    
    // === VALIDACIONES DE CAMPOS OBLIGATORIOS ===
    if (nombre.isEmpty() && apellido.isEmpty()) {
        JOptionPane.showMessageDialog(this, 
            textos.getString("msg.nombreYapellido"),
            textos.getString("app.title"),
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    // === VALIDAR CORREO Y TELÉFONO ===
    if (!val.validarTelefono(txtTelefono) && !txtTelefono.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, 
            textos.getString("msg.errorTelefono"),
            textos.getString("app.title"),
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    if (!val.validarEmail(txtCorreo) && !txtCorreo.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, 
            textos.getString("msg.errorCorreo"),
            textos.getString("app.title"),
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    sincronizarCamposConModelos();
    Persona existente = dao.buscarPersona(nombre, apellido);

    if (existente != null) {
        int opcion = 
        JOptionPane.showConfirmDialog(this,
                textos.getString("msg.confirmarContactoExiste"),
                textos.getString("app.title"),
                JOptionPane.YES_NO_OPTION);

        if (opcion == JOptionPane.YES_OPTION) {
            // Actualizar persona
            Persona actualizada = new Persona(
                    existente.getId(),
                    nombre,
                    apellido,
                    fechaISO,
                    txtEmpresa.getText(),
                    txtPuesto.getText()
            );
            dao.actualizar(actualizada);

            // --- Sincronizar TELÉFONOS ---
            List<Telefono> telBD = dao.obtenerTelefonos(actualizada.getId());
            List<Telefono> telGUI = new ArrayList<>();
            for (int i = 0; i < modeloTelefonos.size(); i++) {
                telGUI.add(modeloTelefonos.getElementAt(i));
            }

            for (Telefono tBD : telBD) {
                if (!telGUI.contains(tBD)) {
                    dao.eliminarTelefonoPorId(tBD.getId());
                }
            }
            for (Telefono tGUI : telGUI) {
                if (tGUI.getId() == 0) {
                    dao.insertarTelefono(actualizada.getId(), tGUI.getNumero(), tGUI.getLabel());
                } else {
                    dao.updateTelefono(tGUI.getId(), tGUI.getNumero(), tGUI.getLabel());
                }
            }

            // --- Sincronizar CORREOS ---
            List<Correo> corBD = dao.obtenerCorreos(actualizada.getId());
            List<Correo> corGUI = new ArrayList<>();
            for (int i = 0; i < modeloCorreos.size(); i++) {
                corGUI.add(modeloCorreos.getElementAt(i));
            }

            for (Correo cBD : corBD) {
                if (!corGUI.contains(cBD)) {
                    dao.eliminarCorreoPorId(cBD.getId());
                }
            }
            for (Correo cGUI : corGUI) {
                if (cGUI.getId() == 0) {
                    dao.insertarCorreo(actualizada.getId(), cGUI.getCorreo(), cGUI.getLabel());
                } else {
                    dao.updateCorreo(cGUI.getId(), cGUI.getCorreo(), cGUI.getLabel());
                }
            }

            // --- Sincronizar DIRECCIONES ---
            List<Dir> dirBD = dao.obtenerDirs(actualizada.getId());
            List<Dir> dirGUI = new ArrayList<>();
            for (int i = 0; i < modeloDir.size(); i++) {
                dirGUI.add(modeloDir.getElementAt(i));
            }

            for (Dir dBD : dirBD) {
                if (!dirGUI.contains(dBD)) {
                    dao.eliminarDirPorId(dBD.getId());
                }
            }
            for (Dir dGUI : dirGUI) {
                if (dGUI.getId() == 0) {
                    dao.insertarDir(actualizada.getId(), dGUI);
                } else {
                    dao.updateDireccion(dGUI.getId(), dGUI);
                }
            }
            JOptionPane.showMessageDialog(this, 
                textos.getString("msg.guardado"),
                textos.getString("app.title"),
                JOptionPane.WARNING_MESSAGE);
        } else {
            return; // No hacer nada
        }
    } else {
        // Nuevo contacto
        Persona nueva = new Persona(
                0,
                nombre,
                apellido,
                txtFechaNac.getText(),
                txtEmpresa.getText(),
                txtPuesto.getText()
        );

        int idPersona = dao.insertar(nueva);
        if (idPersona != -1) {
        for (int i = 0; i < modeloTelefonos.size(); i++) {
            Telefono t = modeloTelefonos.getElementAt(i);
            dao.insertarTelefono(idPersona, t.getNumero(), t.getLabel());
        }
        for (int i = 0; i < modeloCorreos.size(); i++) {
            Correo c = modeloCorreos.getElementAt(i);
            dao.insertarCorreo(idPersona, c.getCorreo(), c.getLabel());
        }
        for (int i = 0; i < modeloDir.size(); i++) {
            Dir d = modeloDir.getElementAt(i);
            dao.insertarDir(idPersona, d);
        }

        JOptionPane.showMessageDialog(this, 
            textos.getString("msg.guardado"),
            textos.getString("app.title"),
            JOptionPane.WARNING_MESSAGE);
        selectedPersonaId = idPersona;
        cargarPersonas();
        selectPersonaById(idPersona);
    } else {
    JOptionPane.showMessageDialog(this, 
            textos.getString("msg.error"),
            textos.getString("app.title"),
            JOptionPane.WARNING_MESSAGE);
    }


            }

    cargarPersonas();
    limpiarFormulario();

    }//GEN-LAST:event_btnGuardarActionPerformed
    
    private void btnActualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarActionPerformed
    if (selectedPersonaId == -1) {
        JOptionPane.showMessageDialog(this, 
            textos.getString("msg.noSeleccionado"),
            textos.getString("app.title"),
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    // Validaciones previas
    if (!val.validarTelefono(txtTelefono) && !txtTelefono.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, 
            textos.getString("msg.errorTelefono"),
            textos.getString("app.title"),
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    if (!val.validarEmail(txtCorreo) && !txtCorreo.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, 
            textos.getString("msg.errorCorreo"),
            textos.getString("app.title"),
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    // Validar fecha
    String fechaTexto = txtFechaNac.getText();
    LocalDate fechaValida = validarFecha(fechaTexto, idiomaActual);
    String fechaISO = fechaValida != null ? fechaValida.toString() : ""; // formato yyyy-MM-dd

    // --- Actualizar persona principal por ID ---
    sincronizarCamposConModelos();
    Persona actualizada = new Persona(
        selectedPersonaId,  // ✅ usar siempre el ID
        txtNombre.getText().trim(),
        txtApellido.getText().trim(),
        fechaISO,
        txtEmpresa.getText().trim(),
        txtPuesto.getText().trim()
    );

    dao.actualizar(actualizada); // ✅ actualiza en la tabla persona por ID

    // --- Telefonos ---
    List<Telefono> telBD = dao.obtenerTelefonos(actualizada.getId());
    List<Telefono> telGUI = new ArrayList<>();
    for (int i = 0; i < modeloTelefonos.size(); i++) telGUI.add(modeloTelefonos.getElementAt(i));

    // eliminar los que ya no existen en GUI
    for (Telefono tBD : telBD) {
        boolean encontrado = telGUI.stream().anyMatch(tGUI -> tGUI.getId() == tBD.getId());
        if (!encontrado) dao.eliminarTelefonoPorId(tBD.getId());
    }

    // insertar/actualizar los de la GUI
    for (Telefono tGUI : telGUI) {
        if (tGUI.getId() == 0) {
            int nuevoId = dao.insertarTelefono(actualizada.getId(), tGUI.getNumero(), tGUI.getLabel());
            if (nuevoId != -1) tGUI.setId(nuevoId);
        } else {
            dao.updateTelefono(tGUI.getId(), tGUI.getNumero(), tGUI.getLabel());
        }
    }

    // --- Correos ---
    List<Correo> corBD = dao.obtenerCorreos(actualizada.getId());
    List<Correo> corGUI = new ArrayList<>();
    for (int i = 0; i < modeloCorreos.size(); i++) corGUI.add(modeloCorreos.getElementAt(i));

    for (Correo cBD : corBD) {
        boolean encontrado = corGUI.stream().anyMatch(cGUI -> cGUI.getId() == cBD.getId());
        if (!encontrado) dao.eliminarCorreoPorId(cBD.getId());
    }

    for (Correo cGUI : corGUI) {
        if (cGUI.getId() == 0) {
            int nuevoId = dao.insertarCorreo(actualizada.getId(), cGUI.getCorreo(), cGUI.getLabel());
            if (nuevoId != -1) cGUI.setId(nuevoId);
        } else {
            dao.updateCorreo(cGUI.getId(), cGUI.getCorreo(), cGUI.getLabel());
        }
    }

    // --- Direcciones ---
    List<Dir> dirBD = dao.obtenerDirs(actualizada.getId());
    List<Dir> dirGUI = new ArrayList<>();
    for (int i = 0; i < modeloDir.size(); i++) dirGUI.add(modeloDir.getElementAt(i));

    for (Dir dBD : dirBD) {
        boolean encontrado = dirGUI.stream().anyMatch(dGUI -> dGUI.getId() == dBD.getId());
        if (!encontrado) dao.eliminarDirPorId(dBD.getId());
    }

    for (Dir dGUI : dirGUI) {
        if (dGUI.getId() == 0) {
            int nuevoId = dao.insertarDir(actualizada.getId(), dGUI);
            if (nuevoId != -1) dGUI.setId(nuevoId);
        } else {
            dao.updateDireccion(dGUI.getId(), dGUI);
        }
    }

    // --- Recargar interfaz ---
    cargarPersonas();
    selectPersonaById(actualizada.getId()); // ✅ re-selecciona la misma persona

    JOptionPane.showMessageDialog(this, 
        textos.getString("msg.actualizado"),
        textos.getString("app.title"),
        JOptionPane.INFORMATION_MESSAGE);

    }//GEN-LAST:event_btnActualizarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
    Persona seleccionada = listaPersonas.getSelectedValue();
    int count=0;
    if (seleccionada != null) {
        dao.eliminar(seleccionada.getId()); 
        // gracias a ON DELETE CASCADE en la BD, se eliminan también teléfonos, correos y direcciones
        cargarPersonas();
        limpiarFormulario();
        count=count+1;
    }
        JOptionPane.showMessageDialog(this, 
            textos.getString(count+"msg.eliminado"),
            textos.getString("app.title"),
            JOptionPane.WARNING_MESSAGE);
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        
        limpiarFormulario();
    
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnAgregarTelefonoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarTelefonoActionPerformed
    if (btnAgregarTelefono.getText().equals("-")) {
        // BORRAR
        Telefono seleccionado = modeloTelefonos.getElementAt(index);
        int confirm = 
        JOptionPane.showConfirmDialog(this,
                textos.getString("msg.eliminarTelefono")+ seleccionado.getNumero() + "?",
                textos.getString("app.title"),
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (seleccionado.getId() != 0) {
                // Borrar de BD si ya existe
                dao.eliminarTelefonoPorId(seleccionado.getId());
            } else {
                // Si era un teléfono nuevo sin guardar aún
                telefonosTemporales.removeIf(t -> t.getNumero().equals(seleccionado.getNumero()));
            }
            modeloTelefonos.removeElement(seleccionado);
        }
        txtTelefono.setText("");
        txtTelefonoLabel.setText("");
        btnAgregarTelefono.setText("+");
    } 
    else {
        guardarTelefono();
    }                                                  
    
    }//GEN-LAST:event_btnAgregarTelefonoActionPerformed
    
    private void listaTelefonosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listaTelefonosMouseClicked
        index = listaTelefonos.locationToIndex(evt.getPoint());
        index = listaTelefonos.locationToIndex(evt.getPoint());
    if (index >= 0) {
        Telefono seleccionado = modeloTelefonos.getElementAt(index);
        txtTelefono.setText(seleccionado.getNumero());
        txtTelefonoLabel.setText(seleccionado.getLabel());
        btnAgregarTelefono.setText("-");
    }
    }//GEN-LAST:event_listaTelefonosMouseClicked

    private void txtTelefonoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtTelefonoKeyReleased
    
    String numero = txtTelefono.getText().trim();

    if (!numero.isEmpty()) {
        // Buscar si ya existe en el modelo
        Telefono duplicado = null;
        for (int i = 0; i < modeloTelefonos.size(); i++) {
            Telefono t = modeloTelefonos.getElementAt(i);
            if (t.getNumero().equals(numero)) {
                duplicado = t;
                break;
            }
        }

        if (duplicado != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "El número " + numero + " ya existe.\n¿Desea eliminarlo?",
                    "Número duplicado",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                if (duplicado.getId() != 0) {
                    dao.eliminarTelefonoPorId(duplicado.getId()); // eliminar desde BD
                } else {
                    telefonosTemporales.removeIf(t -> t.getNumero().equals(numero)); // eliminar temporal
                }
                modeloTelefonos.removeElement(duplicado);
                txtTelefono.setText("");
            } else {
                // Limpia el campo para evitar duplicados
                txtTelefono.setText("");
            }
        }
    }

    }//GEN-LAST:event_txtTelefonoKeyReleased

    private void btnAgregarCorreoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarCorreoActionPerformed
    if (btnAgregarCorreo.getText().equals("-")) {
        Correo seleccionado = modeloCorreos.getElementAt(index);
        int confirm = 
        JOptionPane.showConfirmDialog(this,
                textos.getString("msg.eliminarCorreo")+ seleccionado.getCorreo() + "?",
                textos.getString("app.title"),
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Persona persona = listaPersonas.getSelectedValue();
            if (persona != null) {
                dao.eliminarCorreoPorId( seleccionado.getId());
            } else {
                correosTemporales.removeIf(c -> c.getCorreo().equals(seleccionado.getCorreo()));
            }
            modeloCorreos.removeElement(seleccionado);
        }
        txtCorreo.setText("");
        txtCorreoLabel.setText("");
        btnAgregarCorreo.setText("+");
    } 

    else {
        guardarCorreo();
        }
    }//GEN-LAST:event_btnAgregarCorreoActionPerformed

    private void listaCorreosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listaCorreosMouseClicked
        index = listaCorreos.locationToIndex(evt.getPoint());
        index = listaCorreos.locationToIndex(evt.getPoint());
    if (index >= 0) {
        Correo seleccionado = modeloCorreos.getElementAt(index);
        txtCorreo.setText(seleccionado.getCorreo());
        txtCorreoLabel.setText(seleccionado.getLabel());
        btnAgregarCorreo.setText("-");
    }
    }//GEN-LAST:event_listaCorreosMouseClicked

    private void listaDirMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listaDirMouseClicked
        index = listaDir.locationToIndex(evt.getPoint());
        index = listaDir.locationToIndex(evt.getPoint());
    if (index >= 0) {
        Dir seleccionado = modeloDir.getElementAt(index);
        txtPais.setText(seleccionado.getPais());
        txtDir1.setText(seleccionado.getDireccion1());
        txtDir2.setText(seleccionado.getDireccion2());
        txtCodigoPostal.setText(seleccionado.getCodigoPostal());
        txtCiudad.setText(seleccionado.getCiudad());
        txtProvincia.setText(seleccionado.getProvincia());
        txtPOBox.setText(seleccionado.getPoBox());
        txtDirLabel.setText(seleccionado.getLabel());
        btnAgregarDireccion.setText("-");
    }    }//GEN-LAST:event_listaDirMouseClicked

    private void btnAgregarDireccionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarDireccionActionPerformed

    if (btnAgregarDireccion.getText().equals("-")) {
        Dir seleccionado = modeloDir.getElementAt(index);
        int confirm = 
        JOptionPane.showConfirmDialog(this,
                textos.getString("msg.eliminardir")+ seleccionado.getLabel() + "?",
                textos.getString("app.title"),
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Persona persona = listaPersonas.getSelectedValue();
            if (persona != null) {
                dao.eliminarDirPorId( seleccionado.getId());
            } else {
                dirTemporales.removeIf(c -> c.getDireccion1().equals(seleccionado.getDireccion1()));
            }
            modeloDir.removeElement(seleccionado);
        }
        limpiarDireccion();
        btnAgregarDireccion.setText("+");
    } 

    else {
        guardarDir();
        }
    

    }//GEN-LAST:event_btnAgregarDireccionActionPerformed

    private void txtTelefonoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtTelefonoKeyTyped
        val.permitirSoloNumeros(evt);
    }//GEN-LAST:event_txtTelefonoKeyTyped

    private void txtNombreKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNombreKeyTyped
        val.permitirSoloLetras(evt);
    }//GEN-LAST:event_txtNombreKeyTyped

    private void txtApellidoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtApellidoKeyTyped
        val.permitirSoloLetras(evt);
    }//GEN-LAST:event_txtApellidoKeyTyped

    private void txtPaisKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPaisKeyTyped
        val.permitirSoloLetras(evt);
    }//GEN-LAST:event_txtPaisKeyTyped

    private void txtCorreoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCorreoKeyTyped
        val.validarEmail(txtCorreo);
    }//GEN-LAST:event_txtCorreoKeyTyped

    private void txtBuscarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscarKeyPressed
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
    @Override
    public void insertUpdate(DocumentEvent e) { filtrarPersonas(); }
    @Override
    public void removeUpdate(DocumentEvent e) { filtrarPersonas(); }
    @Override
    public void changedUpdate(DocumentEvent e) { filtrarPersonas(); }
});

    }//GEN-LAST:event_txtBuscarKeyPressed

    private JMenuBar crearMenu() {
    JMenuBar menuBar = new JMenuBar();

    JMenu menuConfig = new JMenu("☰ " + textos.getString("label.configuracion"));
    JMenu menuIdioma = new JMenu("🌐 " + textos.getString("label.idioma"));

    JMenuItem itemEs = new JMenuItem("Español");
    JMenuItem itemEn = new JMenuItem("English");
    JMenuItem itemFr = new JMenuItem("Français");
    JMenuItem itemPt = new JMenuItem("Português");

    itemEs.addActionListener(e -> { cambiarIdioma(new Locale("es")); guardarIdiomaConfig(); });
    itemEn.addActionListener(e -> { cambiarIdioma(new Locale("en")); guardarIdiomaConfig(); });
    itemFr.addActionListener(e -> { cambiarIdioma(new Locale("fr")); guardarIdiomaConfig(); });
    itemPt.addActionListener(e -> { cambiarIdioma(new Locale("pt")); guardarIdiomaConfig(); });

    menuIdioma.add(itemEs);
    menuIdioma.add(itemEn);
    menuIdioma.add(itemFr);
    menuIdioma.add(itemPt);

    JMenuItem itemImportar = new JMenuItem(textos.getString("menu.importar"));
    itemImportar.addActionListener(e -> importarContactosDesdeCSV());

    JMenuItem itemExportar = new JMenuItem(textos.getString("menu.exportar"));
    itemExportar.addActionListener(e -> exportarContactosAGoogleCSV());

    JMenuItem itemQR = new JMenuItem(textos.getString("menu.qr"));
    itemQR.addActionListener(e -> {
        Persona seleccionada = listaPersonas.getSelectedValue();
        generarQRDeContacto(seleccionada);
    });

    JMenuItem itemSalir = new JMenuItem(textos.getString("menu.salir"));
    itemSalir.addActionListener(e -> dispose());

    menuConfig.add(menuIdioma);
    menuConfig.addSeparator();
    menuConfig.add(itemImportar);
    menuConfig.add(itemExportar);
    menuConfig.add(itemQR);
    menuConfig.addSeparator();
    menuConfig.add(itemSalir);

    menuBar.add(menuConfig);
    return menuBar;
}

    private void cambiarIdioma(Locale nuevoLocale) {
    try {
        Locale idiomaAterior= idiomaActual;
        idiomaActual = nuevoLocale;
        
        textos = ResourceBundle.getBundle(
            "com.mycompany.agendadecontactos.resources.messages",
            idiomaActual
        );

        // === TITULOS ===
        setTitle(textos.getString("app.title"));

        // === ETIQUETAS ===
        labelName.setText(textos.getString("label.nombre"));
        labelLast.setText(textos.getString("label.apellido"));
        labelTelefono.setText(textos.getString("label.telefono"));
        labelCorreo.setText(textos.getString("label.correo"));
        labelDireccion.setText(textos.getString("label.direccion"));
        labelEmpresa.setText(textos.getString("label.empresa"));
        labelPuesto.setText(textos.getString("label.puesto"));
        labelFechaNac.setText(textos.getString("label.fecha"));
        //labelBuscar.setText(textos.getString("label.buscar"));
        labelEtiqueta1.setText(textos.getString("label.etiqueta"));
        labelEtiqueta2.setText(textos.getString("label.etiqueta"));
        labelEtiqueta3.setText(textos.getString("label.etiqueta"));
        labelPais.setText(textos.getString("label.pais"));
        labelDir1.setText(textos.getString("label.dir1"));
        labelDir2.setText(textos.getString("label.dir2"));
        labelPostal.setText(textos.getString("label.codigopostal"));
        labelCiudad.setText(textos.getString("label.ciudad"));
        labelProvincia.setText(textos.getString("label.provincia"));
        labelPOBox.setText(textos.getString("label.pobox"));

        // === BOTONES ===
        btnGuardar.setText(textos.getString("btn.guardar"));
        btnActualizar.setText(textos.getString("btn.actualizar"));
        btnEliminar.setText(textos.getString("btn.eliminar"));
        btnLimpiar.setText(textos.getString("btn.limpiar"));
        //btnNuevo.setText(textos.getString("btn.nuevo"));
        //txtBuscar.setText(textos.getString("btn.buscar"));
        //btnSalir.setText(textos.getString("btn.salir"));
        btnAgregarTelefono.setText(textos.getString("btn.agregarTelefono"));
        btnAgregarCorreo.setText(textos.getString("btn.agregarCorreo"));
        btnAgregarDireccion.setText(textos.getString("btn.agregarDireccion"));
        
        // === Actualizar formato de fecha según el idioma actual ===
        String fechaTexto = txtFechaNac.getText();
        if (fechaTexto != null && !fechaTexto.trim().isEmpty()) {
            try {
                LocalDate fechaValida = validarFecha(fechaTexto, idiomaAterior);
                if (fechaValida != null) {
                    String fechaISO = fechaValida.toString(); // formato estándar yyyy-MM-dd
                    txtFechaNac.setText(formatearFechaParaMostrar(fechaISO, idiomaActual));
                }
            } catch (Exception e) {
                System.out.println("⚠ No se pudo reformatear la fecha: " + fechaTexto);
            }
}

    

        
        // --- Reconstruir menú traducido ---
        setJMenuBar(crearMenu());
        revalidate();
        repaint();
        
        // --- Traducir botones de diálogos ---
        actualizarBotonesDialogos(textos);

        // === Placeholder dinámico para el campo de fecha ===
String formato;
switch (idiomaActual.getLanguage()) {
    case "en" -> formato = "MM/dd/yyyy";
    case "fr", "pt", "es" -> formato = "dd/MM/yyyy";
    default -> formato = "dd/MM/yyyy";
}

// Limpia hints anteriores si los hay
for (Component c : txtFechaNac.getComponents()) {
    if (c instanceof JLabel) {
        txtFechaNac.remove(c);
    }
}

new TextPrompt(formato, txtFechaNac);

// Limpia hints anteriores si los hay
for (Component c : txtBuscar.getComponents()) {
    if (c instanceof JLabel) {
        txtBuscar.remove(c);
    }
}

new TextPrompt(textos.getString("btn.buscar"), txtBuscar);


    } catch (MissingResourceException e) {
        System.err.println("⚠️ No se encontró el archivo de idioma: " + nuevoLocale);
        e.printStackTrace();
        // fallback a español
    }
}
    
    private void actualizarBotonesDialogos(ResourceBundle textos) {
        UIManager.put("OptionPane.yesButtonText", textos.getString("dialog.yes"));
        UIManager.put("OptionPane.noButtonText", textos.getString("dialog.no"));
        UIManager.put("OptionPane.cancelButtonText", textos.getString("dialog.cancel"));
        UIManager.put("OptionPane.okButtonText", textos.getString("dialog.ok"));
}
   
    private void cargarPersonas(){
    modeloLista.clear();
    List<Persona> personas = dao.obtenerTodas();

    // Ordenar alfabéticamente por nombre y luego por apellido
    personas.sort((p1, p2) -> {
        int comp = p1.getNombre().compareToIgnoreCase(p2.getNombre());
        if (comp == 0) {
            comp = p1.getApellido().compareToIgnoreCase(p2.getApellido());
        }
        return comp;
    });

    for (Persona p : personas) {
        modeloLista.addElement(p);
    }
}

    private void cargarTelefonos(Persona persona){
        modeloTelefonos.clear();
    if (persona != null){
        for (Telefono tel : dao.obtenerTelefonos(persona.getId())){
            modeloTelefonos.addElement(tel);
        }
    } else {
        txtTelefono.setText("");
        txtTelefonoLabel.setText("");
    }
    }
    
    private void cargarCorreos(Persona persona){
        modeloCorreos.clear();
    if (persona != null){
        for (Correo cor : dao.obtenerCorreos(persona.getId())){
            modeloCorreos.addElement(cor);
        }
    } else {
        txtCorreo.setText("");
        txtCorreoLabel.setText("");
    }
    }
    
    private void cargarDir(Persona persona){
        modeloDir.clear();
    if (persona != null){
        for (Dir dir : dao.obtenerDirs(persona.getId())){
            modeloDir.addElement(dir);
        }
    } else {
        txtDir1.setText("");
        txtDirLabel.setText("");
    }
    }
    
    private void guardarIdiomaConfig() {
    try (java.io.FileWriter fw = new java.io.FileWriter(CONFIG_FILE)) {
        fw.write(idiomaActual.toLanguageTag());

    } catch (Exception e) {
    }
}

    private Locale cargarIdiomaConfig() {
    try (java.util.Scanner sc = new java.util.Scanner(new java.io.File(CONFIG_FILE))) {
        String lang = sc.nextLine().trim();
        return Locale.forLanguageTag(lang);
    } catch (Exception e) {
        return Locale.getDefault();
    }
}

    private boolean telefonoExisteEnModelo(String numero) {
    for (int i = 0; i < modeloTelefonos.size(); i++) {
        if (modeloTelefonos.getElementAt(i).getNumero().equals(numero)) return true;
    }
    return false;
}
    
    private void limpiarFormulario() {
        limpiarPersona();
        limpiarTelefono();
        limpiarCorreo();
        limpiarDireccion();
    }
    
    private void limpiarPersona(){
        txtNombre.setText("");
        txtApellido.setText("");
        txtFechaNac.setText("");
        txtEmpresa.setText("");
        txtPuesto.setText("");
        listaPersonas.clearSelection();
        selectedPersonaId = -1;
    }

    private void limpiarTelefono(){
        modeloTelefonos.clear();
        txtTelefono.setText("");
        txtTelefonoLabel.setText("");
    }

    private void limpiarCorreo(){
        modeloCorreos.clear();
        txtCorreo.setText("");
        txtCorreoLabel.setText("");
    }

    private void limpiarDireccion(){
        modeloDir.clear();
        txtPais.setText("");
        txtDir1.setText("");
        txtDir2.setText("");
        txtCodigoPostal.setText("");
        txtCiudad.setText("");
        txtProvincia.setText("");
        txtPOBox.setText("");
        txtDirLabel.setText("");
        txtDirLabel.setText("");
    }

    private void selectPersonaById(int id) {
    for (int i = 0; i < modeloLista.size(); i++) {
        if (modeloLista.getElementAt(i).getId() == id) {
            listaPersonas.setSelectedIndex(i);
            listaPersonas.ensureIndexIsVisible(i);
            return;
        }
    }
    listaPersonas.clearSelection();
}

    private boolean guardarTelefono(){
    // AGREGAR
        String numero = txtTelefono.getText().trim();
        String label = txtTelefonoLabel.getText().trim();
        if (numero.isEmpty()) return false;
        if (!val.validarTelefonoString(numero)) {
            JOptionPane.showMessageDialog(this, 
            textos.getString("msg.errorTelefono"),
            textos.getString("app.title"),
            JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (label.isEmpty()) label = "Otro";

        Persona persona = listaPersonas.getSelectedValue();

        // prevenir duplicados
        if (telefonoExisteEnModelo(numero)) {
            JOptionPane.showMessageDialog(this, 
                textos.getString("msg.numeroExiste"),
                textos.getString("app.title"),
                JOptionPane.WARNING_MESSAGE);
            return false;
        }

        Telefono nuevo;
        if (persona != null) {
            // Guardar directamente en BD
            int idTel = dao.insertarTelefono(persona.getId(), numero, label);
            nuevo = new Telefono(idTel, numero, label);
        } else {
            // Guardar temporalmente (persona no existe todavía)
            nuevo = new Telefono(0, numero, label);
            telefonosTemporales.add(nuevo);
        }

        modeloTelefonos.addElement(nuevo);
        txtTelefono.setText("");
        txtTelefonoLabel.setText("");
        return true;
    }
    
    private boolean guardarCorreo(){
    String correo = txtCorreo.getText().trim();
        String label = txtCorreoLabel.getText().trim();
        if (correo.isEmpty()) return false;
        if (!val.validarEmailString(correo)) {
            JOptionPane.showMessageDialog(this, 
                textos.getString("msg.errorCorreo"),
                textos.getString("app.title"),
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (label.isEmpty()) label = "Otro";

        Correo nuevo = new Correo(correo, label);
        Persona persona = listaPersonas.getSelectedValue();
        if (persona != null) {
            dao.insertarCorreo(persona.getId(), correo, label);
        } else {
            correosTemporales.add(nuevo);
        }
        modeloCorreos.addElement(nuevo);
        txtCorreo.setText("");
        txtCorreoLabel.setText("");
        return true;
    }

    private boolean guardarDir(){
        String pais = txtPais.getText().trim();
        String dir1 = txtDir1.getText().trim();
        String dir2 = txtDir2.getText().trim();
        String codigoPostal = txtCodigoPostal.getText().trim();
        String ciudad = txtCiudad.getText().trim();
        String provincia = txtProvincia.getText().trim();
        String poBox = txtPOBox.getText().trim();
        String label = txtDirLabel.getText().trim();

        if (dir1.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                textos.getString("msg.nohaydireccion"),
                textos.getString("app.title"),
                JOptionPane.WARNING_MESSAGE);
            
            return false;
        }
        if (label.isEmpty()) label = "Otro";

        Dir nuevaDir = new Dir(pais, dir1, dir2, codigoPostal, ciudad, provincia, poBox, label);
        Persona persona = listaPersonas.getSelectedValue();

        if (persona != null) {
            dao.insertarDir(persona.getId(), nuevaDir);
        } else {
            dirTemporales.add(nuevaDir);
        }

        modeloDir.addElement(nuevaDir);

        txtPais.setText("");
        txtDir1.setText("");
        txtDir2.setText("");
        txtCodigoPostal.setText("");
        txtCiudad.setText("");
        txtProvincia.setText("");
        txtPOBox.setText("");
        txtDirLabel.setText("");
        return true;
    }

    private void sincronizarCamposConModelos() {
    // --- TELÉFONO ---
    String numero = txtTelefono.getText().trim();
    String labelTel = txtTelefonoLabel.getText().trim();
    if (!numero.isEmpty()) {
        boolean existe = false;
        for (int i = 0; i < modeloTelefonos.size(); i++) {
            if (modeloTelefonos.getElementAt(i).getNumero().equals(numero)) {
                existe = true;
                break;
            }
        }
        if (!existe) {
            if (labelTel.isEmpty()) labelTel = "Otro";
            modeloTelefonos.addElement(new Telefono(0, numero, labelTel));
        }
    }

    // --- CORREO ---
    String correo = txtCorreo.getText().trim();
    String labelCor = txtCorreoLabel.getText().trim();
    if (!correo.isEmpty()) {
        boolean existe = false;
        for (int i = 0; i < modeloCorreos.size(); i++) {
            if (modeloCorreos.getElementAt(i).getCorreo().equals(correo)) {
                existe = true;
                break;
            }
        }
        if (!existe) {
            if (labelCor.isEmpty()) labelCor = "Otro";
            modeloCorreos.addElement(new Correo(0, correo, labelCor));
        }
    }

    // --- DIRECCIÓN ---
    String pais = txtPais.getText().trim();
    String dir1 = txtDir1.getText().trim();
    String dir2 = txtDir2.getText().trim();
    String postal = txtCodigoPostal.getText().trim();
    String ciudad = txtCiudad.getText().trim();
    String provincia = txtProvincia.getText().trim();
    String poBox = txtProvincia.getText().trim(); // tu campo de PO Box
    String labelDir = txtDirLabel.getText().trim();

    // Solo si hay al menos algo en dirección
    if (!pais.isEmpty() || !dir1.isEmpty() || !dir2.isEmpty() ||
        !postal.isEmpty() || !ciudad.isEmpty() || !provincia.isEmpty() || !poBox.isEmpty()) {

        Dir nueva = new Dir(0, pais, dir1, dir2, postal, ciudad, provincia, poBox,
                labelDir.isEmpty() ? "Principal" : labelDir);

        boolean existe = false;
        for (int i = 0; i < modeloDir.size(); i++) {
            Dir d = modeloDir.getElementAt(i);
            if (d.getDireccion1().equalsIgnoreCase(dir1) && d.getCodigoPostal().equalsIgnoreCase(postal)) {
                existe = true;
                break;
            }
        }
        if (!existe) modeloDir.addElement(nueva);
    }
}
    
    private LocalDate validarFecha(String textoFecha, Locale locale) {
        String[] formatos;
        switch (locale.getLanguage()) {
            case "en" -> formatos = new String[]{"MM/dd/yyyy", "yyyy-MM-dd"};
            case "fr", "pt", "es" -> formatos = new String[]{"dd/MM/yyyy", "yyyy-MM-dd"};
            default -> formatos = new String[]{"yyyy-MM-dd"};
        }

        for (String formato : formatos) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formato);
                return LocalDate.parse(textoFecha, formatter);
            } catch (DateTimeParseException ignored) {}
        }
        return null;
    }

    private String formatearFechaParaMostrar(String fechaISO, Locale locale) {
    if (fechaISO == null || fechaISO.isEmpty()) return "";
    try {
        LocalDate fecha = LocalDate.parse(fechaISO); // asume yyyy-MM-dd
        DateTimeFormatter formato;
        switch (locale.getLanguage()) {
            case "en" -> formato = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            case "fr", "pt", "es" -> formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            default -> formato = DateTimeFormatter.ISO_LOCAL_DATE;
        }
        return fecha.format(formato);
    } catch (DateTimeParseException e) {
        return fechaISO; // si hay error, devolver texto original
    }
}   

    private void importarContactosDesdeCSV() {
    JFileChooser fileChooser = new JFileChooser();
    FileNameExtensionFilter filtro = new FileNameExtensionFilter("Archivos CSV (*.csv)", "csv");
    fileChooser.setFileFilter(filtro);
    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        File archivo = fileChooser.getSelectedFile();
        try (Reader reader = new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8)) {

            CSVParser parser = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withIgnoreHeaderCase()
                    .withTrim()
                    .parse(reader);

            // === POSIBLES COLUMNAS ===
            String[] posiblesNombre = {"Given Name", "First Name", "Nombre", "Name", "Nombres"};
            String[] posiblesApellido = {"Family Name", "Last Name", "Apellidos", "Apellido", "Surname"};
            String[] posiblesTelefono = {
                "Phone 1 - Value", "Phone 2 - Value", "Phone 3 - Value",
                "Teléfono 1 - Valor", "Teléfono 2 - Valor", "Teléfono 3 - Valor",
                "Phone", "Phone Number"
            };
            String[] posiblesEmail = {
                "E-mail 1 - Value", "E-mail 2 - Value", "E-mail 3 - Value",
                "Email 1 - Value", "Email 2 - Value", "Email 3 - Value",
                "Correo electrónico 1 - Valor", "Correo electrónico 2 - Valor", "Correo electrónico 3 - Valor"
            };
            String[] posiblesEmpresa = {"Organization 1 - Name", "Organization Name", "Empresa", "Company"};
            String[] posiblesPuesto = {"Organization 1 - Title", "Organization Title", "Cargo", "Puesto", "Job Title"};

            int cantidad = 0;

            for (CSVRecord record : parser) {
                String nombre = getCampoFlexible(record, posiblesNombre);
                String apellido = getCampoFlexible(record, posiblesApellido);
                String empresa = getCampoFlexible(record, posiblesEmpresa);
                String puesto = getCampoFlexible(record, posiblesPuesto);

                if ((nombre + apellido).trim().isEmpty()) continue;

                Persona existente = dao.buscarPersona(nombre, apellido);
                if (existente != null) continue;

                cantidad++;
                Persona nueva = new Persona(0, nombre, apellido, "", empresa, puesto);
                int idPersona = dao.insertar(nueva);

                // === TELÉFONOS ===
                for (String posibleTel : posiblesTelefono) {
                    if (record.isMapped(posibleTel)) {
                        String telefonosRaw = record.get(posibleTel).trim();
                        if (!telefonosRaw.isEmpty()) {
                            // Soporta múltiples teléfonos en un solo campo
                            String[] telefonos = telefonosRaw.split("\\s*[:;,/|]+\\s*");
                            for (String tel : telefonos) {
                                tel = tel.trim();
                                if (!tel.isEmpty()) dao.insertarTelefono(idPersona, tel, "Móvil");
                            }
                        }
                    }
                }

                // === CORREOS ===
                for (String posibleEmail : posiblesEmail) {
                    if (record.isMapped(posibleEmail)) {
                        String email = record.get(posibleEmail).trim();
                        if (!email.isEmpty()) dao.insertarCorreo(idPersona, email, "Personal");
                    }
                }

                // === DIRECCIONES === (Address 1 - X)
int maxDir = 5; // hasta 5 direcciones (1-5)
for (int i = 1; i <= maxDir; i++) {
    String base = "Address " + i + " - ";
    String dirLabel = getSafe(record, base + "Label");
    String dirFormatted = getSafe(record, base + "Formatted");
    String dirStreet = getSafe(record, base + "Street");
    String dirCity = getSafe(record, base + "City");
    String dirPOBox = getSafe(record, base + "PO Box");
    String dirRegion = getSafe(record, base + "Region");
    String dirPostal = getSafe(record, base + "Postal Code");
    String dirCountry = getSafe(record, base + "Country");
    String dirExtended = getSafe(record, base + "Extended Address");

    // Normalizar saltos de línea y espacios
    dirFormatted = dirFormatted.replace("\n", ", ").trim();
    dirStreet = dirStreet.replace("\n", ", ").trim();
    dirCity = dirCity.replace("\n", ", ").trim();
    dirRegion = dirRegion.replace("\n", ", ").trim();
    dirCountry = dirCountry.replace("\n", ", ").trim();

    // Si la dirección formateada tiene contenido, úsala como fuente principal
    if (!dirFormatted.isEmpty()) {
        Dir nuevaDir = new Dir(
                dirCountry.isEmpty() ? extraerPais(dirFormatted) : dirCountry,
                dirFormatted, // usar el texto completo como dirección 1
                dirExtended,
                dirPostal,
                dirCity.isEmpty() ? extraerCiudad(dirFormatted) : dirCity,
                dirRegion,
                dirPOBox,
                dirLabel.isEmpty() ? "Casa" : dirLabel
        );
        dao.insertarDir(idPersona, nuevaDir);
        continue; // pasar a la siguiente dirección
    }

    // Si no existe "Formatted", usar las partes sueltas
    if (!dirStreet.isEmpty() || !dirCity.isEmpty() || !dirCountry.isEmpty()) {
        Dir nuevaDir = new Dir(
                dirCountry,
                dirStreet,
                dirExtended,
                dirPostal,
                dirCity,
                dirRegion,
                dirPOBox,
                dirLabel.isEmpty() ? "Casa" : dirLabel
        );
        dao.insertarDir(idPersona, nuevaDir);
    }
}

            }

            JOptionPane.showMessageDialog(this,
                    cantidad + " " + textos.getString("msg.contactosImportados"),
                    textos.getString("app.title"),
                    JOptionPane.INFORMATION_MESSAGE);

            cargarPersonas();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    textos.getString("msg.errorImportar"),
                    textos.getString("app.title"),
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}

    private String getSafe(CSVRecord record, String header) {
    if (record.isMapped(header)) {
        String val = record.get(header);
        return val != null ? val.trim() : "";
    }
    return "";
}
    
    /**
 * Extrae el país del texto de dirección formateada (heurística simple)
 */
    private String extraerPais(String texto) {
    if (texto == null || texto.isEmpty()) return "";
    String lower = texto.toLowerCase();
    String[] paises = {"venezuela", "colombia", "perú", "mexico", "españa", "argentina", "chile", "usa", "ecuador"};
    for (String p : paises) {
        if (lower.contains(p)) return capitalizar(p);
    }
    return "";
}

/**
 * Extrae una ciudad probable del texto de dirección formateada
 */
    private String extraerCiudad(String texto) {
    if (texto == null || texto.isEmpty()) return "";
    String[] partes = texto.split(",");
    if (partes.length >= 2) return partes[partes.length - 2].trim();
    return "";
}

    private String capitalizar(String s) {
    if (s == null || s.isEmpty()) return "";
    return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
}

    private String getCampoFlexible(CSVRecord record, String[] posiblesNombres) {
    for (String nombreColumna : posiblesNombres) {
        if (record.isMapped(nombreColumna)) {
            String valor = record.get(nombreColumna).trim();
            if (!valor.isEmpty()) return valor;
        }
    }
    return "";
}

    private void exportarContactosAGoogleCSV() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Guardar contactos como CSV");
    int userSelection = fileChooser.showSaveDialog(this);

    if (userSelection == JFileChooser.APPROVE_OPTION) {
        File archivo = fileChooser.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".csv")) {
            archivo = new File(archivo.getAbsolutePath() + ".csv");
        }

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(archivo), StandardCharsets.UTF_8))) {

            // Cabecera oficial de Google Contacts
            writer.println("First Name,Middle Name,Last Name,Phonetic First Name,Phonetic Middle Name,Phonetic Last Name,"
                    + "Name Prefix,Name Suffix,Nickname,File As,Organization Name,Organization Title,Organization Department,"
                    + "Birthday,Notes,Photo,Labels,"
                    + "E-mail 1 - Label,E-mail 1 - Value,E-mail 2 - Label,E-mail 2 - Value,"
                    + "Phone 1 - Label,Phone 1 - Value,Phone 2 - Label,Phone 2 - Value,"
                    + "Address 1 - Label,Address 1 - Formatted,Address 1 - Street,Address 1 - City,Address 1 - PO Box,"
                    + "Address 1 - Region,Address 1 - Postal Code,Address 1 - Country,Address 1 - Extended Address,"
                    + "Website 1 - Label,Website 1 - Value,Event 1 - Label,Event 1 - Value");

            List<Persona> personas = dao.obtenerTodas();

            for (Persona p : personas) {
                String nombre = p.getNombre() != null ? p.getNombre() : "";
                String apellido = p.getApellido() != null ? p.getApellido() : "";
                String empresa = p.getEmpresa() != null ? p.getEmpresa() : "";
                String puesto = p.getPuesto() != null ? p.getPuesto() : "";
                String fecha = (p.getFechaNac() != null && !p.getFechaNac().isEmpty()) ? p.getFechaNac() : "";

                // --- Correos ---
                List<Correo> correos = dao.obtenerCorreos(p.getId());
                String email1Label = correos.size() > 0 ? correos.get(0).getLabel() : "";
                String email1Value = correos.size() > 0 ? correos.get(0).getCorreo() : "";
                String email2Label = correos.size() > 1 ? correos.get(1).getLabel() : "";
                String email2Value = correos.size() > 1 ? correos.get(1).getCorreo() : "";

                // --- Teléfonos ---
                List<Telefono> telefonos = dao.obtenerTelefonos(p.getId());
                String tel1Label = telefonos.size() > 0 ? telefonos.get(0).getLabel() : "";
                String tel1Value = telefonos.size() > 0 ? telefonos.get(0).getNumero() : "";
                String tel2Label = telefonos.size() > 1 ? telefonos.get(1).getLabel() : "";
                String tel2Value = telefonos.size() > 1 ? telefonos.get(1).getNumero() : "";

                // --- Dirección ---
                List<Dir> dirs = dao.obtenerDirs(p.getId());
                String dirLabel = dirs.size() > 0 ? dirs.get(0).getLabel() : "";
                String dirFormatted = "";
                String dirStreet = "";
                String dirCity = "";
                String dirPOBox = "";
                String dirRegion = "";
                String dirPostal = "";
                String dirCountry = "";
                String dirExtended = "";
                if (dirs.size() > 0) {
                    Dir d = dirs.get(0);
                    dirFormatted = (d.getDireccion1() + " " + d.getDireccion2()).trim();
                    dirStreet = d.getDireccion1() != null ? d.getDireccion1() : "";
                    dirCity = d.getCiudad() != null ? d.getCiudad() : "";
                    dirPOBox = d.getPoBox() != null ? d.getPoBox() : "";
                    dirRegion = d.getProvincia() != null ? d.getProvincia() : "";
                    dirPostal = d.getCodigoPostal() != null ? d.getCodigoPostal() : "";
                    dirCountry = d.getPais() != null ? d.getPais() : "";
                    dirExtended = d.getDireccion2() != null ? d.getDireccion2() : "";
                }

                // Escapar comas con comillas dobles
                String csvSafe = String.join(",", new String[]{
                    quote(nombre),
                    "", // Middle Name
                    quote(apellido),
                    "", "", "", "", "", "", "", // phonetic, prefix, suffix, nickname, file as
                    quote(empresa), quote(puesto), "", // organization, title, dept
                    quote(fecha), "", "", "", // birthday, notes, photo, labels
                    quote(email1Label), quote(email1Value),
                    quote(email2Label), quote(email2Value),
                    quote(tel1Label), quote(tel1Value),
                    quote(tel2Label), quote(tel2Value),
                    quote(dirLabel), quote(dirFormatted), quote(dirStreet),
                    quote(dirCity), quote(dirPOBox), quote(dirRegion),
                    quote(dirPostal), quote(dirCountry), quote(dirExtended),
                    "", "", "", "" // website, event
                });

                writer.println(csvSafe);
            }

            writer.flush();

            JOptionPane.showMessageDialog(this,
                    textos.getString("msg.exportado"),
                    textos.getString("app.title"),
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    textos.getString("msg.errorExportar"),
                    textos.getString("app.title"),
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}

    private String quote(String value) {
    if (value == null) return "";
    value = value.replace("\"", "\"\""); // escapar comillas
    return "\"" + value + "\"";
}

    private void generarQRDeContacto(Persona p) {
    if (p == null) {
        JOptionPane.showMessageDialog(this, 
            textos.getString("msg.noSeleccionado"),
            textos.getString("app.title"),
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    StringBuilder vcard = new StringBuilder();
    vcard.append("BEGIN:VCARD\r\n");
    vcard.append("VERSION:3.0\r\n");
    vcard.append("N:").append(p.getApellido()).append(";").append(p.getNombre()).append("\r\n");
    vcard.append("FN:").append(p.getNombre()).append(" ").append(p.getApellido()).append("\r\n");
    if (!p.getEmpresa().isEmpty()) vcard.append("ORG:").append(p.getEmpresa()).append("\r\n");
    if (!p.getPuesto().isEmpty()) vcard.append("TITLE:").append(p.getPuesto()).append("\r\n");

    for (Telefono t : dao.obtenerTelefonos(p.getId())) {
        vcard.append("TEL;TYPE=").append(t.getLabel()).append(":").append(t.getNumero()).append("\r\n");
    }
    for (Correo c : dao.obtenerCorreos(p.getId())) {
        vcard.append("EMAIL;TYPE=").append(c.getLabel()).append(":").append(c.getCorreo()).append("\r\n");
    }

    vcard.append("END:VCARD\r\n");

    try {
        int size = 300;
        BitMatrix matrix = new MultiFormatWriter()
                .encode(vcard.toString(), BarcodeFormat.QR_CODE, size, size);
        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
        JOptionPane.showMessageDialog(this, new JLabel(new ImageIcon(image)),
                "QR de " + p.getNombre(), JOptionPane.PLAIN_MESSAGE);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, 
                textos.getString("msg.errorQR"),
                textos.getString("app.title"),
                JOptionPane.WARNING_MESSAGE);
    }
}

    private void filtrarPersonas() {
    String filtro = txtBuscar.getText().trim().toLowerCase();

    modeloLista.clear();
    if (filtro.isEmpty()) {
        cargarPersonas(); // muestra todo
        return;
    }

    // 🔹 Buscar en la base de datos o lista cargada
    List<Persona> personas = dao.obtenerTodas(); // o dao.buscarPorTexto(filtro) si lo implementas
    for (Persona p : personas) {
        boolean coincide = false;

        // Buscar en nombre/apellido
        if ((p.getNombre() + " " + p.getApellido()).toLowerCase().contains(filtro)) coincide = true;

        if (coincide) modeloLista.addElement(p);
    }
}

    
    /**
     * @param args the command line arguments
     */
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnAgregarCorreo;
    private javax.swing.JButton btnAgregarDireccion;
    private javax.swing.JButton btnAgregarTelefono;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JLabel labelCiudad;
    private javax.swing.JLabel labelCorreo;
    private javax.swing.JLabel labelDir1;
    private javax.swing.JLabel labelDir2;
    private javax.swing.JLabel labelDireccion;
    private javax.swing.JLabel labelEmpresa;
    private javax.swing.JLabel labelEtiqueta1;
    private javax.swing.JLabel labelEtiqueta2;
    private javax.swing.JLabel labelEtiqueta3;
    private javax.swing.JLabel labelFechaNac;
    private javax.swing.JLabel labelLast;
    private javax.swing.JLabel labelName;
    private javax.swing.JLabel labelPOBox;
    private javax.swing.JLabel labelPais;
    private javax.swing.JLabel labelPostal;
    private javax.swing.JLabel labelProvincia;
    private javax.swing.JLabel labelPuesto;
    private javax.swing.JLabel labelTelefono;
    private javax.swing.JList<Correo> listaCorreos;
    private javax.swing.JList<Dir> listaDir;
    private javax.swing.JList<Persona> listaPersonas;
    private javax.swing.JList<Telefono> listaTelefonos;
    private javax.swing.JScrollPane scrollLista;
    private javax.swing.JTextField txtApellido;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtCiudad;
    private javax.swing.JTextField txtCodigoPostal;
    private javax.swing.JTextField txtCorreo;
    private javax.swing.JTextField txtCorreoLabel;
    private javax.swing.JTextField txtDir1;
    private javax.swing.JTextField txtDir2;
    private javax.swing.JTextField txtDirLabel;
    private javax.swing.JTextField txtEmpresa;
    private javax.swing.JTextField txtFechaNac;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtPOBox;
    private javax.swing.JTextField txtPais;
    private javax.swing.JTextField txtProvincia;
    private javax.swing.JTextField txtPuesto;
    private javax.swing.JTextField txtTelefono;
    private javax.swing.JTextField txtTelefonoLabel;
    // End of variables declaration//GEN-END:variables
}
