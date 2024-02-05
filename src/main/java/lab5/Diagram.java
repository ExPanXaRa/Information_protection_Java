package lab5;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Diagram {
    private static final int CELL_SIZE = 30; // Размер ячейки в пикселях
    private static final int PADDING = 50; // Отступ от края окна

    public static void main(int a, int b, int p, int y1, int x1, int y2, int x2, int y3, int x3) {
        int gridSize = p; // Размер сетки (количество ячеек)
        ArrayList<Dot> dots = FiveLabRab.genDots(a, b, p);
        int[] xCoords = new int[dots.size() + 2]; // Координаты по оси x
        int i = 0;
        for (i = 0; i < dots.size(); i++) {
            Dot dot = dots.get(i);
            xCoords[i] = dot.takeX();
        }
        int[] yCoords = new int[dots.size() + 2]; // Координаты по оси y
        int j = 0;
        for (j = 0; j < dots.size(); j++) {
            Dot dot = dots.get(j);
            yCoords[j] = dot.takeY();
        }
        //Точки сложения и удвоения
        Dot One = new Dot(y1, x1, a, b, p);
        Dot Second = new Dot(y2, x2, a, b, p);
        Dot Summ = One.sum(Second);
        xCoords[i] = Summ.takeX();
        yCoords[j] = Summ.takeY();
        i++;
        j++;
        Dot Third = new Dot(y3, x3, a, b, p);
        Dot Double = Third.doubl();
        xCoords[i] = Double.takeX();
        yCoords[j] = Double.takeY();
        //Вызов метода рисования
        drawGrid(gridSize, xCoords, yCoords);
    }

    public static void drawGrid(int size, int[] xCoords, int[] yCoords) {
        JFrame frame = new JFrame("Эпилептическая кривая");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize((size + 1) * CELL_SIZE + 2 * PADDING, (size + 1) * CELL_SIZE + 2 * PADDING);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                g.setColor(Color.BLACK);

                // Рисование сетки
                for (int i = 0; i <= size; i++) {
                    g.drawLine(PADDING, PADDING + i * CELL_SIZE, PADDING + size * CELL_SIZE,
                            PADDING + i * CELL_SIZE); // Горизонтальные линии
                    g.drawLine(PADDING + i * CELL_SIZE, PADDING, PADDING + i * CELL_SIZE,
                            PADDING + size * CELL_SIZE); // Вертикальные линии
                }

                // Рисование точек
                for (int i = 0; i < xCoords.length; i++) {
                    int x = xCoords[i];
                    int y = yCoords[i];
                    if (x >= 0 && x < size && y >= 0 && y < size) {
                        int pointX = PADDING + x * CELL_SIZE;
                        int pointY = PADDING + (size - y) * CELL_SIZE;
                        //Точка результат сложения
                        if (i == xCoords.length - 2) {
                            g.setColor(Color.RED);
                            //Точка результат умножения
                        } else if (i == xCoords.length - 1) {
                            g.setColor(Color.BLUE);
                            //Точки эллиптической кривой над полем p
                        } else {
                            g.setColor(Color.BLACK);
                        }
                        g.fillOval(pointX - 5, pointY - 5, 10, 10); // Отмечаем точку кругом
                    }
                }
            }
        };
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }
}

