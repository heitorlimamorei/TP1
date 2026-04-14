package entrepairs.controller;

import entrepairs.model.User;
import entrepairs.service.UserService;
import entrepairs.service.UserService.DeleteUserResult;
import entrepairs.view.UserView;
import entrepairs.view.UserView.PasswordChangeForm;
import entrepairs.view.UserView.ProfileForm;

public class UserController {

    private final UserService userService;
    private final UserView view;

    public UserController(UserService userService, UserView view) {
        this.userService = userService;
        this.view = view;
    }

    public User manage(User activeUser) {
        User currentUser = activeUser;
        while (true) {
            String option = view.showMenu(currentUser);
            try {
                if ("A".equals(option)) {
                    currentUser = updateProfile(currentUser);
                } else if ("B".equals(option)) {
                    changePassword(currentUser);
                } else if ("C".equals(option)) {
                    if (deleteUser(currentUser)) {
                        return null;
                    }
                } else if ("R".equals(option)) {
                    return currentUser;
                } else {
                    view.showMessage("Opção inválida.");
                }
            } catch (Exception exception) {
                view.showMessage("Erro: " + exception.getMessage());
            }
        }
    }

    private User updateProfile(User currentUser) throws Exception {
        ProfileForm form = view.readProfileForm(currentUser);
        User updatedUser = userService.updateProfile(
            currentUser,
            form.getName(),
            form.getEmail(),
            form.getSecretQuestion(),
            form.getSecretAnswer()
        );
        view.showMessage("Dados atualizados com sucesso.");
        return updatedUser;
    }

    private void changePassword(User currentUser) throws Exception {
        PasswordChangeForm form = view.readPasswordChangeForm();
        if (userService.changePassword(currentUser, form.getCurrentPassword(), form.getNewPassword())) {
            view.showMessage("Senha alterada com sucesso.");
            return;
        }
        view.showMessage("Senha atual inválida.");
    }

    private boolean deleteUser(User currentUser) throws Exception {
        if (!view.confirmDeletion()) {
            return false;
        }

        DeleteUserResult result = userService.deleteUser(currentUser);
        if (result == DeleteUserResult.ACTIVE_COURSES_FOUND) {
            view.showMessage("A conta não pode ser excluída enquanto houver cursos ativos vinculados a ela.");
            return false;
        }

        view.showMessage("Conta excluída com sucesso.");
        return true;
    }
}
