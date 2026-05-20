package entrepairs.service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import entrepairs.model.Course;
import entrepairs.model.CourseStatus;
import entrepairs.model.CourseUser;
import entrepairs.model.User;
import entrepairs.repository.adapter.Aed3CourseRepository;
import entrepairs.repository.adapter.Aed3CourseUserRepository;
import entrepairs.repository.adapter.Aed3UserRepository;

public class EnrollmentService {

    private final Aed3CourseUserRepository courseUserRepository;
    private final Aed3CourseRepository courseRepository;
    private final Aed3UserRepository userRepository;

    public EnrollmentService(
        Aed3CourseUserRepository courseUserRepository,
        Aed3CourseRepository courseRepository,
        Aed3UserRepository userRepository
    ) {
        this.courseUserRepository = courseUserRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    public CourseUser enroll(User user, Course course) throws Exception {
        if (course.getOwnerUserId() == user.getId()) {
            throw new IllegalArgumentException("Você não pode se inscrever no próprio curso.");
        }
        if (course.getStatus() != CourseStatus.OPEN_FOR_ENROLLMENT) {
            throw new IllegalArgumentException("Este curso não está aberto para inscrições.");
        }
        if (findByUserAndCourse(user.getId(), course.getId()).isPresent()) {
            throw new IllegalArgumentException("Você já está inscrito neste curso.");
        }
        if (courseRepository.findById(course.getId()).isEmpty()) {
            throw new IllegalArgumentException("Curso não encontrado.");
        }
        if (userRepository.findById(user.getId()).isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }

        CourseUser courseUser = new CourseUser();
        courseUser.setCourseId(course.getId());
        courseUser.setUserId(user.getId());
        courseUser.setEnrollmentDate(LocalDate.now());
        courseUserRepository.create(courseUser);
        return courseUser;
    }

    public Optional<CourseUser> findByUserAndCourse(int userId, int courseId) throws Exception {
        for (CourseUser courseUser : courseUserRepository.findByUser(userId)) {
            if (courseUser.getCourseId() == courseId) {
                return Optional.of(courseUser);
            }
        }
        return Optional.empty();
    }

    public List<CourseUser> listByUser(User user) throws Exception {
        return courseUserRepository.findByUser(user.getId());
    }

    public List<CourseUser> listByCourse(Course course) throws Exception {
        return courseUserRepository.findByCourse(course.getId());
    }

    public boolean cancelEnrollment(CourseUser courseUser) throws Exception {
        return courseUserRepository.delete(courseUser.getId());
    }

    public void deleteByUser(User user) throws Exception {
        courseUserRepository.deleteByUser(user.getId());
    }

    public void deleteByCourse(Course course) throws Exception {
        courseUserRepository.deleteByCourse(course.getId());
    }

    public Path exportCourseEnrollments(Course course) throws Exception {
        Path exportDir = Path.of("data", "exports");
        Files.createDirectories(exportDir);

        Path csvPath = exportDir.resolve("inscritos-" + course.getShareCode() + ".csv");
        StringBuilder csv = new StringBuilder();
        csv.append("nome,email,data_inscricao").append(System.lineSeparator());

        for (CourseUser courseUser : courseUserRepository.findByCourse(course.getId())) {
            Optional<User> user = userRepository.findById(courseUser.getUserId());
            if (user.isPresent()) {
                csv.append(csv(user.get().getName())).append(",");
                csv.append(csv(user.get().getEmail())).append(",");
                csv.append(csv(courseUser.getEnrollmentDate().toString())).append(System.lineSeparator());
            }
        }

        Files.writeString(csvPath, csv.toString(), StandardCharsets.UTF_8);
        return csvPath;
    }

    private String csv(String value) {
        String escaped = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
