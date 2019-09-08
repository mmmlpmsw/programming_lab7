package ifmo.programming.lab7.client;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

public class AuthorizeHelper {

    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    static String enter(String message) throws IOException {
        System.out.print(message);
        return reader.readLine();
    }

    synchronized static String promptHidden(String message) throws IOException {
        Console console = System.console();
        if (console == null) {
            System.out.println("Не удается скрыть ввод.");
            return enter(message);
        }
        char[] responseArray = console.readPassword(message);
        return new String(responseArray);
    }
}
