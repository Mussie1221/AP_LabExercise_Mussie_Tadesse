import java.util.ArrayList;
import java.util.List;

/**
 * Simple model that aggregates the deck and player list for the demo UI.
 * It tracks which player is currently active so the UI can draw cards for them.
 */
public class PokerGameModel {
    private final List<Card> communityCards;
    private final Deck deck;

    public List<Card> getCommunityCards() {
        return communityCards;
    }

    /** Draw n community cards and add them to the list */
    public void dealCommunity(int count) {
        for (int i = 0; i < count; i++) {
            Card c = deck.draw();
            if (c != null) {
                communityCards.add(c);
            }
        }
    }
    private final List<Player> players;
    private int currentPlayerIndex = 0;

    public PokerGameModel() {
        this.deck = new Deck();
        this.communityCards = new ArrayList<>();
        this.players = new ArrayList<>();
    }

    /** Adds a new player with the given name. */
    public void addPlayer(String name) {
        players.add(new Player(name));
    }

    /** Returns the mutable list of players (used by UI). */
    public List<Player> getPlayers() {
        return players;
    }

    /** Returns the underlying deck (for evaluation). */
    public Deck getDeck() {
        return deck;
    }

    /** Sets the index of the player that is currently active. */
    public void setCurrentPlayerIndex(int idx) {
        if (idx >= 0 && idx < players.size()) {
            currentPlayerIndex = idx;
        }
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    /** Draws a card for the current player, adds it to their hand and returns it. */
    public Card drawForCurrentPlayer() {
        Card card = deck.draw();
        if (card != null) {
            players.get(currentPlayerIndex).addCard(card);
        }
        return card;
    }

    /** Shuffles the deck and resets the draw position. */
    public void shuffleDeck() {
        deck.shuffle();
    }
}
