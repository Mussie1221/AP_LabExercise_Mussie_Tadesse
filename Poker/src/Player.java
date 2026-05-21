import java.util.ArrayList;
import java.util.List;

public class Player {
    private final String name;
    private final List<Card> hand;

    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Card> getHand() {
        return hand;
    }

    /** Adds a card to the player's hand. */
    public void addCard(Card card) {
        if (card != null) {
            hand.add(card);
        }
    }

    /** Clears all cards from the player's hand. */
    public void clearHand() {
        hand.clear();
    }

    @Override
    public String toString() {
        return name;
    }
}
