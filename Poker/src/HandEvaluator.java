import java.util.*;
import java.util.stream.Collectors;

/**
 * Evaluates a hand of cards and returns the best HandRank.
 * Returns null if fewer than 5 cards are provided.
 */
public class HandEvaluator {
    public static HandRank evaluate(List<Card> cards) {
        if (cards == null || cards.size() < 5) return null;
        // Use only the first 5 cards for simplicity (can be extended to best‑5 of N).
        List<Card> hand = cards.subList(0, 5);
        Map<Card.Rank, Long> rankCounts = hand.stream()
                .collect(Collectors.groupingBy(Card::getRank, Collectors.counting()));
        Map<Card.Suit, Long> suitCounts = hand.stream()
                .collect(Collectors.groupingBy(Card::getSuit, Collectors.counting()));
        boolean flush = suitCounts.values().stream().anyMatch(c -> c == 5);
        List<Integer> values = hand.stream()
                .map(c -> c.getRank().getValue())
                .sorted()
                .collect(Collectors.toList());
        boolean straight = isStraight(values);
        if (straight && flush && values.get(4) == Card.Rank.ACE.getValue()) return HandRank.ROYAL_FLUSH;
        if (straight && flush) return HandRank.STRAIGHT_FLUSH;
        if (rankCounts.containsValue(4L)) return HandRank.FOUR_OF_KIND;
        if (rankCounts.containsValue(3L) && rankCounts.containsValue(2L)) return HandRank.FULL_HOUSE;
        if (flush) return HandRank.FLUSH;
        if (straight) return HandRank.STRAIGHT;
        if (rankCounts.containsValue(3L)) return HandRank.THREE_OF_KIND;
        long pairCount = rankCounts.values().stream().filter(v -> v == 2L).count();
        if (pairCount == 2) return HandRank.TWO_PAIR;
        if (pairCount == 1) return HandRank.ONE_PAIR;
        return HandRank.HIGH_CARD;
    }

    private static boolean isStraight(List<Integer> vals) {
        List<Integer> distinct = vals.stream().distinct().collect(Collectors.toList());
        if (distinct.size() != 5) return false;
        Collections.sort(distinct);
        boolean consecutive = true;
        for (int i = 1; i < distinct.size(); i++) {
            if (distinct.get(i) - distinct.get(i - 1) != 1) { consecutive = false; break; }
        }
        if (consecutive) return true;
        // Ace low straight (A-2-3-4-5)
        if (distinct.contains(Card.Rank.ACE.getValue())) {
            List<Integer> lowAce = distinct.stream()
                    .map(v -> v == Card.Rank.ACE.getValue() ? 1 : v)
                    .sorted()
                    .collect(Collectors.toList());
            for (int i = 1; i < lowAce.size(); i++) {
                if (lowAce.get(i) - lowAce.get(i - 1) != 1) return false;
            }
            return true;
        }
        return false;
    }
}
