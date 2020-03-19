package prueba_as;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import Conexion.Conectar;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import javax.swing.DefaultListModel;
import java.net.ProtocolException;
import javax.swing.Timer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import org.json.JSONException;

public class Principal extends javax.swing.JFrame {

    private String comando, ruta;
    private Conectar base;
    private final DefaultListModel modelolista = new DefaultListModel();
    private final int numero = 0;
    private Timer timer;
    

    public Principal() {
        try {
            initComponents();
            Iniciar();
            formato();
            timerFrecuencia();
        } catch (ProtocolException | SQLException | JSONException e) {
            Error("Error en la configuracion de la base de datos");
            
        }
            
        
            
        
    }
    private void formato(){// formato para la vista principal
            this.setTitle("Agente de Sincronización");
            setIconImage(new ImageIcon(getClass().getResource("/prueba_as/isotipo-kiotrack-color.png")).getImage());
            this.Nombre.setForeground(Color.white);
            this.Nombre.setOpaque(true);
            this.Nombre.setBackground(Color.GRAY);
            this.logs.setModel(modelolista);
            Servidor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/prueba_as/ico-servidor-offline.png")));
    }
    private void Iniciar() throws ProtocolException, JSONException, SQLException {//para que se centre la ventana
        String sistema = System.getProperty("os.name").toLowerCase().split(" ")[0];
        this.setLocationRelativeTo(null);
        if (sistema.contains("win")) {//aqui pueden configurar la ruta de windows
            ruta = "C:\\Arcos\\Log";
            comando = "explorer.exe ";
        } else if (sistema.contains("linux")) {//aqui pueden configurar la ruta de linux
            ruta = "/Arcos/Log";
            comando = "xdg-open ";
        } else if (sistema.contains("mac")) {//aqui pueden configurar la ruta de mac
            ruta = "/Users/Nono/Arcos/Log";
            comando = "open ";
        }
        Config();
        try {
            try {
                lecturaPendiente();
            } catch (UnsupportedEncodingException ex) {
                Error(ex.getMessage());
            }
        } catch (IOException ex) {
            Error(ex.getMessage());
        }
    }

    private void Config() {//aqui se crea el archivo.config
        File directorio = new File(ruta);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }
        File archivo = new File(ruta + "/BDLocal.config");
        try {
            if (archivo.createNewFile()) {
                añadirLista("se creo el archivo config");
            } else {
                añadirLista("ya existe el archivo config");
                base = new Conectar();
                Scanner leer = new Scanner(archivo);
                while (leer.hasNextLine()) {
                    String[] dato = leer.nextLine().split(": ");
                    switch (dato[0]) {
                        case "host":
                            base.setHost(dato[1]);
                            break;
                        case "puerto":
                            base.setPuerto(dato[1]);
                            break;
                        case "BD":
                            base.setBD(dato[1]);
                            break;
                        case "usuario":
                            base.setUsuario(dato[1]);
                            break;
                        case "clave":
                            base.setClave(dato[1]);
                            break;
                    }
                }
                boolean temp = base.conectar();
                if (temp) {
                    añadirLista("se conecto a la base de datos");
                } else {
                    Error("no se conecto a la base de datos");
                }

            }
        } catch (IOException ex) {
            Error(ex.getMessage());
        }
    }

    private void lecturaPendiente() throws IOException, ProtocolException, UnsupportedEncodingException, JSONException, SQLException {
        try {
            String consulta = "select nombre from arco";// consulta para los datos del arco
            ResultSet resp = base.query(consulta);
            while (resp.next()) {
                System.out.println("hola si entro");
                this.Nombre.setText("Nombre: " + resp.getString(1));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            Error(e.getMessage());
        }

        try {
            String sql = "select lsr.nci,lsr.tid,a.ubicacion,lsr.fk_id_carril,lsr.fk_id_arco,lsr.niv, lsr.id_lectura\n" // consulta para sincronizar lectura
                    + "from lectura_chip_repuve as lsr join arco as a on lsr.fk_id_arco = a.id_arco\n"
                    + "where estatus_sincronizacion = '0'";

            ResultSet rs = base.query(sql);
            System.out.println("----->" + sql);

            while (rs.next()) {
                System.out.println("hola si entro");
                EnviarLectura(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6));
                actualizarEstado(rs.getString(7));

            }

        } catch (IOException | SQLException | JSONException e) {
            Error(e.getMessage());
        }

    }

    private void Error(String error) { //aqui se crean los logs
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/YY\tHH:mm:ss.SSSSSS\t");
        Date fecha = new Date();
        error = formato.format(fecha) + error;
        String nombrelog = "/LogAgenteErr" + new SimpleDateFormat("ddMMYY").format(fecha) + ".log";
        modelolista.addElement(error);
        File log = new File(ruta + nombrelog);
        try {
            log.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(log));
            bw.write(error + "\n");
            bw.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }

    private void añadirLista(String dato) {//aqui se muestra todo en la vista principal
        try {
            SimpleDateFormat formato = new SimpleDateFormat("dd/MM/YY\t\t\tHH:mm:ss.SSSSSS\t\t\t");
            Date fecha = new Date();
            dato = formato.format(fecha) + dato;
            modelolista.addElement(dato);
        } catch (Exception e) {
            Error(e.getMessage());
        }
        
        
    }
    private void iconoServidor(int validar){// se validan el estatus del servidor
        if (validar == 1) {
            Servidor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/prueba_as/ico-servidor-online.png")));
        } else {
            Servidor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/prueba_as/ico-servidor-danger.png")));
        }
    }
    private void EnviarLectura(String nci, String tid, String ubicacion, String fk_id_carril, String fk_id_arco, String nvi) throws UnsupportedEncodingException, ProtocolException, IOException, JSONException {
        //aqui se realiza la sincronizacion con el servidor
        System.out.println("**********************");
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("REALIZANDO SINCRONIZACION");
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("**********************");
        java.net.URL url = new java.net.URL(this.guardarEnlace());
        //"http://35.225.13.244:3001/webservice/registrar_lectura"
        Map<String, Object> params = new LinkedHashMap<>();

        params.put("p_nci", nci);
        params.put("p_tid", tid);
        params.put("p_ubicacion", ubicacion);
        params.put("p_fk_id_carril", fk_id_carril);
        params.put("p_fk_id_arco", fk_id_arco);
        params.put("p_fk_niv", nvi);

        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (postData.length() != 0) {
                postData.append('&');
            }
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()),
                    "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length",
                String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        try {
            conn.getOutputStream().write(postDataBytes);
            String cadena = "";
            Reader in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(), "UTF-8"));
            for (int c = in.read(); c != -1; c = in.read()) {
                cadena = cadena + (char) c; //System.out.print((char) c);
            }

            System.out.println("Respuesta contruida " + cadena);
            int validar = 1;
            iconoServidor(validar);
        } catch (ConnectException ex) {
            Error(ex.getMessage());
            int validar = 2;
            iconoServidor(validar);

        }
    }

    private void actualizarEstado(String id_lectura) {// aqui se actualiza el estado de sincronizacion
        try {
            String update = "update lectura_chip_repuve set estatus_sincronizacion = '1' where id_lectura= " + id_lectura;
            System.out.println("--->" + update);
            if (base.update(update) == 1) {
                añadirLista("lectura sincronizada");
            } else {
                añadirLista("lectura no sincronizada");
                Error("Lectura no sincronizada");
            }
        } catch (Exception e) {
            Error(e.getMessage());
        }

    }

    private void timerFrecuencia() {//timer para la frecuencia de sincronizacion
        timer = new Timer(Integer.parseInt(this.frecuencia.getText()) * 1000, (ActionEvent e) -> {
            try {
                lecturaPendiente();
            } catch (IOException | JSONException | SQLException ex) {
                Error(ex.getMessage());
            }
        });
        timer.start();
    }

    private String guardarEnlace() { //metodo que trae el texto del textfiel ```´`````````
        return this.enlace_servidor.getText();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        logs = new javax.swing.JList<>();
        jLabel1 = new javax.swing.JLabel();
        enlace_servidor = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        frecuencia = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        guardar = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        Acerca = new javax.swing.JButton();
        config = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        Servidor = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        Ubicacion = new javax.swing.JLabel();
        Nombre = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        logs.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jScrollPane1.setViewportView(logs);

        jLabel1.setText("Enlace servidor:");

        enlace_servidor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enlace_servidorActionPerformed(evt);
            }
        });

        jLabel2.setText("Frecuencia de sincronizacion");

        frecuencia.setText("5");
        frecuencia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                frecuenciaActionPerformed(evt);
            }
        });

        jLabel3.setText("Segundos");

        guardar.setText("Guardar");
        guardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guardarActionPerformed(evt);
            }
        });

        jLabel5.setText("Servidor");

        Acerca.setIcon(new javax.swing.ImageIcon(getClass().getResource("/prueba_as/ico-acerca-de-azul.png"))); // NOI18N
        Acerca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AcercaActionPerformed(evt);
            }
        });

        config.setIcon(new javax.swing.ImageIcon("/Users/Nono/Desktop/ico-logs-azul.png")); // NOI18N
        config.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configActionPerformed(evt);
            }
        });

        jLabel4.setText("Fecha");

        jLabel6.setText("Hora");

        jLabel7.setText("Descripción");

        jPanel1.setLayout(null);

        Ubicacion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/prueba_as/ico-marker-blanco.png"))); // NOI18N
        jPanel1.add(Ubicacion);
        Ubicacion.setBounds(570, 10, 21, 28);
        jPanel1.add(Nombre);
        Nombre.setBounds(0, 0, 610, 50);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(frecuencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(guardar, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(enlace_servidor))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(14, 14, 14)
                                        .addComponent(config, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(Acerca, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(Servidor, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel5))))
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(48, 48, 48)
                        .addComponent(jLabel6)
                        .addGap(101, 101, 101)
                        .addComponent(jLabel7)))
                .addContainerGap(29, Short.MAX_VALUE))
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel6)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(Servidor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(enlace_servidor)))
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(Acerca, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(frecuencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3)
                        .addComponent(guardar, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(config, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(43, 43, 43))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void enlace_servidorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enlace_servidorActionPerformed

    }//GEN-LAST:event_enlace_servidorActionPerformed

    private void frecuenciaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_frecuenciaActionPerformed


    }//GEN-LAST:event_frecuenciaActionPerformed

    private void configActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configActionPerformed
        try {
            Runtime.getRuntime().exec(comando + ruta);
        } catch (IOException ex) {
            Error(ex.getMessage());
        }

    }//GEN-LAST:event_configActionPerformed

    private void AcercaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AcercaActionPerformed
        new Acerca().setVisible(true);
    }//GEN-LAST:event_AcercaActionPerformed

    private void guardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guardarActionPerformed

        timer.stop();
        timerFrecuencia();
        guardarEnlace();

    }//GEN-LAST:event_guardarActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            try {
                new Principal().setVisible(true);
            } catch (Exception ex) {
                Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Acerca;
    private javax.swing.JLabel Nombre;
    private javax.swing.JLabel Servidor;
    private javax.swing.JLabel Ubicacion;
    private javax.swing.JButton config;
    private javax.swing.JTextField enlace_servidor;
    private javax.swing.JTextField frecuencia;
    private javax.swing.JButton guardar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JList<String> logs;
    // End of variables declaration//GEN-END:variables
}
