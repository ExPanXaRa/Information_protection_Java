package lab6;

import java.io.*;
import java.net.*;


public class Client {
    private final static String dirPathC = "src\\main\\resources\\lab6\\client1files";
    private final static String dirPathS = "src\\main\\resources\\lab6\\serverfiles";

    public static void main(String[] args) {
        openssl10_LeafKey(dirPathC);
        openssl11_LeafZap(dirPathC);
        openssl12_LeafGen(dirPathC, dirPathS);
        openssl13_LeafGenRead(dirPathC);
        openssl15_smime(dirPathC);
        String[] dirPathS = {
                dirPathC + "\\leaf_cert1.pem",
                dirPathC + "\\Client_signed1.mime"
        };
        // запускаем подключение сокета по известным координатам и инициализируем приём сообщений с консоли клиента
        try (Socket socket = new Socket("localhost", 3345);
             BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
             DataOutputStream oos = new DataOutputStream(socket.getOutputStream());
             DataInputStream ois = new DataInputStream(socket.getInputStream())) {
            while (!socket.isOutputShutdown()) if (br.ready()) {
                // Основная работа
                System.out.println("Клиент отправил сообщение...");
                String clientCommand = br.readLine();
                //Отправка файлов на сервер для пересылки 2 пользователю
                for (String filepath : dirPathS) {
                    File file = new File(filepath);
                    // Отправка имени файла на сервер
                    String filename = file.getName();
                    oos.writeUTF(filename);
                    oos.flush();
                    // Отправка размера файла на сервер
                    long fileSize = file.length();
                    oos.writeLong(fileSize);
                    oos.flush();
                    // Отправка содержимого файла на сервер
                    FileInputStream fis = new FileInputStream(file);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        oos.write(buffer, 0, bytesRead);
                    }
                    oos.flush();
                    fis.close();

                    System.out.println("Файл отправлен: " + filepath);
                }
                // Отправка сообщения "end" для завершения передачи файлов
                oos.writeUTF("end");
                oos.flush();
                System.out.println("Все файлы отправлены.");

                //Получение файлов от 1 пользователя через сервер
                File directory = new File(dirPathC);
                while (true) {
                    // Получение имени файла от клиента
                    String filename = ois.readUTF();
                    if (filename.equals("end")) {
                        break;
                    }

                    // Получение размера файла от сервера
                    long fileSize = ois.readLong();

                    // Создание объекта File для сохранения принятого файла
                    File receivedFile = new File(directory, filename);

                    // Создание FileOutputStream для записи данных в файл
                    FileOutputStream fos = new FileOutputStream(receivedFile);

                    // Чтение данных файла из потока и запись их в FileOutputStream
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long bytesReceived = 0;
                    while (bytesReceived < fileSize && (bytesRead = ois.read(buffer, 0, (int) Math.min(buffer.length, fileSize - bytesReceived))) != -1) {
                        fos.write(buffer, 0, bytesRead);
                        bytesReceived += bytesRead;
                    }
                    fos.close();

                    System.out.println("Принят файл: " + receivedFile.getAbsolutePath());
                }

                //Верификация документа
                openssl16_verifyDoc(dirPathC);


                //отправление данных
                oos.writeUTF(clientCommand);
                oos.flush();
                // проверка условия выхода из соединения
                if (clientCommand.equalsIgnoreCase("выход")) {
                    System.out.println("Клиент закрыл соединение");
                    if (ois.available() != 0) {
                        System.out.println("Ожидание...");
                        String in = ois.readUTF();
                        System.out.println(in);
                    }
                    break;
                }
                if (ois.available() != 0) {
                    String in = ois.readUTF();
                    System.out.println(in);
                }
            }

            System.out.println("Закрытие");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openssl10_LeafKey(String filePath) {
        try { //genpkey -algorithm ED448 -out leaf_keypair.pem
            Runtime runtime = Runtime.getRuntime();
            String opensslCommand = "\"C:\\Program Files\\Git\\usr\\bin\\openssl.exe\" genpkey -algorithm RSA " +
                    "-out \"" + filePath + "\\leaf_keypair1.pem\"";
            Process process = runtime.exec(opensslCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // Ожидание завершения процесса
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Ошибка в openssl10_LeafKey");
            } else {
                System.out.println("openssl10_LeafKey успешно сгенерирован");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void openssl11_LeafZap(String filePath) {
        try { //req -new -subj "/CN=LEAF" -addext "basicConstraints=critical,CA:FALSE" -key leaf_keypair.pem -out leaf_csr.pem
            Runtime runtime = Runtime.getRuntime();
            String opensslCommand = "\"C:\\Program Files\\Git\\usr\\bin\\openssl.exe\" req -new -subj \"/CN=LEAF\" " +
                    "-addext \"basicConstraints=critical,CA:FALSE\" -key \"" + filePath + "\\leaf_keypair1.pem\" " +
                    "-out \"" + filePath + "\\leaf_csr1.pem\"";
            Process process = runtime.exec(opensslCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // Ожидание завершения процесса
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Ошибка в openssl11_LeafZap");
            } else {
                System.out.println("openssl11_LeafZap успешно сгенерирован");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void openssl12_LeafGen(String filePath, String dirPathS) {
        try { //x509 -req -in leaf_csr.pem -CA intermediate_cert.pem -CAkey intermediate_keypair.pem -extfile extensions.cnf -extensions Leaf -days 3650 -out leaf_cert.pem
            Runtime runtime = Runtime.getRuntime();
            String opensslCommand = "\"C:\\Program Files\\Git\\usr\\bin\\openssl.exe\" x509 -req -in \"" + filePath + "\\leaf_csr1.pem\" " +
                    "-CA \"" + dirPathS + "\\intermediate_cert.pem\" " +
                    "-CAkey \"" + dirPathS + "\\intermediate_keypair.pem\" " +
                    "-extfile \"C:\\Users\\aleks\\Desktop\\Универ\\6 семестр\\защита данных\\лабы\\kharchenko\\lab7v2\\extensions.cnf\" " +
                    "-extensions Leaf -days 3650 -out \"" + filePath + "\\leaf_cert1.pem\"";
            Process process = runtime.exec(opensslCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // Ожидание завершения процесса
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Ошибка в openssl12_LeafGen");
            } else {
                System.out.println("openssl12_LeafGen успешно сгенерирован");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void openssl13_LeafGenRead(String filePath) {
        try { //x509 -in leaf_cert.pem -noout -text
            Runtime runtime = Runtime.getRuntime();
            String opensslCommand = "\"C:\\Program Files\\Git\\usr\\bin\\openssl.exe\" x509 " +
                    "-in \"" + filePath + "\\leaf_cert1.pem\" -noout -text";
            Process process = runtime.exec(opensslCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // Ожидание завершения процесса
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Ошибка в openssl13_LeafGenRead");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void openssl15_smime(String filePath) {
        try { //smime -sign -in document.txt -out signed_document.txt -signer certificate.pem -inkey private_key.pem
            Runtime runtime = Runtime.getRuntime();
            String opensslCommand = "\"C:\\Program Files\\Git\\usr\\bin\\openssl.exe\" smime -sign " +
                    "-in \"" + filePath + "\\Client1.txt\" " +
                    "-out \"" + filePath + "\\Client_signed1.mime\" " +
                    "-signer \"" + filePath + "\\leaf_cert1.pem\" " +
                    "-inkey \"" + filePath + "\\leaf_keypair1.pem\" -nocerts";
            Process process = runtime.exec(opensslCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // Ожидание завершения процесса
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Ошибка в openssl15_smime");
            } else {
                System.out.println("Файл успешно подписан сертификатом");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void openssl16_verifyDoc(String filePath) {
        try { //x509 -in leaf_cert.pem -noout -text
            Runtime runtime = Runtime.getRuntime();
            String opensslCommand = "\"C:\\Program Files\\Git\\usr\\bin\\openssl.exe\" smime -verify " +
                    "-in \"" + filePath + "\\Client_signed2.mime\"  " +
                    "-CAfile  \"" + filePath + "\\intermediate_cert.pem\" " +
                    "-certfile \"" + filePath + "\\leaf_cert2.pem\" " +
                    "-out \"" + filePath + "\\Client2.txt\" -partial_chain";
            Process process = runtime.exec(opensslCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // Ожидание завершения процесса
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Ошибка в openssl16_verifyDoc");
            } else {
                System.out.println("openssl16_verifyDoc выполнено");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

