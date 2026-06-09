package entrepairs;

import java.util.Arrays;
import java.util.Map;

import entrepairs.util.CourseNameTerms;

final class CourseNameTermsTest {

    private CourseNameTermsTest() {
    }

    static void run() {
        TestAssertions.assertEquals(
            Arrays.asList("introducao", "inteligencia", "artificial"),
            CourseNameTerms.extract("Introdução à Inteligência Artificial"),
            "Deve remover acentos, caixa e stop words."
        );
        TestAssertions.assertEquals(
            Arrays.asList("inteligencia", "trabalho", "meio", "inteligencia", "artificial"),
            CourseNameTerms.extract("Inteligência no Trabalho por Meio da Inteligência Artificial"),
            "Deve preservar as palavras relevantes e repetidas."
        );
        TestAssertions.assertTrue(
            CourseNameTerms.extract("de 1 a 10").isEmpty(),
            "Deve descartar stop words e numerais."
        );

        Map<String, Float> frequencies = CourseNameTerms.termFrequencies(
            "Inteligência no Trabalho por Meio da Inteligência Artificial"
        );
        TestAssertions.assertClose(0.4, frequencies.get("inteligencia"), 0.00001, "TF de inteligência incorreto.");
        TestAssertions.assertClose(0.2, frequencies.get("artificial"), 0.00001, "TF de artificial incorreto.");
    }
}
