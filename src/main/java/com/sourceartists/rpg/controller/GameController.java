package com.sourceartists.rpg.controller;

import com.sourceartists.rpg.engine.GameEngine;
import com.sourceartists.rpg.exception.HeroOvercomesDeathAndCrushesHisEnemy;
import com.sourceartists.rpg.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class GameController {

    public static final Integer MIN_SUPERBUFF_LEVEL = 75;
    public static final Integer MIN_SUPERBUFF_MORALE = 8;

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

    public boolean defendCastle(Hero hero, Castle castle){
        castle.startDefense(hero);

        List<Hero> heroesArmy = hero.getAllies();
        hero.castOffensiveSpell(heroesArmy);
        hero.getArmyIntoPosition();

        if(!castle.defenseStarted()){
            return false;
        }

        while(hero.isAlive() || !castle.taken()){
            castle.shootAtHeroAndHisArmy(hero);
            gameEngine.attackTheCastle(hero,castle);
        }

        if(hero.isAlive()){return false;}

        return true;
    }

    public void heroDies(Hero hero, Enemy enemy) throws HeroOvercomesDeathAndCrushesHisEnemy {
        Hit hit = gameEngine.hit(enemy, hero);

        while(!hit.isCritical()){
            hit = gameEngine.hit(enemy, hero);
        }

        enemy.performDeadlyFinalBlow();
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
        if(hero.getActiveBuff() != null){
            return;
        }

        if(gameEngine.giveSuperBuff(hero.getLevel(), hero.getMoraleLevel())){
            hero.setActiveBuff(new SuperBuff("super duper buff", buffType));

            return;
        }

        hero.setActiveBuff(new Buff("normal duper buff", buffType));
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
        boolean mainDoorOpened = false;

        for(Lockpick lockpick: hero.getLockpicks()){
            boolean opened = gameEngine.attemptToOpenDoor(lockpick
                    , hero.getLockpickingLevel());

            if(opened){
                mainDoorOpened = true;
                break;
            }
        }

        if(!mainDoorOpened){
            return;
        }

        BigDecimal jeweleryWorth = gameEngine.stealFromJeweleryBox(castle, hero);
        hero.addMoney(jeweleryWorth);
    }


}
