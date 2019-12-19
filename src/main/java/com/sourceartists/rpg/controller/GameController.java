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

import java.io.*;
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

    private static final Integer EXTRA_MONEY_CYCLE = 5;
    private static final Integer FAME_GAIN = 10;

    public void levelUp(Hero hero){
        hero.setLevel(hero.getLevel() + 1);

        if(hero.getLevel() % EXTRA_MONEY_CYCLE == 0)
            hero.addMoney(gameEngine.generateBonusMoney());

        if(gameEngine.didHeroAchieveRemarkableThings(hero))
            hero.addSpell(gameEngine.generateSpecialSpell());
            hero.setLevel(hero.getLevel() + 1);

        for(Monster monster: hero.getMostersSlainedLastLevel)
            hero.addReputation(monster.getReputationGain());
            hero.addFame(FAME_GAIN);
    }



    private AuthenticationDao authenticationDao;

    public User authenticate(String username, String password){
        String encryptedPassword = encryptPassword(password);
        User authenticatedUser = authenticationDao.findUserByNameAndPassword(username, encryptedPassword);

        if(authenticatedUser == null){
            throw new UserNotFoundException();
        }

        return authenticatedUser;
    }

    public boolean defendFortress(Hero hero, Castle castle,
                                  List<Hero> enemyArmy){
        if(enemyArmy.size() < 100
                || enemyArmy.stream()
                            .anyMatch(enemy -> enemy.getName().equals("General"))){
            return true;
        }

        if(hero.getMoraleLevel() <= 1
                && castle.getOuterWalls() == null
                && hero.getAllies().size() < 5){
            return false;
        }

        castle.startDefense(hero, enemyArmy);
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
            boolean isNotOpened = attemptToOpen(lockpick, chest);

            if(!isNotOpened){
                hero.addMoney(chest.getMoney());
                return;
            }
        }
    }

    private boolean attemptToOpen(Lockpick lockpick, Chest chest){
        if(!lockpick.isUsed()){
            return chest.tryToOpen(lockpick, MODE.HARD);
        }else{
            return chest.tryToOpen(lockpick, MODE.EASY);
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

    public void acquireKnighthood(King king, Hero hero){

    }

    public Receipt upgradeArmor(Merchant merchant, Hero hero){
        List<String> armorPieces = hero.prepareRequiredArmorPieces();

        Double totalPrice = armorPieces.stream()
                .map(armorPiece ->
                    merchant.availableProducts().stream()
                        .filter(product -> product.getName() == armorPiece)
                        .findFirst()
                        .orElse(Product.ARMOR)
                )
                .map(merchantProduct -> merchantProduct.getPrice())
                .reduce(0.0, Double::sum);

        return new Receipt("Receipt value: " + totalPrice.toString() + "gold");
    }

    public boolean loadLastSavedGame(){
        boolean worldStateLoaded = gameEngine.loadCurrentState();
        boolean heroStateLoaded = gameEngine.loadCurrentHeroDetails();
        boolean missionsLoaded = gameEngine.loadMissionsState();
        boolean journalLoaded = false;

        try(BufferedReader br = new BufferedReader(new FileReader("journal.txt"))) {
            StringBuilder journalBuilder = new StringBuilder();
            for(String line; (line = br.readLine()) != null; ) {
                journalBuilder.append(line);
            }
            journalLoaded = gameEngine.loadJournal(journalBuilder.toString());
        } catch (IOException e) {
            // error handling
        }

        return worldStateLoaded && heroStateLoaded
                && missionsLoaded && journalLoaded;
    }

    public void payTributeToTheHero(List<GratefulPerson> gratefulPeople,
                                    Hero hero){
        String listOfGifts = "";
        for(GratefulPerson gratefulPerson: gratefulPeople){
            Gift gift = gratefulPerson.giveGift();
            gameEngine.addGiftToHeroTreasureChest(hero, gift);
            listOfGifts += gift.getName() + ": " + gift.getValue();
        }

        gameEngine.logListOfGiftsInJournal(hero, listOfGifts);
    }


}
