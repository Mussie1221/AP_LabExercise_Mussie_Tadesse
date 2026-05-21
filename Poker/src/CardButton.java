import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;

public class CardButton extends JButton {
    private Card card;
    private static final ImageIcon CARD_ICON = new ImageIcon("cards/card_placeholder.png"); // placeholder image

    public CardButton(Card card) {
        super();
        setCard(card);
        setPreferredSize(new Dimension(80, 120));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorder(BorderFactory.createLineBorder(Color.GRAY, 2, true));
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // simple highlight animation on click
                Color original = getBackground();
                setBackground(new Color(70, 130, 180));
                Timer t = new Timer(150, ev -> setBackground(original));
                t.setRepeats(false);
                t.start();
                // Show current card info in tooltip
                setToolTipText(card != null ? card.toString() : "");
            }
        });
    }

    public void setCard(Card card) {
        this.card = card;
        if (card != null) {
            setIcon(CARD_ICON);
            setText("");
        } else {
            setIcon(null);
            setText("?");
        }
    }

    public Card getCard() {
        return card;
    }
}

