package com.sourceartists.rpg.controller;

import com.sourceartists.rpg.engine.GameEngine;
import com.sourceartists.rpg.exception.DoesNotStandAChanceException;
import com.sourceartists.rpg.exception.HeroIsAChickenExcpetion;
import com.sourceartists.rpg.exception.HeroOvercomesDeathAndCrushesHisEnemy;
import com.sourceartists.rpg.exception.HeroSlainedByDragonException;
import com.sourceartists.rpg.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;

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
            boolean opened = attemptToOpen(lockpick
                    , hero.getLockpickingLevel());

            hero.increaseLockpicking();

            if(opened){
                hero.addMoney(chest.getMoney());
                return;
            }
        }
    }

    private boolean attemptToOpen(Lockpick lockpick, Integer lockpickingLevel){
        if(lockpick.isUsed()){
            if(lockpickingLevel > 80){
                return true;
            }
        }else{
            if(lockpickingLevel > 30){
                return true;
            }
        }

        return false;
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

    private static final int LOW_MORALE_LEVEL = 1;
    private static final int FAIR_MORALE_LEVEL = 2;
    private static final int ALMIGHTY_MORALE_LEVEL = 3;

    private static final int LOW_MORALE_STRENGTH_GAIN = -1;
    private static final int FAIR_MORALE_STRENGTH_GAIN = 3;
    private static final int ALMIGHTY_MORALE_STRENGTH_GAIN = 10;

    public boolean fightWithMightyDragon(Hero hero, MightyDragon mightyDragon) throws DoesNotStandAChanceException {
        if(hero.getMoraleLevel().equals(LOW_MORALE_LEVEL)){
            hero.addStrenth(LOW_MORALE_STRENGTH_GAIN);
        }else if(hero.getMoraleLevel().equals(FAIR_MORALE_LEVEL)){
            hero.addStrenth(FAIR_MORALE_STRENGTH_GAIN);
        }else if(hero.getMoraleLevel().equals(ALMIGHTY_MORALE_LEVEL)){
            hero.addStrenth(ALMIGHTY_MORALE_STRENGTH_GAIN);
        }

        boolean dragonSlained = gameEngine.fightWithDragon(hero, mightyDragon);

        return dragonSlained;
    }

    public boolean stealGoldFromDragon(Hero hero, Dragon dragon, Integer amountToSteal)
            throws HeroSlainedByDragonException {
        if(amountToSteal == null || amountToSteal <= 0){
            throw new RuntimeException("Do not chicken right now!");
        }

        if(amountToSteal.compareTo(2000) > 0){
            throw new RuntimeException("You cannot try to steal more than 2000 gold at a time.");
        }

        if(amountToSteal >= Integer.valueOf(1000)){
            boolean dragonSlained = gameEngine.fightWithDragon(hero, dragon);

            if(!dragonSlained){
                throw new HeroSlainedByDragonException();
            }
        }

        return gameEngine.stealGold(hero, dragon);
    }

    public boolean stealTreasureFromDragon(Hero hero, Dragon dragon, List<Treasure> treasures)
            throws HeroIsAChickenExcpetion {
        if(CollectionUtils.isEmpty(treasures)){
            throw new HeroIsAChickenExcpetion();
        }

        return gameEngine.stealTreasures(hero, dragon, treasures);
    }

    public BigDecimal countLoot(List<Treasure> treasures, Hero hero){
        BigDecimal lootWorth = hero.getGold();

        return treasures.stream()
                .map(treasure -> treasure.getTreasureType().getWorth())
                .reduce(lootWorth, (totalWorth, treasureWorth) -> totalWorth.add(treasureWorth));
    }


}
