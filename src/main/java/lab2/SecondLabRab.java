package lab2;

import java.io.*;

public class SecondLabRab {
    private final static String filePath = "src\\main\\resources\\lab2";
    private final static String opensslPath = "C:\\Program Files\\Git\\usr\\bin\\openssl.exe";
    private final static String tuxTail = "\\run\\files\\tux_tail.md";
    private final static String tuxTailEnc = "\\run\\files\\tux_tail_enc";

    public static void start() {
        String KEY = openssl_key();
        byte[] header = decay();

        openssl_aes(KEY, "cbc", header);
        openssl_aes(KEY, "ecb", header);
        openssl_aes(KEY, "cfb", header);
        openssl_aes(KEY, "ofb", header);
    }

    //Шифрование изображения
    public static void openssl_aes(String key, String mode, byte[] header) {
        try {
            ProcessBuilder pb = new ProcessBuilder(opensslPath, "enc", "-aes-256-" + mode, "-in",
                    filePath + tuxTail, "-out", filePath + tuxTailEnc + mode.toUpperCase() + ".md", "-pass", "pass:" + key);
            Process p = pb.start();
            p.waitFor();
            unite(header, mode);
            if (p.waitFor() != 0) {
                System.out.println("Ошибка в openssl_aes");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static byte[] decay() {
        byte[] header = null;
        try {
            //Чтение файла
            FileInputStream fileInputStream = new FileInputStream(filePath + "\\tux.bmp");
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);
            //Отделение хедера
            header = new byte[122];
            dataInputStream.readFully(header);
            //Сохранение файла с остатком
            FileOutputStream fileOutputStream = new FileOutputStream(filePath + tuxTail);
            DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = dataInputStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, length);
            }
            //Закрытие файлов и возвращение хедера
            dataInputStream.close();
            dataOutputStream.close();
            return header;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return header;
    }

    public static void unite(byte[] header, String mode) {
        try {
            //Чтение зашифрованного тела файла
            FileInputStream restInputStream = new FileInputStream(filePath + tuxTailEnc + mode.toUpperCase() + ".md");
            ByteArrayOutputStream restOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = restInputStream.read(buffer)) != -1) {
                restOutputStream.write(buffer, 0, length);
            }
            byte[] rest = restOutputStream.toByteArray();
            //Объединение хедера и конца с зашифрованным телом файла
            FileOutputStream fileOutputStream = new FileOutputStream(filePath + "\\run\\tux_" + mode.toUpperCase() + ".bmp");
            fileOutputStream.write(header);
            fileOutputStream.write(rest);
            //Закрытие файлов
            restInputStream.close();
            fileOutputStream.close();
            //Сообщение об успешности операции шифрования
            System.out.println("Файл tux.bmp зашифрован в tux_" + mode.toUpperCase() + ".bmp" + " шифром AES в режиме " + mode.toUpperCase());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Генерация случайного ключа
    public static String openssl_key() {
        String key = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(opensslPath, "rand", "-hex", "16");
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            key = reader.readLine();
            System.out.println("Случайно сгенерированный ключ: " + key);
            if (p.waitFor() != 0) {
                System.out.println("Ошибка в openssl_key");
            }
            return key;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return key;
    }
}
