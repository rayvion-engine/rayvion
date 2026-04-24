package com.rayvion.game.quest.logic;

import com.rayvion.engine.entity.EntityDeathEvent;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.quest.Quest;
import com.rayvion.engine.quest.QuestLogic;
import com.rayvion.engine.quest.QuestSystem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class KillQuestLogic implements QuestLogic {
    private final EventManager eventManager;
    private final String goalId;
    private final int targetKills;
    private final long playerId;

    private int currentKills = 0;

    @Override
    public void initialize(Quest quest, QuestSystem system) {
        log.info("Initializing KillQuestLogic for quest {} and goal {}", quest.getId(), goalId);
        eventManager.subscribe(EntityDeathEvent.class, event -> {
            if (event.entityId() == playerId) {
                return;
            }

            if (currentKills < targetKills) {
                currentKills++;
                double progress = Math.min(1.0, (double) currentKills / targetKills);
                system.updateProgress(quest.getId(), goalId, progress);
                log.info("Quest '{}' progress updated: {}/{} ({}%)", 
                        quest.getName(), currentKills, targetKills, (int)(progress * 100));
            }
        });
    }
}
