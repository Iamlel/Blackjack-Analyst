package me.lel.core;

public class Rules {
    private final int minimumBet;
    private final int maximumBet;
    private final double penetration;
    private final boolean h17;
    private final boolean das;
    private final int splitAmount;
    private final double insurancePay;
    private final double blackjackPay;
    private final int maxHands;
    private final boolean surrender;
    private final boolean insuranceAllowed;

    public Rules(Builder builder) {
        this.minimumBet = builder.minimumBet;
        this.maximumBet = builder.maximumBet;
        this.penetration = builder.penetration;
        this.h17 = builder.h17;
        this.das = builder.das;
        this.splitAmount = builder.splitAmount;
        this.insurancePay = builder.insurancePay;
        this.blackjackPay = builder.blackjackPay;
        this.maxHands = builder.maxHands;
        this.surrender = builder.surrender;
        this.insuranceAllowed = builder.insuranceAllowed;
    }

    public int getMinimumBet() {
        return minimumBet;
    }

    public int getMaximumBet() {
        return maximumBet;
    }

    public double getPenetration() {
        return penetration;
    }

    public boolean isH17() {
        return h17;
    }

    public boolean isDas() {
        return das;
    }

    public int getSplitAmount() {
        return splitAmount;
    }

    public double getInsurancePay() {
        return insurancePay;
    }

    public double getBlackjackPay() {
        return blackjackPay;
    }

    public int getMaxHands() {
        return maxHands;
    }

    public boolean isSurrenderAllowed() {
        return surrender;
    }

    public boolean isInsuranceAllowed() {
        return insuranceAllowed;
    }

    public static Rules buildDefault() {
        return new Builder().build();
    }

    public static class Builder {
        private int minimumBet = 10;
        private int maximumBet = 1000;
        private double penetration = 0.83;
        private boolean h17 = true;
        private boolean das = true;
        private int splitAmount = 3;
        private double insurancePay = 2;
        private double blackjackPay = (double) 3 / 2;
        private int maxHands = 2;
        private boolean surrender = true;
        private boolean insuranceAllowed = true;

        /**
         * @param minimumBet must be >= 1
         * @throws IllegalArgumentException Invalid argument.
         */
        public Builder minimumBet(int minimumBet) {
            if (minimumBet < 1) {
                throw new IllegalArgumentException("Minimum bet cannot be less than 1.");
            }
            this.minimumBet = minimumBet;
            return this;
        }

        /**
         * @param maximumBet must be >= 1
         * @throws IllegalArgumentException Invalid argument.
         */
        public Builder maximumBet(int maximumBet) {
            if (maximumBet < 1) {
                throw new IllegalArgumentException("Maximum bet cannot be less than 1.");
            }
            this.maximumBet = maximumBet;
            return this;
        }

        /**
         * @param penetration must be between 0 and 1
         * @throws IllegalArgumentException Invalid argument.
         */
        public Builder penetration(double penetration) {
            if (penetration > 1 || penetration <= 0) {
                throw new IllegalArgumentException("Penetration must be between 0 and 1.");
            }
            this.penetration = penetration;
            return this;
        }

        public Builder h17(boolean h17) {
            this.h17 = h17;
            return this;
        }

        public Builder das(boolean das) {
            this.das = das;
            return this;
        }

        /**
         * @param splitAmount can not be 0.
         * @throws IllegalArgumentException Invalid argument.
         */
        public Builder splitAmount(int splitAmount) {
            if (splitAmount < 0) {
                throw new IllegalArgumentException("Split amount can not be 0.");
            }
            this.splitAmount = splitAmount;
            return this;
        }

        /**
         * @param insurancePay must be greater than 0.
         * @throws IllegalArgumentException Invalid argument.
         */
        public Builder insurancePay(double insurancePay) {
            if (insurancePay <= 0) {
                throw new IllegalArgumentException("Insurance pay must be greater than 0.");
            }
            this.insurancePay = insurancePay;
            return this;
        }

        /**
         * @param blackjackPay must be greater than 0.
         * @throws IllegalArgumentException Invalid argument.
         */
        public Builder blackjackPay(double blackjackPay) {
            if (blackjackPay <= 0) {
                throw new IllegalArgumentException("Blackjack pay must be greater than 0.");
            }
            this.blackjackPay = blackjackPay;
            return this;
        }

        /**
         * @param maxHands must be at least 1.
         * @throws IllegalArgumentException Invalid argument.
         */
        public Builder maxHands(int maxHands) {
            if (blackjackPay < 1) {
                throw new IllegalArgumentException("At least one hand must be allowed.");
            }
            this.maxHands = maxHands;
            return this;
        }

        public Builder surrender(boolean surrender) {
            this.surrender = surrender;
            return this;
        }

        public Builder insuranceAllowed(boolean insuranceAllowed) {
            this.insuranceAllowed = insuranceAllowed;
            return this;
        }

        public Rules build() {
            return new Rules(this);
        }
    }
}
