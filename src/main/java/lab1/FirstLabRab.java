package lab1;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class FirstLabRab {
    private final static String dirPath = "src\\main\\resources\\lab1";
    private final static String opensslPath = "C:\\Program Files\\Git\\usr\\bin\\openssl.exe";
    private final static String oldFile = "\\28.bmp";
    private final static String newFile = "\\run\\new28.bmp";


    public static void start() {
        byte[] SHA1 = openssl_sha1();
        Integer fileLong = fileSize(dirPath + oldFile, " исходной картинки ");

        int[] KEY = keyGenerator(fileLong);
        integration(SHA1, KEY, fileLong);
        extraction(KEY);
        fileSize(dirPath + newFile, " полученной картинки ");
    }

    //Извлечение кода SHA1 файла leasing.txt
    private static byte[] openssl_sha1() {
        byte[] sha1_byte = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(opensslPath, "dgst", "-sha1", dirPath + "\\leasing.txt");
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String sha1Long = reader.readLine();
            String sha1 = sha1Long.substring(sha1Long.length() - 40);
            System.out.println("SHA1 файла: " + sha1);
            //Преобразование строки в 16-ричном формате в массив byte[]
            sha1_byte = new byte[sha1.length() / 2];
            for (int i = 0; i < sha1.length(); i += 2) {
                String subStr = sha1.substring(i, i + 2);
                sha1_byte[i / 2] = (byte) Integer.parseInt(subStr, 16);
            }
            if (p.waitFor() != 0) {
            }
            return sha1_byte;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return sha1_byte;
    }

    //Генерация случайного ключа
    private static int[] keyGenerator(int max) {
        HashSet<Integer> set = new HashSet<Integer>();
        Random random = new Random();

        while (set.size() < 160) {
            int randomNumber = random.nextInt((max - 123) + 1) + 123;
            set.add(randomNumber);
        }

        int[] array = new int[160];
        int index = 0;

        for (int number : set) {
            array[index++] = number;
        }

        Arrays.sort(array);
        return array;
    }

    //Определение размера картинки
    private static Integer fileSize(String dirPath, String num) {
        File file = new File(dirPath);
        long fileSizeInBytes = file.length();
        System.out.println("Размер" + num + fileSizeInBytes + " байт");
        return (int) fileSizeInBytes;
    }

    // Внедрение сообщения в BMP-файл
    private static void integration(byte[] sha1, int[] key, int fileLong) {
        File containerFile = new File(dirPath + oldFile);
        File outputFile = new File(dirPath + newFile);
        try {
            byte[] containerBytes = new byte[(int) containerFile.length()];
            FileInputStream fis = new FileInputStream(containerFile);
            fis.read(containerBytes);
            fis.close();
            int j = 0;
            int count = 0;
            int h = 0;
            // Внедрение сообщения
            for (int i = 122; i < fileLong; i++) {
                if (i == key[h]) {
                    int containerByte = containerBytes[i];
                    int messageBit = (sha1[count] >> (7 - j)) & 1; //получение очередного бита сообщения
                    containerByte &= ~(1 << 0); //обнуление младшего бита
                    containerByte |= (messageBit << 0); //запись бита сообщения в младший бит
                    containerBytes[i] = (byte) containerByte;

                    if (j < 7) {
                        j++;
                    } else {
                        j = 0;
                        count++;
                    }
                    h++;
                }
                if (h == sha1.length * 8) {
                    break;
                }
            }
            FileOutputStream fos = new FileOutputStream(outputFile);
            fos.write(containerBytes);
            fos.close();
            //преобразование массива byte[] в строку в 16-ричном формате
            BigInteger bigInt = new BigInteger(1, sha1);
            String hexString = bigInt.toString(16);
            System.out.println("Внедренный хеш код:  " + hexString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Извлечение сообщения из BMP-файла
    private static void extraction(int[] key) {
        File containerFile = new File(dirPath + newFile);
        int key_lenght = key.length;
        try {
            byte[] containerBytes = new byte[(int) containerFile.length()];
            FileInputStream fis = new FileInputStream(containerFile);
            fis.read(containerBytes);
            fis.close();
            int file2Long = (int) containerFile.length();
            byte[] messageBytes = new byte[key_lenght / 8];
            int j = 0;
            int count = 0;
            int h = 0;
            // Извлечение сообщения
            for (int i = 122; i < file2Long; i++) {
                if (i == key[h]) {
                    int containerByte = containerBytes[i];
                    int containerBit = (containerByte >> 0) & 1;//получение младшего бита контейнера
                    messageBytes[count] |= (containerBit << (7 - j));//запись текущего бита в бит сообщения
                    containerBytes[i] = (byte) containerByte;
                    if (j < 7) {
                        j++;
                    } else {
                        j = 0;
                        count++;
                    }
                    h++;
                }
                if (h == messageBytes.length * 8) {
                    break;
                }
            }
            //преобразование массива byte[] в строку в 16-ричном формате
            BigInteger bigInt = new BigInteger(1, messageBytes);
            String hexString = bigInt.toString(16);
            System.out.println("Извлеченный хеш код: " + hexString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}