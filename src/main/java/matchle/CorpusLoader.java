package matchle;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CorpusLoader {

    /**
     * Downloads an English word list from a specified URL and constructs a Corpus
     * containing only words of the specified length.
     * 
     * @param wordLength The required word length (e.g., 5)
     * @return A constructed Corpus, or null if no valid words are found
     * 
     * Implementation details:
     * 1. Downloads word list from GitHub
     * 2. Filters for words of exact length
     * 3. Limits to first 300 words for performance
     * 4. Converts to lowercase and creates NGrams
     */
    public static Corpus loadEnglishWords(int wordLength) {
        List<NGram> ngrams = new ArrayList<>();
        try {
            // 使用更小的词库URL
            URL url = new URL("https://raw.githubusercontent.com/dwyl/english-words/master/words_alpha.txt");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                ngrams = reader.lines()
                        .map(String::trim)
                        .filter(word -> word.length() == wordLength) // 筛选指定长度的单词
                        .map(String::toLowerCase)
                        .limit(300)  // 限制只加载前1000个符合条件的单词
                        .map(NGram::from)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Corpus corpus = Corpus.Builder.of().addAll(ngrams).build();
        return corpus;
    }

    /**
     * 测试各种评分函数。
     */
    public static void main(String[] args) {
        // 构造 5 个字母的词库
        Corpus corpus = loadEnglishWords(5);
        if (corpus == null) {
            System.out.println("Corpus is empty or inconsistent.");
            return;
        }
        System.out.println("Loaded corpus with " + corpus.size() + " 5-letter words.");

        // 选择一个猜测单词，比如 "route"
        NGram guess = NGram.from("abced");
        System.out.println("Testing score functions for guess: " + guess);

        // 测试 score(key, guess) －以 corpus 中某个候选 key 为例
        // 此处取 corpus 中第一个作为 key 示例
        //test score(key,guess) 
        NGram sampleKey = corpus.iterator().next();
        long score = corpus.score(sampleKey, guess);
        System.out.println("Score for key " + sampleKey + " and guess " + guess + " = " + score);

        // 测试 worst-case 分数（对于 guess，取所有候选 key 的最大 score）
        long worstCase = corpus.scoreWorstCase(guess);
        System.out.println("Worst-case score for guess " + guess + " = " + worstCase);

        // 测试 average-case 分数（所有候选 key 的 score 平均值）
        double avgCase = corpus.scoreAverageCase(guess);
        System.out.println("Average-case score for guess " + guess + " = " + avgCase);

        // 测试最佳猜测（根据 worst-case 以及 average-case 准则）
        NGram bestWorst = corpus.bestWorstCaseGuess();
        NGram bestAverage = corpus.bestAverageCaseGuess();
        System.out.println("Best worst-case guess: " + bestWorst);
        System.out.println("Best average-case guess: " + bestAverage);

        // 添加困难案例测试
        testHardCase();
    }

    /**
     * 测试困难案例的评分函数。
     */
    public static void testHardCase() {
        Corpus corpus = loadEnglishWords(5);
        if (corpus == null) {
            System.out.println("Corpus is empty or inconsistent.");
            return;
        }
        
        System.out.println("Loaded corpus with " + corpus.size() + " 5-letter words.");
        System.out.println("\n=== Testing Hard Case ===");
        System.out.println("Loaded special corpus with " + corpus.size() + " words.");

        // 选择 "where" 作为测试单词
        NGram guess = NGram.from("where");
        System.out.println("Testing score functions for guess: " + guess);

        // 测试每个可能的密钥的得分
        for (NGram key : corpus) {
            long score = corpus.score(key, guess);
            System.out.println("Score for key " + key + " and guess " + guess + " = " + score);
        }

        // 测试最佳猜测
        NGram bestWorst = corpus.bestWorstCaseGuess();
        System.out.println("Best worst-case guess: " + bestWorst);
    }
}
