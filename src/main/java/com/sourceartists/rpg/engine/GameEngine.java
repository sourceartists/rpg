package com.sourceartists.rpg.engine;

import com.sourceartists.rpg.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class GameEngine {
    public Spell generateSpecialSpell() {

        return null;
    }

    public BigDecimal generateBonusMoney() {
        return null;
    }

    public Buff generateRandomBuff() {
        return null;
    }

    public void castSpell(Spell mostPowerfullOffensiveSpell, Enemy enemy) {

    }

    public AttackOutcome attack(Weapon weapon, Enemy enemy, boolean fatalityHit) {
        return null;
    }

    public boolean determineCritical(Weapon equippedWeapon, Hero hero) {
        return false;
    }

    public boolean attemptToOpen(Lockpick lockpick, Integer hero, Integer chest) {
        return false;
    }
}
