package lab4;

import java.awt.*;

public class Diagram extends Panel {
    private int[] posl;

    public Diagram(int[] posl) {
        this.posl = posl;
    }

    public void paint(Graphics g) {
        int width = getSize().width;
        int height = getSize().height;
        int xWidth = Math.max(1, width / posl.length);

        // Отрисовка оси координат
        g.setColor(Color.BLACK);
        g.drawLine(0, height / 2, width, height / 2);
        g.drawLine(width / 2, 0, width / 2, height);

        // Отрисовка точек
        g.setColor(Color.BLACK);
        for (int i = 0; i < posl.length; i++) {
            int x = i * xWidth;
            int y = height / 2 - 10 * posl[i];
            g.fillOval(x, y, xWidth / 2, xWidth / 2);
        }
    }
}

