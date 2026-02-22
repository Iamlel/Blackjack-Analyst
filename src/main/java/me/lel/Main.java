package me.lel;

import me.lel.core.Rules;
import me.lel.counting.HiLoCountSystem;
import me.lel.player.Player;
import me.lel.player.strategy.H17BasicStrategy;

import javax.swing.*;
import java.awt.*;

public class Main {

    static void main(String[] args) {
        Player player = new Player(1000000, new H17BasicStrategy());
        Rules rules = new Rules.Builder().minimumBet(1).surrender(false).h17(true).insuranceAllowed(false).splitAmount(3).build();
        Blackjack bj = new Blackjack(new Player[]{player}, new HiLoCountSystem(), rules);
        int hands = 300000;
        for (int i = 0; i < hands; i++) {
            bj.play();
        }
        System.out.println("Bankroll: " + player.getBankroll());
        System.out.println("Difference: " + (player.getBankroll() - 1000000));
        System.out.println("Average: " + (player.getBankroll() - 1000000) / hands);
    }
}

