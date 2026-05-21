import java.util.*;

/**
 * Utility class that evaluates the best poker hand from a set of cards.
 * Supports standard Texas Hold'em hand rankings (high‑card through straight‑flush).
 * The evaluation returns a {@link HandRank} enum that is ordered from weakest
 * to strongest, so {@code compareTo} can be used to decide a winner.
 */
public class PokerHandEvaluator {

    /** Poker hand ranking in increasing strength order. */
    public enum HandRank {
        HIGH_CARD,
        ONE_PAIR,
        TWO_PAIR,
        THREE_OF_A_KIND,
        STRAIGHT,
        FLUSH,
        FULL_HOUSE,
        FOUR_OF_A_KIND,
        STRAIGHT_FLUSH;
    }

    /**
     * Evaluate the strongest 5‑card hand that can be made from the given cards.
     * The method assumes at least 5 cards are supplied; if fewer are provided
     * it falls back to HIGH_CARD.
     */
    public static HandRank evaluate(List<Card> cards) {
        if (cards == null || cards.size() < 5) {
            return HandRank.HIGH_CARD;
        }
        // Count ranks and suits
        Map<Card.Rank, Integer> rankCount = new HashMap<>();
        Map<Card.Suit, Integer> suitCount = new HashMap<>();
        List<Integer> rankValues = new ArrayList<>();
        for (Card c : cards) {
            rankCount.merge(c.getRank(), 1, Integer::sum);
            suitCount.merge(c.getSuit(), 1, Integer::sum);
            rankValues.add(c.getRank().getValue());
        }
        // Sort rank values for straight detection
        Collections.sort(rankValues);
        // Remove duplicates for straight check
        List<Integer> distinct = new ArrayList<>(new LinkedHashSet<>(rankValues));

        boolean isFlush = suitCount.values().stream().anyMatch(cnt -> cnt >= 5);
        boolean isStraight = hasStraight(distinct);

        // Check straight flush first
        if (isFlush && isStraight && containsStraightFlush(cards)) {
            return HandRank.STRAIGHT_FLUSH;
        }
        // Four of a kind
        if (rankCount.containsValue(4)) {
            return HandRank.FOUR_OF_A_KIND;
        }
        // Full house (3 + 2)
        boolean three = rankCount.containsValue(3);
        boolean pair = rankCount.values().stream().filter(v -> v >= 2).count() >= 2; // at least two pairs OR one pair + the three
        if (three && pair) {
            return HandRank.FULL_HOUSE;
        }
        // Flush
        if (isFlush) {
            return HandRank.FLUSH;
        }
        // Straight
        if (isStraight) {
            return HandRank.STRAIGHT;
        }
        // Three of a kind
        if (three) {
            return HandRank.THREE_OF_A_KIND;
        }
        // Two pair
        long pairCount = rankCount.values().stream().filter(v -> v >= 2).count();
        if (pairCount >= 2) {
            return HandRank.TWO_PAIR;
        }
        // One pair
        if (pairCount == 1) {
            return HandRank.ONE_PAIR;
        }
        // Default high card
        return HandRank.HIGH_CARD;
    }

    // Helper: straight detection on a sorted distinct list of rank values
    private static boolean hasStraight(List<Integer> distinct) {
        if (distinct.size() < 5) return false;
        // Check for any 5‑card consecutive sequence
        for (int i = 0; i <= distinct.size() - 5; i++) {
            int start = distinct.get(i);
            boolean ok = true;
            for (int j = 1; j < 5; j++) {
                if (!distinct.contains(start + j)) { ok = false; break; }
            }
            if (ok) return true;
        }
        // Ace‑low straight (A‑2‑3‑4‑5) – treat Ace as 14, add a virtual 1
        if (distinct.contains(14) && distinct.contains(2) && distinct.contains(3) && distinct.contains(4) && distinct.contains(5)) {
            return true;
        }
        return false;
    }

    // Helper: determine if there exists a straight flush among the cards
    private static boolean containsStraightFlush(List<Card> cards) {
        // Group cards by suit
        Map<Card.Suit, List<Card>> bySuit = new HashMap<>();
        for (Card c : cards) {
            bySuit.computeIfAbsent(c.getSuit(), k -> new ArrayList<>()).add(c);
        }
        // For each suit with at least 5 cards, check straight on that subset
        for (Map.Entry<Card.Suit, List<Card>> e : bySuit.entrySet()) {
            if (e.getValue().size() >= 5) {
                List<Integer> vals = new ArrayList<>();
                for (Card c : e.getValue()) vals.add(c.getRank().getValue());
                Collections.sort(vals);
                List<Integer> distinct = new ArrayList<>(new LinkedHashSet<>(vals));
                if (hasStraight(distinct)) return true;
            }
        }
        return false;
    }

    /**
     * Determine the winner between a player hand and a CPU hand given the community cards.
     * Returns one of "Player Wins", "CPU Wins", or "Tie".
     */
    public static String determineWinner(List<Card> playerHand, List<Card> cpuHand, List<Card> communityCards) {
        List<Card> playerCombined = new ArrayList<>();
        playerCombined.addAll(playerHand);
        if (communityCards != null) playerCombined.addAll(communityCards);
        List<Card> cpuCombined = new ArrayList<>();
        cpuCombined.addAll(cpuHand);
        if (communityCards != null) cpuCombined.addAll(communityCards);

        HandRank playerRank = evaluate(playerCombined);
        HandRank cpuRank    = evaluate(cpuCombined);

        int cmp = playerRank.compareTo(cpuRank);
        if (cmp > 0) return "Player Wins";
        if (cmp < 0) return "CPU Wins";
        return "Tie";
    }
}
