package entrepairs.controller;

import java.util.Optional;

import entrepairs.model.User;
import entrepairs.service.AuthService;
import entrepairs.view.AuthenticationView;
import entrepairs.view.AuthenticationView.Credentials;
import entrepairs.view.AuthenticationView.PasswordRecoveryForm;
import entrepairs.view.AuthenticationView.RegistrationForm;

public class AuthController {

    private final AuthService authService;
    private final AuthenticationView view;

    public AuthController(AuthService authService, AuthenticationView view) {
        this.authService = authService;
        this.view = view;
    }

    public User startSession() {
        while (true) {
            String option = view.showInitialMenu();
            try {
                if ("A".equals(option)) {
                    User user = login();
                    if (user != null) {
                        return user;
                    }
                } else if ("B".equals(option)) {
                    return register();
                } else if ("C".equals(option)) {
                    recoverPassword();
                } else if ("S".equals(option)) {
                    return null;
                } else {
                    view.showMessage("Opção inválida.");
                }
            } catch (Exception exception) {
                view.showMessage("Erro: " + exception.getMessage());
            }
        }
    }

    private User login() throws Exception {
        Credentials credentials = view.readCredentials();
        Optional<User> authenticatedUser = authService.authenticate(credentials.getEmail(), credentials.getPassword());
        if (authenticatedUser.isEmpty()) {
            view.showMessage("E-mail ou senha inválidos.");
            return null;
        }
        return authenticatedUser.get();
    }

    private User register() throws Exception {
        RegistrationForm form = view.readRegistrationForm();
        User user = authService.register(
            form.getName(),
            form.getEmail(),
            form.getPassword(),
            form.getSecretQuestion(),
            form.getSecretAnswer()
        );
        view.showMessage("Usuário cadastrado com sucesso.");
        return user;
    }

    private void recoverPassword() throws Exception {
        String email = view.askEmailForRecovery();
        Optional<String> secretQuestion = authService.findSecretQuestion(email);
        if (secretQuestion.isEmpty()) {
            view.showMessage("Usuário não encontrado.");
            return;
        }
        PasswordRecoveryForm form = view.readPasswordRecoveryForm(email, secretQuestion.get());
        if (authService.resetPassword(form.getEmail(), form.getSecretAnswer(), form.getNewPassword())) {
            view.showMessage("Senha redefinida com sucesso.");
            return;
        }
        view.showMessage("Não foi possível redefinir a senha.");
    }
}
