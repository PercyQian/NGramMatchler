package matchle;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class NGramTest {

    //test if create successfully
    @Test
    public void testFromString() {
        NGram ngram = NGram.from("hello");
        assertNotNull(ngram);
        assertEquals(5, ngram.size());
        assertEquals('h', ngram.get(0));
        assertEquals('o', ngram.get(4));
    }

    //test if empty string
    @Test
    public void testFromEmptyString() {
        NGram ngram = NGram.from("");
        assertNotNull(ngram);
        assertEquals(0, ngram.size());
    }

    //test if single character size
    @Test
    public void testFromSingleCharacter() {
        NGram ngram = NGram.from("a");
        assertNotNull(ngram);
        assertEquals(1, ngram.size());
        assertEquals('a', ngram.get(0));
    }
    //test null exception of nullpointerexception for null string
    @Test
    public void testFromNullString() {
        assertThrows(NullPointerException.class, () -> NGram.from((String) null));
    }

    //test if null exception of nullpoointerexception for null list
    @Test
    public void testFromNullList() {
        assertThrows(NullPointerException.class, () -> NGram.from((List<Character>) null));
    }

}
