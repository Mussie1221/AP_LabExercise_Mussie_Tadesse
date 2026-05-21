import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Main Poker GUI – 5-Card Draw, Human vs Computer.
 *
 * Layout (top→bottom):
 *   NORTH  : info bar (chips / pot / result)
 *   CENTER : CPU panel | Community panel | Player panel
 *   SOUTH  : action buttons + message
 *
 * CPU cards stay face-down until showdown, then are revealed automatically.
 */
public class PokerGame extends JFrame {

    // ── Model ─────────────────────────────────────────────────────────────────
    private PokerGameModel model;
    private int playerChips  = 200;
    private int cpuChips     = 200;
    private int pot          = 0;
    private int currentBet   = 0;

    // ── GUI components ────────────────────────────────────────────────────────
    private PlayerPanel    cpuPanel;
    private PlayerPanel    playerPanel;
    private CommunityPanel communityPanel;

    private JLabel resultLabel; // shows winner
    private JLabel potLabel; // displays current pot
    private JLabel messageLabel;
    private JLabel currentBetLabel;

    private JButton dealButton;
    private JButton placeBetButton;
    private JButton drawPhaseButton;
    private JButton foldButton;
    private JPanel  betPanel;

    // ── Constructor ───────────────────────────────────────────────────────────
    public PokerGame() {
        super("5-Card Draw Poker  ·  Human vs Computer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(950, 600);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);

        buildUI();
        resetButtons();
    }

    // ── UI construction ───────────────────────────────────────────────────────
    private void buildUI() {
        // Root panel with a green poker-table gradient
        JPanel root = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(20, 80, 40),
                        getWidth(), getHeight(), new Color(10, 50, 25));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        setContentPane(root);

        // ── NORTH: info bar ──────────────────────────────────────────────────
        root.add(buildInfoBar(), BorderLayout.NORTH);

        // ── CENTER: three-column card area ───────────────────────────────────
        cpuPanel       = new PlayerPanel("CPU", true);
        communityPanel = new CommunityPanel();
        playerPanel    = new PlayerPanel("You", false);

        JPanel centerArea = new JPanel(new GridLayout(1, 3, 14, 0));
        centerArea.setOpaque(false);
        centerArea.add(cpuPanel);
        centerArea.add(communityPanel);
        centerArea.add(playerPanel);
        root.add(centerArea, BorderLayout.CENTER);

        // ── SOUTH: controls ──────────────────────────────────────────────────
        root.add(buildControls(), BorderLayout.SOUTH);
    }

    private JPanel buildInfoBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 6));
        bar.setOpaque(false);

        potLabel = styledLabel("Pot: 0", 16, Color.WHITE);
        resultLabel = styledLabel("", 16, new Color(255, 215, 0)); // gold for winner
        bar.add(styledLabel("♠  5-Card Draw Poker", 17, new Color(220, 200, 130)));
        bar.add(potLabel);
        bar.add(resultLabel);
        return bar;
    }

    private JPanel buildControls() {
        JPanel south = new JPanel(new BorderLayout(0, 6));
        south.setOpaque(false);

        // Bet chip panel
        betPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        betPanel.setOpaque(false);
        betPanel.add(styledLabel("Bet:", 13, Color.WHITE));
        for (int v : new int[]{5, 10, 25, 50}) {
            JButton b = actionButton("+" + v, new Color(180, 140, 40));
            b.addActionListener(e -> { if (currentBet + v <= playerChips) { currentBet += v; refreshBetLabel(); } });
            betPanel.add(b);
        }
        JButton clearBtn = actionButton("Clear", new Color(130, 50, 50));
        clearBtn.addActionListener(e -> { currentBet = 0; refreshBetLabel(); });
        betPanel.add(clearBtn);
        currentBetLabel = styledLabel("→ 0", 13, new Color(255, 220, 100));
        betPanel.add(currentBetLabel);
        placeBetButton = actionButton("Place Bet ✔", new Color(40, 140, 60));
        placeBetButton.addActionListener(e -> placeBet());
        betPanel.add(placeBetButton);
        betPanel.setVisible(false);

        // Main action row
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        actions.setOpaque(false);

        dealButton      = actionButton("Deal New Hand 🃏", new Color(40, 100, 180));
        drawPhaseButton = actionButton("Draw Cards ↩",    new Color(80, 60, 150));
        foldButton      = actionButton("Fold ✕",          new Color(160, 40, 40));

        dealButton.addActionListener(e      -> startRound());
        drawPhaseButton.addActionListener(e -> drawPhase());
        foldButton.addActionListener(e      -> fold());

        actions.add(dealButton);
        actions.add(drawPhaseButton);
        actions.add(foldButton);

        // Message label
        messageLabel = styledLabel("Welcome!  Click  Deal New Hand  to start.", 14, new Color(200, 255, 200));

        south.add(betPanel,      BorderLayout.NORTH);
        south.add(actions,       BorderLayout.CENTER);
        south.add(messageLabel,  BorderLayout.SOUTH);
        return south;
    }

    // ── Game logic ────────────────────────────────────────────────────────────
    private void startRound() {
        if (playerChips <= 0 || cpuChips <= 0) {
            messageLabel.setText(playerChips <= 0 ? "You're out of chips! Game over." : "CPU is out of chips! You win!");
            return;
        }

        // Reset model
        model = new PokerGameModel();
        model.addPlayer("You");
        model.addPlayer("CPU");

        // Deal 5 cards alternating
        for (int i = 0; i < 5; i++) {
            model.setCurrentPlayerIndex(0); model.drawForCurrentPlayer();
            model.setCurrentPlayerIndex(1); model.drawForCurrentPlayer();
        }

        // Reset pot & bet
        pot        = 0;
        currentBet = 0;
        refreshBetLabel();
        potLabel.setText("Pot: 0");

        // Clear hand-rank labels & community
        communityPanel.clear();
        cpuPanel.setHandRank(null);
        playerPanel.setHandRank(null);

        // Refresh displayed cards (CPU face-down)
        refreshHands(false);

        // Update chip labels
        cpuPanel.setChips(cpuChips);
        playerPanel.setChips(playerChips);

        // UI state
        betPanel.setVisible(true);
        drawPhaseButton.setEnabled(false);
        foldButton.setEnabled(false);
        dealButton.setEnabled(false);

        messageLabel.setText("Cards dealt!  Choose your bet, then click  Place Bet.");
    }

    private void placeBet() {
        if (currentBet == 0) { messageLabel.setText("Set a bet amount first."); return; }
        if (currentBet > playerChips) { messageLabel.setText("Not enough chips!"); return; }

        playerChips -= currentBet;
        pot         += currentBet;

        int cpuBet = Math.min((int)(Math.random() * 20) + 5, cpuChips);
        cpuChips   -= cpuBet;
        pot        += cpuBet;

        currentBet = 0;
        refreshBetLabel();
        potLabel.setText("Pot: " + pot);
        cpuPanel.setChips(cpuChips);
        playerPanel.setChips(playerChips);

        betPanel.setVisible(false);
        drawPhaseButton.setEnabled(true);
        foldButton.setEnabled(true);
        messageLabel.setText("Bet placed (CPU bet " + cpuBet + ").  Click  Draw Cards  or  Fold.");
    }

    private void drawPhase() {
        // Each player draws one replacement card
        model.setCurrentPlayerIndex(0); model.drawForCurrentPlayer();
        model.setCurrentPlayerIndex(1); model.drawForCurrentPlayer();

        // Evaluate hands
        List<Card> youHand  = model.getPlayers().get(0).getHand();
        List<Card> cpuHand  = model.getPlayers().get(1).getHand();
        HandRank   youRank  = HandEvaluator.evaluate(youHand);
        HandRank   cpuRank  = HandEvaluator.evaluate(cpuHand);

        // Reveal CPU cards
        refreshHands(true);

        // Show ranks
        playerPanel.setHandRank(youRank != null ? youRank.toString() : "");
        cpuPanel.setHandRank(cpuRank   != null ? cpuRank.toString()  : "");

        // Determine winner
        String msg;
        int cmp = (youRank != null && cpuRank != null) ? youRank.compareTo(cpuRank) : 0;
        if (cmp > 0)      { playerChips += pot; msg = "🎉 You win the pot of " + pot + " chips!"; }
        else if (cmp < 0) { cpuChips    += pot; msg = "Opponent wins the pot of " + pot + " chips."; }
        else              { int half = pot / 2; playerChips += half; cpuChips += half; msg = "Tie — pot split!"; }
        // Update result label with concise winner text
        resultLabel.setText(cmp > 0 ? "Player Wins" : cmp < 0 ? "CPU Wins" : "Tie");

        pot = 0;
        potLabel.setText("Pot: 0");
        cpuPanel.setChips(cpuChips);
        playerPanel.setChips(playerChips);

        messageLabel.setText(msg + "   Click  Deal New Hand  to play again.");
        drawPhaseButton.setEnabled(false);
        foldButton.setEnabled(false);
        dealButton.setEnabled(true);
    }

    private void fold() {
        cpuChips += pot;
        pot       = 0;
        potLabel.setText("Pot: 0");
        cpuPanel.setChips(cpuChips);

        // Reveal CPU cards anyway
        refreshHands(true);
        // Show winner as CPU (fold means CPU wins the pot)
        resultLabel.setText("CPU Wins");
        messageLabel.setText("You folded.  CPU takes the pot.   Click  Deal New Hand.");
        drawPhaseButton.setEnabled(false);
        foldButton.setEnabled(false);
        dealButton.setEnabled(true);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void refreshHands(boolean revealCpu) {
        if (model == null) return;
        List<Card> youHand = model.getPlayers().get(0).getHand();
        List<Card> cpuHand = model.getPlayers().get(1).getHand();
        playerPanel.updateHand(youHand, false);       // player always face-up
        cpuPanel.updateHand(cpuHand, !revealCpu);     // CPU hidden until showdown
    }

    private void refreshBetLabel() {
        currentBetLabel.setText("→ " + currentBet);
    }

    private void resetButtons() {
        dealButton.setEnabled(true);
        drawPhaseButton.setEnabled(false);
        foldButton.setEnabled(false);
        betPanel.setVisible(false);
    }

    // ── Styling helpers ───────────────────────────────────────────────────────
    private JLabel styledLabel(String text, int size, Color color) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, size));
        lbl.setForeground(color);
        return lbl;
    }

    private JButton actionButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isPressed()   ? bg.darker().darker()
                           : getModel().isRollover()  ? bg.brighter()
                           : bg;
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
        return btn;
    }

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new PokerGame().setVisible(true));
    }
}
