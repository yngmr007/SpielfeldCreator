import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;

public class EditorPanel extends JPanel {
    private final int gridSize = 40;
    private java.util.List<Feld> felder = new ArrayList<>();
    private java.util.List<Kante> kanten = new ArrayList<>();
    private Feld selectedFeld = null;

    public EditorPanel() {
        setBackground(Color.WHITE);

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                int x = e.getX() / gridSize;
                int y = e.getY() / gridSize;
                Feld feld = getFeldAt(x, y);
                if (feld != null && feld.data != null && !feld.data.isEmpty()) {
                    setToolTipText("Feld " + feld.id + ": " + feld.data);
                } else {
                    setToolTipText(null);
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int x = e.getX() / gridSize;
                int y = e.getY() / gridSize;

                if (SwingUtilities.isLeftMouseButton(e)) {
                    Feld feld = getFeldAt(x, y);
                    if (feld == null) {
                        feld = new Feld(x, y);
                        felder.add(feld);
                        selectedFeld = null;
                    } else {
                        if (selectedFeld == feld) {
                            selectedFeld = null; // Deselection
                        } else if (selectedFeld != null && selectedFeld != feld && selectedFeld.isNeighbor(feld)) {
                            if (!kanteExists(selectedFeld, feld)) {
                                kanten.add(new Kante(selectedFeld, feld));
                            }
                            selectedFeld = null;
                        } else {
                            selectedFeld = feld;
                        }
                    }
                    repaint();
                } else if (SwingUtilities.isMiddleMouseButton(e)) {
                    Feld feld = getFeldAt(x, y);
                    if (feld != null) {
                        felder.remove(feld);
                        kanten.removeIf(k -> k.getFrom() == feld || k.getTo() == feld);
                        if (selectedFeld == feld) {
                            selectedFeld = null;
                        }
                        repaint();
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    Feld feld = getFeldAt(x, y);
                    if (feld != null) {
                        showContextMenu(e, feld);
                    }
                }
            }
        });
    }
    private boolean kanteExists(Feld a, Feld b) {
        for (Kante k : kanten) {
            if ((k.getFrom() == a && k.getTo() == b) || (k.getFrom() == b && k.getTo() == a)) {
                return true;
            }
        }
        return false;
    }

    private Feld getFeldAt(int x, int y) {
        for (Feld f : felder) {
            if (f.posX == x && f.posY == y) return f;
        }
        return null;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(new Color(211, 209, 209));
        int width = getWidth();
        int height = getHeight();
        for (int x = 0; x < width; x += gridSize) {
            g2d.drawLine(x, 0, x, height);
        }
        for (int y = 0; y < height; y += gridSize) {
            g2d.drawLine(0, y, width, y);
        }

        // Kanten zeichnen
        for (Kante k : kanten) {
            k.draw(g2d, gridSize);
        }

        // Felder zeichnen
        for (Feld f : felder) {
            f.draw(g2d, gridSize, f == selectedFeld);
        }
    }

    public void importFromXML() {
        JFileChooser chooser = new JFileChooser();
        String defaultPath = System.getProperty("user.dir");
        chooser.setCurrentDirectory(new File(defaultPath));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            XMLHandler.load(file, felder, kanten);
            repaint();
        }
    }

    public void exportToXML() {
        JFileChooser chooser = new JFileChooser();
        String defaultPath = System.getProperty("user.dir") + "/spielfeld.xml"; // Standardpfad
        chooser.setSelectedFile(new File(defaultPath));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            XMLHandler.save(file, felder, kanten);
        }
    }

    public void autoconnect() {
        for (Feld feld1 : felder) {
            for (Feld feld2 : felder) {
                if (feld1 != feld2 && feld1.isNeighbor(feld2) && !kanteExists(feld1, feld2)) {
                    kanten.add(new Kante(feld1, feld2));
                }
            }
        }
        repaint();
    }

    public void removeDuplicateConnections() {
        Set<Kante> uniqueKanten = new HashSet<>(kanten);
        kanten.clear();
        kanten.addAll(uniqueKanten);
    }



    private void showContextMenu(MouseEvent e, Feld feld) {
        JPopupMenu menu = new JPopupMenu();
        String[] options = {"Clear", "Krone", "Sperrstein", "Spawn"};
        
        for (String option : options) {
            JMenuItem item = new JMenuItem(option);
            item.addActionListener(ev -> {
                switch (option) {
                    case "Clear" -> feld.data = "";
                    case "Krone" -> feld.data = "Krone";
                    case "Sperrstein" -> feld.data = "Sperrstein";
                    case "Spawn" -> feld.data = "Spawn";
                }
                repaint();
            });
            menu.add(item);
        }
        menu.show(this, e.getX(), e.getY());
    }
    public String exportToString(){return XMLHandler.exportToString(felder, kanten); }
    public void loadFromXMLString(String xmlString) {
        XMLHandler.loadFromString(xmlString, felder, kanten);
        repaint();
    }
    public int getSpielerAnzahl() {
        int spielerAnzahl = 0;
        for (Feld feld : felder) {
            if (feld.data.equals("Spawn")) {
                spielerAnzahl++;
            }
        }
        return spielerAnzahl;
    }
}
