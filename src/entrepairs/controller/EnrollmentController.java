package entrepairs.controller;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import entrepairs.model.Course;
import entrepairs.model.CourseSearchResult;
import entrepairs.model.CourseStatus;
import entrepairs.model.CourseUser;
import entrepairs.model.User;
import entrepairs.service.CourseService;
import entrepairs.service.EnrollmentService;
import entrepairs.service.UserService;
import entrepairs.view.EnrollmentView;
import entrepairs.view.EnrollmentView.CourseUserItem;
import entrepairs.view.EnrollmentView.EnrolledCourseItem;

public class EnrollmentController {

    private static final int PAGE_SIZE = 10;

    private final EnrollmentService enrollmentService;
    private final CourseService courseService;
    private final UserService userService;
    private final EnrollmentView view;

    public EnrollmentController(
        EnrollmentService enrollmentService,
        CourseService courseService,
        UserService userService,
        EnrollmentView view
    ) {
        this.enrollmentService = enrollmentService;
        this.courseService = courseService;
        this.userService = userService;
        this.view = view;
    }

    public void manage(User activeUser) {
        while (true) {
            try {
                List<EnrolledCourseItem> enrollments = buildUserEnrollmentItems(activeUser);
                String option = view.showEnrollmentMenu(enrollments);
                if ("A".equals(option)) {
                    searchCourseByCode(activeUser);
                } else if ("B".equals(option)) {
                    searchCoursesByName(activeUser);
                } else if ("C".equals(option)) {
                    listAllCourses(activeUser);
                } else if ("R".equals(option)) {
                    return;
                } else {
                    int selectedIndex = parseSelection(option, enrollments.size(), false);
                    if (selectedIndex < 0) {
                        view.showMessage("Opção inválida.");
                    } else {
                        showMyEnrollment(activeUser, enrollments.get(selectedIndex));
                    }
                }
            } catch (Exception exception) {
                view.showMessage("Erro: " + exception.getMessage());
            }
        }
    }

    private void searchCourseByCode(User activeUser) throws Exception {
        String shareCode = view.readShareCode();
        Optional<Course> course = courseService.findByShareCode(shareCode);
        if (course.isEmpty()) {
            view.showMessage("Curso não encontrado.");
            return;
        }
        if (course.get().getOwnerUserId() == activeUser.getId()) {
            view.showMessage("Use o menu Meus cursos para gerenciar cursos criados por você.");
            return;
        }
        showCourseForEnrollment(activeUser, course.get());
    }

    private void searchCoursesByName(User activeUser) throws Exception {
        String query = view.readCourseNameQuery();
        List<CourseSearchResult> results = courseService.searchCoursesByName(activeUser, query);
        int totalPages = Math.max(1, (int) Math.ceil(results.size() / (double) PAGE_SIZE));
        int page = 1;

        while (true) {
            int fromIndex = Math.min((page - 1) * PAGE_SIZE, results.size());
            int toIndex = Math.min(fromIndex + PAGE_SIZE, results.size());
            List<CourseSearchResult> currentPage = results.subList(fromIndex, toIndex);
            String option = view.showCourseSearchPage(currentPage, page, totalPages);
            if ("A".equals(option)) {
                if (page > 1) {
                    page--;
                } else {
                    view.showMessage("Você já está na primeira página.");
                }
            } else if ("B".equals(option)) {
                if (page < totalPages) {
                    page++;
                } else {
                    view.showMessage("Você já está na última página.");
                }
            } else if ("R".equals(option)) {
                return;
            } else {
                int selectedIndex = parseSelection(option, currentPage.size(), true);
                if (selectedIndex < 0) {
                    view.showMessage("Opção inválida.");
                } else {
                    showCourseForEnrollment(activeUser, currentPage.get(selectedIndex).getCourse());
                }
            }
        }
    }

    private void listAllCourses(User activeUser) throws Exception {
        List<Course> courses = courseService.listCoursesForEnrollmentSearch(activeUser);
        int totalPages = Math.max(1, (int) Math.ceil(courses.size() / (double) PAGE_SIZE));
        int page = 1;

        while (true) {
            int fromIndex = Math.min((page - 1) * PAGE_SIZE, courses.size());
            int toIndex = Math.min(fromIndex + PAGE_SIZE, courses.size());
            List<Course> currentPage = courses.subList(fromIndex, toIndex);
            String option = view.showCoursePage(currentPage, page, totalPages);
            if ("A".equals(option)) {
                if (page > 1) {
                    page--;
                } else {
                    view.showMessage("Você já está na primeira página.");
                }
            } else if ("B".equals(option)) {
                if (page < totalPages) {
                    page++;
                } else {
                    view.showMessage("Você já está na última página.");
                }
            } else if ("R".equals(option)) {
                return;
            } else {
                int selectedIndex = parseSelection(option, currentPage.size(), true);
                if (selectedIndex < 0) {
                    view.showMessage("Opção inválida.");
                } else {
                    showCourseForEnrollment(activeUser, currentPage.get(selectedIndex));
                    courses = courseService.listCoursesForEnrollmentSearch(activeUser);
                    totalPages = Math.max(1, (int) Math.ceil(courses.size() / (double) PAGE_SIZE));
                    page = Math.min(page, totalPages);
                }
            }
        }
    }

    private void showCourseForEnrollment(User activeUser, Course course) throws Exception {
        while (true) {
            Course currentCourse = courseService.findById(course.getId()).orElse(null);
            if (currentCourse == null) {
                view.showMessage("Curso não encontrado.");
                return;
            }

            boolean alreadyEnrolled = enrollmentService.findByUserAndCourse(activeUser.getId(), currentCourse.getId()).isPresent();
            boolean canEnroll = currentCourse.getStatus() == CourseStatus.OPEN_FOR_ENROLLMENT
                && !alreadyEnrolled
                && currentCourse.getOwnerUserId() != activeUser.getId();
            String authorName = findUserName(currentCourse.getOwnerUserId());
            String option = view.showCourseDetails(currentCourse, authorName, canEnroll, alreadyEnrolled);
            if ("A".equals(option) && canEnroll) {
                enrollmentService.enroll(activeUser, currentCourse);
                view.showMessage("Inscrição realizada com sucesso.");
            } else if ("R".equals(option)) {
                return;
            } else {
                view.showMessage("Opção inválida.");
            }
        }
    }

    private void showMyEnrollment(User activeUser, EnrolledCourseItem item) throws Exception {
        while (true) {
            Optional<CourseUser> currentEnrollment = enrollmentService.findByUserAndCourse(activeUser.getId(), item.getCourse().getId());
            if (currentEnrollment.isEmpty()) {
                view.showMessage("Inscrição não encontrada.");
                return;
            }

            Course course = courseService.findById(item.getCourse().getId()).orElse(null);
            if (course == null) {
                view.showMessage("Curso não encontrado.");
                return;
            }

            EnrolledCourseItem currentItem = new EnrolledCourseItem(course, currentEnrollment.get(), findUserName(course.getOwnerUserId()));
            String option = view.showMyEnrollmentDetails(currentItem);
            if ("A".equals(option)) {
                if (view.confirmEnrollmentCancel()) {
                    enrollmentService.cancelEnrollment(currentEnrollment.get());
                    view.showMessage("Inscrição cancelada.");
                    return;
                }
            } else if ("R".equals(option)) {
                return;
            } else {
                view.showMessage("Opção inválida.");
            }
        }
    }

    public void manageCourseEnrollments(Course course) {
        while (true) {
            try {
                Course currentCourse = courseService.findById(course.getId()).orElse(null);
                if (currentCourse == null) {
                    view.showMessage("Curso não encontrado.");
                    return;
                }

                List<CourseUserItem> enrollments = buildCourseUserItems(currentCourse);
                String option = view.showCourseEnrollmentList(currentCourse, enrollments);
                if ("A".equals(option)) {
                    Path exportPath = enrollmentService.exportCourseEnrollments(currentCourse);
                    view.showMessage("Lista exportada para " + exportPath.toAbsolutePath() + ".");
                } else if ("R".equals(option)) {
                    return;
                } else {
                    int selectedIndex = parseSelection(option, enrollments.size(), false);
                    if (selectedIndex < 0) {
                        view.showMessage("Opção inválida.");
                    } else {
                        showCourseUser(currentCourse, enrollments.get(selectedIndex));
                    }
                }
            } catch (Exception exception) {
                view.showMessage("Erro: " + exception.getMessage());
            }
        }
    }

    private void showCourseUser(Course course, CourseUserItem item) throws Exception {
        while (true) {
            String option = view.showCourseUserDetails(course, item);
            if ("A".equals(option)) {
                if (view.confirmEnrollmentCancel()) {
                    enrollmentService.cancelEnrollment(item.getEnrollment());
                    view.showMessage("Inscrição cancelada.");
                    return;
                }
            } else if ("R".equals(option)) {
                return;
            } else {
                view.showMessage("Opção inválida.");
            }
        }
    }

    private List<EnrolledCourseItem> buildUserEnrollmentItems(User user) throws Exception {
        List<EnrolledCourseItem> items = new ArrayList<>();
        for (CourseUser enrollment : enrollmentService.listByUser(user)) {
            Optional<Course> course = courseService.findById(enrollment.getCourseId());
            if (course.isPresent()) {
                items.add(new EnrolledCourseItem(course.get(), enrollment, findUserName(course.get().getOwnerUserId())));
            }
        }
        items.sort(Comparator
            .comparing((EnrolledCourseItem item) -> item.getCourse().getStartDate())
            .thenComparing(item -> item.getCourse().getName()));
        return items;
    }

    private List<CourseUserItem> buildCourseUserItems(Course course) throws Exception {
        List<CourseUserItem> items = new ArrayList<>();
        for (CourseUser enrollment : enrollmentService.listByCourse(course)) {
            Optional<User> user = userService.findById(enrollment.getUserId());
            if (user.isPresent()) {
                items.add(new CourseUserItem(user.get(), enrollment));
            }
        }
        items.sort(Comparator
            .comparing((CourseUserItem item) -> item.getUser().getName())
            .thenComparing(item -> item.getUser().getEmail()));
        return items;
    }

    private String findUserName(int userId) throws Exception {
        return userService.findById(userId).map(User::getName).orElse("Usuário removido");
    }

    private int parseSelection(String option, int itemCount, boolean acceptZeroAsTen) {
        try {
            int selected = Integer.parseInt(option);
            if (acceptZeroAsTen && selected == 0) {
                selected = 10;
            }
            if (selected < 1 || selected > itemCount) {
                return -1;
            }
            return selected - 1;
        } catch (NumberFormatException exception) {
            return -1;
        }
    }
}
