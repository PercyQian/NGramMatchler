package matchle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class NGramMatcherTest {

    @Test
    public void testExactMatch() {
        NGram key = NGram.from("apple");
        NGram guess = NGram.from("apple");

        Filter filter = NGramMatcher.of(key, guess).match();
        assertTrue(filter.test(NGram.from("apple")), "The key should match itself");
    }

    @Test
    public void testPartialMatch() {
        NGram key = NGram.from("apple");
        NGram guess = NGram.from("applo");

        Filter filter = NGramMatcher.of(key, guess).match();
        assertFalse(filter.test(NGram.from("hello")), "hello should not match");
        assertTrue(filter.test(NGram.from("apple")), "apple should match");
        assertFalse(filter.test(NGram.from("ppale")), "ppale should not match");
    }

    @Test
    public void testCorpusFiltering() {
        Corpus corpus = Corpus.Builder.of()
                .add(NGram.from("apple"))
                .add(NGram.from("pearl"))
                .add(NGram.from("grape"))
                .add(NGram.from("table"))
                .build();

        NGram key = NGram.from("apple");
        NGram guess = NGram.from("applo");
        Filter filter = NGramMatcher.of(key, guess).match();

        assertEquals(1, corpus.size(filter), "Only 'apple' should match");
    }

    @Test
public void testFeedbackPositions() {
    // target word
    NGram key = NGram.from("rebus");
    NGram guess = NGram.from("route");
    
    // create fliter
    Filter filter = NGramMatcher.of(key, guess).match();

    // redux
    // - "redux" 0'r'
    // - "redux"  'e'（1 'e'）， 'o'
    // - "redux" （3 is 'u'）
    // - "redux" not 't'
    // - "redux" else 'e' containsElsewhere(4, "e")
    assertTrue(filter.test(NGram.from("redux")), "redux should match");

   
}
}