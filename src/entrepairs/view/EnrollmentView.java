package entrepairs.view;

import java.util.List;

import entrepairs.model.Course;
import entrepairs.model.CourseStatus;
import entrepairs.model.CourseUser;
import entrepairs.model.User;
import entrepairs.util.DateFormats;

public class EnrollmentView {

    public static class EnrolledCourseItem {
        private final Course course;
        private final CourseUser enrollment;
        private final String authorName;

        public EnrolledCourseItem(Course course, CourseUser enrollment, String authorName) {
            this.course = course;
            this.enrollment = enrollment;
            this.authorName = authorName;
        }

        public Course getCourse() {
            return course;
        }

        public CourseUser getEnrollment() {
            return enrollment;
        }

        public String getAuthorName() {
            return authorName;
        }
    }

    public static class CourseUserItem {
        private final User user;
        private final CourseUser enrollment;

        public CourseUserItem(User user, CourseUser enrollment) {
            this.user = user;
            this.enrollment = enrollment;
        }

        public User getUser() {
            return user;
        }

        public CourseUser getEnrollment() {
            return enrollment;
        }
    }

    private final ConsoleSupport console;

    public EnrollmentView(ConsoleSupport console) {
        this.console = console;
    }

    public String showEnrollmentMenu(List<EnrolledCourseItem> enrollments) {
        console.showHeader("> Início > Minhas inscrições");
        console.println("INSCRIÇÕES");
        if (enrollments.isEmpty()) {
            console.println("Nenhuma inscrição cadastrada.");
        } else {
            for (int index = 0; index < enrollments.size(); index++) {
                Course course = enrollments.get(index).getCourse();
                console.println("(" + (index + 1) + ") " + course.getName() + " - " + DateFormats.format(course.getStartDate()) + statusSuffix(course.getStatus()));
            }
        }
        console.println("");
        console.println("(A) Buscar curso por código");
        console.println("(B) Buscar curso por palavras-chave");
        console.println("(C) Listar todos os cursos");
        console.println("");
        console.println("(R) Retornar ao menu anterior");
        console.println("");
        return console.prompt("Opção: ").toUpperCase();
    }

    public String readShareCode() {
        console.showHeader("> Início > Minhas inscrições > Buscar curso por código");
        return console.prompt("Código do curso: ");
    }

    public String showCoursePage(List<Course> courses, int page, int totalPages) {
        console.showHeader("> Início > Minhas inscrições > Lista de cursos");
        console.println("Página " + page + " de " + totalPages);
        console.println("");
        if (courses.isEmpty()) {
            console.println("Nenhum curso encontrado.");
        } else {
            for (int index = 0; index < courses.size(); index++) {
                int optionNumber = index == 9 ? 0 : index + 1;
                Course course = courses.get(index);
                console.println("(" + optionNumber + ") " + course.getName() + " - " + DateFormats.format(course.getStartDate()) + statusSuffix(course.getStatus()));
            }
        }
        console.println("");
        console.println("(A) Página anterior");
        console.println("(B) Próxima página");
        console.println("");
        console.println("(R) Retornar ao menu anterior");
        console.println("");
        return console.prompt("Opção: ").toUpperCase();
    }

    public String showCourseDetails(Course course, String authorName, boolean canEnroll, boolean alreadyEnrolled) {
        console.showHeader("> Início > Minhas inscrições > Lista de cursos > " + course.getName());
        printCourseDetails(course, authorName);
        console.println("");
        if (alreadyEnrolled) {
            console.println("Você já está inscrito neste curso.");
        } else if (!canEnroll) {
            console.println("Este curso não está disponível para novas inscrições.");
        } else {
            console.println("(A) Fazer minha inscrição no curso");
        }
        console.println("");
        console.println("(R) Retornar ao menu anterior");
        console.println("");
        return console.prompt("Opção: ").toUpperCase();
    }

    public String showMyEnrollmentDetails(EnrolledCourseItem item) {
        Course course = item.getCourse();
        console.showHeader("> Início > Minhas inscrições > " + course.getName());
        printCourseDetails(course, item.getAuthorName());
        console.println("DATA INSCRIÇÃO: " + DateFormats.format(item.getEnrollment().getEnrollmentDate()));
        console.println("");
        console.println("(A) Cancelar minha inscrição no curso");
        console.println("");
        console.println("(R) Retornar ao menu anterior");
        console.println("");
        return console.prompt("Opção: ").toUpperCase();
    }

    public String showCourseEnrollmentList(Course course, List<CourseUserItem> enrollments) {
        console.showHeader("> Início > Meus cursos > " + course.getName() + " > Inscrições");
        if (enrollments.isEmpty()) {
            console.println("Nenhum usuário inscrito neste curso.");
        } else {
            for (int index = 0; index < enrollments.size(); index++) {
                CourseUserItem item = enrollments.get(index);
                console.println("(" + (index + 1) + ") " + item.getUser().getName() + " (" + DateFormats.format(item.getEnrollment().getEnrollmentDate()) + ")");
            }
        }
        console.println("");
        console.println("(A) Exportar lista");
        console.println("");
        console.println("(R) Retornar ao menu anterior");
        console.println("");
        return console.prompt("Opção: ").toUpperCase();
    }

    public String showCourseUserDetails(Course course, CourseUserItem item) {
        console.showHeader("> Início > Meus cursos > " + course.getName() + " > Inscrições > " + item.getUser().getName());
        console.println("NOME..........: " + item.getUser().getName());
        console.println("EMAIL.........: " + item.getUser().getEmail());
        console.println("DATA INSCRIÇÃO: " + DateFormats.format(item.getEnrollment().getEnrollmentDate()));
        console.println("");
        console.println("(A) Cancelar inscrição deste usuário");
        console.println("");
        console.println("(R) Retornar ao menu anterior");
        console.println("");
        return console.prompt("Opção: ").toUpperCase();
    }

    public boolean confirmEnrollmentCancel() {
        String option = console.prompt("Confirma o cancelamento da inscrição? (S/N): ").toUpperCase();
        return "S".equals(option);
    }

    public void showMessage(String message) {
        console.println("");
        console.println(message);
        console.println("");
        console.pause();
    }

    private void printCourseDetails(Course course, String authorName) {
        console.println("CÓDIGO........: " + course.getShareCode());
        console.println("CURSO.........: " + course.getName());
        console.println("AUTOR.........: " + authorName);
        console.println("DESCRIÇÃO.....: " + course.getDescription());
        console.println("DATA DE INÍCIO: " + DateFormats.format(course.getStartDate()));
        console.println("ESTADO........: " + describeStatus(course.getStatus()));
    }

    private String statusSuffix(CourseStatus status) {
        if (status == CourseStatus.ENROLLMENT_CLOSED) {
            return " (INSCRIÇÕES ENCERRADAS)";
        }
        if (status == CourseStatus.COMPLETED) {
            return " (CURSO REALIZADO)";
        }
        if (status == CourseStatus.CANCELED) {
            return " (CURSO CANCELADO)";
        }
        return "";
    }

    private String describeStatus(CourseStatus status) {
        if (status == CourseStatus.OPEN_FOR_ENROLLMENT) {
            return "Aberto para inscrições";
        }
        if (status == CourseStatus.ENROLLMENT_CLOSED) {
            return "Inscrições encerradas";
        }
        if (status == CourseStatus.COMPLETED) {
            return "Curso realizado";
        }
        return "Curso cancelado";
    }
}
