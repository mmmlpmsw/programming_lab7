package ifmo.programming.lab7.server;

import ifmo.programming.lab7.client.AuthorizeHelper;
import ifmo.programming.lab7.client.Room;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.ZoneOffset;
import java.util.ArrayList;

import static ifmo.programming.lab7.Utilities.colorize;
import static ifmo.programming.lab7.server.RegisterHelper.hashPassword;

public class RoomDatabaseInteractor {

    private static String PASSWORD_SALT = "EDfvcpoi456GESChgfgv10";

    static String makeImport(ArrayList<Room> rooms, Connection connection, Integer user_id, String login, String password)throws Exception{
        if (connection == null) return "Сервер не подключён к базе данных.";
        if (user_id == -1 || login == null || password == null) return "Вы не авторизованы. Чтобы войти, введите login.";
        else {
            PreparedStatement s = connection.prepareStatement("select name from users where email = ? and password_hash = ?");
            s.setString(1, login);
            s.setBytes(2, hashPassword(password + PASSWORD_SALT));

            if (s.execute()) {
                try {
                    for (Room room : rooms) {
                        PreparedStatement statement = connection.prepareStatement(
                                "insert into rooms " +
                                        "(name, height, width, x, y, creationdate, thingcount, shelfname, user_id) " +
                                        "values (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                        );
                        statement.setString(1, room.getName());
                        statement.setInt(2, room.getHeight());
                        statement.setInt(3, room.getWidth());
                        statement.setInt(4, room.getX());
                        statement.setInt(5, room.getY());
                        statement.setTimestamp(6, new Timestamp(room.getCreationDate().toEpochSecond(ZoneOffset.UTC) * 1000L));
                        statement.setInt(7, room.getShelf().getThingcount());
                        statement.setString(8, room.getShelf().getName());
                        statement.setInt(9, user_id);
                        statement.execute();
                    }

                    return "Загрузка " + rooms.size() + " комнат прошла успешно.";
                } catch (SQLException e) {
                    e.printStackTrace();
                    return "Возникла внутренняя ошибка сервера.";
                }

            } else {
                return "Вы не авторизованы. Чтобы войти, введите login.";
            }

        }
    }

    static String addRoom(Room room, Connection connection, Integer user_id, String login, String password){
        if (connection == null) return "Сервер не подключён к базе данных.";
        if (user_id == -1 || login == null || password == null) return "Вы не авторизованы. Чтобы войти, введите login.";
        else {
            try {
                PreparedStatement statement = connection.prepareStatement(
                        "select * from " + "users where id = ? and email = ? and password_hash = ?"
                );
                statement.setInt(1, user_id);
                statement.setString(2, login);
                statement.setBytes(3, hashPassword(password + PASSWORD_SALT));

                ResultSet resultSet = statement.executeQuery();
                if (!resultSet.next()) return "Вы не авторизованы. Чтобы войти, введите login.";

                statement = connection.prepareStatement(
                        "insert into " + "rooms " +
                                "(name, height, width, x, y, creationdate, thingcount, shelfname, user_id) " +
                                "values (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                );
                statement.setString(1, room.getName());
                statement.setInt(2, room.getHeight());
                statement.setInt(3, room.getWidth());
                statement.setInt(4, room.getX());
                statement.setInt(5, room.getY());
                statement.setTimestamp(6,new Timestamp(room.getCreationDate().toEpochSecond(ZoneOffset.UTC)*1000L));
                statement.setInt(7, room.getShelf().getThingcount());
                statement.setString(8, room.getShelf().getName());
                statement.setInt(9, user_id);

                statement.execute();
                return "Комната " + room.getName() + " успешно добавлена.";
            } catch (SQLException | NoSuchAlgorithmException| UnsupportedEncodingException e) {
                e.printStackTrace();
                return "Возникла внутренняя ошибка сервера.";
            }
        }
    }

    static String removeRoom(Room room, Connection connection, Integer user_id, String login, String password) {
        if (connection == null) {
            return "Сервер не подключён к базе данных";
        }
        if (user_id == -1 || login == null || password == null) return "Вы не авторизованы. Чтобы войти, введите login.";
        else {
            try {

                PreparedStatement statement = connection.prepareStatement(
                        "select * from " + "users where id = ? and email = ? and password_hash = ?"
                );
                statement.setInt(1, user_id);
                statement.setString(2, login);
                statement.setBytes(3, hashPassword(password + PASSWORD_SALT));

                ResultSet resultSet = statement.executeQuery();
                if (!resultSet.next()) return "Вы не авторизованы. Чтобы войти, введите login.";

                statement = connection.prepareStatement(
                        "delete from rooms where " +
                                "name = ? and height = ? and width = ? and x = ? and y = ? and " +
                                "thingcount = ? and shelfname = ? and user_id = ?"
                );
                statement.setString(1, room.getName());
                statement.setInt(2, room.getHeight());
                statement.setInt(3, room.getWidth());
                statement.setInt(4, room.getX());
                statement.setInt(5, room.getY());
                statement.setInt(6, room.getShelf().getThingcount());
                statement.setString(7, room.getShelf().getName());
                statement.setInt(8, user_id);

                int removed = statement.executeUpdate();
                return "Удалено " + removed + " комнат.";

            } catch (SQLException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
                return "Возникла внутренняя ошибка сервера.";
            }
        }
    }

    static String removeGreaterThanRoom(Room room, Connection connection, Integer user_id, String login, String password) {
        if (connection == null) {
            return "Сервер не подключён к базе данных";
        }
        if (user_id == -1 || login == null || password == null) return "Вы не авторизованы. Чтобы войти, введите login.";
        else {
            try {
                PreparedStatement statement = connection.prepareStatement(
                        "select count(*) from " + "users where id = ? and email = ? and password_hash = ?"
                );
                statement.setInt(1, user_id);
                statement.setString(2, login);
                statement.setBytes(3, hashPassword(password + PASSWORD_SALT));

                ResultSet resultSet = statement.executeQuery();
                if (!resultSet.next()) return "Вы не авторизованы. Чтобы войти, введите login.";
                statement = connection.prepareStatement(
                        "delete from rooms where user_id = ? and name > ?"
                );
                statement.setInt(1, user_id);
                statement.setString(2, room.getName());

                int removed = statement.executeUpdate();
                statement.execute();

                return "Удалено " + removed + " комнат.";

            } catch (SQLException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
                return "Возникла внутренняя ошибка сервера.";
            }
        }
    }


    static String removeLowerThanRoom(Room room, Connection connection, Integer user_id, String login, String password) {
        if (connection == null) {
            return "Сервер не подключён к базе данных";
        }
        if (user_id == -1 || login == null || password == null) return "Вы не авторизованы. Чтобы войти, введите login.";
        else {
            try {
                PreparedStatement statement = connection.prepareStatement(
                        "select count(*) from " + "users where id = ? and email = ? and password_hash = ?"
                );
                statement.setInt(1, user_id);
                statement.setString(2, login);
                statement.setBytes(3, hashPassword(password + PASSWORD_SALT));

                ResultSet resultSet = statement.executeQuery();
                if (!resultSet.next()) return "Вы не авторизованы. Чтобы войти, введите login.";

                statement = connection.prepareStatement(
                        "delete from rooms where user_id = ? and name < ?"
                );
                statement.setInt(1, user_id);
                statement.setString(2, room.getName());

                int removed = statement.executeUpdate();
                statement.execute();

                return "Удалено " + removed + " комнат.";

            } catch (SQLException | NoSuchAlgorithmException | UnsupportedEncodingException e){
                return "Возникла внутренняя ошибка сервера.";
            }
        }
    }


    static String makeShow(Connection connection){
        try {
            Statement statement = connection.createStatement();
            ResultSet roomsResultSet = statement.executeQuery("select * from rooms");
            StringBuilder builder = new StringBuilder();
            if (!roomsResultSet.next()) return ("Коллекция пуста.");
            do {
                Room room = new Room(roomsResultSet.getInt("width"),
                        roomsResultSet.getInt("height"),
                        roomsResultSet.getInt("x"),
                        roomsResultSet.getInt("y"),
                        roomsResultSet.getString("name"),
                        new Room.Shelf(roomsResultSet.getInt("thingcount"), roomsResultSet.getString("shelfname")));
                builder.append(room.toString() + "\n");
            } while (roomsResultSet.next());
            return builder.toString();
        } catch (SQLException e) {
            return "Произошла ошибка SQL.";
        }

    }

    /**
     * @return Информация о сервере в читабельном виде
     */
    static String getInfo(Connection connection) {
        if (connection == null)
            return ("Сервер не подключён к базе данных");
        try {
            ResultSet roomsResult = connection.createStatement().executeQuery("select count(*) from " + "rooms");
            ResultSet usersResult = connection.createStatement().executeQuery("select count(*) from " + "users");
            roomsResult.next();
            usersResult.next();

            int rooms = roomsResult.getInt(1);
            int users = usersResult.getInt(1);

            return "Зарегистрировано пользователей: " + users + "\n" +
                    "Хранится комнат: " + rooms;
        } catch (SQLException e) {
            System.err.println("Во время выдачи информации произошла ошибка SQL: " + e.toString());
            return ("Возникла внутренняя ошибка сервера.");
        }
    }

    /**
     * справка по командам, реализуемым приложением
     * @return справка по командам, реализуемым приложением
     */
    static String getHelp() {
        return colorize("[[RED]]Оу, похоже, вам нужна помощь?" +
                "\nПриложение поддерживает выполнение следующих команд:" +
                "[[YELLOW]]\n\t• add {element}: добавить новый элемент в коллекцию; пример комнаты, которую можно добавить:" +
                "{\"x\": 10, \"y\": 12, \"width\": 5, \"name\": \"хрущевка\", \"height\": 10," +
                " \n\t\"shelf\": { \"size\": 1, \"name\": \"flowers\" } } [[YELLOW]]" +
                "[[BRIGHT_YELLOW]]\n\t• remove_lower: удалить из коллекции все элементы, меньшие, чем заданный;" +
                "\n\t• remove_greater {element}: удалить из коллекции все элементы, превышающие заданный;[[BRIGHT_YELLOW]]" +
                "[[BRIGHT_GREEN]]\n\t• show: вывести в стандартный поток вывода все элементы коллекции в строковом представлении;" +
                "\n\t• info: вывести в стандартный поток вывода информацию о коллекции;[[BRIGHT_GREEN]]" +
                "[[CYAN]]\n\t• load: перечитать коллекцию из файла;" +
                "\n\t• remove {element}: удалить элемент из коллекции по его значению;[[CYAN]]" +
                "[[BLUE]]\n\t• import: добавить данные из файла клиента в коллекцию;" +
                "\n\t• register: зарегистрировать пользователя;[[BLUE]]" +
                "[[PURPLE]]\n\t• login: войти в аккаунт;" +
                "\n\t• logout: выйти из аккаунта;" +
                "[[RESET]]\n\t• help: вызов справки.[[RESET]]");
    }

}
