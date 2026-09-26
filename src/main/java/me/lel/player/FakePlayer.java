package me.lel.player;

import me.lel.player.better.Better;
import me.lel.player.mover.Mover;
import me.lel.player.sidebet.SideBetMover;

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
}
