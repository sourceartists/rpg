package com.sourceartists.rpg.model;

public class Treasure {

    private TreasureType treasureType;

    public Treasure(TreasureType treasureType) {
        this.treasureType = treasureType;
    }

    public TreasureType getTreasureType() {
        return treasureType;
    }

    public void setTreasureType(TreasureType treasureType) {
        this.treasureType = treasureType;
    }
}
