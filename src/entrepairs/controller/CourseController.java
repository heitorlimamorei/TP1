package entrepairs.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import entrepairs.model.Course;
import entrepairs.model.CourseStatus;
import entrepairs.model.User;
import entrepairs.service.CourseService;
import entrepairs.service.CourseService.CancelCourseResult;
import entrepairs.util.DateFormats;
import entrepairs.view.CourseView;
import entrepairs.view.CourseView.CourseForm;

public class CourseController {

    private final CourseService courseService;
    private final CourseView view;

    public CourseController(CourseService courseService, CourseView view) {
        this.courseService = courseService;
        this.view = view;
    }

    public void manage(User activeUser) {
        while (true) {
            try {
                List<Course> courses = courseService.listCourses(activeUser);
                String option = view.showCourseList(courses);
                if ("A".equals(option)) {
                    createCourse(activeUser);
                } else if ("R".equals(option)) {
                    return;
                } else {
                    int selectedIndex = parseCourseSelection(option, courses.size());
                    if (selectedIndex < 0) {
                        view.showMessage("Opção inválida.");
                    } else {
                        showCourseDetails(courses.get(selectedIndex));
                    }
                }
            } catch (Exception exception) {
                view.showMessage("Erro: " + exception.getMessage());
            }
        }
    }

    private void createCourse(User activeUser) throws Exception {
        CourseForm form = view.readNewCourse();
        LocalDate startDate = DateFormats.parse(form.getStartDate());
        courseService.createCourse(activeUser, form.getName(), startDate, form.getDescription());
        view.showMessage("Curso cadastrado com sucesso.");
    }

    private void showCourseDetails(Course course) throws Exception {
        Course currentCourse = refreshCourse(course.getId());
        while (currentCourse != null) {
            String option = view.showCourseDetails(currentCourse);
            if ("A".equals(option)) {
                view.showMessage("O gerenciamento de inscritos será implementado no TP2.");
            } else if ("B".equals(option)) {
                currentCourse = updateCourse(currentCourse);
            } else if ("C".equals(option)) {
                currentCourse = closeEnrollments(currentCourse);
            } else if ("D".equals(option)) {
                currentCourse = concludeCourse(currentCourse);
            } else if ("E".equals(option)) {
                currentCourse = cancelCourse(currentCourse);
            } else if ("R".equals(option)) {
                return;
            } else {
                view.showMessage("Opção inválida.");
            }
        }
    }

    private Course updateCourse(Course currentCourse) throws Exception {
        if (currentCourse.getStatus() == CourseStatus.COMPLETED || currentCourse.getStatus() == CourseStatus.CANCELED) {
            view.showMessage("Cursos concluídos ou cancelados não podem ser editados.");
            return currentCourse;
        }
        CourseForm form = view.readCourseUpdate(currentCourse);
        LocalDate startDate = DateFormats.parse(form.getStartDate());
        Course updatedCourse = courseService.updateCourse(currentCourse, form.getName(), startDate, form.getDescription());
        view.showMessage("Curso atualizado com sucesso.");
        return updatedCourse;
    }

    private Course closeEnrollments(Course currentCourse) throws Exception {
        if (currentCourse.getStatus() != CourseStatus.OPEN_FOR_ENROLLMENT) {
            view.showMessage("A operação só é permitida para cursos abertos para inscrições.");
            return currentCourse;
        }
        Course updatedCourse = courseService.closeEnrollments(currentCourse);
        view.showMessage("Inscrições encerradas.");
        return updatedCourse;
    }

    private Course concludeCourse(Course currentCourse) throws Exception {
        if (currentCourse.getStatus() == CourseStatus.COMPLETED) {
            view.showMessage("O curso já está concluído.");
            return currentCourse;
        }
        if (currentCourse.getStatus() == CourseStatus.CANCELED) {
            view.showMessage("Cursos cancelados não podem ser concluídos.");
            return currentCourse;
        }
        Course updatedCourse = courseService.concludeCourse(currentCourse);
        view.showMessage("Curso concluído.");
        return updatedCourse;
    }

    private Course cancelCourse(Course currentCourse) throws Exception {
        if (!view.confirmCancel()) {
            return currentCourse;
        }
        CancelCourseResult result = courseService.cancelCourse(currentCourse);
        if (result == CancelCourseResult.DELETED) {
            view.showMessage("Curso cancelado e removido do sistema.");
            return null;
        }
        view.showMessage("O curso não foi encontrado.");
        return null;
    }

    private Course refreshCourse(int courseId) throws Exception {
        Optional<Course> course = courseService.findById(courseId);
        return course.orElse(null);
    }

    private int parseCourseSelection(String option, int courseCount) {
        try {
            int selected = Integer.parseInt(option);
            if (selected < 1 || selected > courseCount) {
                return -1;
            }
            return selected - 1;
        } catch (NumberFormatException exception) {
            return -1;
        }
    }
}
