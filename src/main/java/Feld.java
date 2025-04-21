import java.awt.*;
import java.util.Objects;

public class Feld {
    public int posX, posY;
    public int id;
    static int nextId = 0;
    public String data = "";

    public Feld(int x, int y) {
        this.posX = x;
        this.posY = y;
        this.id = nextId++;
    }

    public void draw(Graphics2D g, int gridSize, boolean selected) {
        int px = posX * gridSize;
        int py = posY * gridSize;

        if (selected) {
            g.setColor(new Color(128, 223, 120)); // selected
        } else if (!Objects.equals(data, "")) {
            if(data.contains("Sperrstein")) g.setColor(new Color(255, 0, 0));
            else if(data.contains("Krone")) g.setColor(new Color(255, 255, 0));
            else g.setColor(new Color(53, 222, 234)); // has value
        } else {
            g.setColor(new Color(138, 138, 138)); // empty
        }
        g.fillOval(px + 5, py + 5, gridSize - 10, gridSize - 10);
    }

    public boolean isNeighbor(Feld other) {
        int dx = Math.abs(this.posX - other.posX);
        int dy = Math.abs(this.posY - other.posY);
        return (dx + dy == 1);
    }
}
