package com.sourceartists.rpg.model;

import java.util.List;

public class Enemy {

    private String name;
    private Integer level;
    private boolean alive = true;
    private Buff activeBuff;
    private List<Power> powers;
    private List<Spell> spells;

    public boolean isAlive() {
        return alive;
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

    public void setAlive(boolean alive) {
        this.alive = alive;
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
}
