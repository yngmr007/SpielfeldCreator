import java.awt.*;

public class Kante {
    private Feld from;
    private Feld to;

    public Kante(Feld from, Feld to) {
        this.from = from;
        this.to = to;
    }

    public void draw(Graphics2D g, int gridSize) {
        int x1 = from.posX * gridSize + gridSize / 2;
        int y1 = from.posY * gridSize + gridSize / 2;
        int x2 = to.posX * gridSize + gridSize / 2;
        int y2 = to.posY * gridSize + gridSize / 2;

        g.setColor(new Color(154, 0, 0));
        g.setStroke(new BasicStroke(4));
        g.drawLine(x1, y1, x2, y2);
    }

    public Feld getFrom() { return from; }
    public Feld getTo() { return to; }
}
