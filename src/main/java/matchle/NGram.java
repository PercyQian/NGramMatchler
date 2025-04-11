package matchle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class NGram implements  Iterable<IndexedCharacter> {
    private final ArrayList<Character> ngram;
    private final Set<Character> charset;

    private NGram(List<Character> characters) {
        this.ngram = new ArrayList<>(characters);
        this.charset = new HashSet<>(characters);
    }

    public static final NGram from(List<Character> characters) {
        NullCharacterException.validate(characters);
        return new NGram(characters);
    }

    public static final NGram from(String word) {
        Objects.requireNonNull(word, "Word cannot be null");
        List<Character> characters = new ArrayList<>();
        for (char c : word.toCharArray()) {
            characters.add(c);
        }
        return from(characters);
    }

    public Character get(int index) {
        return ngram.get(index);
    }

    public int size() {
        return ngram.size();
    }

    public boolean matches(IndexedCharacter c) {
        return ngram.get(c.index()).equals(c.character()); 
    }

    public boolean contains(char c) {
        return charset.contains(c);
    }

    public boolean containsElsewhere(IndexedCharacter c) {
        return charset.contains(c.character()) && !matches(c);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NGram)) return false;
        NGram other = (NGram) obj;
        return ngram.equals(other.ngram);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ngram);
    }

    public Stream<IndexedCharacter> stream() {
        return IntStream.range(0, ngram.size())
                .mapToObj(i -> new IndexedCharacter(i, ngram.get(i)));
    }

    @Override
    public Iterator<IndexedCharacter> iterator() {
        return new NGramIterator(); 
    }

    private final class NGramIterator implements Iterator<IndexedCharacter> {
        private int index = 0;

        @Override
        public boolean hasNext() {
            return index < ngram.size();
        }

        @Override
        public IndexedCharacter next() {
            if (!hasNext()) throw new NoSuchElementException();
            return new IndexedCharacter(index, ngram.get(index++));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Character c : ngram) {
            sb.append(c);
        }
        return sb.toString();
    }
}
