package entrepairs;

public class TestRunner {

    public static void main(String[] args) throws Exception {
        run("CourseNameTermsTest", CourseNameTermsTest::run);
        run("ListaInvertidaTest", ListaInvertidaTest::run);
        run("Aed3CourseRepositoryTest", Aed3CourseRepositoryTest::run);
        run("EnrollmentControllerTest", EnrollmentControllerTest::run);
        System.out.println("Todos os testes passaram.");
    }

    private static void run(String name, CheckedRunnable test) throws Exception {
        test.run();
        System.out.println("[OK] " + name);
    }

    @FunctionalInterface
    private interface CheckedRunnable {
        void run() throws Exception;
    }
}
