import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class EditorFrame extends JFrame {
    private EditorPanel editorPanel;

    public EditorFrame() {
        setTitle("Spielfeld Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        editorPanel = new EditorPanel();
        add(editorPanel, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Datei");

        JMenuItem openItem = new JMenuItem(new AbstractAction("Importieren") {
            public void actionPerformed(ActionEvent e) {
                editorPanel.importFromXML();
            }
        });

        JMenuItem saveItem = new JMenuItem(new AbstractAction("Speichern") {
            public void actionPerformed(ActionEvent e) {
                editorPanel.exportToXML();
            }
        });

        JMenuItem autoConnector = new JMenuItem(new AbstractAction("Autoconnect") {
            public void actionPerformed(ActionEvent e) {
                editorPanel.autoconnect();
            }
        });

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(autoConnector);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }
}