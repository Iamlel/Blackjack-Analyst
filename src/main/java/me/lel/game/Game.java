package me.lel.game;

import me.lel.core.Rules;
import me.lel.player.Player;

public interface Game {
    void play();
    Player[] getPlayers();
    Rules getRules();
}
