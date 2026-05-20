package entrepairs.controller;

import entrepairs.model.User;
import entrepairs.view.HomeView;

public class ApplicationController {

    private final AuthController authController;
    private final UserController userController;
    private final CourseController courseController;
    private final EnrollmentController enrollmentController;
    private final HomeView homeView;

    public ApplicationController(
        AuthController authController,
        UserController userController,
        CourseController courseController,
        EnrollmentController enrollmentController,
        HomeView homeView
    ) {
        this.authController = authController;
        this.userController = userController;
        this.courseController = courseController;
        this.enrollmentController = enrollmentController;
        this.homeView = homeView;
    }

    public void run() {
        while (true) {
            User activeUser = authController.startSession();
            if (activeUser == null) {
                return;
            }

            if (!handleMainMenu(activeUser)) {
                return;
            }
        }
    }

    private boolean handleMainMenu(User activeUser) {
        User currentUser = activeUser;
        while (true) {
            String option = homeView.showMainMenu(currentUser);
            if ("A".equals(option)) {
                currentUser = userController.manage(currentUser);
                if (currentUser == null) {
                    return true;
                }
            } else if ("B".equals(option)) {
                courseController.manage(currentUser);
            } else if ("C".equals(option)) {
                enrollmentController.manage(currentUser);
            } else if ("L".equals(option)) {
                return true;
            } else if ("S".equals(option)) {
                return false;
            } else {
                homeView.showMessage("Opção inválida.");
            }
        }
    }
}
