package me.lel.core;

/**
 * Table rules for a game of blackjack. Build them with {@link Builder}, whose methods list the default for each rule.
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
     * Returns the table minimum, which is also the size of one betting unit.
     */
    public int getMinimumBet() {
        return minimumBet;
    }

    /**
     * Returns the largest initial bet allowed on one hand.
     */
    public int getMaximumBet() {
        return maximumBet;
    }

    /**
     * Returns the fraction of the shoe dealt before it is reshuffled.
     */
    public double getPenetration() {
        return penetration;
    }

    /**
     * Returns whether the dealer hits soft 17.
     */
    public boolean isH17() {
        return h17;
    }

    /**
     * Returns whether doubling after a split is allowed.
     */
    public boolean isDas() {
        return das;
    }

    /**
     * Returns whether surrendering after a split is allowed.
     */
    public boolean isSas() {
        return sas;
    }

    /**
     * Returns how many times one player may split in a round, counted across all of their hands.
     */
    public int getSplitAmount() {
        return splitAmount;
    }

    /**
     * Returns the insurance payout as a multiple of the insurance bet, so 2 means 2:1.
     */
    public double getInsurancePay() {
        return insurancePay;
    }

    /**
     * Returns the blackjack payout as a multiple of the bet, so 1.5 means 3:2.
     */
    public double getBlackjackPay() {
        return blackjackPay;
    }

    /**
     * Returns the most hands one player may be dealt at the start of a round. Hands created by splitting don't count.
     */
    public int getMaxHands() {
        return maxHands;
    }

    public boolean isLateSurrender() {
        return lateSurrender;
    }

    public boolean isEarlySurrender() {
        return earlySurrender;
    }

    public boolean isInsuranceAllowed() {
        return insuranceAllowed;
    }

    public boolean isReSplitAces() {
        return reSplitAces;
    }

    /**
     * Returns whether split aces may take more cards. This is always true when {@link #isDoubleSplitAces()} is.
     */
    public boolean isHitSplitAces() {
        return hitSplitAces;
    }

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
         * Sets the table minimum, which is also the size of one betting unit. Defaults to 10.
         *
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
         * Sets the largest initial bet allowed on one hand. Larger bets are capped at it. Defaults to 1000.
         *
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
         * Sets the fraction of the shoe dealt before it is reshuffled. Defaults to 0.83.
         *
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
         * Sets whether the dealer hits soft 17. Defaults to {@code true}.
         */
        public Builder h17(boolean h17) {
            this.h17 = h17;
            return this;
        }

        /**
         * Sets whether doubling after a split is allowed. Defaults to {@code true}.
         */
        public Builder das(boolean das) {
            this.das = das;
            return this;
        }

        /**
         * Sets whether surrendering after a split is allowed. Defaults to {@code false}.
         */
        public Builder sas(boolean sas) {
            this.sas = sas;
            return this;
        }

        /**
         * Sets how many times one player may split in a round, counted across all of their hands. Defaults to 3, so a
         * single hand can become at most four. Use 0 to turn splitting off.
         *
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
         * Sets the insurance payout as a multiple of the insurance bet. Defaults to 2, which is 2:1.
         *
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
         * Sets the blackjack payout as a multiple of the bet. Defaults to 1.5, which is 3:2.
         *
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
         * Sets the most hands one player may be dealt at the start of a round. Hands created by splitting don't
         * count. Defaults to 2.
         *
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
         * Sets whether a player may surrender after the dealer checks for blackjack. Defaults to {@code true}.
         */
        public Builder lateSurrender(boolean lateSurrender) {
            this.lateSurrender = lateSurrender;
            return this;
        }

        /**
         * Sets whether a player may surrender before the dealer checks for blackjack. Defaults to {@code false}.
         */
        public Builder earlySurrender(boolean earlySurrender) {
            this.earlySurrender = earlySurrender;
            return this;
        }

        /**
         * Sets whether insurance is offered when the dealer shows an ace. Defaults to {@code true}.
         */
        public Builder insuranceAllowed(boolean insuranceAllowed) {
            this.insuranceAllowed = insuranceAllowed;
            return this;
        }

        /**
         * Sets whether split aces may be split again. Defaults to {@code false}.
         */
        public Builder reSplitAces(boolean reSplitAces) {
            this.reSplitAces = reSplitAces;
            return this;
        }

        /**
         * Sets whether split aces may take more than one card. Defaults to {@code false}.
         */
        public Builder hitSplitAces(boolean hitSplitAces) {
            this.hitSplitAces = hitSplitAces;
            return this;
        }

        /**
         * Sets whether split aces may double. Turning this on also lets split aces hit. Defaults to {@code false}.
         */
        public Builder doubleSplitAces(boolean doubleSplitAces) {
            this.doubleSplitAces = doubleSplitAces;
            return this;
        }

        public Rules build() {
            return new Rules(this);
        }
    }
}
