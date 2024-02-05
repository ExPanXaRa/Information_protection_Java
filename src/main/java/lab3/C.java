package lab3;

import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;


public class C {
    static String opensslPath = "C:\\Program Files\\Git\\usr\\bin\\openssl.exe";
    static String dirPath = "src\\main\\resources\\lab3";

    public static void main(String[] args) throws InterruptedException {
        //Генерация файлов
        BigInteger N = openssl_keyModul();

        //

        try (ServerSocket server = new ServerSocket(3355);) {
            Socket client = server.accept();
            System.out.print("Клиент подключен\n");
            // канал чтения из сокета
            DataInputStream in = new DataInputStream(client.getInputStream());
            // канал записи в сокет
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            while (!client.isClosed()) {
                String mix = in.readUTF();
                // инициализация проверки условия продолжения работы с клиентом по этому сокету	по кодовому слову
                if (mix.equalsIgnoreCase("выход")) {
                    System.out.println("Отключение ...");
                    out.writeUTF("Ответ сервера - " + mix + " - сделано");
                    Thread.sleep(3000);
                    break;
                }
                String signedMessage = null;
                String initialMessage = null;
                //Основная работа
                try {
                    String[] parts = mix.split(",", 2);
                    byte[] bytes = parts[1].getBytes(); // создаем массив байтов, извлекая байты из подписи
                    byte[] decodedBytes = new Base64().decode(bytes); // декодируем байты с использованием Base64-декодирования
                    BigInteger sig = new BigInteger(decodedBytes);

                    String elo = takeE();
                    BigInteger e = new BigInteger(elo);// получаем публичную экспоненту ключа

                    BigInteger signedMessageBigInt = sig.modPow(e, N); // вычисляем sig^e modN, если получаем исходное сообщение, значит подпись действительна, это работает, потому что (m^d)^e modN = m
                    signedMessage = new String(signedMessageBigInt.toByteArray()); // создаем строку на основе результата вычисления выше

                    byte[] msg = openssl_sha1(parts[0]);
                    BigInteger partl = new BigInteger(msg);
                    initialMessage = new String(partl.toByteArray()); // создаем строку на основе исходного сообщения, для которого мы хотели получить подпись

                    if (signedMessage.equals(initialMessage)) // сравниваем две строки, если они равны, полученная подпись является действительной
                    {
                        System.out.println("Верификация подписи успешно завершена"); // выводим сообщение об успешной верификации подписи
                        out.writeUTF("Верификация подписи удалась");
                    } else {
                        System.out.println("Верификация подписи не удалась"); // выводим сообщение о неудачной верификации подписи
                        out.writeUTF("Верификация подписи удалась");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                //
                //out.writeUTF("Полученная подпись \n" + signedMessage + "\n Настоящая подпись\n" + initialMessage);
                System.out.println("Сервер ответил клиенту.");
                out.flush();

            }
            // если условие выхода - верно выключаем соединения
            in.close();
            out.close();
            client.close();
            server.close();
            System.out.println("Подключение закрыто");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Модули
    public static String takeE() {
        String fileName = dirPath + "\\privatekeyInfo.txt"; // Укажите путь к вашему файлу
        String line = null;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while ((line = br.readLine()) != null) {
                if (line.contains("publicExponent:")) {
                    line = line.replace("publicExponent:", "")
                            .replace("(0x10001)", "")
                            .replaceAll("\\s+", "");
                    return line;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }

    public static BigInteger openssl_keyModul() {
        BigInteger modulus = null;
        try { //openssl rsa -in privatekey.pem -noout -modulus
            ProcessBuilder pb2 = new ProcessBuilder(opensslPath, "rsa",
                    "-in", dirPath + "\\privatekey.pem", "-noout", "-modulus");
            Process p2 = pb2.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p2.getInputStream()));
            String key = reader.readLine();
            key = key.substring(8);
            modulus = new BigInteger(key, 16);
            System.out.println("Модуль закрытого ключа: " + modulus);
            if (p2.waitFor() != 0) {
                System.out.println("Ошибка в openssl_keyInfo");
            }
            return modulus;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return modulus;
    }

    public static byte[] openssl_sha1(String message) {
        byte[] sha1_byte = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(opensslPath, "dgst", "-sha1",
                    dirPath + "\\message.txt");
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String sha1Long = reader.readLine();
            String sha1 = sha1Long.substring(sha1Long.length() - 40);
            //System.out.println("SHA1 файла: " + sha1);
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


    //
}

