package entrepairs.view;

public class AuthenticationView {

    public static class Credentials {
        private final String email;
        private final String password;

        public Credentials(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }
    }

    public static class RegistrationForm {
        private final String name;
        private final String email;
        private final String password;
        private final String secretQuestion;
        private final String secretAnswer;

        public RegistrationForm(String name, String email, String password, String secretQuestion, String secretAnswer) {
            this.name = name;
            this.email = email;
            this.password = password;
            this.secretQuestion = secretQuestion;
            this.secretAnswer = secretAnswer;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }

        public String getSecretQuestion() {
            return secretQuestion;
        }

        public String getSecretAnswer() {
            return secretAnswer;
        }
    }

    public static class PasswordRecoveryForm {
        private final String email;
        private final String secretAnswer;
        private final String newPassword;

        public PasswordRecoveryForm(String email, String secretAnswer, String newPassword) {
            this.email = email;
            this.secretAnswer = secretAnswer;
            this.newPassword = newPassword;
        }

        public String getEmail() {
            return email;
        }

        public String getSecretAnswer() {
            return secretAnswer;
        }

        public String getNewPassword() {
            return newPassword;
        }
    }

    private final ConsoleSupport console;

    public AuthenticationView(ConsoleSupport console) {
        this.console = console;
    }

    public String showInitialMenu() {
        console.showHeader(null);
        console.println("(A) Login");
        console.println("(B) Novo usuário");
        console.println("(C) Recuperar senha");
        console.println("");
        console.println("(S) Sair");
        console.println("");
        return console.prompt("Opção: ").toUpperCase();
    }

    public Credentials readCredentials() {
        console.showHeader("> Acesso > Login");
        String email = console.prompt("E-mail: ");
        String password = console.prompt("Senha: ");
        return new Credentials(email, password);
    }

    public RegistrationForm readRegistrationForm() {
        console.showHeader("> Acesso > Novo usuário");
        String name = console.prompt("Nome: ");
        String email = console.prompt("E-mail: ");
        String password = console.prompt("Senha: ");
        String secretQuestion = console.prompt("Pergunta secreta: ");
        String secretAnswer = console.prompt("Resposta secreta: ");
        return new RegistrationForm(name, email, password, secretQuestion, secretAnswer);
    }

    public PasswordRecoveryForm readPasswordRecoveryForm(String email, String secretQuestion) {
        console.showHeader("> Acesso > Recuperar senha");
        console.println("E-mail: " + email);
        console.println("Pergunta secreta: " + secretQuestion);
        String secretAnswer = console.prompt("Resposta secreta: ");
        String newPassword = console.prompt("Nova senha: ");
        return new PasswordRecoveryForm(email, secretAnswer, newPassword);
    }

    public String askEmailForRecovery() {
        console.showHeader("> Acesso > Recuperar senha");
        return console.prompt("E-mail: ");
    }

    public void showMessage(String message) {
        console.println("");
        console.println(message);
        console.println("");
        console.pause();
    }
}
