package me.lel.simulation.ssj;

import me.lel.game.SimpleGame;
import me.lel.player.Player;
import umontreal.ssj.stat.list.ListOfTalliesWithCovariance;

public class BlackjackTallyList<E extends BlackjackTally> extends ListOfTalliesWithCovariance<E> {
    public static BlackjackTallyList<BlackjackTally> create(SimpleGame game) {
        BlackjackTallyList<BlackjackTally> playerStatContainer = new BlackjackTallyList<>();
        for (Player player : game.getPlayers()) {
            playerStatContainer.add(new BlackjackTally(game.getRules().getMinimumBet(), player.getBankroll()));
        }

        playerStatContainer.init();
        return playerStatContainer;
    }
}
