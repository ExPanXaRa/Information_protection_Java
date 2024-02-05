package lab5;

public class Dot {
    private int y;
    private int x;
    private int a;
    private int b;
    private int p;

    public Dot(int y1, int x1, int a1, int b1, int p1) {
        y = y1;
        x = x1;
        a = a1;
        b = b1;
        p = p1;
    }

    @Override
    //Шаблон возврата для вывода в консоль
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public int takeX() {
        return x;
    }

    public int takeY() {
        return y;
    }

    public Dot sum(Dot dots) {
        int liambda;
        //Проверка, не совпадают ли точки
        if (x == dots.takeX() && y == dots.takeY()) {
            //Формула вычисления лямбды при одинаковых точках(удвоение)
            liambda = (3 * x * x + a) * inv((2 * y), p) % p;
        } else {
            //Формула вычисления лямбды при разных точках(сложение)
            liambda = (dots.takeY() - y) * inv((dots.takeX() - x), p) % p;
        }
        //Формула вычисления x координаты точки результата сложения/удвоения
        int xSum = (liambda * liambda - x - dots.takeX()) % p;
        //Формула вычисления y координаты точки результата сложения/удвоения
        int ySum = (liambda * (x - xSum) - y) % p;
        //Проверка, лежит ли точка внутри матрицы p*p
        while (xSum < 0) {
            xSum += p;
        }
        while (xSum > p) {
            xSum -= p;
        }
        while (ySum < 0) {
            ySum += p;
        }
        while (ySum > p) {
            ySum -= p;
        }
        return new Dot(ySum, xSum, a, b, p);
    }

    public Dot doubl() {
        //Формула вычисления лямбды
        int liambda = (3 * x * x + a) * inv((2 * y), p) % p;
        //Формула вычисления x координаты точки результата сложения
        int xSum = (liambda * liambda - x - x) % p;
        //Формула вычисления y координаты точки результата сложения
        int ySum = (liambda * (x - xSum) - y) % p;
        //Проверка, лежит ли точка внутри матрицы p*p
        while (xSum < 0) {
            xSum += p;
        }
        while (xSum > p) {
            xSum -= p;
        }
        while (ySum < 0) {
            ySum += p;
        }
        while (ySum > p) {
            ySum -= p;
        }
        return new Dot(ySum, xSum, a, b, p);
    }

    private int inv(int a, int p) {
        a = a % p;
        for (int x = 1; x < p; x++) {
            if ((a * x) % p == 1) {
                return x;
            }
        }
        return -1;
    }
}

