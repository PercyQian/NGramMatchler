package matchle;

import java.util.function.ToLongFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CorpusScoringTest {
    
    private Corpus corpus;
    
    @BeforeEach
    public void setUp() {
        // 构造包含 4 个 n-gram 的词库
        // construct corpus include 4 ngram
        corpus = Corpus.Builder.of()
                    .add(NGram.from("rebus"))
                    .add(NGram.from("redux"))
                    .add(NGram.from("route"))
                    .add(NGram.from("hello"))
                    .build();
        // if corpus is null or different length, build() will return null, so corpus not null here
        // 根据设计要求，如果词库为空或单词长度不一致，build() 会返回 null，
        // 因此这里 corpus 应该不为 null
        assertNotNull(corpus, "Corpus should not be null when valid n-grams are added");
    }
    
    @Test
    public void testScore() {
        // 对于 key "rebus" 和 guess "route"，反馈筛选后，只有 "rebus" 和 "redux" 符合条件，score 应返回 2
        // for key 'rebus' and guess 'route' after flitering, only 'rebus' and redux fullfill condion, return 2
        long score = corpus.score(NGram.from("rebus"), NGram.from("route"));
        assertEquals(2, score, "Score for key 'rebus' and guess 'route' should be 2");
    }
    
    @Test
    public void testScoreWorstCase() {
        // 根据示例，对于 guess "route"，worst-case 得分应为 2
        // for
        long worstCase = corpus.scoreWorstCase(NGram.from("route"));
        assertEquals(2, worstCase, "Worst-case score for guess 'route' should be 2");
    }
    
    @Test
    public void testScoreAverageCase() {
        // 根据示例，对于 guess "route"，average-case 得分应为 (2+1+1+1)/4 = 1.5
        double avgCase = corpus.scoreAverageCase(NGram.from("route"));
        assertEquals(1.5, avgCase, 0.0001, "Average-case score for guess 'route' should be 1.5");
    }
    
    @Test
    public void testBestWorstCaseGuess() {
        NGram bestWorst = corpus.bestWorstCaseGuess();
        assertNotNull(bestWorst, "Best worst-case guess should not be null");
        
        // 遍历 corpus，计算每个候选猜测的 worst-case 得分，并找出最小值
        long minWorst = Long.MAX_VALUE;
        for (NGram candidate : corpus) {
            long worst = corpus.scoreWorstCase(candidate);
            if (worst < minWorst) {
                minWorst = worst;
            }
        }
        long bestWorstScore = corpus.scoreWorstCase(bestWorst);
        assertEquals(minWorst, bestWorstScore, "Best worst-case guess should yield minimal worst-case score");
    }
    
    @Test
    public void testBestAverageCaseGuess() {
        NGram bestAvg = corpus.bestAverageCaseGuess();
        assertNotNull(bestAvg, "Best average-case guess should not be null");
        
        // 遍历 corpus，计算每个候选猜测的 average-case 得分，并找出最小值
        double minAvg = Double.MAX_VALUE;
        for (NGram candidate : corpus) {
            double avg = corpus.scoreAverageCase(candidate);
            if (avg < minAvg) {
                minAvg = avg;
            }
        }
        double bestAvgScore = corpus.scoreAverageCase(bestAvg);
        assertEquals(minAvg, bestAvgScore, 0.0001, "Best average-case guess should yield minimal average-case score");
    }
    
    @Test
    public void testBestGuessWithCustomCriterion() {
        // 使用 worst-case 得分作为自定义准则
        ToLongFunction<NGram> worstCaseCriterion = ngram -> corpus.scoreWorstCase(ngram);
        NGram bestCustom = corpus.bestGuess(worstCaseCriterion);
        assertNotNull(bestCustom, "Best guess with custom criterion should not be null");
        
        // bestGuess 应该与 bestWorstCaseGuess 一致（因为两者均采用 worst-case 得分作为准则）
        NGram bestWorst = corpus.bestWorstCaseGuess();
        long scoreCustom = corpus.scoreWorstCase(bestCustom);
        long scoreWorst = corpus.scoreWorstCase(bestWorst);
        assertEquals(scoreWorst, scoreCustom, "Custom best guess should match best worst-case guess when using worst-case criterion");
    }
    
    @Test
    public void testEmptyCorpus() {
        // 根据设计要求，当没有添加任何 n-gram 时，build() 应返回 null
        Corpus emptyCorpus = Corpus.Builder.of().build();
        assertNull(emptyCorpus, "Empty corpus should be null when no n-grams are added");
    }
}
