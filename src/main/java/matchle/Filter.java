package matchle;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public final class Filter {
    private final Predicate<NGram> predicate;
    private String pattern;

    private Filter(Predicate<NGram> predicate) {
        this.predicate = predicate;
        this.pattern = "";
    }

    public static Filter from(Predicate<NGram> predicate) {
        Objects.requireNonNull(predicate, "Predicate cannot be null");
        return new Filter(predicate);
    }

    public boolean test(NGram ngram) {
        return predicate.test(ngram);
    }

    public Filter and(Optional<Filter> other) {
        return other.map(o -> new Filter(this.predicate.and(o.predicate))).orElse(this);
    }

    public Filter withPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public static final Filter FALSE = new Filter(ngram -> false);

    @Override
    public String toString() {
        return pattern.isEmpty() ? "Filter[]" : pattern;
    }
}