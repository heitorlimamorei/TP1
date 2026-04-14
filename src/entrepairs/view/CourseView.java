package entrepairs.view;

import java.util.List;

import entrepairs.model.Course;
import entrepairs.model.CourseStatus;
import entrepairs.util.DateFormats;

public class CourseView {

    public static class CourseForm {
        private final String name;
        private final String startDate;
        private final String description;

        public CourseForm(String name, String startDate, String description) {
            this.name = name;
            this.startDate = startDate;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getDescription() {
            return description;
        }
    }

    private final ConsoleSupport console;

    public CourseView(ConsoleSupport console) {
        this.console = console;
    }

    public String showCourseList(List<Course> courses) {
        console.showHeader("> Início > Meus cursos");
        console.println("CURSOS");
        if (courses.isEmpty()) {
            console.println("Nenhum curso cadastrado.");
        } else {
            for (int index = 0; index < courses.size(); index++) {
                Course course = courses.get(index);
                console.println("(" + (index + 1) + ") " + course.getName() + " - " + DateFormats.format(course.getStartDate()));
            }
        }
        console.println("");
        console.println("(A) Novo curso");
        console.println("(R) Retornar ao menu anterior");
        console.println("");
        return console.prompt("Opção: ").toUpperCase();
    }

    public CourseForm readNewCourse() {
        console.showHeader("> Início > Meus cursos > Novo curso");
        String name = console.prompt("Nome: ");
        String startDate = console.prompt("Data de início (dd/MM/yyyy): ");
        String description = console.prompt("Descrição detalhada: ");
        return new CourseForm(name, startDate, description);
    }

    public CourseForm readCourseUpdate(Course course) {
        console.showHeader("> Início > Meus cursos > " + course.getName() + " > Corrigir");
        String name = console.prompt("Nome [" + course.getName() + "]: ");
        String startDate = console.prompt("Data de início [" + DateFormats.format(course.getStartDate()) + "]: ");
        String description = console.prompt("Descrição detalhada (ENTER para manter): ");
        return new CourseForm(
            name.isBlank() ? course.getName() : name,
            startDate.isBlank() ? DateFormats.format(course.getStartDate()) : startDate,
            description.isBlank() ? course.getDescription() : description
        );
    }

    public String showCourseDetails(Course course) {
        console.showHeader("> Início > Meus cursos > " + course.getName());
        console.println("CÓDIGO........: " + course.getShareCode());
        console.println("NOME..........: " + course.getName());
        console.println("DESCRIÇÃO.....: " + course.getDescription());
        console.println("DATA DE INÍCIO: " + DateFormats.format(course.getStartDate()));
        console.println("ESTADO........: " + describeStatus(course.getStatus()));
        console.println("");
        console.println(course.getStatus().getDescription());
        console.println("");
        console.println("(A) Gerenciar inscritos no curso");
        console.println("(B) Corrigir dados do curso");
        console.println("(C) Encerrar inscrições");
        console.println("(D) Concluir curso");
        console.println("(E) Cancelar curso");
        console.println("");
        console.println("(R) Retornar ao menu anterior");
        console.println("");
        return console.prompt("Opção: ").toUpperCase();
    }

    public boolean confirmCancel() {
        String option = console.prompt("Confirma o cancelamento do curso? (S/N): ").toUpperCase();
        return "S".equals(option);
    }

    public void showMessage(String message) {
        console.println("");
        console.println(message);
        console.println("");
        console.pause();
    }

    private String describeStatus(CourseStatus status) {
        if (status == CourseStatus.OPEN_FOR_ENROLLMENT) {
            return "0 - Curso ativo e recebendo inscrições";
        }
        if (status == CourseStatus.ENROLLMENT_CLOSED) {
            return "1 - Curso ativo, mas sem novas inscrições";
        }
        if (status == CourseStatus.COMPLETED) {
            return "2 - Curso realizado e concluído";
        }
        return "3 - Curso cancelado";
    }
}
