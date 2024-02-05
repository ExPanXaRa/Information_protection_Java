package lab3;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

public class B {
    static String opensslPath = "C:\\Program Files\\Git\\usr\\bin\\openssl.exe";
    static String dirPath = "src\\main\\resources\\lab3";

    public static void main(String[] args) throws InterruptedException {
        //Генерация файлов
        openssl_privateKey();
        openssl_publicKey();
        openssl_keyInfo();
        BigInteger N = openssl_keyModul();
        //

        try (ServerSocket server = new ServerSocket(3345);) {
            Socket client = server.accept();
            System.out.print("Клиент подключен\n");
            // канал чтения из сокета
            DataInputStream in = new DataInputStream(client.getInputStream());
            // канал записи в сокет
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            while (!client.isClosed()) {
                String hiddenMessage = in.readUTF();
// инициализация проверки условия продолжения работы с клиентом по этому сокету	по кодовому слову
                if (hiddenMessage.equalsIgnoreCase("выход")) {
                    System.out.println("Отключение ...");
                    out.writeUTF("Ответ сервера - " + hiddenMessage + " - сделано");
                    Thread.sleep(3000);
                    break;
                }
                //Основная работа
                BigInteger bigInteger = new BigInteger(hiddenMessage);
                String takedP = takeP();
                BigInteger P = new BigInteger(takedP, 16); // получаем простое число p, использованное при генерации ключей
                String takedQ = takeQ();
                BigInteger Q = new BigInteger(takedQ, 16); // получаем простое число q, использованное при генерации ключей
                String takedD = takeD();
                BigInteger d = new BigInteger(takedD, 16); // получаем приватный показатель d

                // Разделяем выражение hiddenMessage^d modN на два по модулям p и q
                BigInteger PinverseModQ = P.modInverse(Q); // вычисляем обратное значение p по модулю q
                BigInteger QinverseModP = Q.modInverse(P); // вычисляем обратное значение q по модулю p
                // Разделяем сообщение mu на два сообщения m1 и m2 по модулям p и q
                BigInteger m1 = bigInteger.modPow(d, N).mod(P); // вычисляем m1 = (hiddenMessage^d mod N) mod P
                BigInteger m2 = bigInteger.modPow(d, N).mod(Q); // вычисляем m2 = (hiddenMessage^d mod N) mod Q
                // Комбинируем вычисленные m1 и m2, чтобы вычислить e_podpis
                // Вычисляем e_podpis: (m1 * Q * QinverseModP + m2 * P * PinverseModQ) mod N, где N = P * Q
                BigInteger e_podpis = ((m1.multiply(Q).multiply(QinverseModP)).add(m2.multiply(P).multiply(PinverseModQ))).mod(N);
                //
                System.out.println("Подписанное сообщение: " + e_podpis);
                out.writeUTF(e_podpis.toString());
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
    public static void openssl_privateKey() {
        try { //openssl genpkey -algorithm RSA -out privatekey.pem -pkeyopt rsa_keygen_bits:1024
            ProcessBuilder pb = new ProcessBuilder(opensslPath, "genpkey",
                    "-algorithm", "RSA", "-out", dirPath + "\\privatekey.pem", "-pkeyopt", "rsa_keygen_bits:1024");
            Process p = pb.start();
            System.out.println("PrivateKey успешно сгенерирован");
            if (p.waitFor() != 0) {
                System.out.println("Ошибка в openssl_privateKey");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void openssl_publicKey() {
        try { //openssl rsa -pubout -in privatekey.pem -out publickey.pem
            ProcessBuilder pb = new ProcessBuilder(opensslPath, "rsa",
                    "-pubout", "-in", dirPath + "\\privatekey.pem", "-out", dirPath + "\\publickey.pem");
            Process p = pb.start();
            System.out.println("PublicKey успешно сгенерирован");
            if (p.waitFor() != 0) {
                System.out.println("Ошибка в openssl_publicKey");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
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

    public static String takeP() {
        String filePath = dirPath + "\\privatekeyInfo.txt";
        String startMarker = "prime1:";
        String endMarker = "prime2:";
        StringBuilder extractedText = new StringBuilder();
        boolean isExtractionStarted = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(startMarker)) {
                    isExtractionStarted = true;
                    continue;
                } else if (line.equals(endMarker)) {
                    break;
                }

                if (isExtractionStarted) {
                    extractedText.append(line);
                    extractedText.append(System.lineSeparator());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return extractedText.toString().replaceAll("[\\s:]", "");
    }

    public static String takeQ() {
        String filePath = dirPath + "\\privatekeyInfo.txt";
        String startMarker = "prime2:";
        String endMarker = "exponent1:";
        StringBuilder extractedText = new StringBuilder();
        boolean isExtractionStarted = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(startMarker)) {
                    isExtractionStarted = true;
                    continue;
                } else if (line.equals(endMarker)) {
                    break;
                }

                if (isExtractionStarted) {
                    extractedText.append(line);
                    extractedText.append(System.lineSeparator());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return extractedText.toString().replaceAll("[\\s:]", "");
    }

    public static String takeD() {
        String filePath = dirPath + "\\privatekeyInfo.txt";
        String startMarker = "privateExponent:";
        String endMarker = "prime1:";
        StringBuilder extractedText = new StringBuilder();
        boolean isExtractionStarted = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(startMarker)) {
                    isExtractionStarted = true;
                    continue;
                } else if (line.equals(endMarker)) {
                    break;
                }

                if (isExtractionStarted) {
                    extractedText.append(line);
                    extractedText.append(System.lineSeparator());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return extractedText.toString().replaceAll("[\\s:]", "");
    }

    public static void openssl_keyInfo() {
        try { //openssl rsa -text -in privatekey.pem
            ProcessBuilder pb2 = new ProcessBuilder(opensslPath, "rsa",
                    "-text", "-in", dirPath + "\\privatekey.pem", "-out", dirPath + "\\privatekeyInfo.txt");
            Process p2 = pb2.start();
            System.out.println("PrivatekeyInfo успешно сгенерирован");
            if (p2.waitFor() != 0) {
                System.out.println("Ошибка в openssl_keyInfo");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    //
}
