package entrepairs;

import java.time.LocalDate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import entrepairs.model.Course;
import entrepairs.model.CourseSearchResult;
import entrepairs.model.CourseStatus;
import entrepairs.repository.adapter.Aed3CourseRepository;

final class Aed3CourseRepositoryTest {

    private Aed3CourseRepositoryTest() {
    }

    static void run() throws Exception {
        int firstId;
        int thirdId;
        try (Aed3CourseRepository repository = new Aed3CourseRepository()) {
            Course first = create(repository, 1, "Introdução à Inteligência Artificial", "course0001");
            create(repository, 2, "Inteligência Emocional para Gestores", "course0002");
            Course third = create(
                repository,
                3,
                "Inteligência no Trabalho por Meio da Inteligência Artificial",
                "course0003"
            );
            create(repository, 4, "Introdução à Gestão de Equipes", "course0004");
            firstId = first.getId();
            thirdId = third.getId();

            List<CourseSearchResult> results = repository.searchByNameTerms("Inteligência Artificial");
            TestAssertions.assertEquals(3, results.size(), "A busca deve encontrar os três cursos relacionados.");
            TestAssertions.assertEquals(firstId, results.get(0).getCourse().getId(), "O curso 1 deve ter a maior relevância.");
            TestAssertions.assertEquals(thirdId, results.get(1).getCourse().getId(), "O curso 3 deve ter a segunda maior relevância.");
            TestAssertions.assertClose(0.808, results.get(0).getScore(), 0.002, "TF-IDF do curso 1 incorreto.");
            TestAssertions.assertClose(0.710, results.get(1).getScore(), 0.002, "TF-IDF do curso 3 incorreto.");

            Course updated = third.copy();
            updated.setName("Programação em Java");
            TestAssertions.assertTrue(repository.update(updated), "A atualização do curso deve funcionar.");
            results = repository.searchByNameTerms("Inteligência Artificial");
            TestAssertions.assertEquals(2, results.size(), "O nome antigo deve sair do índice após atualização.");

            TestAssertions.assertTrue(repository.delete(firstId), "A exclusão do curso deve funcionar.");
            results = repository.searchByNameTerms("Inteligência Artificial");
            TestAssertions.assertEquals(1, results.size(), "O curso excluído deve sair do índice.");
        }

        try (Aed3CourseRepository reopened = new Aed3CourseRepository()) {
            List<CourseSearchResult> results = reopened.searchByNameTerms("Programação Java");
            TestAssertions.assertEquals(1, results.size(), "O índice deve permanecer válido após reabertura.");
            TestAssertions.assertEquals(thirdId, results.get(0).getCourse().getId(), "Curso reaberto incorreto.");
            TestAssertions.assertTrue(
                reopened.searchByNameTerms("de para 123").isEmpty(),
                "Consulta sem termos válidos deve retornar lista vazia."
            );
        }

        Files.delete(Path.of("data", "indexes", "courses", "name-terms.dict"));
        Files.delete(Path.of("data", "indexes", "courses", "name-terms.blocks"));
        try (Aed3CourseRepository rebuilt = new Aed3CourseRepository()) {
            List<CourseSearchResult> results = rebuilt.searchByNameTerms("Programação Java");
            TestAssertions.assertEquals(1, results.size(), "Cursos existentes devem ser reindexados automaticamente.");
            TestAssertions.assertEquals(thirdId, results.get(0).getCourse().getId(), "Curso reconstruído incorreto.");
        }
    }

    private static Course create(
        Aed3CourseRepository repository,
        int ownerId,
        String name,
        String shareCode
    ) throws Exception {
        Course course = new Course(
            0,
            ownerId,
            name,
            LocalDate.of(2026, 7, ownerId),
            "Descrição de teste",
            shareCode,
            CourseStatus.OPEN_FOR_ENROLLMENT
        );
        repository.create(course);
        return course;
    }
}
