package me.lel.core;

/**
 * Table rules for a game of blackjack. Build them with {@link Builder}, whose methods explain each rule and list its
 * default.
 */
public class Rules {
    private final int minimumBet;
    private final int maximumBet;
    private final double penetration;
    private final boolean h17;
    private final boolean das;
    private final boolean sas;
    private final int splitAmount;
    private final boolean insuranceAllowed;
    private final double insurancePay;
    private final double blackjackPay;
    private final int maxHands;
    private final boolean lateSurrender;
    private final boolean earlySurrender;
    private final boolean reSplitAces;
    private final boolean hitSplitAces;
    private final boolean doubleSplitAces;

    /**
     * Copies the builder's settings. {@link Builder#build()} calls this for you.
     *
     * @param builder the settings to copy
     */
    public Rules(Builder builder) {
        this.minimumBet = builder.minimumBet;
        this.maximumBet = builder.maximumBet;
        this.penetration = builder.penetration;
        this.h17 = builder.h17;
        this.das = builder.das;
        this.sas = builder.sas;
        this.splitAmount = builder.splitAmount;
        this.insuranceAllowed = builder.insuranceAllowed;
        this.insurancePay = builder.insurancePay;
        this.blackjackPay = builder.blackjackPay;
        this.maxHands = builder.maxHands;
        this.lateSurrender = builder.lateSurrender;
        this.earlySurrender = builder.earlySurrender;
        this.reSplitAces = builder.reSplitAces;
        this.hitSplitAces = builder.hitSplitAces || builder.doubleSplitAces;
        this.doubleSplitAces = builder.doubleSplitAces;
    }

    /**
     * Returns the table minimum, the smallest bet allowed on a hand.
     *
     * @return the minimum bet
     */
    public int getMinimumBet() {
        return minimumBet;
    }

    /**
     * Returns the table maximum, the largest bet allowed on one hand.
     *
     * @return the maximum bet
     */
    public int getMaximumBet() {
        return maximumBet;
    }

    /**
     * Returns the fraction of the shoe dealt before it is reshuffled. See {@link Builder#penetration(double)}.
     *
     * @return the penetration, above 0 and at most 1
     */
    public double getPenetration() {
        return penetration;
    }

    /**
     * Returns whether the dealer hits soft 17. See {@link Builder#h17(boolean)}.
     *
     * @return {@code true} if the dealer hits soft 17
     */
    public boolean isH17() {
        return h17;
    }

    /**
     * Returns whether doubling after a split is allowed.
     *
     * @return {@code true} if doubling after a split is allowed
     */
    public boolean isDas() {
        return das;
    }

    /**
     * Returns whether surrendering after a split is allowed.
     *
     * @return {@code true} if surrendering after a split is allowed
     */
    public boolean isSas() {
        return sas;
    }

    /**
     * Returns how many times one player may split in a round, counted across all of their hands.
     *
     * @return the number of splits allowed
     */
    public int getSplitAmount() {
        return splitAmount;
    }

    /**
     * Returns the insurance payout as a multiple of the insurance bet, so 2 means 2:1.
     *
     * @return the insurance payout multiple
     */
    public double getInsurancePay() {
        return insurancePay;
    }

    /**
     * Returns the blackjack payout as a multiple of the bet, so 1.5 means 3:2.
     *
     * @return the blackjack payout multiple
     */
    public double getBlackjackPay() {
        return blackjackPay;
    }

    /**
     * Returns the most hands one player may be dealt at the start of a round. Hands created by splitting don't count.
     *
     * @return the most starting hands per player
     */
    public int getMaxHands() {
        return maxHands;
    }

    /**
     * Returns whether late surrender is allowed. See {@link Builder#lateSurrender(boolean)}.
     *
     * @return {@code true} if late surrender is allowed
     */
    public boolean isLateSurrender() {
        return lateSurrender;
    }

    /**
     * Returns whether early surrender is allowed. See {@link Builder#earlySurrender(boolean)}.
     *
     * @return {@code true} if early surrender is allowed
     */
    public boolean isEarlySurrender() {
        return earlySurrender;
    }

    /**
     * Returns whether insurance is offered when the dealer shows an ace. See {@link Builder#insurancePay(double)}.
     *
     * @return {@code true} if insurance is offered
     */
    public boolean isInsuranceAllowed() {
        return insuranceAllowed;
    }

    /**
     * Returns whether split aces may be split again.
     *
     * @return {@code true} if split aces may be split again
     */
    public boolean isReSplitAces() {
        return reSplitAces;
    }

    /**
     * Returns whether split aces may take more cards. This is always true when {@link #isDoubleSplitAces()} is.
     *
     * @return {@code true} if split aces may hit
     */
    public boolean isHitSplitAces() {
        return hitSplitAces;
    }

    /**
     * Returns whether split aces may double.
     *
     * @return {@code true} if split aces may double
     */
    public boolean isDoubleSplitAces() {
        return doubleSplitAces;
    }

    @Override
    public String toString() {
        return "Rules"
                + "\nMinimum Bet: " + this.minimumBet
                + "\nMaximum Bet: " + this.maximumBet
                + "\nPenetration: " + this.penetration
                + "\nHit 17: " + this.h17
                + "\nDouble After Split: " + this.das
                + "\nSurrender After Split: " + this.sas
                + "\nSplit Amount: " + this.splitAmount
                + "\nInsurance Allowed?: " + this.insuranceAllowed
                + "\nInsurance Multiplier: " + this.insurancePay
                + "\nBlackjack Multiplier: " + this.blackjackPay
                + "\nMaximum Hands: " + this.maxHands
                + "\nLate Surrender: " + this.lateSurrender
                + "\nEarly Surrender: " + this.earlySurrender
                + "\nResplit Aces Allowed?: " + this.reSplitAces
                + "\nHit Split Aces Allowed?: " + this.hitSplitAces
                + "\nDouble Split Aces Allowed?: " + this.doubleSplitAces;
    }

    /**
     * Returns rules with every setting at its {@link Builder} default.
     *
     * @return the default rules
     */
    public static Rules buildDefault() {
        return new Builder().build();
    }

    /**
     * Builds {@link Rules}. Each setting starts at the default given on its method, so you only set the rules that
     * differ.
     */
    public static class Builder {
        private int minimumBet = 10;
        private int maximumBet = 1000;
        private double penetration = 0.83;
        private boolean h17 = true;
        private boolean das = true;
        private boolean sas = false;
        private int splitAmount = 3;
        private boolean insuranceAllowed = true;
        private double insurancePay = 2;
        private double blackjackPay = (double) 3 / 2;
        private int maxHands = 2;
        private boolean lateSurrender = true;
        private boolean earlySurrender = false;
        private boolean reSplitAces = false;
        private boolean hitSplitAces = false;
        private boolean doubleSplitAces = false;

        /**
         * Sets the table minimum, the smallest bet allowed on a hand. Defaults to 10.
         *
         * @param minimumBet the minimum bet, at least 1
         * @return this builder
         * @throws IllegalArgumentException if {@code minimumBet} is less than 1
         */
        public Builder minimumBet(int minimumBet) {
            if (minimumBet < 1) {
                throw new IllegalArgumentException("Minimum bet cannot be less than 1.");
            }
            this.minimumBet = minimumBet;
            return this;
        }

        /**
         * Sets the table maximum, the largest bet allowed on one hand. Defaults to 1000.
         *
         * @param maximumBet the maximum bet, at least 1
         * @return this builder
         * @throws IllegalArgumentException if {@code maximumBet} is less than 1
         */
        public Builder maximumBet(int maximumBet) {
            if (maximumBet < 1) {
                throw new IllegalArgumentException("Maximum bet cannot be less than 1.");
            }
            this.maximumBet = maximumBet;
            return this;
        }

        /**
         * Sets the penetration: the fraction of the shoe dealt before it is reshuffled. Deeper penetration helps card
         * counters, because the count says the most about the cards left near the end of the shoe. Defaults to 0.83.
         *
         * @param penetration the fraction dealt, above 0 and at most 1
         * @return this builder
         * @throws IllegalArgumentException if {@code penetration} is 0 or less, or greater than 1
         */
        public Builder penetration(double penetration) {
            if (penetration > 1 || penetration <= 0) {
                throw new IllegalArgumentException("Penetration must be between 0 and 1.");
            }
            this.penetration = penetration;
            return this;
        }

        /**
         * Sets whether the dealer hits soft 17. A soft 17 is a 17 that counts an ace as 11, such as an ace and a 6. The
         * dealer always stands on 18 or more and on a hard 17, so this rule only decides whether they draw to a soft
         * 17. Hitting soft 17 is slightly worse for the player. Defaults to {@code true}.
         *
         * @param h17 {@code true} if the dealer hits soft 17
         * @return this builder
         */
        public Builder h17(boolean h17) {
            this.h17 = h17;
            return this;
        }

        /**
         * Sets whether a player may double after splitting. Doubling means doubling the bet in exchange for exactly one
         * more card. Defaults to {@code true}.
         *
         * @param das {@code true} if doubling after a split is allowed
         * @return this builder
         */
        public Builder das(boolean das) {
            this.das = das;
            return this;
        }

        /**
         * Sets whether a player may surrender a hand that came from a split. Defaults to {@code false}.
         *
         * @param sas {@code true} if surrendering after a split is allowed
         * @return this builder
         */
        public Builder sas(boolean sas) {
            this.sas = sas;
            return this;
        }

        /**
         * Sets how many times one player may split in a round, counted across all of their hands. Splitting turns a
         * pair into two hands, each with a bet equal to the original. Defaults to 3, so a single hand can become at
         * most four. Use 0 to turn splitting off.
         *
         * @param splitAmount the number of splits allowed, at least 0
         * @return this builder
         * @throws IllegalArgumentException if {@code splitAmount} is negative
         */
        public Builder splitAmount(int splitAmount) {
            if (splitAmount < 0) {
                throw new IllegalArgumentException("Split amount cannot be negative.");
            }
            this.splitAmount = splitAmount;
            return this;
        }

        /**
         * Sets the insurance payout as a multiple of the insurance bet. Insurance is a side bet, offered when the
         * dealer shows an ace, that pays if the dealer has blackjack. Defaults to 2, which is 2:1.
         *
         * @param insurancePay the payout multiple, above 0
         * @return this builder
         * @throws IllegalArgumentException if {@code insurancePay} is 0 or less
         */
        public Builder insurancePay(double insurancePay) {
            if (insurancePay <= 0) {
                throw new IllegalArgumentException("Insurance pay must be greater than 0.");
            }
            this.insurancePay = insurancePay;
            return this;
        }

        /**
         * Sets the blackjack payout as a multiple of the bet. A blackjack is an ace and a ten-value card as a hand's
         * first two cards. Defaults to 1.5, which is 3:2.
         *
         * @param blackjackPay the payout multiple, above 0
         * @return this builder
         * @throws IllegalArgumentException if {@code blackjackPay} is 0 or less
         */
        public Builder blackjackPay(double blackjackPay) {
            if (blackjackPay <= 0) {
                throw new IllegalArgumentException("Blackjack pay must be greater than 0.");
            }
            this.blackjackPay = blackjackPay;
            return this;
        }

        /**
         * Sets the most hands one player may be dealt at the start of a round, like playing several spots at the table.
         * Hands created by splitting don't count. Defaults to 2.
         *
         * @param maxHands the most starting hands per player, at least 1
         * @return this builder
         * @throws IllegalArgumentException if {@code maxHands} is less than 1
         */
        public Builder maxHands(int maxHands) {
            if (maxHands < 1) {
                throw new IllegalArgumentException("At least one hand must be allowed.");
            }
            this.maxHands = maxHands;
            return this;
        }

        /**
         * Sets whether late surrender is allowed. Surrendering gives up a hand in exchange for half the bet back. Late
         * surrender is only offered after the dealer checks for blackjack, so it doesn't help against a dealer
         * blackjack. Defaults to {@code true}.
         *
         * @param lateSurrender {@code true} if late surrender is allowed
         * @return this builder
         */
        public Builder lateSurrender(boolean lateSurrender) {
            this.lateSurrender = lateSurrender;
            return this;
        }

        /**
         * Sets whether early surrender is allowed. Early surrender is offered before the dealer checks for blackjack,
         * so it also saves half the bet when the dealer has one. Defaults to {@code false}.
         *
         * @param earlySurrender {@code true} if early surrender is allowed
         * @return this builder
         */
        public Builder earlySurrender(boolean earlySurrender) {
            this.earlySurrender = earlySurrender;
            return this;
        }

        /**
         * Sets whether insurance is offered when the dealer shows an ace. See {@link #insurancePay(double)}. Defaults
         * to {@code true}.
         *
         * @param insuranceAllowed {@code true} if insurance is offered
         * @return this builder
         */
        public Builder insuranceAllowed(boolean insuranceAllowed) {
            this.insuranceAllowed = insuranceAllowed;
            return this;
        }

        /**
         * Sets whether a split ace that is dealt another ace may be split again. Defaults to {@code false}.
         *
         * @param reSplitAces {@code true} if split aces may be split again
         * @return this builder
         */
        public Builder reSplitAces(boolean reSplitAces) {
            this.reSplitAces = reSplitAces;
            return this;
        }

        /**
         * Sets whether split aces may take more than one card. Most casinos give each split ace exactly one card.
         * Defaults to {@code false}.
         *
         * @param hitSplitAces {@code true} if split aces may hit
         * @return this builder
         */
        public Builder hitSplitAces(boolean hitSplitAces) {
            this.hitSplitAces = hitSplitAces;
            return this;
        }

        /**
         * Sets whether split aces may double. Turning this on also lets split aces hit. Defaults to {@code false}.
         *
         * @param doubleSplitAces {@code true} if split aces may double
         * @return this builder
         */
        public Builder doubleSplitAces(boolean doubleSplitAces) {
            this.doubleSplitAces = doubleSplitAces;
            return this;
        }

        /**
         * Creates rules with this builder's settings.
         *
         * @return the new rules
         */
        public Rules build() {
            return new Rules(this);
        }
    }
}
