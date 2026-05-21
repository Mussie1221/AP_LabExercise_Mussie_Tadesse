import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * A panel that shows a player's name, chip count, and up to 5 card panels.
 * faceDown controls whether all cards are rendered back-face.
 */
public class PlayerPanel extends JPanel {
    private final String playerName;
    private final boolean isCpu;
    private final CardPanel[] cardPanels = new CardPanel[5];
    private final JLabel nameLabel;
    private final JLabel chipsLabel;
    private final JLabel handRankLabel;

    public PlayerPanel(String playerName, boolean isCpu) {
        this.playerName = playerName;
        this.isCpu = isCpu;
        setOpaque(false);
        setLayout(new BorderLayout(4, 6));

        // Name + chips header
        JPanel header = new JPanel(new GridLayout(3, 1, 0, 2));
        header.setOpaque(false);
        nameLabel = new JLabel(playerName, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLabel.setForeground(Color.WHITE);

        chipsLabel = new JLabel("Chips: 200", SwingConstants.CENTER);
        chipsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chipsLabel.setForeground(new Color(220, 200, 130));

        handRankLabel = new JLabel(" ", SwingConstants.CENTER);
        handRankLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        handRankLabel.setForeground(new Color(180, 255, 180));

        header.add(nameLabel);
        header.add(chipsLabel);
        header.add(handRankLabel);
        add(header, BorderLayout.NORTH);

        // Cards row
        JPanel cardsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        cardsRow.setOpaque(false);
        for (int i = 0; i < 5; i++) {
            cardPanels[i] = new CardPanel(null, isCpu);
            cardsRow.add(cardPanels[i]);
        }
        add(cardsRow, BorderLayout.CENTER);
    }

    /** Refresh card panels from a list of Card objects */
    public void updateHand(List<Card> hand, boolean faceDown) {
        for (int i = 0; i < 5; i++) {
            Card c = (i < hand.size()) ? hand.get(i) : null;
            cardPanels[i].setCard(c, faceDown);
        }
        repaint();
    }

    /** Reveal all CPU cards at showdown */
    public void revealAll() {
        for (CardPanel cp : cardPanels) cp.reveal();
    }

    public void setChips(int chips) {
        chipsLabel.setText("Chips: " + chips);
    }

    public void setHandRank(String rank) {
        handRankLabel.setText(rank == null ? " " : rank);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
        g2.dispose();
    }
}
