import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

/**
 * Renders a single playing card as a Swing component.
 * White card with rank/suit text, red for hearts/diamonds.
 * Shows "??" face-down when hidden.
 */
public class CardPanel extends JPanel {
    private static final int W = 72;
    private static final int H = 100;

    private Card card;
    private boolean faceDown;

    public CardPanel() {
        this(null, true);
    }

    public CardPanel(Card card, boolean faceDown) {
        this.card = card;
        this.faceDown = faceDown;
        setPreferredSize(new Dimension(W, H));
        setOpaque(false);
    }

    public void setCard(Card card, boolean faceDown) {
        this.card = card;
        this.faceDown = faceDown;
        repaint();
    }

    public void reveal() {
        faceDown = false;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        RoundRectangle2D cardShape = new RoundRectangle2D.Float(2, 2, W - 4, H - 4, 12, 12);

        if (card == null) {
            // Empty slot – draw faint outline
            g2.setColor(new Color(255, 255, 255, 40));
            g2.fill(cardShape);
            g2.setColor(new Color(255, 255, 255, 80));
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(cardShape);
            g2.dispose();
            return;
        }

        if (faceDown) {
            // Card back – dark blue pattern
            g2.setColor(new Color(30, 60, 120));
            g2.fill(cardShape);
            g2.setColor(new Color(255, 255, 255, 60));
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(cardShape);
            // Draw "??" in center
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 20));
            FontMetrics fm = g2.getFontMetrics();
            String txt = "??";
            g2.drawString(txt, (W - fm.stringWidth(txt)) / 2, H / 2 + fm.getAscent() / 2 - 2);
        } else {
            // Card face – white background
            g2.setColor(Color.WHITE);
            g2.fill(cardShape);
            g2.setColor(new Color(180, 180, 180));
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(cardShape);

            String rankStr = rankLabel(card.getRank());
            String suitStr = suitSymbol(card.getSuit());
            boolean isRed = card.getSuit() == Card.Suit.HEARTS || card.getSuit() == Card.Suit.DIAMONDS;
            Color cardColor = isRed ? new Color(200, 30, 30) : new Color(20, 20, 20);

            // Top-left rank + suit
            g2.setColor(cardColor);
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2.drawString(rankStr, 6, 16);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.drawString(suitStr, 6, 28);

            // Bottom-right rank + suit (rotated)
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(rankStr, W - 6 - fm.stringWidth(rankStr), H - 18);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            fm = g2.getFontMetrics();
            g2.drawString(suitStr, W - 6 - fm.stringWidth(suitStr), H - 6);

            // Center suit symbol large
            g2.setFont(new Font("SansSerif", Font.PLAIN, 30));
            fm = g2.getFontMetrics();
            g2.drawString(suitStr, (W - fm.stringWidth(suitStr)) / 2, H / 2 + fm.getAscent() / 2 - 4);
        }
        g2.dispose();
    }

    private String rankLabel(Card.Rank r) {
        return switch (r) {
            case ACE   -> "A";
            case KING  -> "K";
            case QUEEN -> "Q";
            case JACK  -> "J";
            case TEN   -> "10";
            default    -> String.valueOf(r.getValue());
        };
    }

    private String suitSymbol(Card.Suit s) {
        return switch (s) {
            case HEARTS   -> "♥";
            case DIAMONDS -> "♦";
            case CLUBS    -> "♣";
            case SPADES   -> "♠";
        };
    }
}
