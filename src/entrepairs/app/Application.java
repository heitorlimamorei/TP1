package entrepairs.app;

import entrepairs.controller.ApplicationController;
import entrepairs.controller.AuthController;
import entrepairs.controller.CourseController;
import entrepairs.controller.EnrollmentController;
import entrepairs.controller.UserController;
import entrepairs.repository.adapter.Aed3CourseRepository;
import entrepairs.repository.adapter.Aed3CourseUserRepository;
import entrepairs.repository.adapter.Aed3UserRepository;
import entrepairs.service.AuthService;
import entrepairs.service.CourseService;
import entrepairs.service.EnrollmentService;
import entrepairs.service.NanoIdShareCodeGenerator;
import entrepairs.service.PasswordHasher;
import entrepairs.service.Sha256PasswordHasher;
import entrepairs.service.UserService;
import entrepairs.view.AuthenticationView;
import entrepairs.view.ConsoleSupport;
import entrepairs.view.CourseView;
import entrepairs.view.EnrollmentView;
import entrepairs.view.HomeView;
import entrepairs.view.UserView;

public class Application {

    public static void main(String[] args) {
        try (
            Aed3UserRepository userRepository = new Aed3UserRepository();
            Aed3CourseRepository courseRepository = new Aed3CourseRepository();
            Aed3CourseUserRepository courseUserRepository = new Aed3CourseUserRepository()
        ) {
            PasswordHasher passwordHasher = new Sha256PasswordHasher();

            AuthService authService = new AuthService(userRepository, passwordHasher);
            UserService userService = new UserService(userRepository, courseRepository, courseUserRepository, passwordHasher);
            CourseService courseService = new CourseService(courseRepository, new NanoIdShareCodeGenerator());
            EnrollmentService enrollmentService = new EnrollmentService(courseUserRepository, courseRepository, userRepository);

            ConsoleSupport console = new ConsoleSupport();
            AuthenticationView authenticationView = new AuthenticationView(console);
            HomeView homeView = new HomeView(console);
            UserView userView = new UserView(console);
            CourseView courseView = new CourseView(console);
            EnrollmentView enrollmentView = new EnrollmentView(console);

            AuthController authController = new AuthController(authService, authenticationView);
            UserController userController = new UserController(userService, userView);
            EnrollmentController enrollmentController = new EnrollmentController(enrollmentService, courseService, userService, enrollmentView);
            CourseController courseController = new CourseController(courseService, enrollmentController, courseView);

            ApplicationController applicationController = new ApplicationController(
                authController,
                userController,
                courseController,
                enrollmentController,
                homeView
            );

            applicationController.run();
        } catch (Exception exception) {
            System.err.println("Erro fatal ao iniciar o sistema: " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}
