package com.sourceartists.rpg.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import com.google.common.collect.Ordering;
import com.sourceartists.rpg.engine.GameEngine;
import com.sourceartists.rpg.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class GameControllerTest {

    @InjectMocks
    private GameController gameControllerSUT;

    @Mock
    private GameEngine gameEngineMock;

    @BeforeEach
    private void init(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldLevelUpWithoutBonusSpell() throws Exception{
        // Arrange
        Hero hero = new Hero();
        hero.setLevel(14);

        // Act
        gameControllerSUT.levelUp(hero);

        // Assert
        verify(gameEngineMock).generateBonusMoney();
        verify(gameEngineMock).generateRandomBuff();

        verify(gameEngineMock, never()).generateSpecialSpell();
    }

    @Test
    public void shouldKillBossWithCriticalHit() throws Exception{
        // Arrange
        Hero hero = new Hero();
        Weapon weapon = new Weapon();
        hero.setEquippedWeapon(weapon);

        AttackOutcome normalAttackOutcome = new AttackOutcome();
        AttackOutcome finalAttackOutcome = new AttackOutcome();
        finalAttackOutcome.setDeadly(true);

        Boss boss = new Boss();

        when(gameEngineMock.determineCritical(weapon,hero))
                .thenReturn(false)
                .thenReturn(false)
                .thenReturn(true);

        when(gameEngineMock.attack(eq(weapon), eq(boss), anyBoolean()))
                .thenReturn(normalAttackOutcome)
                .thenReturn(normalAttackOutcome)
                .thenReturn(finalAttackOutcome);

        // Act
        gameControllerSUT.fightTheBoss(hero, boss);

        // Assert
        ArgumentCaptor<Boolean> criticalHitCaptor = ArgumentCaptor.forClass(Boolean.class);

        verify(gameEngineMock, times(3)).attack(eq(weapon), eq(boss), criticalHitCaptor.capture());

        assertThat(criticalHitCaptor.getValue()).isTrue();
    }

    @Captor
    private ArgumentCaptor<Lockpick> lockpickCaptor;
    @Captor
    private ArgumentCaptor<Integer> lockpickLevelCaptor;
    @Captor
    private ArgumentCaptor<Integer> guardianChanceCaptor;

    @Test
    public void shouldOpenLootAfterMultiAttempt() throws Exception{
        // Arrange
        Hero hero = new Hero();
        hero.setLockpicks(Arrays.asList(
                new Lockpick[]{new Lockpick(1), new Lockpick(2), new Lockpick(3)}));

        Chest chest = new Chest();

        when(gameEngineMock.attemptToOpen(any(Lockpick.class), anyInt(), anyInt()))
                .thenReturn(false)
                .thenReturn(false)
                .thenReturn(true);

        // Act
        gameControllerSUT.openLootChest(hero,chest);

        // Assert
        verify(gameEngineMock, times(3)).attemptToOpen(lockpickCaptor.capture()
            , lockpickLevelCaptor.capture()
            , guardianChanceCaptor.capture());

        List<Lockpick> lockpicks = lockpickCaptor.getAllValues();
        List<Integer> lockpickLevels = lockpickLevelCaptor.getAllValues();
        List<Integer> guardianChances = guardianChanceCaptor.getAllValues();

        assertThat(new HashSet<>(lockpicks).size()).isEqualTo(3);
        assertThat(Ordering.natural().isOrdered(lockpickLevels)).isTrue();
        assertThat(Ordering.natural().isOrdered(guardianChances)).isTrue();
    }
}