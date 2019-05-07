package com.sourceartists.rpg.model;

import java.math.BigDecimal;

public class Chest {

    private BigDecimal money = BigDecimal.valueOf(50);
    private Integer percentageChanceToSpawnGuardian = Integer.valueOf(10);

    public Integer getPercentageChanceToSpawnGuardian() {
        return percentageChanceToSpawnGuardian;
    }

    public void setPercentageChanceToSpawnGuardian(Integer percentageChanceToSpawnGuardian) {
        this.percentageChanceToSpawnGuardian = percentageChanceToSpawnGuardian;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public void increaseChanceToSpawnGuardian(){
        percentageChanceToSpawnGuardian += 5;
    }
}
