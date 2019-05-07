package com.sourceartists.rpg.controller;

import com.sourceartists.rpg.engine.GameEngine;
import com.sourceartists.rpg.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameController {

    @Autowired
    private GameEngine gameEngine;

    public void levelUp(Hero hero){
        hero.setLevel(hero.getLevel() + 1);

        if(hero.getLevel() % 10 == 0){
            hero.addSpell(gameEngine.generateSpecialSpell());
        }

        if(hero.getLevel() % 5 == 0){
            hero.addMoney(gameEngine.generateBonusMoney());
        }

        if(hero.getActiveBuff() == null){
            hero.setActiveBuff(gameEngine.generateRandomBuff());
        }
    }

    public void takeOverCastle(Hero hero, Castle castle){

    }

    public void heroDies(Hero hero){

    }

    public void openLootChest(Hero hero, Chest chest){
        for(Lockpick lockpick: hero.getLockpicks()){
            boolean opened = gameEngine.attemptToOpen(lockpick
                    , hero.getLockpickingLevel(), chest.getPercentageChanceToSpawnGuardian());

            hero.increaseLockpicking();
            chest.increaseChanceToSpawnGuardian();

            if(opened){
                hero.addMoney(chest.getMoney());
                return;
            }
        }
    }

    public void gainBuff(Hero hero, BuffType buffType){

    }

    public void fightTheBoss(Hero hero, Boss boss){
        gameEngine.castSpell(hero.mostPowerfullOffensiveSpell(), boss);

        while(boss.isAlive()){
            boolean criticalHit = gameEngine.determineCritical(hero.getEquippedWeapon(), hero);

            AttackOutcome attackOutcome = gameEngine.attack(
                    hero.getEquippedWeapon(), boss, criticalHit);

            if(attackOutcome.isDeadly()){
                boss.setAlive(false);
            }
        }
    }

    public void breakIntoCastleAndSteal(Hero hero, Castle castle){

    }
}
