package lab4;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public class FourthLabRab {
    private final static String filePath = "src\\main\\resources\\lab4";


    public static void start() {
        //Пользователь вводит начальное значение регистра и позиции отводов образующего многочлена
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите начальное значение регистра: ");
        String input = scanner.nextLine();
        int[] reg = new int[input.length()];
        for (int i = 0; i < input.length(); i++) {
            char digitChar = input.charAt(i);
            int digit = Character.getNumericValue(digitChar);
            reg[i] = digit;
        }
        System.out.print("Позиции отводов образующего многочлена: ");
        String input2 = scanner.nextLine();
        int[] otv = new int[input2.length()];
        for (int i = 0; i < input2.length(); i++) {
            char digitChar = input2.charAt(i);
            int digit = Character.getNumericValue(digitChar);
            otv[i] = digit;
        }
        scanner.close();
        // 1000 11001

        // Генерация псевдослучайной последовательности из 100 значений
        int[] posl = sdvig(100, reg, otv);
        System.out.println("Псевдослучайная последовательность: " + Arrays.toString(posl));

        //Создание диаграммы для визуализации результата
        Diagram diagram = new Diagram(posl);
        Frame frame = new Frame();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                System.exit(0);
            }
        });
        frame.add(diagram);
        frame.setSize(1500, 150);
        frame.setVisible(true);

        // Вычисление значение критерия χ^2
        double x2 = x2(posl);
        System.out.println("Результаты оценки последовательности критерием χ^2: " + x2);

        //Шифрование путем однократного гаммирования
        byte[] imageBytes = fileBytes(filePath + "\\tux.bmp"); //создание массива с байтами изображения tux.bmp
        // Генерация псевдослучайно последовательности длинной в количество байт изображения
        byte[] keyStream = toByteArray(sdvig((imageBytes.length - 122) * 8, reg, otv));
        // Однократное гаммирование байт изображения псевдослучайной последовательностью
        for (int i = 0; i < imageBytes.length - 122; i++) {
            byte[] gammByte = new byte[]{(byte) (imageBytes[i + 122] ^ keyStream[i])}; //XQR по 8 бит
            System.arraycopy(gammByte, 0, imageBytes, 122 + i, 1);
        }

        // Сохранение зашифрованного изображения
        saveFile(filePath + "\\run\\tux_gam.bmp", imageBytes);
    }

    // Расчет критерия χ^2
    private static double x2(int[] posl) {
        final int slots = 2; // Количество "ячеек" для χ^2 теста
        final double expeect = posl.length / (double) slots; // Ожидаемая частота для каждой "ячейки"
        double[] expeectMass = new double[slots]; // Массив для хранения наблюдаемых частот
        // Подсчет наблюдаемых частот для каждой "ячейки"
        for (int i = 0; i < posl.length; i++) {
            expeectMass[posl[i]]++;
        }
        double x2 = 0.0;
        // Подсчет χ^2 статистики
        for (int i = 0; i < slots; i++) {
            double delta = expeectMass[i] - expeect;
            x2 += delta * delta / expeect;
        }
        return x2;
    }

    private static byte[] fileBytes(String filename) {
        try (RandomAccessFile file = new RandomAccessFile(filename, "r")) {
            byte[] bytes = new byte[(int) file.length()];
            file.readFully(bytes);
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveFile(String filename, byte[] bytes) {
        try (RandomAccessFile file = new RandomAccessFile(filename, "rw")) {
            file.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] toByteArray(int[] bits) {
        byte[] bytes = new byte[bits.length / 8];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 0;
            for (int j = 0; j < 8; j++) {
                bytes[i] |= (bits[i * 8 + j] << j);
            }
        }
        return bytes;
    }

    // Метод генерации псевдослучайной последовательности
    private static int[] sdvig(int length, int[] reg, int[] otv) {
        int[] posl = new int[length];
        //Сдвиг битов и запись старшего в псевдослучайную последовательность
        for (int i = 0; i < length; i++) {
            // Берем старший бит
            int bit = reg[reg.length - 1];
            // Сдвигаем все биты вправо
            for (int j = reg.length - 1; j > 0; j--) {
                reg[j] = reg[j - 1];
            }
            //Запись старшего бита в начало
            reg[0] = bit;
            //Выполнение XOR если старший бит равен 1
            if (bit == 1) {
                for (int j = 1; j < reg.length; j++) {
                    if (otv[j] == 1) {
                        reg[j] ^= 1;
                    }
                }
            }
            posl[i] = bit;
        }
        return posl;
    }

}

