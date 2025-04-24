import client.ApiClient;
import client.AuthClient;
import client.TokenStorage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;

public class EditorFrame extends JFrame {
    private EditorPanel editorPanel;
    private JPanel toasterPanel;
    
    public EditorFrame() {
        setTitle("Spielfeld Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        editorPanel = new EditorPanel();
        add(editorPanel, BorderLayout.CENTER);
        
        // Toaster panel setup
        toasterPanel = new JPanel();
        toasterPanel.setLayout(new BoxLayout(toasterPanel, BoxLayout.Y_AXIS));
        toasterPanel.setOpaque(false);
        add(toasterPanel, BorderLayout.SOUTH);

        JMenuBar menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("Datei");
        fileMenu.add(createMenuItem("Importieren", e -> editorPanel.importFromXML()));
        fileMenu.add(createMenuItem("Speichern", e -> editorPanel.exportToXML()));
        fileMenu.add(createMenuItem("Autoconnect", e -> editorPanel.autoconnect()));
        menuBar.add(fileMenu);
        
        // Server Menu
        JMenu serverMenu = new JMenu("Server");
        serverMenu.add(createMenuItem("Login", e -> showLoginDialog()));
        serverMenu.add(createMenuItem("Laden", e -> loadFromServer()));
        serverMenu.add(createMenuItem("Speichern", e -> showUploadDialog()));
        menuBar.add(serverMenu);
        
        setJMenuBar(menuBar);
    }
    
    private JMenuItem createMenuItem(String text, ActionListener listener) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(listener);
        return item;
    }
    private void showUploadDialog() {
        JDialog loginDialog = new JDialog(this, "Upload", true);
        loginDialog.setLayout(new GridLayout(3, 2));

        JTextField gameNameField = new JTextField(); //todo: read and input game name when loaded from server

        loginDialog.add(new JLabel("Name of the game:"));
        loginDialog.add(gameNameField);

        JButton loginButton = new JButton("Upload");
        loginButton.addActionListener(e -> {
            saveToServer(gameNameField.getText());
            loginDialog.dispose();
        });

        loginDialog.add(new JLabel());
        loginDialog.add(loginButton);
        loginDialog.pack();
        loginDialog.setLocationRelativeTo(this);
        loginDialog.setVisible(true);
    }
    
    private void showLoginDialog() {
        JDialog loginDialog = new JDialog(this, "Login", true);
        loginDialog.setLayout(new GridLayout(3, 2));
        
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        
        loginDialog.add(new JLabel("Benutzername:"));
        loginDialog.add(usernameField);
        loginDialog.add(new JLabel("Passwort:"));
        loginDialog.add(passwordField);
        
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> {
            try {
                String token = AuthClient.login(usernameField.getText(), new String(passwordField.getPassword()));
                TokenStorage.saveToken(token);
                showToast("Erfolgreich eingeloggt");
                loginDialog.dispose();
            } catch (IOException ex) {
                showToast("Login fehlgeschlagen: " + ex.getMessage());
            }
        });
        
        loginDialog.add(new JLabel());
        loginDialog.add(loginButton);
        loginDialog.pack();
        loginDialog.setLocationRelativeTo(this);
        loginDialog.setVisible(true);
    }
    

    private void loadFromServer() {
        try {
            String xmlData = ApiClient.sendAuthenticatedRequest("https://localhost:8443/api/load", "GET", null);
            editorPanel.loadFromXMLString(xmlData);
            showToast("Daten erfolgreich geladen");
        } catch (Exception e) {
            showToast("Fehler beim Laden: " + e.getMessage());
        }
    }
    
    private void saveToServer(String gameName) {
        try {
            String xmlData = editorPanel.exportToString();
            String jsonData = "{\"name\": \"" + gameName + "\", \"spielerAnzahl\": \"" + editorPanel.getSpielerAnzahl() + "\"}";
            ApiClient.uploadWithHttpClient(TokenStorage.getToken(), xmlData, jsonData);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            showToast("Fehler beim Speichern: " + e.getMessage());
        }
    }
    
    public void showToast(String message) {
        JLabel toast = new JLabel(message);
        toast.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        toast.setBackground(new Color(50, 50, 50));
        toast.setForeground(Color.WHITE);
        toast.setOpaque(true);
        
        toasterPanel.add(toast);
        toasterPanel.revalidate();
        
        Timer timer = new Timer(3000, e -> {
            toasterPanel.remove(toast);
            toasterPanel.revalidate();
        });
        timer.setRepeats(false);
        timer.start();
    }
}