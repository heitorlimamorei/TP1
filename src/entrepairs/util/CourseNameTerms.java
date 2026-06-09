package entrepairs.util;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class CourseNameTerms {

    private static final Set<String> STOP_WORDS = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
        "a", "ao", "aos", "aquela", "aquelas", "aquele", "aqueles", "aquilo", "as",
        "ate", "com", "como", "da", "das", "de", "dela", "delas", "dele", "deles",
        "depois", "do", "dos", "e", "ela", "elas", "ele", "eles", "em", "entre", "era",
        "essa", "essas", "esse", "esses", "esta", "estas", "este", "estes", "eu", "foi",
        "isso", "isto", "ja", "lhe", "lhes", "mais", "mas", "me", "mesmo", "meu", "meus",
        "minha", "minhas", "muito", "na", "nas", "nem", "no", "nos", "nossa", "nossas",
        "nosso", "nossos", "num", "numa", "o", "os", "ou", "para", "pela", "pelas", "pelo",
        "pelos", "por", "qual", "quando", "que", "quem", "se", "sem", "seu", "seus", "sua",
        "suas", "tambem", "te", "tem", "tendo", "ter", "tu", "um", "uma", "umas", "uns",
        "voce", "voces",
        "zero", "dois", "duas", "tres", "quatro", "cinco", "seis", "sete", "oito", "nove",
        "dez", "primeiro", "primeira", "segundo", "segunda", "terceiro", "terceira"
    )));

    private CourseNameTerms() {
    }

    public static List<String> extract(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^\\p{Alnum}]+", " ")
            .trim();

        if (normalized.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> terms = new ArrayList<>();
        for (String term : normalized.split("\\s+")) {
            if (!STOP_WORDS.contains(term) && !term.matches("\\d+")) {
                terms.add(term);
            }
        }
        return terms;
    }

    public static Set<String> uniqueTerms(String text) {
        return new LinkedHashSet<>(extract(text));
    }

    public static Map<String, Float> termFrequencies(String text) {
        List<String> terms = extract(text);
        if (terms.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Integer> occurrences = new LinkedHashMap<>();
        for (String term : terms) {
            occurrences.put(term, occurrences.getOrDefault(term, 0) + 1);
        }

        Map<String, Float> frequencies = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> occurrence : occurrences.entrySet()) {
            frequencies.put(occurrence.getKey(), occurrence.getValue() / (float) terms.size());
        }
        return frequencies;
    }
}
