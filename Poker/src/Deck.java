import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a standard 52‑card deck.
 * Provides shuffle() and draw() operations used by PokerGameModel.
 */
public class Deck {
    private final List<Card> cards = new ArrayList<>();
    private int drawIndex = 0; // next card to draw

    public Deck() {
        // Populate the deck with one of each rank/suit combination
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
        shuffle(); // start with a random order
    }

    /** Randomly shuffles the deck and resets the draw pointer. */
    public void shuffle() {
        Collections.shuffle(cards);
        drawIndex = 0;
    }

    /**
     * Draws the next card from the deck.
     * @return the next {@link Card} or {@code null} if the deck is exhausted.
     */
    public Card draw() {
        if (drawIndex >= cards.size()) {
            return null; // no cards left
        }
        return cards.get(drawIndex++);
    }

    /** Returns how many cards remain in the deck. */
    public int cardsRemaining() {
        return cards.size() - drawIndex;
    }
}
