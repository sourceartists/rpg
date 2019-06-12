package com.sourceartists.rpg.model;

public class Buff {

    private String name;
    private BuffType buffType;
    private Integer timeLeft = 60;

    public Buff(String name, BuffType buffType){
        this.name = name;
        this.buffType = buffType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BuffType getBuffType() {
        return buffType;
    }

    public void setBuffType(BuffType buffType) {
        this.buffType = buffType;
    }

    public Integer getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(Integer timeLeft) {
        this.timeLeft = timeLeft;
    }
}
