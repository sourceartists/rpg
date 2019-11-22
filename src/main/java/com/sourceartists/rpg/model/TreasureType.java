package com.sourceartists.rpg.model;

import java.math.BigDecimal;

public enum TreasureType {

    EMERALD(BigDecimal.valueOf(1000)),
    RUBY(BigDecimal.valueOf(500)),
    DIAMOND(BigDecimal.valueOf(2500));

    private BigDecimal worth;

    TreasureType(BigDecimal worth) {
        this.worth = worth;
    }

    public BigDecimal getWorth() {
        return worth;
    }
}
