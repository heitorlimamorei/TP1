package entrepairs.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConsoleSupport {

    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

    public void showHeader(String breadcrumb) {
        println("EntrePares 1.0");
        println("--------------");
        if (breadcrumb != null && !breadcrumb.isBlank()) {
            println(breadcrumb);
            println("");
        }
    }

    public void println(String value) {
        System.out.println(value);
    }

    public void print(String value) {
        System.out.print(value);
    }

    public String prompt(String label) {
        print(label);
        try {
            String value = reader.readLine();
            return value == null ? "" : value.trim();
        } catch (IOException exception) {
            throw new IllegalStateException("Input could not be read.", exception);
        }
    }

    public void pause() {
        prompt("Pressione ENTER para continuar...");
    }
}
