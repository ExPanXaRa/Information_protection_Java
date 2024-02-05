package lab5;

import java.util.ArrayList;
import java.util.Scanner;


public class FiveLabRab {
    public static void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите значение переменной a: ");
        int a = scanner.nextInt();
        System.out.print("Введите значение переменной b∈Zp: ");
        int b = scanner.nextInt();
        System.out.print("Введите значение переменной p - простое число(p*p-размер матрицы): ");
        int p = scanner.nextInt();

        //Генерация точек внутри конечного поля
        ArrayList<Dot> dots = genDots(a, b, p);
        System.out.println("Найдено точек: " + dots.size());
        System.out.println("Список точек:" + dots);

        System.out.print("Введите значение координаты x для первой точки сложения: ");
        int x1 = scanner.nextInt();
        System.out.print("Введите значение координаты y для первой точки сложения: ");
        int y1 = scanner.nextInt();
        System.out.print("Введите значение координаты x для второй точки сложения: ");
        int x2 = scanner.nextInt();
        System.out.print("Введите значение координаты y для второй точки сложения: ");
        int y2 = scanner.nextInt();

        //Сложение 2 точек
        Dot One = new Dot(y1, x1, a, b, p);
        Dot Second = new Dot(y2, x2, a, b, p);
        Dot Summa = One.sum(Second);
        System.out.println("Результат операции сложения точек (красная точка): " + Summa);


        System.out.print("Введите значение координату x для точки удвоения: ");
        int x3 = scanner.nextInt();
        System.out.print("Введите значение координату y для точки удвоения: ");
        int y3 = scanner.nextInt();

        //Удвоение точки
        Dot Third = new Dot(y3, x3, a, b, p);
        Dot Double = Third.doubl();
        System.out.println("Результат операции удвоения точки (синяя точка): " + Double);

        //Рисование диаграммы
        Diagram.main(a, b, p, y1, x1, y2, x2, y3, x3);
    }


    public static ArrayList<Dot> genDots(int a, int b, int p) {
        ArrayList<Dot> dots = new ArrayList<>();
        for (int x = 0; x < p; x++) {
            for (int y = 0; y < p; y++) {
                Dot dot = new Dot(y, x, a, b, p);
                //Проверка по формуле, принадлежит ли точка заданной кривой
                if (((y * y) % p) == ((x * x * x + a * x + b) % p)) {
                    dots.add(dot);
                }
            }
        }
        return dots;

    }
}




