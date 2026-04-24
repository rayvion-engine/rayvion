package com.rayvion.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Input;
import com.rayvion.engine.bindings.BindingEvent;
import com.rayvion.engine.bindings.BindingGroup;
import com.rayvion.engine.bindings.BindingParameter;
import com.rayvion.engine.bindings.BindingSystem;
import com.rayvion.engine.bindings.impl.BindingSystemImpl;
import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.entity.EntitySystem;
import com.rayvion.engine.entity.EntitySystemImpl;
import com.rayvion.engine.ai.AiSystem;
import com.rayvion.engine.ai.impl.AiSystemImpl;
import com.rayvion.engine.graphics.*;
import com.rayvion.game.ai.MeleeAiStrategy;
import com.rayvion.game.ai.PathfindingAiStrategy;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.event.impl.DefaultEventManager;
import com.rayvion.engine.graphics.impl.GraphicsSystemImpl;
import com.rayvion.engine.scheduler.SchedulerSystem;
import com.rayvion.engine.scheduler.impl.DefaultSchedulerSystem;
import com.rayvion.engine.system.manager.SystemManager;
import com.rayvion.engine.system.tick.TickSystem;
import com.rayvion.engine.system.tick.impl.DefaultTickSystem;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;
import com.rayvion.engine.transform.impl.TransformSystemImpl;
import com.rayvion.engine.world.WorldSystem;
import com.rayvion.engine.world.impl.WorldSystemImpl;
import com.rayvion.engine.physics.PhysicsSystem;
import com.rayvion.engine.physics.impl.PhysicsSystemImpl;
import com.rayvion.engine.inventory.InventorySystem;
import com.rayvion.engine.inventory.impl.InventorySystemImpl;
import com.rayvion.engine.input.InputSystem;
import com.rayvion.game.input.LibGdxInputSystem;
import com.rayvion.game.render.LibGdxRenderingSystem;
import com.rayvion.engine.graphics.impl.CameraSystemImpl;
import com.rayvion.engine.characteristic.CharacteristicSystem;
import com.rayvion.engine.characteristic.CharacteristicDescriptor;
import com.rayvion.engine.characteristic.impl.CharacteristicSystemImpl;
import com.rayvion.engine.inventory.GroundItemSystem;
import com.rayvion.engine.inventory.Inventory;
import com.rayvion.engine.inventory.InventoryItem;
import com.rayvion.engine.inventory.impl.GroundItemSystemImpl;
import com.rayvion.engine.equipment.EquipmentSystem;
import com.rayvion.engine.equipment.impl.EquipmentSystemImpl;
import com.rayvion.game.ui.*;
import com.badlogic.gdx.InputMultiplexer;
import com.rayvion.engine.input.KeyEvent;
import com.rayvion.game.combat.SwordSystem;
import com.rayvion.game.combat.HandgunSystem;
import com.rayvion.game.combat.DeathSystem;
import com.rayvion.game.combat.DamageFeedbackSystem;
import com.rayvion.engine.quest.Quest;
import com.rayvion.engine.quest.QuestGoal;
import com.rayvion.engine.quest.QuestSystem;
import com.rayvion.engine.quest.impl.QuestSystemImpl;
import com.rayvion.engine.entity.EntityDeathEvent;
import com.rayvion.game.consumable.ConsumableSystem;
import com.rayvion.game.quest.logic.KillQuestLogic;
import com.rayvion.game.combat.HealingSystem;
import com.rayvion.engine.inventory.ConsumableItemUseEvent;
import com.rayvion.engine.audio.AudioSystem;
import com.rayvion.game.audio.LibGdxAudioSystem;
import com.rayvion.game.mechanism.MechanismSystem;
import com.rayvion.engine.equipment.EquippedEvent;
import com.rayvion.engine.equipment.UnequippedEvent;
import com.rayvion.game.AnimationStateSystem;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class EchoesBelowGame extends ApplicationAdapter {
    private static final double MAP_TILE_SIZE = 32.0;
    private static final int MAP_WIDTH = 40;
    private static final int MAP_HEIGHT = 30;

    private SystemManager systemManager;
    private LibGdxRenderingSystem renderingSystem;
    private InventoryUI inventoryUI;
    private HudUI hudUI;
    private QuestHudUI questHudUI;
    private GameOverUI gameOverUI;
    private VictoryUI victoryUI;
    private MainMenuUI mainMenuUI;
    private InputMultiplexer inputMultiplexer;
    private boolean playerDead = false;
    private boolean playerWon = false;

    public enum GameState {
        MAIN_MENU,
        PLAYING,
        GAME_OVER,
        VICTORY
    }

    private GameState gameState = GameState.MAIN_MENU;

    // Engine Systems
    private TickSystem tickSystem;
    private GraphicsSystem graphicsSystem;
    private TransformSystem transformSystem;
    private WorldSystem worldSystem;
    private EntitySystem entitySystem;
    private PhysicsSystem physicsSystem;
    private EventManager eventManager;
    private AiSystem aiSystem;
    private CameraSystem cameraSystem;
    private CharacteristicSystem characteristicSystem;
    private GroundItemSystem groundItemSystem;
    private InventorySystem inventorySystem;
    private BindingSystem bindingSystem;
    private EquipmentSystem equipmentSystem;
    private QuestSystem questSystem;
    private DamageFeedbackSystem damageFeedbackSystem;
    private AudioSystem audioSystem;
    private MechanismSystem mechanismSystem;
    private InputSystem inputSystem;
    private long currentWorldId = 0;
    private long currentPlayerId;

    @Override
    public void create() {
        currentWorldId = 0; // Initialize world ID early
        systemManager = new SystemManager();

        // 1. Initialize core systems
        eventManager = new DefaultEventManager();
        SchedulerSystem schedulerSystem = new DefaultSchedulerSystem();
        tickSystem = new DefaultTickSystem();
        transformSystem = new TransformSystemImpl();
        graphicsSystem = new GraphicsSystemImpl();
        entitySystem = new EntitySystemImpl();
        worldSystem = new WorldSystemImpl();
        physicsSystem = new PhysicsSystemImpl();
        aiSystem = new AiSystemImpl();
        cameraSystem = new CameraSystemImpl();
        inventorySystem = new InventorySystemImpl(eventManager);
        inventorySystem.init();

        characteristicSystem = new CharacteristicSystemImpl(eventManager);
        characteristicSystem.init();

        questSystem = new QuestSystemImpl();
        questSystem.init();

        // Register default characteristics
        characteristicSystem.registerCharacteristic(new CharacteristicDescriptor<>("health", "Health", "Current vitality", Double.class, 100.0));
        characteristicSystem.registerCharacteristic(new CharacteristicDescriptor<>("max_health", "Max Health", "Maximum vitality", Double.class, 100.0));
        characteristicSystem.registerCharacteristic(new CharacteristicDescriptor<>("speed", "Speed", "Movement speed", Double.class, 200.0));
        characteristicSystem.registerCharacteristic(new CharacteristicDescriptor<>("strength", "Strength", "Attack power", Double.class, 10.0));
        characteristicSystem.registerCharacteristic(new CharacteristicDescriptor<>("defense", "Defense", "Damage reduction", Double.class, 0.0));
        characteristicSystem.registerCharacteristic(new CharacteristicDescriptor<>("facing_angle", "Facing Angle", "Direction entity is facing", Double.class, 0.0));
        characteristicSystem.registerCharacteristic(new CharacteristicDescriptor<>("width", "Width", "Visual and physical width", Double.class, 32.0));
        characteristicSystem.registerCharacteristic(new CharacteristicDescriptor<>("height", "Height", "Visual and physical height", Double.class, 32.0));
        characteristicSystem.registerCharacteristic(new CharacteristicDescriptor<>("is_ground_item", "Is Ground Item", "Whether this entity is an item on the ground", Double.class, 0.0));
        characteristicSystem.registerCharacteristic(new CharacteristicDescriptor<>("animation_state", "Animation State", "Current action state", String.class, "idle"));
        characteristicSystem.registerCharacteristic(new CharacteristicDescriptor<>("equipment_state", "Equipment State", "Current equipment state", String.class, "unarmed"));
        characteristicSystem.registerCharacteristic(new CharacteristicDescriptor<>("animation_map", "Animation Map", "Map of state to graphics", java.util.Map.class, null));
        
        audioSystem = new LibGdxAudioSystem();
        audioSystem.init();

        groundItemSystem = new GroundItemSystemImpl(entitySystem, transformSystem, inventorySystem, worldSystem, graphicsSystem, audioSystem, characteristicSystem);
        groundItemSystem.init();

        mechanismSystem = new MechanismSystem(entitySystem, transformSystem, graphicsSystem, physicsSystem, audioSystem, inventorySystem, currentWorldId);
        mechanismSystem.init();

        // 2. Add them to SystemManager
        systemManager.addSystem(schedulerSystem);
        systemManager.addSystem(tickSystem);
        systemManager.addSystem(transformSystem);
        systemManager.addSystem(graphicsSystem);
        systemManager.addSystem(entitySystem);
        systemManager.addSystem(worldSystem);
        systemManager.addSystem(physicsSystem);
        systemManager.addSystem(aiSystem);
        systemManager.addSystem(cameraSystem);
        systemManager.addSystem(inventorySystem);
        systemManager.addSystem(characteristicSystem);
        systemManager.addSystem(groundItemSystem);
        systemManager.addSystem(mechanismSystem);
        systemManager.addSystem(questSystem);
        systemManager.addSystem(eventManager);
        
        AnimationStateSystem animationStateSystem = new AnimationStateSystem();
        systemManager.addSystem(animationStateSystem);

        damageFeedbackSystem = new DamageFeedbackSystem();
        systemManager.addSystem(damageFeedbackSystem);

        systemManager.addSystem(audioSystem);

        // Start background music
        audioSystem.playMusic("bgm");

        HealingSystem healingSystem = new HealingSystem();
        systemManager.addSystem(healingSystem);

        ConsumableSystem consumableSystem = new ConsumableSystem();
        systemManager.addSystem(consumableSystem);

        // 3. Initialize input system
        inputSystem = new LibGdxInputSystem(eventManager);
        systemManager.addSystem(inputSystem);

        // 3.1 Initialize binding system
        bindingSystem = new BindingSystemImpl(eventManager);
        systemManager.addSystem(bindingSystem);

        // 3.2 Initialize equipment system
        equipmentSystem = new EquipmentSystemImpl();
        systemManager.addSystem(equipmentSystem);

        // Setup default bindings
        BindingGroup movement = bindingSystem.createGroup("Movement");
        BindingParameter forward = bindingSystem.createParameter(movement, "Forward");
        bindingSystem.addBinding(forward, Input.Keys.W);
        bindingSystem.addBinding(forward, Input.Keys.UP);

        BindingParameter backward = bindingSystem.createParameter(movement, "Backward");
        bindingSystem.addBinding(backward, Input.Keys.S);
        bindingSystem.addBinding(backward, Input.Keys.DOWN);

        BindingParameter left = bindingSystem.createParameter(movement, "Left");
        bindingSystem.addBinding(left, Input.Keys.A);
        bindingSystem.addBinding(left, Input.Keys.LEFT);

        BindingParameter right = bindingSystem.createParameter(movement, "Right");
        bindingSystem.addBinding(right, Input.Keys.D);
        bindingSystem.addBinding(right, Input.Keys.RIGHT);

        BindingGroup interaction = bindingSystem.createGroup("Interaction");
        BindingParameter interact = bindingSystem.createParameter(interaction, "Interact");
        bindingSystem.addBinding(interact, Input.Keys.E);
        bindingSystem.addBinding(interact, Input.Keys.F);

        BindingParameter attack = bindingSystem.createParameter(interaction, "Attack");
        bindingSystem.addBinding(attack, Input.Keys.SPACE);

        BindingParameter inventory = bindingSystem.createParameter(interaction, "Inventory");
        bindingSystem.addBinding(inventory, Input.Keys.I);

        eventManager.subscribe(BindingEvent.class, event -> {
            log.debug("EchoesBelowGame: BindingEvent: {} Type: {}", event.parameter().name(), event.type());
            if (event.parameter().equals(interact) && event.type() == KeyEvent.Type.KEY_DOWN) {
                groundItemSystem.tryInteract(new Entity(currentPlayerId));
                mechanismSystem.tryInteract(new Entity(currentPlayerId));
            }
            if (event.parameter().equals(inventory) && event.type() == KeyEvent.Type.KEY_DOWN) {
                inventoryUI.setVisible(!inventoryUI.isVisible());
            }
        });

        // Subscribe to item use
        eventManager.subscribe(ConsumableItemUseEvent.class, event -> {
            log.info("Item used: {} by entity {}", event.item().name(), event.user().id());
            if ("first_aid_kit".equals(event.item().id())) {
                healingSystem.addEffect(event.user(), 100.0, 5.0);
            }
        });

        // Listen for equipment changes to update animation state
        eventManager.subscribe(EquippedEvent.class, event -> {
            if (event.entityId() == currentPlayerId) {
                String type = event.item().type().toLowerCase();
                String id = event.item().id().toLowerCase();
                String weaponState = "unarmed";
                
                if (type.contains("rifle") || id.contains("rifle")) weaponState = "rifle";
                else if (type.contains("sword") || id.contains("sword")) weaponState = "sword";
                else if (type.contains("handgun") || id.contains("handgun") || id.contains("pistol")) weaponState = "handgun";
                
                characteristicSystem.setValue(new Entity(currentPlayerId), "equipment_state", weaponState);
            }
        });

        eventManager.subscribe(UnequippedEvent.class, event -> {
            if (event.entityId() == currentPlayerId) {
                characteristicSystem.setValue(new Entity(currentPlayerId), "equipment_state", "unarmed");
            }
        });

        // 4. Initialize rendering system
        renderingSystem = createRenderingSystem();
        renderingSystem.init();

        // 5. Initialize Main Menu
        mainMenuUI = new MainMenuUI(this::startGame);

        // 6. Setup input multiplexer for Main Menu
        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(mainMenuUI.getStage());
        Gdx.input.setInputProcessor(inputMultiplexer);
        
        gameState = GameState.MAIN_MENU;
    }

    private void startGame() {
        log.info("Starting game from Main Menu...");
        
        // 1. Setup basic game state (world, player, etc.)
        setupBasicGame();

        // 2. Initialize gameplay UI
        inventoryUI = createInventoryUI();
        hudUI = createHudUI();
        questHudUI = createQuestHudUI();
        gameOverUI = createGameOverUI();
        victoryUI = new VictoryUI();

        // 3. Subscribe to player death
        eventManager.subscribe(EntityDeathEvent.class, event -> {
            if (event.entityId() == currentPlayerId) {
                playerDead = true;
                gameState = GameState.GAME_OVER;
                gameOverUI.show();
                log.info("Player has died — showing Game Over screen");
            } else {
                // Check if all enemies are dead
                checkWinCondition();
            }
        });

        // 4. Update input multiplexer for gameplay
        inputMultiplexer.clear();
        inputMultiplexer.addProcessor(inventoryUI.getStage());
        inputMultiplexer.addProcessor((LibGdxInputSystem) inputSystem);
        
        gameState = GameState.PLAYING;
    }

    private void setupBasicGame() {
        log.info("EchoesBelowGame: Starting Cyberpunk Echoes Below...");

        // --- Map Layout (40x30) ---
        // Bottom-left: Detention Cell 7 (player start)
        // Center: Maintenance Sector (corridor with enemies)
        // Top-left: Server Control (melee guard + lever)
        // Right side: Executive Chamber (behind gate, boss)
        int width = MAP_WIDTH;
        int height = MAP_HEIGHT;
        String[][] tiles = new String[height][width];

        // Fill everything with walls first
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[y][x] = "wall";
            }
        }

        // --- Carve out rooms ---

        // Room 1: Detention Cell 7 (bottom-left) x:1-9, y:1-8
        for (int y = 1; y <= 8; y++)
            for (int x = 1; x <= 9; x++)
                tiles[y][x] = "floor";

        // Corridor from Detention Cell going up: x:4-6, y:9-14
        for (int y = 9; y <= 14; y++)
            for (int x = 4; x <= 6; x++)
                tiles[y][x] = "floor";

        // Room 2: Maintenance Sector (center) x:1-22, y:15-22
        for (int y = 15; y <= 22; y++)
            for (int x = 1; x <= 22; x++)
                tiles[y][x] = "floor";

        // Add some cover/pillars in Maintenance Sector
        tiles[17][8] = "wall";
        tiles[17][9] = "wall";
        tiles[20][14] = "wall";
        tiles[20][15] = "wall";
        tiles[18][18] = "wall";

        // Room 3: Server Control (top-left) x:1-12, y:23-28
        for (int y = 23; y <= 28; y++)
            for (int x = 1; x <= 12; x++)
                tiles[y][x] = "floor";

        // Doorway from Maintenance to Server Control: x:4-6, y:23 (already floor from room carve)

        // Room 4: Executive Chamber (right side) x:25-38, y:15-28
        for (int y = 15; y <= 28; y++)
            for (int x = 25; x <= 38; x++)
                tiles[y][x] = "floor";

        // The gate passage: x:23-24, y:18-19 — we leave as wall, gate entity goes here
        // Open the passage tiles so the gate entity blocks it
        tiles[18][23] = "floor";
        tiles[19][23] = "floor";
        tiles[18][24] = "floor";
        tiles[19][24] = "floor";

        // Add pillars inside Executive Chamber
        tiles[20][30] = "wall";
        tiles[25][33] = "wall";
        tiles[22][28] = "wall";

        TiledWorldGraphics worldGraphics = new TiledWorldGraphics(width, height, 32.0, tiles);
        graphicsSystem.setWorldGraphics(worldGraphics);

        // Add physics bodies for walls
        double tileSize = worldGraphics.tileSize();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ("wall".equals(tiles[y][x])) {
                    double centerX = x * tileSize + tileSize / 2.0;
                    double centerY = y * tileSize + tileSize / 2.0;
                    physicsSystem.createStaticBoxBody(currentWorldId, centerX, centerY, tileSize, tileSize);
                }
            }
        }

        // Add a world
        worldSystem.addWorld(new com.rayvion.engine.world.World() {
            @Override public long getId() { return currentWorldId; }
        });

        // --- Player Setup (spawns in Detention Cell 7) ---
        Entity player = entitySystem.createEntity();
        currentPlayerId = player.id();
        worldSystem.addEntityToWorld(currentWorldId, currentPlayerId);

        Transform t = new Transform();
        t.setX(5 * tileSize + tileSize / 2.0);  // center of cell
        t.setY(4 * tileSize + tileSize / 2.0);
        t.setZ(0);
        transformSystem.setTransform(currentPlayerId, t);

        physicsSystem.createCircleBody(currentWorldId, currentPlayerId, 16.0, false);

        characteristicSystem.setValue(player, "health", 150.0);
        characteristicSystem.setValue(player, "max_health", 150.0);
        characteristicSystem.setValue(player, "speed", 240.0);

        PlayerMovementSystem playerMovementSystem = new PlayerMovementSystem(currentWorldId, currentPlayerId);
        systemManager.addSystem(playerMovementSystem);

        SwordSystem swordSystem = new SwordSystem(currentWorldId, currentPlayerId);
        systemManager.addSystem(swordSystem);

        com.rayvion.game.combat.RifleSystem rifleSystem = new com.rayvion.game.combat.RifleSystem(currentWorldId, currentPlayerId);
        systemManager.addSystem(rifleSystem);

        HandgunSystem handgunSystem = new HandgunSystem(currentWorldId, currentPlayerId);
        systemManager.addSystem(handgunSystem);

        com.rayvion.game.combat.ShieldSystem shieldSystem = new com.rayvion.game.combat.ShieldSystem();
        systemManager.addSystem(shieldSystem);

        DeathSystem deathSystem = new DeathSystem(currentWorldId);
        systemManager.addSystem(deathSystem);

        // Setup player animation map
        Map<String, EntityGraphics> animMap = getStringEntityGraphicsMap();
        characteristicSystem.setValue(player, "animation_map", animMap);
        characteristicSystem.setValue(player, "animation_state", "idle");
        characteristicSystem.setValue(player, "equipment_state", "unarmed");
        characteristicSystem.setValue(player, "width", 48.0);
        characteristicSystem.setValue(player, "height", 40.0);

        // --- Player Inventory (cyberpunk themed) ---
        Inventory playerInventory = inventorySystem.createInventory(player);
        playerInventory.addItem(new InventoryItem("sword_01", "Thermal Katana", "A superheated mono-molecular blade", "weapon", new TextureGraphics("sword"), new TextureGraphics("player_sword"), false));
        playerInventory.addItem(new InventoryItem("first_aid_kit", "Trauma Kit", "Military-grade nanite healing. Restores 100 HP over 5s", "consumable", new TextureGraphics("first_aid_kit"), false));

        // Camera
        cameraSystem.setTargetEntity(player.id());
        cameraSystem.setZoom(0.5f);

        // --- Ground Items ---
        // Smart Pistol in the Detention Cell
        groundItemSystem.dropItem(currentWorldId,
            new InventoryItem("handgun_01", "Smart Pistol", "Neural-linked semi-auto sidearm", "weapon",
                new TextureGraphics("survivor-idle_handgun_0"), new TextureGraphics("player"), false),
            3 * tileSize, 2 * tileSize);

        // Med-Stim near corridor entrance
        groundItemSystem.dropItem(currentWorldId,
            new InventoryItem("first_aid_kit", "Med-Stim", "Quick-inject healing stimulant", "consumable",
                new TextureGraphics("first_aid_kit"), false),
            5 * tileSize, 10 * tileSize);

        // Credits scattered in Maintenance Sector
        groundItemSystem.dropItem(currentWorldId,
            new InventoryItem("gold", "Credits", "Corporate digital currency", "currency",
                new TextureGraphics("gold"), true),
            10 * tileSize, 18 * tileSize);
        groundItemSystem.dropItem(currentWorldId,
            new InventoryItem("gold", "Credits", "Corporate digital currency", "currency",
                new TextureGraphics("gold"), true),
            16 * tileSize, 20 * tileSize);

        // Plasma Rifle inside Executive Chamber (reward for getting past the gate)
        groundItemSystem.dropItem(currentWorldId,
            new InventoryItem("rifle_01", "Plasma Rifle", "Fires superheated plasma bolts", "weapon",
                new TextureGraphics("rifle"), new TextureGraphics("player_rifle"), false),
            30 * tileSize, 20 * tileSize);

        // --- Enemies ---
        // Maintenance Sector: 3 Corporate Guards
        spawnEnemy(6 * tileSize, 17 * tileSize, currentPlayerId);
        spawnEnemy(12 * tileSize, 19 * tileSize, currentPlayerId);
        spawnEnemy(18 * tileSize, 16 * tileSize, currentPlayerId);

        // Server Control: Cyber-Ninja (melee guard)
        spawnMeleeEnemy(7 * tileSize, 26 * tileSize, currentPlayerId);

        // Executive Chamber: Corporate Overseer (boss)
        spawnBossEnemy(32 * tileSize, 22 * tileSize, currentPlayerId);

        // --- Quests ---
        setupSystemOverrideQuest();
        setupAccessGrantedQuest();
        setupExecutiveDeletionQuest();

        // --- Gate & Lever ---
        spawnGateAndLever();
    }

    private static Map<String, EntityGraphics> getStringEntityGraphicsMap() {
        Map<String, EntityGraphics> animMap = new java.util.HashMap<>();

        AnimationGraphics idleHandgun = new AnimationGraphics(
            List.of(
                "survivor-idle_handgun_0", "survivor-idle_handgun_1", "survivor-idle_handgun_2", "survivor-idle_handgun_3",
                "survivor-idle_handgun_4", "survivor-idle_handgun_5", "survivor-idle_handgun_6", "survivor-idle_handgun_7",
                "survivor-idle_handgun_8", "survivor-idle_handgun_9", "survivor-idle_handgun_10"
            ),
            0.05, true
        );

        AnimationGraphics moveHandgun = new AnimationGraphics(
            List.of(
                "survivor-move_handgun_0", "survivor-move_handgun_1", "survivor-move_handgun_2", "survivor-move_handgun_3",
                "survivor-move_handgun_4", "survivor-move_handgun_5", "survivor-move_handgun_6", "survivor-move_handgun_7",
                "survivor-move_handgun_8", "survivor-move_handgun_9", "survivor-move_handgun_10"
            ),
            0.05, true
        );

        // Use handgun animations for unarmed as fallback for now
        animMap.put("idle_unarmed", idleHandgun);
        animMap.put("move_unarmed", moveHandgun);
        animMap.put("idle_handgun", idleHandgun);
        animMap.put("move_handgun", moveHandgun);

        // Define placeholders for rifle and sword using TextureGraphics since we don't have animations yet
        animMap.put("idle_rifle", new TextureGraphics("player_rifle"));
        animMap.put("move_rifle", new TextureGraphics("player_rifle"));

        // Sword animations (Atlas-style Wagging)
        AnimationGraphics idleSword = new AnimationGraphics(
            List.of(
                "survivor-idle_handgun_0", "survivor-idle_handgun_1", "survivor-idle_handgun_2", "survivor-idle_handgun_3",
                "survivor-idle_handgun_4", "survivor-idle_handgun_5", "survivor-idle_handgun_6", "survivor-idle_handgun_7",
                "survivor-idle_handgun_8", "survivor-idle_handgun_9", "survivor-idle_handgun_10"
            ),
            0.05, true
        );
        AnimationGraphics moveSword = new AnimationGraphics(
            List.of(
                "survivor-move_handgun_0", "survivor-move_handgun_1", "survivor-move_handgun_2", "survivor-move_handgun_3",
                "survivor-move_handgun_4", "survivor-move_handgun_5", "survivor-move_handgun_6", "survivor-move_handgun_7",
                "survivor-move_handgun_8", "survivor-move_handgun_9", "survivor-move_handgun_10"
            ),
            0.05, true
        );
        animMap.put("idle_sword", idleSword);
        animMap.put("move_sword", moveSword);

        // Attack animation for sword (Fast swing)
        AnimationGraphics attackSword = new AnimationGraphics(
            List.of("player_attack", "player_attack_2", "player_attack"),
            0.05, false
        );
        animMap.put("attack_sword", attackSword);

        AnimationGraphics shootHandgunAnim = new AnimationGraphics(
            List.of("survivor-shoot_handgun_0", "survivor-shoot_handgun_1", "survivor-shoot_handgun_2"),
            0.05, false
        );
        animMap.put("shoot_handgun", shootHandgunAnim);

        AnimationGraphics shootRifleAnim = new AnimationGraphics(
            List.of("player_shoot", "player_rifle"),
            0.05, false
        );
        animMap.put("shoot_rifle", shootRifleAnim);
        return animMap;
    }

    private void spawnGateAndLever() {
        double tileSize = 32.0;

        // 1. Create Security Gate (blocks passage to Executive Chamber)
        Entity gate = entitySystem.createEntity();
        worldSystem.addEntityToWorld(currentWorldId, gate.id());
        
        Transform gateTransform = new Transform();
        gateTransform.setX(23.5 * tileSize);  // center of passage x:23-24
        gateTransform.setY(18.5 * tileSize);  // center of passage y:18-19
        transformSystem.setTransform(gate.id(), gateTransform);
        
        physicsSystem.createBoxBody(currentWorldId, gate.id(), 64, 64, true);
        graphicsSystem.setEntityGraphics(gate.id(), new TextureGraphics("gate_closed"));
        characteristicSystem.setValue(gate, "width", 64.0);
        characteristicSystem.setValue(gate, "height", 64.0);
        
        // 2. Create Terminal (lever) in Server Control room
        Entity lever = entitySystem.createEntity();
        worldSystem.addEntityToWorld(currentWorldId, lever.id());
        
        Transform leverTransform = new Transform();
        leverTransform.setX(10 * tileSize);  // inside Server Control
        leverTransform.setY(26 * tileSize);
        transformSystem.setTransform(lever.id(), leverTransform);
        
        graphicsSystem.setEntityGraphics(lever.id(), new TextureGraphics("lever_off"));
        characteristicSystem.setValue(lever, "width", 32.0);
        characteristicSystem.setValue(lever, "height", 32.0);
        
        // 3. Register in MechanismSystem
        mechanismSystem.registerLever(lever.id(), gate.id());
        log.info("Spawned security gate {} and terminal {}", gate.id(), lever.id());
    }

    private void setupSystemOverrideQuest() {
        Quest quest = new Quest("system_override", "System Override", "Eliminate the corporate guards patrolling the maintenance sector.");
        QuestGoal killGoal = new QuestGoal("kill_guards", "Neutralize 3 Corporate Guards", 0.0);
        quest.addGoal(killGoal);
        quest.setLogic(new KillQuestLogic(eventManager, "kill_guards", 3, currentPlayerId));
        questSystem.registerQuest(quest);
    }

    private void setupAccessGrantedQuest() {
        Quest quest = new Quest("access_granted", "Access Granted", "Take down the cyber-ninja guarding the server terminal.");
        QuestGoal killGoal = new QuestGoal("kill_ninja", "Neutralize the Cyber-Ninja", 0.0);
        quest.addGoal(killGoal);
        quest.setLogic(new KillQuestLogic(eventManager, "kill_ninja", 1, currentPlayerId));
        questSystem.registerQuest(quest);
    }

    private void setupExecutiveDeletionQuest() {
        Quest quest = new Quest("executive_deletion", "Executive Deletion", "Assassinate the Corporate Overseer in the executive chamber.");
        QuestGoal killGoal = new QuestGoal("kill_boss", "Eliminate the Corporate Overseer", 0.0);
        quest.addGoal(killGoal);
        quest.setLogic(new KillQuestLogic(eventManager, "kill_boss", 1, currentPlayerId));
        questSystem.registerQuest(quest);
    }

    private void spawnEnemy(double x, double y, long targetId) {
        Entity enemy = entitySystem.createEntity();
        worldSystem.addEntityToWorld(currentWorldId, enemy.id());

        Transform t = new Transform();
        t.setX(x);
        t.setY(y);
        transformSystem.setTransform(enemy.id(), t);

        physicsSystem.createCircleBody(currentWorldId, enemy.id(), 16.0, false);
        graphicsSystem.setEntityGraphics(enemy.id(), new TextureGraphics("enemy_smart"));
        characteristicSystem.setValue(enemy, "width", 32.0);
        characteristicSystem.setValue(enemy, "height", 32.0);

        // Set enemy characteristics
        characteristicSystem.setValue(enemy, "health", 50.0);
        characteristicSystem.setValue(enemy, "max_health", 50.0);
        characteristicSystem.setValue(enemy, "speed", 120.0);
        aiSystem.setStrategy(enemy.id(), new PathfindingAiStrategy(
            targetId, currentWorldId, transformSystem, physicsSystem, characteristicSystem, MAP_TILE_SIZE, MAP_WIDTH, MAP_HEIGHT
        ));

        graphicsSystem.setHealthBarVisible(enemy.id(), true);
    }

    private void spawnMeleeEnemy(double x, double y, long targetId) {
        Entity enemy = entitySystem.createEntity();
        worldSystem.addEntityToWorld(currentWorldId, enemy.id());

        Transform t = new Transform();
        t.setX(x);
        t.setY(y);
        transformSystem.setTransform(enemy.id(), t);

        physicsSystem.createCircleBody(currentWorldId, enemy.id(), 16.0, false);
        graphicsSystem.setEntityGraphics(enemy.id(), new TextureGraphics("enemy_smart"));
        characteristicSystem.setValue(enemy, "width", 32.0);
        characteristicSystem.setValue(enemy, "height", 32.0);

        // Set enemy characteristics
        characteristicSystem.setValue(enemy, "health", 80.0);
        characteristicSystem.setValue(enemy, "max_health", 80.0);
        characteristicSystem.setValue(enemy, "speed", 90.0);
        characteristicSystem.setValue(enemy, "strength", 15.0);

        aiSystem.setStrategy(enemy.id(), new MeleeAiStrategy(
            targetId, currentWorldId, transformSystem, physicsSystem, characteristicSystem, eventManager, MAP_TILE_SIZE, MAP_WIDTH, MAP_HEIGHT
        ));
        
        // Setup enemy animation map
        java.util.Map<String, com.rayvion.engine.graphics.EntityGraphics> enemyAnimMap = new java.util.HashMap<>();
        enemyAnimMap.put("idle_sword", new TextureGraphics("enemy_smart"));
        enemyAnimMap.put("move_sword", new TextureGraphics("enemy_smart"));
        enemyAnimMap.put("attack_sword", new TextureGraphics("enemy_attack"));
        
        characteristicSystem.setValue(enemy, "animation_map", enemyAnimMap);
        characteristicSystem.setValue(enemy, "animation_state", "idle");
        characteristicSystem.setValue(enemy, "equipment_state", "sword");

        // Give sword
        Inventory inventory = inventorySystem.createInventory(enemy);
        InventoryItem sword = new InventoryItem("enemy_sword", "Iron Sword", "A sharp iron sword", "weapon", new TextureGraphics("sword"), false);
        inventory.addItem(sword);
        equipmentSystem.equip(enemy.id(), sword);

        graphicsSystem.setHealthBarVisible(enemy.id(), true);
    }

    private void spawnBossEnemy(double x, double y, long targetId) {
        Entity boss = entitySystem.createEntity();
        worldSystem.addEntityToWorld(currentWorldId, boss.id());

        Transform t = new Transform();
        t.setX(x);
        t.setY(y);
        transformSystem.setTransform(boss.id(), t);

        physicsSystem.createCircleBody(currentWorldId, boss.id(), 20.0, false);
        graphicsSystem.setEntityGraphics(boss.id(), new TextureGraphics("enemy_smart"));
        characteristicSystem.setValue(boss, "width", 48.0);
        characteristicSystem.setValue(boss, "height", 48.0);

        // Boss stats: tough, fast, hits hard
        characteristicSystem.setValue(boss, "health", 200.0);
        characteristicSystem.setValue(boss, "max_health", 200.0);
        characteristicSystem.setValue(boss, "speed", 130.0);
        characteristicSystem.setValue(boss, "strength", 25.0);
        characteristicSystem.setValue(boss, "defense", 5.0);

        aiSystem.setStrategy(boss.id(), new MeleeAiStrategy(
            targetId, currentWorldId, transformSystem, physicsSystem, characteristicSystem, eventManager, MAP_TILE_SIZE, MAP_WIDTH, MAP_HEIGHT
        ));

        // Setup boss animation map
        java.util.Map<String, com.rayvion.engine.graphics.EntityGraphics> bossAnimMap = new java.util.HashMap<>();
        bossAnimMap.put("idle_sword", new TextureGraphics("enemy_smart"));
        bossAnimMap.put("move_sword", new TextureGraphics("enemy_smart"));
        bossAnimMap.put("attack_sword", new TextureGraphics("enemy_attack"));

        characteristicSystem.setValue(boss, "animation_map", bossAnimMap);
        characteristicSystem.setValue(boss, "animation_state", "idle");
        characteristicSystem.setValue(boss, "equipment_state", "sword");

        // Give boss a weapon
        Inventory inventory = inventorySystem.createInventory(boss);
        InventoryItem sword = new InventoryItem("boss_blade", "Overseer's Blade", "A corporate-issue mono-edge blade", "weapon", new TextureGraphics("sword"), false);
        inventory.addItem(sword);
        equipmentSystem.equip(boss.id(), sword);

        graphicsSystem.setHealthBarVisible(boss.id(), true);
        log.info("Spawned Corporate Overseer (Boss) at ({}, {})", x, y);
    }

    @Override
    public void resize(int width, int height) {
        renderingSystem.resize(width, height);
        if (mainMenuUI != null) mainMenuUI.resize(width, height);
        if (inventoryUI != null) inventoryUI.resize(width, height);
        if (hudUI != null) hudUI.resize(width, height);
        if (questHudUI != null) questHudUI.resize(width, height);
        if (gameOverUI != null) gameOverUI.resize(width, height);
        if (victoryUI != null) victoryUI.resize(width, height);
    }

    private void restartGame() {
        log.info("Restarting game...");
        dispose();
        playerDead = false;
        playerWon = false;
        create();
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        if (gameState == GameState.MAIN_MENU) {
            mainMenuUI.render(delta);
            return;
        }

        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render the scene
        renderingSystem.render();
        
        // Render UI
        inventoryUI.render();
        hudUI.render();
        questHudUI.render();

        // Game Over overlay
        if (gameState == GameState.GAME_OVER) {
            gameOverUI.render();

            // Check for restart key
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                restartGame();
            }
            return;
        }

        // Victory overlay
        if (gameState == GameState.VICTORY) {
            victoryUI.render();

            // Check for restart key
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                restartGame();
            }
            return;
        }
        
        // Debug player position
        if (transformSystem.hasTransform(currentPlayerId)) {
            Transform t = transformSystem.getTransform(currentPlayerId);
            log.trace("Render: Player position: {}, {}", t.getX(), t.getY());
        }
    }

    @Override
    public void dispose() {
        renderingSystem.dispose();
        if (mainMenuUI != null) mainMenuUI.dispose();
        if (inventoryUI != null) inventoryUI.dispose();
        if (hudUI != null) hudUI.dispose();
        if (questHudUI != null) questHudUI.dispose();
        if (gameOverUI != null) gameOverUI.dispose();
        if (victoryUI != null) victoryUI.dispose();
        if (audioSystem != null) audioSystem.dispose();
    }

    // Testing Getters

    private void checkWinCondition() {
        if (playerDead || playerWon) return;

        // An enemy is an entity in the current world that:
        // 1. Is not the player
        // 2. Has a health characteristic
        long enemyCount = worldSystem.getEntities(currentWorldId).stream()
                .filter(id -> id != currentPlayerId)
                .filter(id -> characteristicSystem.hasCharacteristic(new Entity(id), "health"))
                .count();

        if (enemyCount == 0) {
            playerWon = true;
            gameState = GameState.VICTORY;
            victoryUI.show();
            log.info("All enemies neutralized! Victory screen triggered.");
        }
    }
    SystemManager getSystemManager() { return systemManager; }
    EventManager getEventManager() { return eventManager; }
    QuestSystem getQuestSystem() { return questSystem; }
    long getCurrentPlayerId() { return currentPlayerId; }

    protected LibGdxRenderingSystem createRenderingSystem() {
        return new LibGdxRenderingSystem(graphicsSystem, transformSystem, cameraSystem, characteristicSystem, damageFeedbackSystem);
    }

    protected InventoryUI createInventoryUI() {
        return new InventoryUI(inventorySystem, equipmentSystem, eventManager, currentPlayerId);
    }

    protected HudUI createHudUI() {
        return new HudUI(characteristicSystem, eventManager, currentPlayerId);
    }

    protected QuestHudUI createQuestHudUI() {
        return new QuestHudUI(questSystem);
    }

    protected GameOverUI createGameOverUI() {
        return new GameOverUI();
    }
}
