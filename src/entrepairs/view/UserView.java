package entrepairs.view;

import entrepairs.model.User;

public class UserView {

    public static class ProfileForm {
        private final String name;
        private final String email;
        private final String secretQuestion;
        private final String secretAnswer;

        public ProfileForm(String name, String email, String secretQuestion, String secretAnswer) {
            this.name = name;
            this.email = email;
            this.secretQuestion = secretQuestion;
            this.secretAnswer = secretAnswer;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getSecretQuestion() {
            return secretQuestion;
        }

        public String getSecretAnswer() {
            return secretAnswer;
        }
    }

    public static class PasswordChangeForm {
        private final String currentPassword;
        private final String newPassword;

        public PasswordChangeForm(String currentPassword, String newPassword) {
            this.currentPassword = currentPassword;
            this.newPassword = newPassword;
        }

        public String getCurrentPassword() {
            return currentPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }
    }

    private final ConsoleSupport console;

    public UserView(ConsoleSupport console) {
        this.console = console;
    }

    public String showMenu(User user) {
        console.showHeader("> Início > Meus dados");
        console.println("NOME.............: " + user.getName());
        console.println("E-MAIL...........: " + user.getEmail());
        console.println("PERGUNTA SECRETA.: " + user.getSecretQuestion());
        console.println("");
        console.println("(A) Corrigir meus dados");
        console.println("(B) Alterar senha");
        console.println("(C) Excluir minha conta");
        console.println("");
        console.println("(R) Retornar ao menu anterior");
        console.println("");
        return console.prompt("Opção: ").toUpperCase();
    }

    public ProfileForm readProfileForm(User user) {
        console.showHeader("> Início > Meus dados > Corrigir");
        String name = console.prompt("Nome [" + user.getName() + "]: ");
        String email = console.prompt("E-mail [" + user.getEmail() + "]: ");
        String secretQuestion = console.prompt("Pergunta secreta [" + user.getSecretQuestion() + "]: ");
        String secretAnswer = console.prompt("Nova resposta secreta (ENTER para manter): ");
        return new ProfileForm(
            name.isBlank() ? user.getName() : name,
            email.isBlank() ? user.getEmail() : email,
            secretQuestion.isBlank() ? user.getSecretQuestion() : secretQuestion,
            secretAnswer
        );
    }

    public PasswordChangeForm readPasswordChangeForm() {
        console.showHeader("> Início > Meus dados > Alterar senha");
        String currentPassword = console.prompt("Senha atual: ");
        String newPassword = console.prompt("Nova senha: ");
        return new PasswordChangeForm(currentPassword, newPassword);
    }

    public boolean confirmDeletion() {
        console.showHeader("> Início > Meus dados > Excluir conta");
        String option = console.prompt("Confirma a exclusão da sua conta? (S/N): ").toUpperCase();
        return "S".equals(option);
    }

    public void showMessage(String message) {
        console.println("");
        console.println(message);
        console.println("");
        console.pause();
    }
}
