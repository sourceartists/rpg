package com.sourceartists.rpg.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.google.common.collect.Ordering;
import com.sourceartists.rpg.engine.GameEngine;
import com.sourceartists.rpg.exception.HeroIsAChickenExcpetion;
import com.sourceartists.rpg.exception.HeroOvercomesDeathAndCrushesHisEnemy;
import com.sourceartists.rpg.exception.HeroSlainedByDragonException;
import com.sourceartists.rpg.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

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
    public void shouldIncreaseLevelByOne_whenGainingLevel() throws Exception{
        // Arrange
        Hero hero = new Hero();
        Integer initialHeroLevel = Integer.valueOf(14);
        hero.setLevel(initialHeroLevel);

        // Act
        gameControllerSUT.levelUp(hero);

        // Assert
        assertThat(hero.getLevel()).isEqualTo(initialHeroLevel + 1);
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
    public void shouldOpenLootOnFirstAttempt() throws Exception{
        // Arrange
        Hero hero = new Hero();
        Lockpick theLockpick = new Lockpick(1);
        hero.setLockpicks(Arrays.asList(
                new Lockpick[]{theLockpick, new Lockpick(2), new Lockpick(3)}));

        Chest chest = new Chest();

        when(gameEngineMock.attemptToOpen(theLockpick, 10, 20))
                .thenReturn(true);

        // Guard Assert
        assertThat(hero.getGold()).isEqualByComparingTo(BigDecimal.valueOf(100));

        // Act
        gameControllerSUT.openLootChest(hero,chest);

        // Assert
        assertThat(hero.getGold()).isEqualByComparingTo(BigDecimal.valueOf(100).add(chest.getMoney()));
    }

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

    @Test
    public void shouldGiveHeroSuperBuff() throws Exception{
        // Arrange
        Integer heroLevel = 75;
        Integer heroMorale = 8;
        Hero hero = new Hero();
        hero.setLevel(heroLevel);
        hero.setMoraleLevel(heroMorale);

        when(gameEngineMock.giveSuperBuff(75, 8)).thenReturn(true);

        // Act
        gameControllerSUT.gainBuff(hero, BuffType.OFFENSIVE);

        // Assert
        assertThat(hero.getActiveBuff()).isInstanceOf(SuperBuff.class);
        assertThat(hero.getActiveBuff().getBuffType()).isEqualTo(BuffType.OFFENSIVE);
    }

    @Test
    public void shouldBreakIntoCastleAndStealJewelery() throws Exception{
        // Arrange
        Castle castle = new Castle();
        Hero hero = new Hero();
        BigDecimal jeweleryWorth = BigDecimal.valueOf(100);
        hero.setLockpicks(Arrays.asList(new Lockpick[]{new Lockpick(1), new Lockpick(2)}));

        when(gameEngineMock.stealFromJeweleryBox(castle, hero)).thenReturn(jeweleryWorth);

        when(gameEngineMock.attemptToOpenDoor(any(Lockpick.class), eq(hero.getLockpickingLevel())))
                .thenReturn(true);

        // Guard Assert
        assertThat(hero.getGold()).isEqualTo(BigDecimal.valueOf(100));

        // Act
        gameControllerSUT.breakIntoCastleAndSteal(hero, castle);

        // Assert
        verify(gameEngineMock, times(1)).attemptToOpenDoor(
                any(Lockpick.class), eq(hero.getLockpickingLevel()));
        assertThat(hero.getGold()).isEqualTo(BigDecimal.valueOf(100).add(jeweleryWorth));
    }

    @Test
    @Disabled
    public void shouldDefendTheCastle() throws Exception{
        // Arrange
        Hero hero = new Hero();
        Castle castleSpy = spy(new Castle());

        doNothing().when(castleSpy).startDefense(hero);
        doReturn(true).when(castleSpy).defenseStarted();

        // Act
        boolean castleDefended = gameControllerSUT.defendCastle(hero, castleSpy);

        // Assert
        assertThat(castleDefended).isTrue();
    }

    @Test
    public void heroShouldNotDieJustLikeThat() throws Exception{
        // Arrange
        Hero hero = new Hero();
        Enemy enemyStub = mock(Enemy.class);
        Hit hit = new Hit();
        hit.setCritical(true);

        when(gameEngineMock.hit(enemyStub, hero)).thenReturn(hit);

        doThrow(new HeroOvercomesDeathAndCrushesHisEnemy())
                .when(enemyStub).performDeadlyFinalBlow();

        // Act
        Exception expectedException = null;

        try {
            gameControllerSUT.heroDies(hero, enemyStub);
        } catch (HeroOvercomesDeathAndCrushesHisEnemy e){
            expectedException = e;
        }

        // Assert
        assertThat(expectedException).isInstanceOf(HeroOvercomesDeathAndCrushesHisEnemy.class);
        assertThat(hero.isAlive()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = BuffType.class, mode = EnumSource.Mode.INCLUDE,
            names = {"INCREASED_STRENTH" ,"INCREASED_AGILITY" ,"INCREASED_SPEED"})
    public void shouldWinWithMightyDragon(BuffType activeBuff) throws Exception{
        // Given
        Hero hero = new Hero();
        MightyDragon dragon = new MightyDragon();
        hero.setActiveBuff(new Buff("Active buff", activeBuff));

        given(gameEngineMock.fightWithDragon(hero, dragon))
                .willReturn(true);

        // When
        boolean dragonSlained = gameControllerSUT.fightWithMightyDragon(hero, dragon);

        // Then
        assertThat(dragonSlained).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {1001,1500,2000})
    public void shouldThrowException_whenHeroSlained_givenTryingToStealGold(
            int goldAmountToSteal
    ) throws Exception{
        // Given
        Hero hero = new Hero();
        Dragon dragon = new Dragon();

        given(gameEngineMock.fightWithDragon(hero, dragon))
                .willThrow(new HeroSlainedByDragonException());

        // When
        assertThatThrownBy(() -> gameControllerSUT.stealGoldFromDragon(hero, dragon, goldAmountToSteal))
        // Then
                .isInstanceOf(HeroSlainedByDragonException.class);

    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    public void shouldThrowException_whenNoTreasureMeantToBeStolen(List<Treasure> treasures) throws Exception{
        // Given
        Hero hero = new Hero();
        Dragon dragon = new Dragon();

        // When
        assertThatThrownBy(() -> gameControllerSUT.stealTreasureFromDragon(hero,dragon,treasures))
                // Then
                .isInstanceOf(HeroIsAChickenExcpetion.class);
    }

    static Stream<Arguments> shouldCountLoot(){
        Treasure diamondTreasure = new Treasure(TreasureType.DIAMOND);
        Treasure emeraldTreasure = new Treasure(TreasureType.EMERALD);
        Treasure rubyTreasure = new Treasure(TreasureType.RUBY);

        List<Treasure> treasuresOne = Arrays.asList(
                new Treasure[]{diamondTreasure, emeraldTreasure});
        List<Treasure> treasuresTwo = Arrays.asList(
                new Treasure[]{diamondTreasure, emeraldTreasure, rubyTreasure});
        List<Treasure> treasuresThree = Arrays.asList(
                new Treasure[]{diamondTreasure, diamondTreasure, diamondTreasure});

        return Stream.of(
            arguments(treasuresOne, new Hero(1000), BigDecimal.valueOf(4500)),
            arguments(treasuresTwo, new Hero(0), BigDecimal.valueOf(4000)),
            arguments(treasuresThree, new Hero(1), BigDecimal.valueOf(7501))
        );
    }

    @ParameterizedTest
    @MethodSource
    public void shouldCountLoot(List<Treasure> treasures, Hero hero,
                                BigDecimal expectedLootWorth) throws Exception{
        // When
        BigDecimal lootWorth = gameControllerSUT.countLoot(treasures, hero);

        // Then
        assertThat(lootWorth)
                .isEqualByComparingTo(expectedLootWorth);
    }
}