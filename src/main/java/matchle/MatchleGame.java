package matchle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MatchleGame {
    public static void main(String[] args) {
        

        Corpus corpus = CorpusLoader.loadEnglishWords(5);
        if (corpus == null || corpus.size() == 0) {
            System.out.println("Corpus is empty or invalid.");
            // 使用默认词库作为后备
            corpus = Corpus.Builder.of()
                    .add(NGram.from("rebus"))
                    .add(NGram.from("redux"))
                    .add(NGram.from("route"))
                    .add(NGram.from("hello"))
                    .build();
        }
        
        
        // 随机选择一个密钥
        List<NGram> keys = new ArrayList<>(corpus.corpus());
        Collections.shuffle(keys);
        NGram key = keys.get(0);
        
        // 为了游戏保密，实际运行中可能不显示 key，但这里为了调试显示出来
        System.out.println("Secret key (hidden): " + key);
        
        // 最大轮次
        int maxRounds = 10;
        
        // 初始时没有任何过滤条件，使用 Optional.empty()
        Optional<Filter> accumulatedFilter = Optional.empty();
        // 候选词库，初始为整个 corpus
        Corpus candidateCorpus = corpus;
        
        for (int round = 1; round <= maxRounds; round++) {
            System.out.println("==== Round " + round + " ====");
            // 从候选词库中选择最佳猜测（例如采用 worst-case 得分策略）
            NGram guess = candidateCorpus.bestWorstCaseGuess();
            System.out.println("Best guess: " + guess);
            
            // 如果猜测正确则结束游戏
            if (guess.equals(key)) {
                System.out.println("Guessed correctly! The key is: " + key);
                return;
            }
            
            // 生成本轮的过滤器（反馈）：将真实密钥与当前猜测匹配得到反馈
            Filter roundFilter = NGramMatcher.of(key, guess).match();
            System.out.println("Round filter: " + roundFilter);
            
            // 累积过滤器，使用逻辑 AND 组合（如果已有过滤器，则与本轮过滤器逻辑相与）
            if (accumulatedFilter.isPresent()) {
                accumulatedFilter = Optional.of(accumulatedFilter.get().and(Optional.of(roundFilter)));
            } else {
                accumulatedFilter = Optional.of(roundFilter);
            }
            
            // 根据累积过滤器更新候选词库
            Corpus newCorpus = Corpus.Builder.of(candidateCorpus)
                    .filter(accumulatedFilter.get())
                    .build();
            
            if (newCorpus == null) {
                System.out.println("No valid candidates remain. The key was: " + key);
                return;
            }
            candidateCorpus = newCorpus;
            
            System.out.println("Remaining candidate count: " + candidateCorpus.size());
            
            // 如果候选词库缩减到只剩一个，则直接结束
            if (candidateCorpus.size() == 1) {
                NGram remaining = candidateCorpus.corpus().iterator().next();
                System.out.println("Candidate corpus reduced to one: " + remaining);
                if (remaining.equals(key)) {
                    System.out.println("Found key: " + key);
                } else {
                    System.out.println("Remaining candidate does not match key. Key was: " + key);
                }
                return;
            }
            
            // 如果候选词库为空，则游戏失败
            if (candidateCorpus.size() == 0) {
                System.out.println("No candidates remain. The key was: " + key);
                return;
            }
        }
        
        System.out.println("Max rounds reached. The secret key was: " + key);
    }
}
