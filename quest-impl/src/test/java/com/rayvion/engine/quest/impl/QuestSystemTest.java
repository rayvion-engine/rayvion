package com.rayvion.engine.quest.impl;

import com.rayvion.engine.quest.Quest;
import com.rayvion.engine.quest.QuestGoal;
import com.rayvion.engine.quest.QuestSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuestSystemTest {
    private QuestSystem questSystem;

    @BeforeEach
    void setUp() {
        questSystem = new QuestSystemImpl();
        questSystem.init();
    }

    @Test
    void testQuestRegistrationAndProgress() {
        Quest quest = new Quest("main_quest", "Main Quest", "The primary objective");
        QuestGoal goal1 = new QuestGoal("collect_wood", "Collect 10 wood", 0.0);
        QuestGoal goal2 = new QuestGoal("build_house", "Build a house", 0.0);
        
        quest.addGoal(goal1);
        quest.addGoal(goal2);
        
        questSystem.registerQuest(quest);
        
        assertNotNull(questSystem.getQuest("main_quest"));
        assertEquals(2, questSystem.getQuest("main_quest").getGoals().size());
        
        // Updt progress of frst goal
        questSystem.updateProgress("main_quest", "collect_wood", 0.5);
        assertEquals(0.5, goal1.getProgress());
        assertFalse(quest.isCompleted());
        
        // Complete first goal
        questSystem.updateProgress("main_quest", "collect_wood", 1.0);
        assertTrue(goal1.isCompleted());
        assertFalse(quest.isCompleted());
        
        // Complete second goal
        questSystem.updateProgress("main_quest", "build_house", 1.0);
        assertTrue(goal2.isCompleted());
        assertTrue(quest.isCompleted());
    }

    @Test
    void testDynamicGoals() {
        Quest quest = new Quest("dynamic_quest", "Dynamic Quest", "Goals change over time");
        questSystem.registerQuest(quest);
        
        // Initially no goals
        assertFalse(quest.isCompleted());
        
        // Add a goal later
        QuestGoal goal1 = new QuestGoal("talk_to_npc", "Talk to the NPC", 0.0);
        quest.addGoal(goal1);
        
        assertEquals(1, quest.getGoals().size());
        
        // Update progress
        questSystem.updateProgress("dynamic_quest", "talk_to_npc", 1.0);
        assertTrue(goal1.isCompleted());
        assertTrue(quest.isCompleted());
        
        // Ad anothr goal aftr completn (re-openng the quest effectvly)
        QuestGoal goal2 = new QuestGoal("escort_npc", "Escort NPC to town", 0.0);
        quest.addGoal(goal2);
        
        assertFalse(quest.isCompleted());
        assertEquals(2, quest.getGoals().size());
    }
}
