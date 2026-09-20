package me.lel.game;

import me.lel.core.Rules;
import me.lel.player.Player;
import umontreal.ssj.mcqmctools.MonteCarloModelDoubleArray;

// simple SimpleGame with players and rules and an instance of montecarlomodeldoublearray
public interface SimpleGame extends MonteCarloModelDoubleArray {
    Player[] getPlayers();
    Rules getRules();
    void reset();
}
