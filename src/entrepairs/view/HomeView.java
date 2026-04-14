package entrepairs.view;

import entrepairs.model.User;

public class HomeView {

    private final ConsoleSupport console;

    public HomeView(ConsoleSupport console) {
        this.console = console;
    }

    public String showMainMenu(User user) {
        console.showHeader("> Início");
        console.println("Usuário ativo: " + user.getName() + " <" + user.getEmail() + ">");
        console.println("");
        console.println("(A) Meus dados");
        console.println("(B) Meus cursos");
        console.println("(C) Minhas inscrições");
        console.println("(L) Logout");
        console.println("");
        console.println("(S) Sair");
        console.println("");
        return console.prompt("Opção: ").toUpperCase();
    }

    public void showEnrollmentsPlaceholder() {
        console.showHeader("> Início > Minhas inscrições");
        console.println("A funcionalidade de inscrições será implementada no TP2.");
        console.println("");
        console.pause();
    }

    public void showMessage(String message) {
        console.println("");
        console.println(message);
        console.println("");
        console.pause();
    }
}
