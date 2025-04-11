package matchle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 一个简单的 Swing GUI 版 Matchle 游戏
 */
public class MatchleGUI extends JFrame {

    // 游戏相关数据
    private Corpus corpus;             // 原始词库
    private NGram secretKey;           // 随机选出的密钥
    private Corpus candidateCorpus;    // 当前候选词库（不断缩小）
    private Filter accumulatedFilter;  // 累积的反馈过滤器（初始为 null）

    // UI 控件
    private JTextField guessField;
    private JTextArea feedbackArea;
    private JLabel candidateLabel;
    private JLabel bestGuessLabel;
    private JButton submitButton;
    private JButton newGameButton;

    public MatchleGUI() {
        super("Matchle Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        initUI();
        startNewGame();
    }

    private void initUI() {
        // 顶部面板：输入和按钮
        JPanel topPanel = new JPanel(new FlowLayout());
        JLabel guessLabel = new JLabel("Enter guess (5 letters): ");
        guessField = new JTextField(10);
        submitButton = new JButton("Submit Guess");
        newGameButton = new JButton("New Game");
        topPanel.add(guessLabel);
        topPanel.add(guessField);
        topPanel.add(submitButton);
        topPanel.add(newGameButton);

        // 中间文本区域：显示反馈信息
        feedbackArea = new JTextArea(10, 50);
        feedbackArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(feedbackArea);

        // 底部面板：显示候选词数量和最佳猜测建议
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1));
        candidateLabel = new JLabel("Remaining candidates: ");
        bestGuessLabel = new JLabel("Best guess suggestion: ");
        bottomPanel.add(candidateLabel);
        bottomPanel.add(bestGuessLabel);

        // 布局整合
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // 按钮事件绑定
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitGuess();
            }
        });
        newGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startNewGame();
            }
        });
    }

    /**
     * 开始一局新游戏：重置词库、随机选择密钥、清空累积过滤器等
     */
    private void startNewGame() {
        // load the corpus
        // 使用 CorpusLoader 加载 5 个字母的词库
        corpus = CorpusLoader.loadEnglishWords(5);
        if (corpus == null || corpus.size() == 0) {
            JOptionPane.showMessageDialog(this, "Failed to load corpus. Using default corpus instead.", "Warning", JOptionPane.WARNING_MESSAGE);
            // 使用默认词库作为后备
            // use the default corpus as a backup
            corpus = Corpus.Builder.of()
                    .add(NGram.from("rebus"))
                    .add(NGram.from("redux"))
                    .add(NGram.from("route"))
                    .add(NGram.from("hello"))
                    .build();
        }

        // choose a secret key from the corpus
        // 从 corpus 随机选取一个密钥
        List<NGram> keyList = new ArrayList<>(corpus.corpus());
        Collections.shuffle(keyList);
        secretKey = keyList.get(0);
        // 重置候选词库和累积过滤器
        candidateCorpus = corpus;
        accumulatedFilter = null;
        feedbackArea.setText("");
        candidateLabel.setText("Remaining candidates: " + candidateCorpus.size());
        bestGuessLabel.setText("Best guess suggestion: " + candidateCorpus.bestWorstCaseGuess());
        guessField.setText("");
        submitButton.setEnabled(true);
        // 为了调试，可以在这里打印密钥，实际游戏中可隐藏
        System.out.println("Secret key: " + secretKey);
    }

    /**
     * Handles the logic when a guess is submitted.
     */
    private void submitGuess() {
        String guessStr = validateAndGetGuess();
        if (guessStr == null) return;
        
        NGram guess = NGram.from(guessStr);
        displayGuess(guessStr);
        
        if (checkForWin(guess)) return;
        
        updateGameState(guess);
    }

    // validate the guess
    private String validateAndGetGuess() {
        String guessStr = guessField.getText().trim().toLowerCase();
        if (guessStr.length() != 5) {
            JOptionPane.showMessageDialog(this, "Please enter a 5-letter word.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return guessStr;
    }

    // display the guess
    private void displayGuess(String guessStr) {
        feedbackArea.append("Your guess: " + guessStr + "\n");
    }

    // check if the guess is the secret key
    private boolean checkForWin(NGram guess) {
        if (guess.equals(secretKey)) {
            feedbackArea.append("Congratulations! You guessed the key: " + secretKey.toString() + "\n");
            submitButton.setEnabled(false);
            return true;
        }
        return false;
    }

    /**
     * Updates the game state with the new guess
     */
    private void updateGameState(NGram guess) {
        Filter roundFilter = generateRoundFilter(guess);
        updateAccumulatedFilter(roundFilter);
        updateCandidateCorpus();
        displayCandidates();
        updateLabels();
    }

    /**
     * Generates a filter based on the current guess and secret key
     */
    private Filter generateRoundFilter(NGram guess) {
        Filter roundFilter = NGramMatcher.of(secretKey, guess).match();
        feedbackArea.append("Round filter pattern: " + roundFilter.toString() + "\n");
        return roundFilter;
    }

    /**
     * Updates the accumulated filter with the new round filter
     */
    private void updateAccumulatedFilter(Filter roundFilter) {
        if (accumulatedFilter != null) {
            accumulatedFilter = accumulatedFilter.and(Optional.of(roundFilter));
        } else {
            accumulatedFilter = roundFilter;
        }
    }

    /**
     * Updates the candidate corpus based on the accumulated filter
     */
    private void updateCandidateCorpus() {
        candidateCorpus = Corpus.Builder.of(candidateCorpus).filter(accumulatedFilter).build();
        if (candidateCorpus == null || candidateCorpus.size() == 0) {
            feedbackArea.append("No candidates remain. The key was: " + secretKey.toString() + "\n");
            submitButton.setEnabled(false);
        }
    }

    /**
     * Displays the current candidates in the feedback area
     */
    private void displayCandidates() {
        if (candidateCorpus == null) return;
        String candidates = candidateCorpus.corpus().stream()
            .map(NGram::toString)
            .collect(Collectors.joining(", "));
        feedbackArea.append("Candidates: [" + candidates + "]\n\n");
    }

    /**
     * Updates the UI labels with current game state
     */
    private void updateLabels() {
        if (candidateCorpus != null) {
            candidateLabel.setText("Remaining candidates: " + candidateCorpus.size());
            bestGuessLabel.setText("Best guess suggestion: " + candidateCorpus.bestWorstCaseGuess());
        }
        guessField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MatchleGUI gui = new MatchleGUI();
            gui.setVisible(true);
        });
    }
}
