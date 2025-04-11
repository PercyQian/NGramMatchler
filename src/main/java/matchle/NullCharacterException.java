package matchle;

import java.util.List;
import java.util.Objects;

public final class NullCharacterException extends Exception {
    private static final long serialVersionUID = 1L;
    private final int index;

    public NullCharacterException(int index) {
        super("Null character found at index: " + index);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    
    public static List<Character> validate(List<Character> ngram) {
        Objects.requireNonNull(ngram, "NGram list cannot be null");

        for (int i = 0; i < ngram.size(); i++) {
            if (ngram.get(i) == null) {
                throw new IllegalArgumentException(new NullCharacterException(i));
            }
        }
        return ngram;
    }
}
