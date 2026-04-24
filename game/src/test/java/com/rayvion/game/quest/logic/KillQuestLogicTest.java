package com.rayvion.game.quest.logic;

import com.rayvion.engine.entity.EntityDeathEvent;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.quest.Quest;
import com.rayvion.engine.quest.QuestSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class KillQuestLogicTest {
    private EventManager eventManager;
    private QuestSystem questSystem;
    private Quest quest;
    private String goalId;
    private int targetKills;
    private long playerId;
    private KillQuestLogic killQuestLogic;

    @BeforeEach
    void setUp() {
        eventManager = mock(EventManager.class);
        questSystem = mock(QuestSystem.class);
        quest = mock(Quest.class);
        goalId = "test_goal";
        targetKills = 2;
        playerId = 1L;
        killQuestLogic = new KillQuestLogic(eventManager, goalId, targetKills, playerId);
        
        when(quest.getId()).thenReturn("test_quest");
        when(quest.getName()).thenReturn("Test Quest");
    }

    @Test
    void testInitializeSubscribesToEvent() {
        killQuestLogic.initialize(quest, questSystem);
        verify(eventManager).subscribe(eq(EntityDeathEvent.class), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testEntityDeathIncrementsKills() {
        killQuestLogic.initialize(quest, questSystem);

        ArgumentCaptor<Consumer<EntityDeathEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(EntityDeathEvent.class), captor.capture());
        Consumer<EntityDeathEvent> handler = captor.getValue();

        // Kill non-player entity
        handler.accept(new EntityDeathEvent(2L, 1L));
        verify(questSystem).updateProgress("test_quest", goalId, 0.5);

        // Kill another non-player entity
        handler.accept(new EntityDeathEvent(3L, 1L));
        verify(questSystem).updateProgress("test_quest", goalId, 1.0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testPlayerDeathDoesNotIncrementKills() {
        killQuestLogic.initialize(quest, questSystem);

        ArgumentCaptor<Consumer<EntityDeathEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(EntityDeathEvent.class), captor.capture());
        Consumer<EntityDeathEvent> handler = captor.getValue();

        // Player dies
        handler.accept(new EntityDeathEvent(playerId, 1L));
        verify(questSystem, never()).updateProgress(any(), any(), anyDouble());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testKillsDoNotExceedTarget() {
        killQuestLogic.initialize(quest, questSystem);

        ArgumentCaptor<Consumer<EntityDeathEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(EntityDeathEvent.class), captor.capture());
        Consumer<EntityDeathEvent> handler = captor.getValue();

        // Kill 1
        handler.accept(new EntityDeathEvent(2L, 1L));
        // Kill 2
        handler.accept(new EntityDeathEvent(3L, 1L));
        
        reset(questSystem);

        // Kill 3 (beyond target)
        handler.accept(new EntityDeathEvent(4L, 1L));
        verify(questSystem, never()).updateProgress(any(), any(), anyDouble());
    }
}
