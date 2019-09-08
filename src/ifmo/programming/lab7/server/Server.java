package ifmo.programming.lab7.server;

import ifmo.programming.lab7.transmitter.Receiver;
import ifmo.programming.lab7.Message;
import ifmo.programming.lab7.json.JSONParseException;
import ifmo.programming.lab7.transmitter.ReceiverListener;

import javax.mail.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.net.InetAddress;
import java.nio.file.AccessDeniedException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;
import java.util.Properties;

public class Server {
    private static ServerConfig config;
    private static Connection connection;
    static Session mailSession;

    public static void main(String[] args) {
        try {
            initConfig();
            initDatabaseConnection();
            initTables();
            initEmail();

            Receiver receiver = new Receiver(config.getPort(), false);
            receiver.setListener(generateListener());
            receiver.startListening();

            System.out.println("Сервер слушает порт " + config.getPort() + "...");
        } catch (Throwable e) {
            System.out.println("Не получилось запустить сервер: " + e.toString());
        }
    }

    private static void processRequest(int requestID, byte[] data, InetAddress address) {
        Message message;
        try {
            message = Message.deserialize(data);
            String text = message.getText();
            System.out.println("Пришёл запрос: " + text);

            new Thread(new Resolver(message, requestID, address)).start();

        } catch (ClassNotFoundException | IOException e) {
            System.out.println("Не получилось обработать запрос: " + e.toString());
        }
    }


    private static ReceiverListener generateListener() {
        return new ReceiverListener() {
            @Override
            public void received(int requestID, byte[] data, InetAddress address) {
                processRequest(requestID, data, address);
            }

            @Override
            public void exceptionThrown(Exception e) {
                System.out.println("Не получилось принять запрос: " + e.toString());
            }
        };
    }

    /**
     * Отправляет приказ на отчисление в ближайший принтер
     * @param message Прощальное сообщение
     */
    private static void sendDown(String message) {
        System.err.println(message);
        System.exit(-1);
    }

    private static void initConfig() {
        System.out.println("Загружается конфигурация...");
        try {
            config = ServerConfig.fromFile("config/server-config.json");
        } catch (FileNotFoundException e) {
            sendDown("Нет файла настроек. Он должен быть тут: config/server-config.json");
        } catch (AccessDeniedException e) {
            sendDown("За проезд передайте.");
        } catch (IOException e) {
            sendDown("Ошибка ввода-вывода: " + e.getMessage());
        } catch (JSONParseException e) {
            sendDown("Файл настроек повреждён: " + e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException e) {
            sendDown(e.getMessage());
        } catch (Throwable e) {
            sendDown("Совершенно неожиданная ошибка, которая не должна была произойти вообще: " + e.toString());
        }
        System.out.println("Конфигурация загружена");
    }

    /**
     * Подключается к базе данных
     */
    private static void initDatabaseConnection() {
        System.out.println("Соединяемся с базой данных...");

        try {
            Class.forName(config.getJdbcDriver());
        } catch (ClassNotFoundException e) {
            sendDown("Чтобы подключиться к базе данных, нужен драйвер: " + config.getJdbcDriver());
        }

        String databaseUrl = String.format(
                "jdbc:%s://%s:%s/%s",
                config.getJdbcLangProtocol(),
                config.getDatabaseHost(),
                config.getDatabasePort(),
                config.getDatabaseName()
        );
        try {
            connection = DriverManager.getConnection(databaseUrl, config.getDatabaseUser(), config.getDatabasePassword());
        } catch (SQLException e) {
            sendDown("Не получилось соединиться с базой данных: " + e.toString());
        }

        System.out.println("Соединились с базой данных!");
    }

    /**
     * Создаёт необходимые таблицы, если их нет
     */
    private static void initTables() {
        try {
            Statement statement = connection.createStatement();
            statement.execute("create table if not exists rooms " +
                    "(id serial primary key not null, name text, height integer, width integer, x integer, y integer," +
                    "creationdate timestamp, thingcount integer, shelfname text, user_id integer)"
            );
            statement.execute("create table if not exists users (" +
                    "id serial primary key not null, name text, email text unique, password_hash bytea)"
            );
        } catch (SQLException e) {
            sendDown("Не получилось создать таблицы: " + e.toString());
        }
    }

    /**
     * Инициализирует соединение с JavaMail API
     */
    private static void initEmail() {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "in-v3.mailjet.com");
        properties.put("mail.smtp.port", 587);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.ssl.enable", "false");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Authenticator mailAuth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("e06513d48ba28a105caff1c08c4f0031", "bd95b9f237746446436ce4cb8420ef32");
            }
        };
        mailSession = Session.getDefaultInstance(properties, mailAuth);


    }

    static void sendEMail(String to, String subject, String content) throws MessagingException {

        MimeMessage message = new MimeMessage(mailSession);
        message.setFrom("mmmlpmsw@protonmail.com");
        message.setRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject);
        message.setContent(content, "text/html; charset=utf-8");

        Transport.send(message);
    }


        /**
         * @return Конфигурация сервера
         */
    public static ServerConfig getConfig() {
        return config;
    }

    public static Connection getConnection() {
        return connection;
    }
}
