package lab3;

import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.*;
import java.security.*;
import java.math.BigInteger;
import java.io.IOException;
import java.security.SecureRandom;

public class A {

    static String wut;
    static String opensslPath = "C:\\Program Files\\Git\\usr\\bin\\openssl.exe";
    static String dirPath = "src\\main\\resources\\lab3";

    public static void main(String[] args) throws InterruptedException, UnknownHostException {
        //Генерация файлов
        BigInteger N = openssl_keyModul();

        //
// запускаем подключение сокета по известным координатам и инициализируем приём сообщений с консоли клиента
        try (Socket socket = new Socket("localhost", 3345);
             BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
             DataOutputStream oos = new DataOutputStream(socket.getOutputStream());
             DataInputStream ois = new DataInputStream(socket.getInputStream());) {
            System.out.println("Кандидаты: \n 1. Биба \n 2. Боба \n Введите имя вашего кандидата");
            while (!socket.isOutputShutdown()) {
                if (br.ready()) {
// Основная работа
                    System.out.println("Клиент отправил сообщение B...");
                    Thread.sleep(1000);
                    String clientCommand = br.readLine();
                    //Основная работа

                    File file = new File(dirPath + "\\message.txt");// вычисляем хеш SHA1 для сообщения
                    FileWriter fileWriter = new FileWriter(file);
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                    bufferedWriter.write(clientCommand);
                    bufferedWriter.close();
                    fileWriter.close();

                    byte[] msg = openssl_sha1();// получаем байты хешированного сообщения

                    BigInteger m = new BigInteger(msg); // создаем объект BigInteger на основе извлеченных байтов сообщения

                    String elo = takeE();
                    BigInteger e = new BigInteger(elo);// получаем публичный экспоненту 'e' из ключа

                    // Генерируем случайное число, чтобы оно принадлежало к Z*n, было больше 1 и, следовательно, r было обратимо в Z*n
                    SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
                    byte[] randomBytes = new byte[10]; // создаем массив байтов для хранения r
                    BigInteger one = new BigInteger("1"); // создаем объект BigInteger, равный 1, чтобы в дальнейшем можно было сравнить его с r для проверки r > 1
                    BigInteger gcd = null; // инициализируем переменную gcd значением null
                    BigInteger r;
                    do {
                        random.nextBytes(randomBytes); // генерируем случайные байты с помощью функции SecureRandom
                        r = new BigInteger(randomBytes); // создаем объект BigInteger на основе сгенерированных случайных байтов, представляющих число r
                        gcd = r.gcd(N); // вычисляем наибольший общий делитель для случайного числа r и модуля пары ключей
                    } while (!gcd.equals(one) || r.compareTo(N) >= 0 || r.compareTo(one) <= 0); // повторяем, пока не получим значение r, которое удовлетворяет всем условиям и принадлежит Z*n и > 1


                    // Продолжение вычисления hiddenMessage
                    BigInteger hiddenMessage = ((r.modPow(e, N)).multiply(m)).mod(N); // Боб вычисляет mu = H(msg) * r^e mod N
                    System.out.println(hiddenMessage);

                    //
                    //Отправка данных
                    System.out.println("Зашифрованное сообщение: " + hiddenMessage.toString());
                    oos.writeUTF(hiddenMessage.toString());
                    oos.flush();
                    Thread.sleep(1000);
                    // проверяем условие выхода из соединения

                    //некст степ
                    String e_podpisS = ois.readUTF();
                    BigInteger e_podpis = new BigInteger(e_podpisS);
                    BigInteger s = r.modInverse(N).multiply(e_podpis).mod(N); // A вычисляет sig = mu'*r^-1 mod N, обратное значение r mod N умножается на hiddenByBMessage mod N, чтобы удалить маскировочный фактор

                    byte[] bytes = new Base64().encode(s.toByteArray()); // кодируем с использованием кодирования Base64, чтобы можно было прочитать все символы
                    String signature = new String(bytes); // создаем строку на основе массива байт, представляющего подпись

                    //System.out.println("Сообщение, эл. подпись: ");
                    wut = clientCommand + "," + signature; //задаем значение сообщения, которое будет отправлено С
                    System.out.println(wut);

                    //oos.writeUTF(clientCommand + "," + s);
                    //oos.flush();


                    //
                    Thread.sleep(2000);
                    if (ois.available() != 0) {
                        String in = ois.readUTF();
                        System.out.println(in);
                    }

                    try (Socket socket2 = new Socket("localhost", 3355);
                         BufferedReader br2 = new BufferedReader(new InputStreamReader(System.in));
                         DataOutputStream oos2 = new DataOutputStream(socket2.getOutputStream());
                         DataInputStream ois2 = new DataInputStream(socket2.getInputStream());) {
                        while (!socket2.isOutputShutdown()) {
                            if (br2.ready()) {
                                // Основная работа
                                System.out.println("Клиент отправил сообщение C...");
                                Thread.sleep(1000);
                                System.out.println("Зашифрованное сообщение: \n" + wut + "\n для продолжения введите 'далее'");
                                //clientCommand = br2.readLine();
                                //Основная работа

                                //отправление данных
                                oos2.writeUTF(wut);
                                oos2.flush();
                                Thread.sleep(1000);
                                // проверяем условие выхода из соединения
                                if (!wut.equals("")) {
                                    System.out.println("Клиент закрыл соединение");
                                    Thread.sleep(2000);
                                    if (ois2.available() != 0) {
                                        System.out.println("Ожидание...");
                                        String in = ois2.readUTF();
                                        System.out.println(in);
                                    }
                                    break;
                                }
                                //некст степ

                                //
                                Thread.sleep(2000);
                                if (ois2.available() != 0) {
                                    String in = ois2.readUTF();
                                    System.out.println(in);
                                }
                            }
                        }
                        System.out.println("Закрытие");
                    } catch (IOException el) {
                        el.printStackTrace();
                    }
                    if (!wut.equals("")) {
                        System.out.println("Клиент закрыл соединение");
                        Thread.sleep(2000);
                        if (ois.available() != 0) {
                            System.out.println("Ожидание...");
                            String in = ois.readUTF();
                            System.out.println(in);
                        }
                        break;
                    }
                }
            }
            System.out.println("Закрытие");
        } catch (UnknownHostException el) {
            el.printStackTrace();
        } catch (IOException el) {
            el.printStackTrace();
        } catch (NoSuchAlgorithmException el) {
            throw new RuntimeException(el);
        } catch (NoSuchProviderException el) {
            throw new RuntimeException(el);
        }
    }


    //Модули
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

    public static byte[] openssl_sha1() {
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

}

