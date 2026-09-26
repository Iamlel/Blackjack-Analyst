package me.lel.player;

import me.lel.player.better.Better;
import me.lel.player.mover.Mover;
import me.lel.player.sidebet.SideBetMover;

/**
 * A player with unlimited money, for measuring a strategy without the run ending in ruin. The bankroll starts at 0
 * and tracks net winnings, so it can go negative, and the player never dies.
 * <p>
 * With a starting bankroll of 0, {@link me.lel.simulation.ssj.BlackjackTally#getROR()} is meaningless for this
 * player. Pass a real bankroll to {@link me.lel.simulation.ssj.BlackjackTally#getROR(double)} instead.
 */
public class FakePlayer extends Player {
    public FakePlayer(Mover mover, Better better, SideBetMover sideBet) {
        super(0, mover, better, sideBet);
    }

    @Override
    public boolean has(double amount) {
        return true;
    }

    @Override
    public boolean isDead(int minimumBet) {
        return false;
    }

    /**
     * Returns a new fake player with the same strategies. Its bankroll starts at 0, like any fake player's.
     */
    @Override
    public FakePlayer clone() {
        return new FakePlayer(getMover(), getBetter(), getSideBetMover());
    }
}
