package com.sourceartists.rpg.model;

import java.math.BigDecimal;
import java.util.List;

public class Hero {

    private String name;
    private Integer level;
    private Integer moraleLevel;
    private BigDecimal money = BigDecimal.valueOf(100);
    private HeroClass heroClass;

    private Integer lockpickingLevel = Integer.valueOf(10);

    private Buff activeBuff;
    private List<Power> powers;
    private List<Spell> spells;
    private List<Hero> allies;
    private List<Armor> equippedArmor;
    private List<Lockpick> lockpicks;
    private Weapon equippedWeapon;
    private Castle castle;
    private boolean alive;

    public Integer getLockpickingLevel() {
        return lockpickingLevel;
    }

    public void setLockpickingLevel(Integer lockpickingLevel) {
        this.lockpickingLevel = lockpickingLevel;
    }

    public void increaseLockpicking(){
        ++lockpickingLevel;
    }

    public void decreaseMorale(){
        --moraleLevel;
    }

    public Integer getMoraleLevel() {
        return moraleLevel;
    }

    public void setMoraleLevel(Integer moraleLevel) {
        this.moraleLevel = moraleLevel;
    }

    public void addMoney(BigDecimal money){
        setMoney(this.money.add(money));
    }

    public List<Lockpick> getLockpicks() {
        return lockpicks;
    }

    public void setLockpicks(List<Lockpick> lockpicks) {
        this.lockpicks = lockpicks;
    }

    public Spell mostPowerfullOffensiveSpell(){
        return spells.get(0);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public HeroClass getHeroClass() {
        return heroClass;
    }

    public void setHeroClass(HeroClass heroClass) {
        this.heroClass = heroClass;
    }

    public Buff getActiveBuff() {
        return activeBuff;
    }

    public void setActiveBuff(Buff activeBuff) {
        this.activeBuff = activeBuff;
    }

    public List<Power> getPowers() {
        return powers;
    }

    public void setPowers(List<Power> powers) {
        this.powers = powers;
    }

    public List<Spell> getSpells() {
        return spells;
    }

    public void setSpells(List<Spell> spells) {
        this.spells = spells;
    }

    public List<Hero> getAllies() {
        return allies;
    }

    public void setAllies(List<Hero> allies) {
        this.allies = allies;
    }

    public List<Armor> getEquippedArmor() {
        return equippedArmor;
    }

    public void setEquippedArmor(List<Armor> equippedArmor) {
        this.equippedArmor = equippedArmor;
    }

    public Weapon getEquippedWeapon() {
        return equippedWeapon;
    }

    public void setEquippedWeapon(Weapon equippedWeapon) {
        this.equippedWeapon = equippedWeapon;
    }

    public Castle getCastle() {
        return castle;
    }

    public void setCastle(Castle castle) {
        this.castle = castle;
    }

    public void addSpell(Spell generateSpecialSpell) {
        this.spells.add(generateSpecialSpell);
    }

    public void castOffensiveSpell(List<Hero> heroesArmy) {

    }

    public void getArmyIntoPosition() {

    }

    public void attack(Castle castle) {

    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isAlive() {
        return false;
    }
}
