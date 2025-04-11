package matchle;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

public final class Corpus implements Iterable<NGram> {
    private final Set<NGram> corpus;

    private Corpus(Set<NGram> corpus) {
        this.corpus = Set.copyOf(corpus);
    }

    public Set<NGram> corpus() {
        return corpus;
    }

    public int wordSize() {
        return corpus.isEmpty() ? 0 : corpus.iterator().next().size();
    }

    public boolean contains(NGram ngram) {
        return corpus.contains(ngram);
    }

    public int size() {
        return corpus.size();
    }

    public long size(Filter filter) {
        return corpus.stream().filter(filter::test).count();
    }

    @Override
    public Iterator<NGram> iterator() {
        return corpus.iterator();
    }

    // add stream into corpus
    public java.util.stream.Stream<NGram> stream() {
        return corpus.stream();
    }

    // ---------------- new added function for hw4 ----------------

    /**
     * 计算给定 key 和 guess 之间的评分，
     * 评分定义为：应用 NGramMatcher 生成的 Filter 后，
     * 剩余词库中符合该 Filter 条件的 n-gram 个数。
     * 如果 corpus 为空，则抛出 IllegalStateException。
     * compute score between key and guess
     * score: after NGramMater's filter, the left amount dictonary fullfill Filter condion 
     * is corpus null, give illegalStateException
     */
    public long score(NGram key, NGram guess) {
        if (corpus.isEmpty()) {
            throw new IllegalStateException("Corpus is empty");
        }
        Filter filter = NGramMatcher.of(key, guess).match();
        return corpus.stream().filter(filter::test).count();
    }

    /**
     * 对于给定的猜测 n-gram，计算其 worst-case 分数，
     * 即在 corpus 中将每个 n-gram 作为 key 计算 score(key, guess)，
     * 返回这些评分中的最大值。
     * for given guess n-gram compute it  as worst-case score
     * return average score
     */
    public long scoreWorstCase(NGram guess) {
        if (corpus.isEmpty()) {
            throw new IllegalStateException("Corpus is empty");
        }
        long worst = 0;
        for (NGram key : corpus) {
            long s = score(key, guess);
            worst = Math.max(worst, s);
        }
        return worst;
    }

    /**
     * 对于给定的猜测 n-gram，计算其 average-case 分数，
     * 即在 corpus 中将每个 n-gram 作为 key 计算 score(key, guess)，
     * 返回这些评分的平均值。
     * for given guess n-gram compute it  as average-case score
     * return average score
     */
    public double scoreAverageCase(NGram guess) {
        if (corpus.isEmpty()) {
            throw new IllegalStateException("Corpus is empty");
        }
        long total = 0;
        int count = 0;
        for (NGram key : corpus) {
            total += score(key, guess);
            count++;
        }
        return (double) total / count;
    }

    /**
     * 返回最佳的 worst-case 猜测，即遍历 corpus 中所有 n-gram，
     * 选择那个使得 scoreWorstCase(guess) 最小的 n-gram。
     * return best worst-case guess by go over all n-gram in corpus
     * choose make score worstCase smallest ngram
     */
    public NGram bestWorstCaseGuess() {
        if (corpus.isEmpty()) {
            throw new IllegalStateException("Corpus is empty");
        }
        NGram best = null;
        long bestScore = Long.MAX_VALUE;
        for (NGram guess : corpus) {
            long score = scoreWorstCase(guess);
            if (score < bestScore) {
                bestScore = score;
                best = guess;
            }
        }
        return best;
    }

    /**
     * 返回最佳的 average-case 猜测，即遍历 corpus 中所有 n-gram，
     * 选择那个使得 scoreAverageCase(guess) 最小的 n-gram。
     * return best average-case guess by go over all n-gram in corpus
     * choose make scoreAverageCase(guess ) smallest n-gram
     * 
     */
    public NGram bestAverageCaseGuess() {
        if (corpus.isEmpty()) {
            throw new IllegalStateException("Corpus is empty");
        }
        NGram best = null;
        double bestScore = Double.MAX_VALUE;
        for (NGram guess : corpus) {
            double score = scoreAverageCase(guess);
            if (score < bestScore) {
                bestScore = score;
                best = guess;
            }
        }
        return best;
    }

    /**
     * 根据自定义准则返回最佳猜测。准则是一个 ToLongFunction，
     * 对每个候选 n-gram 计算一个长整型分数，
     * 最后选择使该分数最小的 n-gram。
     * return best guess based on special rules, a toLongFunction
     * compute a long for each n-gram
     * choose smallest n-gram score
     */
    
    public NGram bestGuess(ToLongFunction<NGram> criterion) {
        if (corpus.isEmpty()) {
            throw new IllegalStateException("Corpus is empty");
        }
        NGram best = null;
        long bestScore = Long.MAX_VALUE;
        for (NGram guess : corpus) {
            long score = criterion.applyAsLong(guess);
            if (score < bestScore) {
                bestScore = score;
                best = guess;
            }
        }
        return best;
    }
    
    // ---------------- Builder inside class ----------------

    public static final class Builder {
        private final Set<NGram> ngrams;

        // 使用带参数的构造方法
        private Builder(Set<NGram> ngrams) {
            this.ngrams = new HashSet<>(ngrams);
        }
        
        public static final Builder EMPTY = new Builder(new HashSet<>());
        
        public static final Builder of(Corpus corpus) {
            Objects.requireNonNull(corpus, "Corpus cannot be null");
            return new Builder(corpus.corpus());
        }
        
        public static Builder of() {
            return new Builder(new HashSet<>());
        }
        
        public Builder add(NGram ngram) {
            Objects.requireNonNull(ngram, "NGram cannot be null");
            ngrams.add(ngram);
            return this;
        }
        
        public Builder addAll(Collection<NGram> ngramsCollection) {
            Objects.requireNonNull(ngramsCollection, "Collection cannot be null");
            ngrams.addAll(
                ngramsCollection.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet())
            );
            return this;
        }
        
        public Builder filter(Filter filter) {
            return new Builder(ngrams.stream().filter(filter::test).collect(Collectors.toSet()));
        }
        
        public boolean isConsistent(Integer wordSize) {
            return ngrams.stream().allMatch(ngram -> ngram.size() == wordSize);
        }
        
        public Corpus build() {
            return ngrams.isEmpty() || !isConsistent(wordSize()) ? null : new Corpus(ngrams);
        }
        
        private int wordSize() {
            return ngrams.isEmpty() ? 0 : ngrams.iterator().next().size();
        }
    }
}
