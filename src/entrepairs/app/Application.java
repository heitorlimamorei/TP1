package entrepairs.app;

import entrepairs.controller.ApplicationController;
import entrepairs.controller.AuthController;
import entrepairs.controller.CourseController;
import entrepairs.controller.UserController;
import entrepairs.repository.adapter.Aed3CourseRepository;
import entrepairs.repository.adapter.Aed3UserRepository;
import entrepairs.service.AuthService;
import entrepairs.service.CourseService;
import entrepairs.service.NanoIdShareCodeGenerator;
import entrepairs.service.PasswordHasher;
import entrepairs.service.Sha256PasswordHasher;
import entrepairs.service.UserService;
import entrepairs.view.AuthenticationView;
import entrepairs.view.ConsoleSupport;
import entrepairs.view.CourseView;
import entrepairs.view.HomeView;
import entrepairs.view.UserView;

public class Application {

    public static void main(String[] args) {
        try (
            Aed3UserRepository userRepository = new Aed3UserRepository();
            Aed3CourseRepository courseRepository = new Aed3CourseRepository()
        ) {
            PasswordHasher passwordHasher = new Sha256PasswordHasher();

            AuthService authService = new AuthService(userRepository, passwordHasher);
            UserService userService = new UserService(userRepository, courseRepository, passwordHasher);
            CourseService courseService = new CourseService(courseRepository, new NanoIdShareCodeGenerator());

            ConsoleSupport console = new ConsoleSupport();
            AuthenticationView authenticationView = new AuthenticationView(console);
            HomeView homeView = new HomeView(console);
            UserView userView = new UserView(console);
            CourseView courseView = new CourseView(console);

            AuthController authController = new AuthController(authService, authenticationView);
            UserController userController = new UserController(userService, userView);
            CourseController courseController = new CourseController(courseService, courseView);

            ApplicationController applicationController = new ApplicationController(
                authController,
                userController,
                courseController,
                homeView
            );

            applicationController.run();
        } catch (Exception exception) {
            System.err.println("Erro fatal ao iniciar o sistema: " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}
