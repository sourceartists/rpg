package com.sourceartists.rpg.model;

import java.util.Objects;

public class Lockpick {

    private Integer id;
    private boolean used;

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public Lockpick(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lockpick lockpick = (Lockpick) o;
        return id.equals(lockpick.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
