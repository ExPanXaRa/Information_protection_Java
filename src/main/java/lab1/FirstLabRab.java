package lab1;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class FirstLabRab {
    public static void start() {
        String filePath = "src\\main\\resources\\lab1";
        String opensslPath = "C:\\Program Files\\Git\\usr\\bin\\openssl.exe";
        String oldFIle = "\\28.bmp";
        String newFile = "\\new28.bmp";

        byte[] sha1 = openssl_sha1(filePath, opensslPath);
        Integer fileLong = fileSize(filePath + oldFIle, " исходной картинки ");

        int[] key = keyGenerator(fileLong);
        integration(sha1, key, fileLong, filePath, oldFIle, newFile);
        extraction(key, filePath, newFile);
        fileSize(filePath + newFile, " полученной картинки ");
    }

    //Извлечение кода SHA1 файла leasing.txt
    public static byte[] openssl_sha1(String filePath, String opensslPath) {
        byte[] sha1_byte = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(opensslPath, "dgst", "-sha1", filePath + "\\leasing.txt");
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
        } catch (Exception e) {
            e.getMessage();
        }

        return sha1_byte;
    }

    //Генерация случайного ключа
    public static int[] keyGenerator(int max) {
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
    public static Integer fileSize(String filePath, String num) {
        File file = new File(filePath);
        long fileSizeInBytes = file.length();
        System.out.println("Размер" + num + fileSizeInBytes + " байт");
        return (int) fileSizeInBytes;
    }

    // Внедрение сообщения в BMP-файл
    public static void integration(byte[] sha1, int[] key, int fileLong, String filePath, String oldFIle, String newFile) {
        File containerFile = new File(filePath + oldFIle);
        File outputFile = new File(filePath + newFile);
        try {
            byte[] containerBytes = new byte[(int) containerFile.length()];
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
        } catch (Exception e) {
            e.getMessage();
        }
    }

    // Извлечение сообщения из BMP-файла
    public static void extraction(int[] key, String filePath, String newFile) {
        File containerFile = new File(filePath + newFile);
        int key_lenght = key.length;
        try {
            byte[] containerBytes = new byte[(int) containerFile.length()];
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
        } catch (Exception e) {
            e.getMessage();
        }
    }
}