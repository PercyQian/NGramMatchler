package matchle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CorpusScoreDebugTest {
    private Corpus corpus;
    
    @BeforeEach
    public void setUp() {
        // 构造包含 4 个 n-gram 的词库
        corpus = Corpus.Builder.of()
                    .add(NGram.from("rebus"))
                    .add(NGram.from("redux"))
                    .add(NGram.from("route"))
                    .add(NGram.from("hello"))
                    .build();
        assertNotNull(corpus, "Corpus should not be null when valid n-grams are added");
    }
    
    @Test
    public void debugScoreBreakdown() {
        NGram guess = NGram.from("route");
        long totalScore = 0;
        int count = 0;
        
        System.out.println("Debugging score breakdown for guess: " + guess);
        for (NGram key : corpus) {
            // 对每个候选 key，计算 score(key, guess)
            long score = corpus.score(key, guess);
            totalScore += score;
            count++;
            System.out.println("-----");
            System.out.println("Key: " + key);
            System.out.println("Score: " + score);
            // 输出详细匹配信息
            String debugInfo = NGramMatcher.of(key, guess).debugMatch();
            System.out.println(debugInfo);
        }
        double avgScore = (double) totalScore / count;
        System.out.println("Total score = " + totalScore + " over " + count + " keys, average = " + avgScore);
        // 你可以断言平均分为预期值，例如 1.25 或1.5
        // assertEquals(1.25, avgScore, 0.0001, "Average score should be as expected");
        assertEquals(1.25, avgScore, 0.0001, "Average-case score for guess 'route' should be 1.25");
    }
}
