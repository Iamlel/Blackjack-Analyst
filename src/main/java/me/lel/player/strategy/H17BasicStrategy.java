package me.lel.player.strategy;

import me.lel.core.Action;
import me.lel.core.Rules;

public class H17BasicStrategy implements Strategy {

    @Override
    public Action action(
            int hand,
            int dealerHand,
            boolean soft,
            boolean canSplit,
            Rules rules,
            double count
    ) {

        if (dealerHand == 1) {
            dealerHand += 10;
        }

        // ------------------------
        // LATE SURRENDER OPTIONS
        // ------------------------
        if (rules.isSurrenderAllowed() && !soft) {
            // surrender 17 on A
            if (hand == 17 && dealerHand == 11) return Action.SURRENDER;

            // surrender 16 vs 9,10,A
            if (hand == 16 && (dealerHand == 11 || (!canSplit && dealerHand >= 9))) {
                return Action.SURRENDER;
            }

            // surrender 15 vs 10,A
            if (hand == 15 && dealerHand >= 10)
                return Action.SURRENDER;

        }

        // ------------------------
        // PAIR SPLITTING
        // ------------------------
        if (canSplit) {
            switch (hand) {
                case 20:  // T,T
                    return Action.STAND;
                case 18:  // 9,9
                    if (dealerHand <= 9 && dealerHand != 7) return Action.SPLIT;
                    return Action.STAND;
                case 16:  // 8,8
                    return Action.SPLIT;
                case 14:  // 7,7
                    if (dealerHand <= 7) return Action.SPLIT;
                    break;
                case 12:  // 6,6
                    if (dealerHand <= 6)
                        return Action.SPLIT;
                    break;
                case 10: // 5,5
                    break;
                case 8:  // 4,4
                    if (dealerHand == 5 || dealerHand == 6)
                        return Action.SPLIT;
                    break;
                case 6:  // 3,3
                case 4:  // 2,2
                    if (dealerHand <= 7)
                        return Action.SPLIT;
                    break;
            }
        }

        // ------------------------
        // SOFT HANDS
        // ------------------------
        if (soft) {
            // Soft 20 (A,9)
            if (hand == 20) return Action.STAND;

            // Soft 19 (A,8)
            if (hand == 19) {
                if (dealerHand == 6) return Action.DOUBLE_STAND;
                return Action.STAND;
            }

            // Soft 18 (A,7)
            if (hand == 18) {
                if (dealerHand <= 6) return Action.DOUBLE_STAND;
                if (dealerHand <= 8) return Action.STAND;
                return Action.HIT;
            }

            // Soft 17 (A,6), Soft 16 (A,5), Soft 15 (A,4), Soft 14 (A,3), Soft 13 (A,2)
            if (hand >= 13 && hand <= 17) {
                // map soft totals to doubling ranges
                if (hand == 17) { // A,6
                    if (dealerHand >= 3 && dealerHand <= 6) return Action.DOUBLE;
                }
                if (hand == 15 || hand == 16) { // A,4 or A,5
                    if (dealerHand >= 4 && dealerHand <= 6) return Action.DOUBLE;
                }
                if (hand == 13 || hand == 14) { // A,2 or A,3
                    if (dealerHand >= 5 && dealerHand <= 6) return Action.DOUBLE;
                }
                return Action.HIT;
            }
        }

        // ------------------------
        // HARD HANDS
        // ------------------------
        // 17 and up
        if (hand >= 17) return Action.STAND;

        // Hard 11
        if (hand == 11) return Action.DOUBLE;

        // Hard 10
        if (hand == 10) {
            if (dealerHand <= 9) return Action.DOUBLE;
            return Action.HIT;
        }

        // Hard 9
        if (hand == 9) {
            if (dealerHand >= 3 && dealerHand <= 6) return Action.DOUBLE;
            return Action.HIT;
        }

        // Hard 12
        if (hand == 12) {
            if (dealerHand >= 4 && dealerHand <= 6) return Action.STAND;
            return Action.HIT;
        }

        // Hard 13–16
        if (hand >= 13 && hand <= 16) {
            if (dealerHand <= 6) return Action.STAND;
            return Action.HIT;
        }

        // Hard <= 8
        return Action.HIT;
    }

    @Override
    public boolean insurance(double count) {
        return false; // never take insurance in basic
    }
}
