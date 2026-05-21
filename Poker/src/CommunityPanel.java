import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Displays up to 5 community cards centered between the two player panels.
 */
public class CommunityPanel extends JPanel {
    private final CardPanel[] communityCards = new CardPanel[5];

    public CommunityPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(4, 6));

        JLabel title = new JLabel("Community Cards", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(new Color(220, 200, 130));
        add(title, BorderLayout.NORTH);

        JPanel cardsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        cardsRow.setOpaque(false);
        for (int i = 0; i < 5; i++) {
            communityCards[i] = new CardPanel(null, false);
            cardsRow.add(communityCards[i]);
        }
        add(cardsRow, BorderLayout.CENTER);
    }

    /** Update visible community cards (null = empty slot) */
    public void updateCards(List<Card> cards) {
        for (int i = 0; i < 5; i++) {
            Card c = (cards != null && i < cards.size()) ? cards.get(i) : null;
            communityCards[i].setCard(c, false);
        }
        repaint();
    }

    /** Clear all community card slots */
    public void clear() {
        updateCards(null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
        g2.dispose();
    }
}
