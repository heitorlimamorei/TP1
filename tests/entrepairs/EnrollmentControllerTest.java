package entrepairs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import entrepairs.controller.EnrollmentController;
import entrepairs.model.Course;
import entrepairs.model.CourseStatus;
import entrepairs.model.User;
import entrepairs.repository.adapter.Aed3CourseRepository;
import entrepairs.repository.adapter.Aed3CourseUserRepository;
import entrepairs.repository.adapter.Aed3UserRepository;
import entrepairs.service.CourseService;
import entrepairs.service.EnrollmentService;
import entrepairs.service.Sha256PasswordHasher;
import entrepairs.service.UserService;
import entrepairs.view.ConsoleSupport;
import entrepairs.view.EnrollmentView;

final class EnrollmentControllerTest {

    private EnrollmentControllerTest() {
    }

    static void run() throws Exception {
        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String input = "B\nArquitetura Distribuída\n1\nR\nB\nR\nR\n";

        try (
            Aed3UserRepository userRepository = new Aed3UserRepository();
            Aed3CourseRepository courseRepository = new Aed3CourseRepository();
            Aed3CourseUserRepository courseUserRepository = new Aed3CourseUserRepository()
        ) {
            User author = createUser(userRepository, "Autor", "autor@test.local");
            User activeUser = createUser(userRepository, "Aluno", "aluno@test.local");
            createCourse(courseRepository, author.getId(), "Arquitetura Distribuída", "arch000001");
            for (int index = 2; index <= 11; index++) {
                createCourse(
                    courseRepository,
                    author.getId(),
                    String.format("Arquitetura Distribuída %02d", index),
                    String.format("arch%06d", index)
                );
            }
            createCourse(courseRepository, activeUser.getId(), "Arquitetura Distribuída Avançada", "archown001");

            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8.name()));

            CourseService courseService = new CourseService(courseRepository, () -> "unused0001");
            EnrollmentService enrollmentService = new EnrollmentService(
                courseUserRepository,
                courseRepository,
                userRepository
            );
            UserService userService = new UserService(
                userRepository,
                courseRepository,
                courseUserRepository,
                new Sha256PasswordHasher()
            );
            EnrollmentController controller = new EnrollmentController(
                enrollmentService,
                courseService,
                userService,
                new EnrollmentView(new ConsoleSupport())
            );

            controller.manage(activeUser);
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
        }

        String screen = output.toString(StandardCharsets.UTF_8.name());
        TestAssertions.assertTrue(
            screen.contains("> Início > Minhas inscrições > Busca por palavras-chave"),
            "O fluxo deve abrir a tela de busca por palavras-chave."
        );
        TestAssertions.assertTrue(
            screen.contains("Arquitetura Distribuída -"),
            "O curso de outra pessoa deve aparecer no resultado."
        );
        TestAssertions.assertTrue(
            !screen.contains("Arquitetura Distribuída Avançada"),
            "O curso do usuário ativo não deve aparecer no resultado."
        );
        TestAssertions.assertTrue(screen.contains("relevância"), "A tela deve exibir a pontuação de relevância.");
        TestAssertions.assertTrue(
            screen.contains("CÓDIGO........: arch000001"),
            "A seleção do resultado deve abrir os detalhes do curso."
        );
        TestAssertions.assertTrue(screen.contains("Página 2 de 2"), "A paginação deve alcançar a segunda página.");
    }

    private static User createUser(Aed3UserRepository repository, String name, String email) throws Exception {
        User user = new User(0, name, email, "hash", "Pergunta?", "hash");
        repository.create(user);
        return user;
    }

    private static void createCourse(
        Aed3CourseRepository repository,
        int ownerId,
        String name,
        String shareCode
    ) throws Exception {
        repository.create(new Course(
            0,
            ownerId,
            name,
            LocalDate.of(2026, 8, 1),
            "Descrição de teste",
            shareCode,
            CourseStatus.OPEN_FOR_ENROLLMENT
        ));
    }
}
