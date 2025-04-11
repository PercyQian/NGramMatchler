package matchle;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CorpusTest {
    private Corpus corpus;
    private NGram word1, word2, word3;

    @BeforeEach
    public void setUp() {
        word1 = NGram.from("apple");
        word2 = NGram.from("pearl");
        word3 = NGram.from("grape");

        corpus = Corpus.Builder.of()
                .add(word1)
                .add(word2)
                .add(word3)
                .build();
    }

    @Test
    public void testCorpusContainsAddedWords() {
        assertTrue(corpus.contains(word1), "Corpus should contain 'apple'");
        assertTrue(corpus.contains(word2), "Corpus should contain 'pearl'");
        assertTrue(corpus.contains(word3), "Corpus should contain 'grape'");
    }

    @Test
    public void testCorpusDoesNotContainNonexistentWord() {
        NGram nonExistent = NGram.from("table");
        assertFalse(corpus.contains(nonExistent), "Corpus should NOT contain 'table'");
    }

    @Test
    public void testCorpusWordSize() {
        assertEquals(5, corpus.wordSize(), "Word size should be 5 for all words in corpus");
    }

    @Test
    public void testCorpusIterator() {
        int count = 0;
        for (NGram ngram : corpus) {
            count++;
        }
        assertEquals(3, count, "Iterator should return exactly 3 elements");
    }

    @Test
    public void testCorpusStream() {
        Set<NGram> words = corpus.stream().collect(Collectors.toSet());
        assertTrue(words.contains(word1) && words.contains(word2) && words.contains(word3),
                "Stream should contain all added words");
    }

    @Test
    public void testEmptyCorpus() {
        Corpus emptyCorpus = Corpus.Builder.of().build();
        assertNull(emptyCorpus, "Empty corpus should be null when no words are added");
    }

    @Test
    public void testInconsistentWordSizes() {
        Corpus.Builder builder = Corpus.Builder.of()
                .add(NGram.from("apple"))
                .add(NGram.from("banana"));  // Different length (6)

        assertNull(builder.build(), "Corpus should be null if word sizes are inconsistent");
    }

    @Test
    public void testAddNull() {
        Corpus.Builder builder = Corpus.Builder.of();
        assertThrows(NullPointerException.class, () -> builder.add(null),
                "Adding null to the builder should throw NullPointerException");
    }
}
