package lab6;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final static String dirPathC = "src\\main\\resources\\lab6\\client2files";
    private final static String dirPathS = "src\\main\\resources\\lab6\\serverfiles";

    public static void main(String[] args) {
        openssl1_SamDoubleKey(dirPathS);
        openssl2_SamZapr(dirPathS);
        openssl3_SamZaprRead(dirPathS);
        openssl4_SamGen(dirPathS);
        openssl5_SamGenRead(dirPathS);
        openssl6_NeSamoKey(dirPathS);
        openssl7_NeSamoZap(dirPathS);
        openssl8_NeSamoGen(dirPathS);
        openssl9_NeSamoGenRead(dirPathS);
        String[] dirPathS2 = {dirPathS + "\\intermediate_cert.pem"};
        String[] dirPathS3 = {dirPathS + "\\intermediate_cert.pem"};
        String[] dirPathS4;


        try (ServerSocket server = new ServerSocket(3345)) {
            Socket client1 = server.accept();
            Socket client2 = server.accept();
            System.out.print("Клиент подключен\n");
            // канал чтения из сокета
            DataInputStream in1 = new DataInputStream(client1.getInputStream());
            DataInputStream in2 = new DataInputStream(client2.getInputStream());
            //Канал записи в сокет
            DataOutputStream out1 = new DataOutputStream(client1.getOutputStream());
            DataOutputStream out2 = new DataOutputStream(client2.getOutputStream());
            while (!client1.isClosed() || !client2.isClosed()) {
                //Основная работа
                //Создание директории для сохранения принятых файлов
                File directory = new File(dirPathS);
                if (!directory.exists()) {
                    directory.mkdirs();
                }


                //Получение файлов от 1 пользователя
                while (true) {
                    //Получение имени файла от 1 пользователя
                    String filename = in1.readUTF();
                    if (filename.equals("end")) {
                        break;
                    }
                    // Получение размера файла от 1 пользователя
                    long fileSize = in1.readLong();
                    // Создание объекта File для сохранения принятого файла
                    File receivedFile = new File(directory, filename);
                    //Добавление в массив строк местоположения полученного файла
                    String[] newdirPathS = new String[dirPathS2.length + 1];  // Создание нового массива с дополнительным размером
                    for (int i = 0; i < dirPathS2.length; i++) {
                        newdirPathS[i] = dirPathS2[i];  // Копирование исходных значений в новый массив
                    }
                    newdirPathS[dirPathS2.length] = directory + "\\" + filename;
                    dirPathS2 = newdirPathS;  // Присваивание нового массива исходному
                    // Создание FileOutputStream для записи данных в файл
                    FileOutputStream fos = new FileOutputStream(receivedFile);
                    // Чтение данных файла из потока и запись их в FileOutputStream
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long bytesReceived = 0;
                    while (bytesReceived < fileSize && (bytesRead = in1.read(buffer, 0, (int) Math.min(buffer.length, fileSize - bytesReceived))) != -1) {
                        fos.write(buffer, 0, bytesRead);
                        bytesReceived += bytesRead;
                    }
                    fos.close();
                    System.out.println("Принят файл: " + receivedFile.getAbsolutePath());
                }
                boolean very1 = openssl14_Verify(dirPathS);

                //Получение файлов от 2 пользователя
                while (true) {
                    // Получение имени файла от 2 пользователя
                    String filename2 = in2.readUTF();
                    if (filename2.equals("end")) {
                        break;
                    }
                    // Получение размера файла от 2 пользователя
                    long fileSize2 = in2.readLong();
                    // Создание объекта File для сохранения принятого файла
                    File receivedFile2 = new File(directory, filename2);
                    //Добавление в массив строк местоположения полученного файла
                    String[] newdirPathS = new String[dirPathS3.length + 1];  // Создание нового массива с дополнительным размером
                    for (int i = 0; i < dirPathS3.length; i++) {
                        newdirPathS[i] = dirPathS3[i];  // Копирование исходных значений в новый массив
                    }
                    newdirPathS[dirPathS3.length] = directory + "\\" + filename2;
                    dirPathS3 = newdirPathS;  // Присваивание нового массива исходному
                    // Создание FileOutputStream для записи данных в файл
                    FileOutputStream fos2 = new FileOutputStream(receivedFile2);
                    // Чтение данных файла из потока и запись их в FileOutputStream
                    byte[] buffer2 = new byte[4096];
                    int bytesRead2;
                    long bytesReceived2 = 0;
                    while (bytesReceived2 < fileSize2 && (bytesRead2 = in2.read(buffer2, 0, (int) Math.min(buffer2.length, fileSize2 - bytesReceived2))) != -1) {
                        fos2.write(buffer2, 0, bytesRead2);
                        bytesReceived2 += bytesRead2;
                    }
                    fos2.close();
                    System.out.println("Принят файл: " + receivedFile2.getAbsolutePath());
                }
                boolean very2 = openssl14_Verify2(dirPathS);

                //Отправка файлов 2 пользователю
                if (very2 = true) {
                    for (String filepath : dirPathS2) {
                        File file = new File(filepath);
                        if (!file.exists()) {
                            System.out.println("Файл не существует: " + filepath);
                            continue;
                        }
                        // Отправка имени файла 2 пользователю
                        String filename = file.getName();
                        out2.writeUTF(filename);
                        out2.flush();
                        // Отправка размера файла 2 пользователю
                        long fileSize = file.length();
                        out2.writeLong(fileSize);
                        out2.flush();
                        // Отправка содержимого файла 2 пользователю
                        FileInputStream fis = new FileInputStream(file);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            out2.write(buffer, 0, bytesRead);
                        }
                        out2.flush();
                        fis.close();
                        System.out.println("Файл отправлен: " + filepath);
                    }
                    // Отправка сообщения "end" для завершения передачи файлов
                    out2.writeUTF("end");
                    out2.flush();
                }

                //Отправка файлов 1 пользователю
                if (very1 = true) {
                    for (String filepath : dirPathS3) {
                        File file = new File(filepath);
                        if (!file.exists()) {
                            System.out.println("Файл не существует: " + filepath);
                            continue;
                        }

                        // Отправка имени файла 1 пользователю
                        String filename = file.getName();
                        out1.writeUTF(filename);
                        out1.flush();
                        // Отправка размера файла 1 пользователю
                        long fileSize = file.length();
                        out1.writeLong(fileSize);
                        out1.flush();
                        // Отправка содержимого файла 1 пользователю
                        FileInputStream fis = new FileInputStream(file);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            out1.write(buffer, 0, bytesRead);
                        }
                        out1.flush();
                        fis.close();

                        System.out.println("Файл отправлен: " + filepath);
                    }
                    // Отправка сообщения "end" для завершения передачи файлов
                    out1.writeUTF("end");
                    out1.flush();
                }

                System.out.println("Все файлы успешно приняты.");
            }
            // если условие выхода - верно выключение соединения
            in1.close();
            out1.close();
            client1.close();
            in2.close();
            out2.close();
            client2.close();
            server.close();
            System.out.println("Подключение закрыто");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void openssl1_SamDoubleKey(String filePath) {
        try { //genpkey -algorithm ED448 -out root_keypair.pem
            Runtime runtime = Runtime.getRuntime();
            String opensslCommand = "\"C:\\Program Files\\Git\\usr\\bin\\openssl.exe\" genpkey -algorithm RSA -out \"" + filePath + "\\root_keypair.pem\"";
            Process process = runtime.exec(opensslCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // Ожидание завершения процесса
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Ошибка в openssl1_SamDoubleKey");
            } else {
                System.out.println("openssl1_SamDoubleKey успешно сгенерирован");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void openssl2_SamZapr(String filePath) {
        try { //req -new -subj "/CN=ROOT CA" -addext "basicConstraints=critical,CA:TRUE" -key root_keypair.pem -out root_csr.pem
            Runtime runtime = Runtime.getRuntime();
            String opensslCommand = "\"C:\\Program Files\\Git\\usr\\bin\\openssl.exe\" req -new -subj \"/CN=ROOT CA\" " +
                    "-addext \"basicConstraints=critical,CA:TRUE\" -key \""
                    + filePath + "\\root_keypair.pem\" -out \"" + filePath + "\\root_csr.pem\"";
            Process process = runtime.exec(opensslCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // Ожидание завершения процесса
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Ошибка в openssl2_SamZapr");
            } else {
                System.out.println("openssl2_SamZapr успешно сгенерирован");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void openssl3_SamZaprRead(String filePath) {
        try { //req -in root_csr.pem -noout -text
            Runtime runtime = Runtime.getRuntime();
            String opensslCommand = "\"C:\\Program Files\\Git\\usr\\bin\\openssl.exe\" req " +
                    "-in \"" + filePath + "\\root_csr.pem\" -noout -text";
            Process process = runtime.exec(opensslCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // Ожидание завершения процесса
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Ошибка в openssl3_SamZaprRead");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void openssl4_SamGen(String filePath) {
        try { //x509 -req -in root_csr.pem -signkey root_keypair.pem -days 3650 -out root_cert.pem
            Runtime runtime = Runtime.getRuntime();
            String opensslCommand = "\"C:\\Program Files\\Git\\usr\\bin\\openssl.exe\" x509 -req " +
                    "-in \"" + filePath + "\\root_csr.pem\" -signkey \"" + filePath + "\\root_keypair.pem\" " +
                    "-days 3650 -out \"" + filePath + "\\root_cert.pem\"";
            Process process = runtime.exec(opensslCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // Ожидание завершения процесса
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Ошибка в openssl4_SamGen");
            } else {
                System.out.println("openssl4_SamGen успешно сгенерирован");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void openssl5_SamGenRead(String filePath) {
        try { //x509 -in root_cert.pem -noout -text
            Runtime runtime = Runtime.getRuntime();
            String opensslCommand = "\"C:\\Program Files\\Git\\usr\\bin\\openssl.exe\" x509 " +
                    "-in \"" + filePath + "\\root_cert.pem\" -noout -text";
            Process process = runtime.exec(opensslCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // Ожидание завершения процесса
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Ошибка в openssl5_SamGenRead");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void openssl6_NeSamoKey(String filePath) {
        try { //genpkey -algorithm ED448 -out intermediate_keypair.pem
            Runtime runtime = Runtime.getRuntime();
            String opensslCommand = "\"C:\\Program Files\\Git\\usr\\bin\\openssl.exe\" genpkey " +
                    "-algorithm ED448 -out \"" + filePath + "\\intermediate_keypair.pem\"";
            Process process = runtime.exec(opensslCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // Ожидание завершения процесса
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Ошибка в openssl6_NeSamoKey");
            } else {
                System.out.println("openssl6_NeSamoKey успешно сгенерирован");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void openssl7_NeSamoZap(String filePath) {
        try { //req -new -subj "/CN=INTERMEDIATE CA" -addext "basicConstraints=critical,CA:TRUE" -key intermediate_keypair.pem -out intermediate_csr.pem
            Runtime runtime = Runtime.getRuntime();
            String opensslCommand = "\"C:\\Program Files\\Git\\usr\\bin\\openssl.exe\" req -new -subj \"/CN=INTERMEDIATE CA\" " +
                    "-addext \"basicConstraints=critical,CA:TRUE\" -key \"" + filePath + "\\intermediate_keypair.pem\" " +
                    "-out \"" + filePath + "\\intermediate_csr.pem\"";
            Process process = runtime.exec(opensslCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // Ожидание завершения процесса
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Ошибка в openssl7_NeSamoZap");
            } else {
                System.out.println("openssl7_NeSamoZap успешно сгенерирован");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void openssl8_NeSamoGen(String filePath) {
        try { //x509 -req -in intermediate_csr.pem -CA root_cert.pem -CAkey root_keypair.pem -extfile extensions.cnf -extensions nonLeaf -days 3650 -out intermediate_cert.pem
            Runtime runtime = Runtime.getRuntime();
            String opensslCommand = "\"C:\\Program Files\\Git\\usr\\bin\\openssl.exe\" x509 -req " +
                    "-in \"" + filePath + "\\intermediate_csr.pem\" -CA \"" + filePath + "\\root_cert.pem\" " +
                    "-CAkey \"" + filePath + "\\root_keypair.pem\" " +
                    "-extfile \"C:\\Users\\aleks\\Desktop\\Универ\\6 семестр\\защита данных\\лабы\\kharchenko\\lab7v2\\extensions.cnf\" " +
                    "-extensions nonLeaf -days 3650 -out \"" + filePath + "\\intermediate_cert.pem\"";
            Process process = runtime.exec(opensslCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // Ожидание завершения процесса
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Ошибка в openssl8_NeSamoGen");
            } else {
                System.out.println("openssl8_NeSamoGen успешно сгенерирован");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void openssl9_NeSamoGenRead(String filePath) {
        try { //x509 -in intermediate_cert.pem -noout -text
            Runtime runtime = Runtime.getRuntime();
            String opensslCommand = "\"C:\\Program Files\\Git\\usr\\bin\\openssl.exe\" x509 " +
                    "-in \"" + filePath + "\\intermediate_cert.pem\" -noout -text";
            Process process = runtime.exec(opensslCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // Ожидание завершения процесса
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Ошибка в openssl9_NeSamoGenRead");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static boolean openssl14_Verify(String filePath) {
        boolean ret = false;
        try { //verify -verbose -show_chain -trusted root_cert.pem -untrusted intermediate_cert.pem leaf_cert.pem
            Runtime runtime = Runtime.getRuntime();
            String opensslCommand = "\"C:\\Program Files\\Git\\usr\\bin\\openssl.exe\" verify -verbose -show_chain " +
                    "-trusted \"" + filePath + "\\root_cert.pem\" " +
                    "-untrusted \"" + filePath + "\\intermediate_cert.pem\" \"" + filePath + "\\leaf_cert1.pem\"";
            Process process = runtime.exec(opensslCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // Ожидание завершения процесса
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Ошибка в openssl4_Verify");
                ret = false;
            } else {
                ret = true;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static boolean openssl14_Verify2(String filePath) {
        boolean ret = false;
        try { //verify -verbose -show_chain -trusted root_cert.pem -untrusted intermediate_cert.pem leaf_cert.pem
            Runtime runtime = Runtime.getRuntime();
            String opensslCommand = "\"C:\\Program Files\\Git\\usr\\bin\\openssl.exe\" verify -verbose -show_chain " +
                    "-trusted \"" + filePath + "\\root_cert.pem\" " +
                    "-untrusted \"" + filePath + "\\intermediate_cert.pem\" \"" + filePath + "\\leaf_cert2.pem\"";
            Process process = runtime.exec(opensslCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // Ожидание завершения процесса
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Ошибка в openssl4_Verify");
                ret = false;
            } else {
                ret = true;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static void openssl12_LeafGen1(String filePath) {
        try { //x509 -req -in leaf_csr.pem -CA intermediate_cert.pem -CAkey intermediate_keypair.pem -extfile extensions.cnf -extensions Leaf -days 3650 -out leaf_cert.pem
            Runtime runtime = Runtime.getRuntime();
            String opensslCommand = "\"C:\\Program Files\\Git\\usr\\bin\\openssl.exe\" x509 -req -in \"" + filePath + "\\leaf_csr1.pem\" " +
                    "-CA \"" + filePath + "\\intermediate_cert.pem\" " +
                    "-CAkey \"" + filePath + "\\intermediate_keypair.pem\" " +
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

}
