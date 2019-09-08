package ifmo.programming.lab7.client;


import ifmo.programming.lab7.Utils.StringEntity;
import ifmo.programming.lab7.transmitter.Receiver;
import ifmo.programming.lab7.transmitter.SenderAdapter;
import ifmo.programming.lab7.Message;
import ifmo.programming.lab7.Utilities;
import ifmo.programming.lab7.json.JSONParseException;
import ifmo.programming.lab7.server.FileLoader;
import ifmo.programming.lab7.transmitter.ReceiverListener;
import ifmo.programming.lab7.transmitter.Sender;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.file.AccessDeniedException;

import static ifmo.programming.lab7.Utilities.*;

public class CommandReader {
    private int id = -1;
    private String login, password;

    private static final int SENDING_PORT = 6666;
    private int port;
    private Receiver receiver;

    CommandReader (int port, Receiver receiver) {
        this.port = port;
        this.receiver = receiver;
    }

    // TODO: Нормально назвать функцию
    void IMMA_CHARGIN_MAH_LAZER() {
        System.out.print("Введите команду >>> ");
        String command;
        try {
            while ((command = getNextCommand()) != null) {
                command = command.replace("\\s{2,}", " ").trim();
                if (command.isEmpty()) {
                    System.out.println("Введите команду.");
                }

                int spaceIndex = command.indexOf(" ");
                if (spaceIndex == -1) {
                    processCommand(command, null);
                } else {
                    String name = command.substring(0, spaceIndex);
                    String arg = command.substring(spaceIndex + 1);
                    processCommand(name, arg);
                }

                System.out.print("Введите команду >>> ");
            }
        }catch (IOException e) {
            System.out.println("Ошибка ввода.");
        }

    }

    private void processCommand(String name, String arg) {

        switch (name){

            case "exit": System.exit(0);
            case "import":
                doImport(name, arg);
                return;

            case "save":
            case "load":
                doWithFilenameArgument(name, arg);
                return;

            case "add":
            case "remove":
            case "remove_greater":
            case "remove_lower":
                doWithRoomArgument(name, arg);
                return;

            case "register":
                doRegister();
                return;

            case "login":
                if (id != -1) {
                    System.out.println("Выйдите из аккаунта. Для этого следуетт ввести команду logout.");
                    return;
                }
                doLogin();
                return;

            case "logout":
                if (id == -1) System.out.println("Вы и не заходили.");
                else {
                    login = null;
                    password = null;
                    System.out.println("Вы вышли.");
                    id = -1;
                }
                return;

            case "help":
            case "info":
            case "show":
            default:
                doWithRoomArgument(name, null);
        }


    }

    /**
     * формирует команду import и передает ее на сериализацию
     * @param name имя команды
     * @param filename имя файла, из которого достается содержимое
     */
    private void doImport(String name, String filename){
        try {
            String content = FileLoader.getFileContent(filename);
            Message message = new Message(name, new StringEntity().set(content));
            message.setUserid(id);
            message.setLogin(login);
            message.setPassword(password);
            send(message);
        }
        catch (AccessDeniedException e) { System.out.println("Нет доступа к файлу"); }
        catch (FileNotFoundException e) { System.out.println("Ошибка: файл не найден."); }
        catch (IOException e) { System.out.println("Ошибка ввода-вывода: " + e.getMessage()); }
        catch (Exception e) { System.out.println(e.getMessage()); }

    }

    /**
     * Выполняет команду, аргумент которой
     * является json-представлением экземпляра класса Room
     * @param name имя команжы
     * @param arg аргумент команды
     */
    private void doWithRoomArgument(String name, String arg) {
            Message message = new Message(name, null);
            if (arg != null)
                try {
                    message.setAttachment(RoomFactory.makeRoomFromJSON(arg));
                    message.setUserid(id);
                    message.setLogin(login);
                    message.setPassword(password);
                } catch (JSONParseException ignored) {}
            send(message);
    }

    /**
     * Выполняет команду, аргументом которой является название файла
     * @param name имя команды
     * @param filename аргумент команды - название файла
     */
    private void doWithFilenameArgument(String name, String filename) {
       /* try {*/
        Message message = new Message(name, new StringEntity().set(filename));
        message.setUserid(id);
        message.setLogin(login);
        message.setPassword(password);
        send(message);
       /* } catch (IOException e) { e.getMessage(); }*/
    }

    private void doRegister() {
        try {
            System.out.println(colorize("[[BLUE]]Регистрация нового пользователя[[RESET]]"));
            System.out.println("Введите 'cancel', чтобы выйти");

            String name, email;

            while ((name = AuthorizeHelper.enter("Введите имя: ")).length() < 2 && !name.equals("cancel"))
                System.out.println("Ваше имя не должно быть короче двух символов");
            if (name.equals("cancel")) {
                System.out.println("Регистрация отменена.");
                IMMA_CHARGIN_MAH_LAZER();
            }

            while ((email = AuthorizeHelper.enter("Введите e-mail: ")).length() == 0 || !Utilities.isValidEmailAddress(email) && !email.equals("cancel"))
                System.out.println("Введите корректный e-mail.");
            if (email.equals("cancel")) {
                System.out.println("Регистрация отменена.");
                IMMA_CHARGIN_MAH_LAZER();
            }

            Message message = new Message("register", new StringEntity().set(new String[] {name, email}));
            send(message);

        } catch (IOException e) {
            System.out.println("Ошибка ввода-вывода " + e.getMessage());
        }
    }

    private void doLogin() {
        try {
            System.out.println("Вход в аккаунт");
            System.out.println("Введите 'cancel', чтобы выйти");
            String email, password;

            email = AuthorizeHelper.enter("Email: ");
            if (email.equals("cancel")){
                System.out.println("Вход отменён");
                IMMA_CHARGIN_MAH_LAZER();
            }
            password = AuthorizeHelper.enter("Пароль: ");
            if (password.equals("cancel")) {
                System.out.println("Вход отменён");
                IMMA_CHARGIN_MAH_LAZER();
            }

            Message message = new Message("login", new StringEntity().set(new String[] {email, password}));
            send(message);
        } catch (IOException e) {
            System.out.println("Ошибка ввода/вывода " + e.getMessage());
        }

    }



    private void send(Message message){
        message.setSourcePort(port);
        try {
            Sender.send(message.serialize(), InetAddress.getByName("localhost"), SENDING_PORT, true, new SenderAdapter() {
                @Override
                public void onSuccess() {
                    System.out.println("Команда отправилась, жду ответ...");
                    waitForServerResponse();
                }

                @Override
                public void onError(String message) {
                    System.out.println("Не получилось отправить запрос: " + message);
                    IMMA_CHARGIN_MAH_LAZER();
                }
            });
        } catch (IOException e) {
            System.out.println("Не получилось сформировать запрос: " + e.getMessage());
        }
    }

    private void waitForServerResponse() {
        Thread outer = Thread.currentThread();
        receiver.setListener(new ReceiverListener() {
            @Override
            public void received(int requestID, byte[] data, InetAddress address) {
                try {
                    Message message = Message.deserialize(data);
                    System.out.println("Вот что ответил сервер: " + message.getText());
                    if (message.hasAttachment()) {
                        String[] attachment = (String[])message.getAttachment();
                        id = Integer.parseInt(attachment[0]);
                        login = attachment[1];
                        password = attachment[2];
                    }

                    try {
                        Thread.sleep(50);
                        outer.interrupt();
                    } catch (InterruptedException ignored) {
                        IMMA_CHARGIN_MAH_LAZER();
                    }

                } catch (IOException | ClassNotFoundException e) {}
            }

            @Override
            public void exceptionThrown(Exception e) {
                e.printStackTrace();
                System.out.println("Не удалось получить ответ сервера: " + e.toString());
                outer.interrupt();
            }
        });

        try {
            Thread.sleep(3000);
            System.out.println("Сервер ничего не ответил");
        } catch (InterruptedException ignored) {
        } finally {
            IMMA_CHARGIN_MAH_LAZER();
        }
    }


    /**
     * читает команду со стандартного потока ввода
     * @return преобразованная в строку многострочная команда
     * @throws IOException когда что-то идёт не так
     */
    private static String getNextCommand() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder builder = new StringBuilder();
        char running;
        boolean inString = false;
        do {
            int current = reader.read();
            if (current == -1) {
                return null;
            }
            running = (char)current;

            if (running != ';' || inString) {  builder.append(running); }
            if (running == '"') { inString = !inString;}
        } while (running != ';' || inString);
        return builder.toString();
    }
}
