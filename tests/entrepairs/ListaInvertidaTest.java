package entrepairs;

import java.nio.file.Files;
import java.nio.file.Path;

import aed3.ListaInvertida;
import aed3.ListaInvertida.ElementoLista;

final class ListaInvertidaTest {

    private ListaInvertidaTest() {
    }

    static void run() throws Exception {
        Path directory = Files.createTempDirectory("lista-invertida-test");
        Path dictionary = directory.resolve("terms.dict");
        Path blocks = directory.resolve("terms.blocks");

        try (ListaInvertida index = new ListaInvertida(2, dictionary.toString(), blocks.toString())) {
            TestAssertions.assertTrue(index.create("java", new ElementoLista(3, 0.5f)), "Primeira inserção deve funcionar.");
            TestAssertions.assertTrue(index.create("java", new ElementoLista(1, 0.25f)), "Segunda inserção deve funcionar.");
            TestAssertions.assertTrue(index.create("java", new ElementoLista(2, 0.75f)), "Overflow deve criar novo bloco.");
            TestAssertions.assertTrue(!index.create("java", new ElementoLista(2, 1.0f)), "ID duplicado deve ser rejeitado.");

            ElementoLista[] entries = index.read("java");
            TestAssertions.assertEquals(3, entries.length, "A lista deve conter três elementos.");
            TestAssertions.assertEquals(1, entries[0].getId(), "A leitura deve ordenar por ID.");
            TestAssertions.assertClose(0.75, entries[1].getFrequencia(), 0.00001, "A frequência deve ser persistida.");
            TestAssertions.assertTrue(index.delete("java", 2), "A exclusão deve remover o ID.");
            TestAssertions.assertTrue(!index.delete("java", 99), "A exclusão de ID inexistente deve retornar falso.");
        }

        try (ListaInvertida reopened = new ListaInvertida(2, dictionary.toString(), blocks.toString())) {
            ElementoLista[] entries = reopened.read("java");
            TestAssertions.assertEquals(2, entries.length, "Os dados devem sobreviver à reabertura.");
            TestAssertions.assertEquals(1, entries[0].getId(), "Primeiro ID persistido incorreto.");
            TestAssertions.assertEquals(3, entries[1].getId(), "Segundo ID persistido incorreto.");
        }
    }
}
