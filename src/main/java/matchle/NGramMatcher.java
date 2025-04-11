package matchle;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

final class NGramMatcher {
    private final NGram key;
    private final NGram guess;

    private NGramMatcher(NGram key, NGram guess) {
        this.key = key;
        this.guess = guess;
    }

    public static NGramMatcher of(NGram key, NGram guess) {
        // 长度不同仍返回 matcher，在 match() 中会处理
        return new NGramMatcher(key, guess);
    }

    public Filter match() {
        if (key.size() != guess.size()) {
            return Filter.FALSE;
        }
        boolean[] keyMatched = new boolean[key.size()];
        boolean[] guessMatched = new boolean[guess.size()];

        Map<Integer, Character> correctMatches = computeCorrectMatches(keyMatched, guessMatched);
        Set<IndexedCharacter> misplacedMatches = computeMisplacedMatches(keyMatched, guessMatched);
        Set<Character> absentLetters = computeAbsentLetters(guessMatched);

        return buildFilter(correctMatches, misplacedMatches, absentLetters);
    }
    
    // 调试方法，打印详细的匹配信息
    public String debugMatch() {
        if (key.size() != guess.size()) {
            return "Size mismatch between key and guess.";
        }
        boolean[] keyMatched = new boolean[key.size()];
        boolean[] guessMatched = new boolean[guess.size()];
        
        Map<Integer, Character> correctMatches = computeCorrectMatches(keyMatched, guessMatched);
        Set<IndexedCharacter> misplacedMatches = computeMisplacedMatches(keyMatched, guessMatched);
        Set<Character> absentLetters = computeAbsentLetters(guessMatched);
        
        StringBuilder sb = new StringBuilder();
        sb.append("Key: ").append(key).append("\n");
        sb.append("Guess: ").append(guess).append("\n");
        sb.append("Correct Matches: ").append(correctMatches).append("\n");
        sb.append("Misplaced Matches: ").append(misplacedMatches).append("\n");
        sb.append("Absent Letters: ").append(absentLetters).append("\n");
        return sb.toString();
    }

    private Map<Integer, Character> computeCorrectMatches(boolean[] keyMatched, boolean[] guessMatched) {
        Map<Integer, Character> correctMatches = new HashMap<>();
        for (int i = 0; i < key.size(); i++) {
            if (key.get(i).equals(guess.get(i))) {
                correctMatches.put(i, key.get(i));
                keyMatched[i] = true;
                guessMatched[i] = true;
            }
        }
        return correctMatches;
    }

    // 提取辅助方法：在 key 中查找第 guessIndex 位未匹配且等于 guess.get(guessIndex) 的字符索引
    private int findMisplacedMatchIndex(int guessIndex, boolean[] keyMatched) {
        for (int j = 0; j < key.size(); j++) {
            if (!keyMatched[j] && key.get(j).equals(guess.get(guessIndex))) {
                return j;
            }
        }
        return -1;
    }

    /**
     * Computes misplaced matches between key and guess.
     * A match is considered misplaced if the character exists in the key
     * but at a different position.
     */
    private Set<IndexedCharacter> computeMisplacedMatches(boolean[] keyMatched, boolean[] guessMatched) {
        Set<IndexedCharacter> misplacedMatches = new HashSet<>();
        for (int i = 0; i < guess.size(); i++) {
            if (!guessMatched[i]) {
                int j = findMisplacedMatchIndex(i, keyMatched);
                if (j != -1) {
                    misplacedMatches.add(new IndexedCharacter(i, guess.get(i)));
                    keyMatched[j] = true;
                    guessMatched[i] = true;
                }
            }
        }
        return misplacedMatches;
    }

    private Set<Character> computeAbsentLetters(boolean[] guessMatched) {
        Set<Character> absentLetters = new HashSet<>();
        for (int i = 0; i < guess.size(); i++) {
            if (!guessMatched[i]) {
                absentLetters.add(guess.get(i));
            }
        }
        return absentLetters;
    }

    private String buildDisplayPattern(Map<Integer, Character> correctMatches) {
        char[] display = new char[key.size()];
        Arrays.fill(display, '_');
        correctMatches.forEach((k, v) -> display[k] = v);
        return new String(display);
    }

    private String formatMisplacedLetters(Set<IndexedCharacter> misplacedMatches) {
        if (misplacedMatches.isEmpty()) {
            return "";
        }
        return ", Misplaced: " + misplacedMatches.stream()
            .map(IndexedCharacter::character)
            .map(String::valueOf)
            .collect(Collectors.joining());
    }

    private String formatAbsentLetters(Set<Character> absentLetters) {
        if (absentLetters.isEmpty()) {
            return "";
        }
        return ", Absent: " + absentLetters.stream()
            .map(String::valueOf)
            .collect(Collectors.joining());
    }

    private Filter buildFilter(Map<Integer, Character> correctMatches,
                             Set<IndexedCharacter> misplacedMatches,
                             Set<Character> absentLetters) {
        String pattern = "Correct: " + buildDisplayPattern(correctMatches)
            + formatMisplacedLetters(misplacedMatches)
            + formatAbsentLetters(absentLetters);

        return Filter.from(ngram -> 
            correctMatches.entrySet().stream().allMatch(e -> ngram.get(e.getKey()).equals(e.getValue()))
            && misplacedMatches.stream().allMatch(ngram::containsElsewhere)
            && absentLetters.stream().noneMatch(ngram::contains)
        ).withPattern(pattern);
    }
}
