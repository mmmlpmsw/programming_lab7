package ifmo.programming.lab7.server;

import ifmo.programming.lab7.transmitter.SenderAdapter;
import ifmo.programming.lab7.Message;
import ifmo.programming.lab7.Utils.StringEntity;
import ifmo.programming.lab7.client.Room;
import ifmo.programming.lab7.transmitter.Sender;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.nio.file.AccessDeniedException;
import java.sql.*;
import java.util.ArrayList;

class Resolver implements Runnable {

    private Message message;
    private int requestID;
    private static InetAddress address;

    Resolver(Message message, int requestID, InetAddress address){
        this.address = address;
        this.message = message;
        this.requestID = requestID;
    }

    @Override
    public void run() {
        try {
            Message response = Resolver.resolve(message, requestID);
            respond(response.serialize(), message.getSourcePort(), address);
        } catch (IOException e) {
            System.err.println("Ошибка ввода/вывода: " + e.toString());
        } catch (SQLException e) {
            try {
                respond(m("На сервере SQL: " + e.toString()).serialize(), message.getSourcePort(), address);
            } catch (IOException ignored) {}
        }
    }

    static Message resolve(Message message, int requestID) throws SQLException {
        Connection connection = Server.getConnection();

        switch (message.getText()) {

            case "help":
                return m(RoomDatabaseInteractor.getHelp());

            case "info":
                return m(RoomDatabaseInteractor.getInfo(connection));

            case "show":
                return m(RoomDatabaseInteractor.makeShow(connection));

            case "load":
                if (!message.hasAttachment()) {
                    return m("Имя файла не указано.\n" +
                            "Введите help для получения справки.");
                }
                try {
                    if (!(message.getAttachment() instanceof StringEntity)) {
                        return m("Клиент отправил запрос в неверном формате : аргумент сообщения должен быть строкой.");
                    }
                    ArrayList<Room> rooms = JSONParse.getRoomsFromJSON(FileLoader.getFileContent(((StringEntity) message.getAttachment()).getString()));
                    return m(RoomDatabaseInteractor.makeImport(rooms, connection, message.getUserid(), message.getLogin(), message.getPassword()));
                } catch (AccessDeniedException e) {
                    return m("Ошибка: нет доступа для чтения.");
                } catch (FileNotFoundException e) {
                    return m("Ошибка: файл не найден.");
                } catch (IOException e) {
                    return m("Ошибка чтения/записи.");
                } catch (Exception ignored) {
                    ignored.getMessage();
                    return m("Произошла ошибка.");
                }

            case "import":
                if (!message.hasAttachment()) {
                    return m("Имя файла не указано.\n" +
                            "Введите help для получения справки.");
                }
                try {
                    if (!(message.getAttachment() instanceof StringEntity)) {
                        return m("Клиент отправил запрос в неверном формате : аргумент сообщения должен быть строкой.");
                    }
                    ArrayList<Room> rooms = JSONParse.getRoomsFromJSON(((StringEntity) message.getAttachment()).getString());
                    return m(RoomDatabaseInteractor.makeImport(rooms, connection, message.getUserid(), message.getLogin(), message.getPassword()));
                } catch (AccessDeniedException e) {
                    return m("Ошибка: нет доступа для чтения.");
                } catch (FileNotFoundException e) {
                    return m("Ошибка: файл не найден.");
                } catch (IOException e) {
                    return m("Ошибка чтения/записи.");
                } catch (Exception e) {
                    e.printStackTrace();
                    return m("Возникла ошибка.");
                }

            case "remove_lower":
                try {
                    if (!message.hasAttachment()) {
                        return m("Нельзя удалить объект из коллекции: клиент отправил данные в неверном формате. " +
                                "Введите help для получения справки.");
                    }
                    if (!(message.getAttachment() instanceof Room)) {
                        return m("Клиент отправил данные в неверном формате : аргумент должен быть сериализованным объектом.");
                    }
                    Room room = (Room)message.getAttachment();
                    return m(RoomDatabaseInteractor.removeLowerThanRoom(room, connection, message.getUserid(), message.getLogin(), message.getPassword()));

                } catch (Exception e) {
                    return m("Не получилось удалить комнату: " + e.getMessage());
                }

            case "remove_greater":
                try {
                    if (!message.hasAttachment()) {
                        return m("Нельзя удалить объект из коллекции: клиент отправил данные в неверном формате. " +
                                "Введите help для получения справки.");
                    }
                    if (!(message.getAttachment() instanceof Room)) {
                        return m("Клиент отправил данные в неверном формате : аргумент должен быть сериализованным объектом.");
                    }
                    Room room = (Room)message.getAttachment();
                    return m(RoomDatabaseInteractor.removeGreaterThanRoom(room, connection, message.getUserid(), message.getLogin(), message.getPassword()));

                } catch (Exception e) {
                    return m("Не получилось удалить комнату: " + e.getMessage());
                }

            case "remove":
                try {
                    if (!message.hasAttachment()) {
                        return m("Нельзя добавить объект в коллекцию: клиент отправил данные в неверном формате. " +
                                "Введите help для получения справки.");
                    }
                    if (!(message.getAttachment() instanceof Room)) {
                        return m("Клиент отправил данные в неверном формате : аргумент должен быть сериализованным объектом.");
                    }
                    Room room = (Room)message.getAttachment();
                    return m(RoomDatabaseInteractor.removeRoom(room, connection, message.getUserid(), message.getLogin(), message.getPassword()));

                } catch (Exception e) {
                    return m("Не получилось удалить комнату: " + e.getMessage());
                }

            case "add":
                    if (!message.hasAttachment()) {
                        return m("Нельзя добавить объект в коллекцию: клиент отправил данные в неверном формате. " +
                                "Введите help для получения справки.");
                    }
                    if (!(message.getAttachment() instanceof Room)) {
                        return m("Клиент отправил данные в неверном формате : аргумент должен быть сериализованным объектом.");
                    }
                Room room = (Room) message.getAttachment();
                return m(RoomDatabaseInteractor.addRoom(room, connection, message.getUserid(), message.getLogin(), message.getPassword()));


            case "register": {
                if (!(message.getAttachment() instanceof StringEntity)) {
                    return m("Клиент отправил данные в неверном формате");
                }
                StringEntity entityAttachment = (StringEntity) message.getAttachment();
                String[] attachment = entityAttachment.getArguments();
                if (attachment.length < 2) {
                    return m("Клиент отправил неполные данные");
                }
                String name = attachment[0];
                String email = attachment[1];

                return m(UsersDatabaseInteractor.register(name, email, connection));
            }

            case "login":
                if (!(message.getAttachment() instanceof StringEntity)) {
                    return m("Клиент отправил данные в неверном формате");
                }
                StringEntity entityAttachment = (StringEntity) message.getAttachment();
                String[] attachment = entityAttachment.getArguments();
                if (attachment.length < 2) {
                    return m("Клиент отправил неполные данные");
                }
                String email = attachment[0];
                String pswd = attachment[1];
                return UsersDatabaseInteractor.login(email, pswd, connection);

            default:
                return m("Не знаю такой команды");
        }
    }


    private static void respond(byte[] data, int port, InetAddress address) {
        Sender.send(data, address, port, false, new SenderAdapter() {
            @Override
            public void onError(String message) {
                System.out.println("Не получилось ответить на запрос: " + message);
            }
        });
    }

    /**
     * Генерирует {@link Message} из текста и аргумента.
     * Использует короткое имя, чтобы вызовы не делали код большим
     *
     * @param text текст сообщения
     *
     * @param argument аргумент
     *
     * @return сообщение
     */
    static Message m(String text, Serializable argument) {
        return new Message(text, argument);
    }

    private static Message m(String text) {
        return m(text, null);
    }


}
