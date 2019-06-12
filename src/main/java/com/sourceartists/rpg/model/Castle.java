package com.sourceartists.rpg.model;

public class Castle {

    private boolean taken;

    public void startDefense(Hero hero) {

    }

    public boolean defenseStarted() {
        return false;
    }

    public boolean taken() {
        return false;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }

    public void shootAtHeroAndHisArmy(Hero hero) {

    }
}
