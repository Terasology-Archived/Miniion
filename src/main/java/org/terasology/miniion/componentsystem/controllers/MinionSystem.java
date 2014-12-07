/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.miniion.componentsystem.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.action.GiveItemAction;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.miniion.components.AnimationComponent;
import org.terasology.miniion.components.MinionComponent;
import org.terasology.miniion.components.namesComponent;
import org.terasology.miniion.utilities.MinionRecipe;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.logic.AnimEndEvent;
import org.terasology.rendering.logic.SkeletalMeshComponent;

/**
 * Created with IntelliJ IDEA. User: Overdhose Date: 10/05/12 Time: 17:54
 * Minionsystem gives you some control over the minions. this is the home of the
 * minionbar.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class MinionSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(MinionSystem.class);
    
    @In
    private LocalPlayer localPlayer;
    @In
    private EntityManager entityManager;
    @In
    private InventoryManager inventoryManager;

    //private static final int PRIORITY_LOCAL_PLAYER_OVERRIDE = 160;
    private static boolean showactiveminion = false;
    private static boolean showSelectionOverlay = false;
    private static EntityRef activeminion;

    private static List<MinionRecipe> recipeslist = new ArrayList<MinionRecipe>();

    @Override
    public void initialise() {
//        // experimental popup menu for the minion command tool
//        guiManager.registerWindow("activeminiion", UIActiveMinion.class);
//        // ui to create summonable cards
//        guiManager.registerWindow("cardbook", UICardBook.class);
//        // ui to manage summoned minions, selecting one sets it active!
//        guiManager.registerWindow("oreobook", UIScreenBookOreo.class);
//        initRecipes();
    }

    @ReceiveEvent
    public void onPlayerSpawn(OnPlayerSpawnedEvent event, EntityRef player, InventoryComponent inventory) {
//        createAndGiveItemToPlayerIfPossible("miniion:minioncommand", player);
//        createAndGiveItemToPlayerIfPossible("miniion:cardbook", player);
//        createAndGiveItemToPlayerIfPossible("miniion:emptycard", player);
//        createAndGiveItemToPlayerIfPossible("miniion:emptycard", player);
//        createAndGiveItemToPlayerIfPossible("miniion:emptycard", player);
//        createAndGiveItemToPlayerIfPossible("Miniion:zonebook", player);
//        createAndGiveItemToPlayerIfPossible("Miniion:zonetool", player);
    }

    private void createAndGiveItemToPlayerIfPossible(String uri, EntityRef player) {
        EntityRef item = entityManager.create(uri);
        GiveItemAction action = new GiveItemAction(EntityRef.NULL, item);
        player.send(action);
        if (!action.isConsumed()) {
            logger.warn(uri + " could not be created and given to player.");
            item.destroy();
        }
            
    }

    /**
     * Ugly way to retrieve a name from a prefab
     * 
     * @return a ":" seperated string, with name and flavor text.
     */
    public static String getName() {
        PrefabManager prefMan = CoreRegistry.get(PrefabManager.class);
        Prefab prefab = prefMan.getPrefab("miniion:nameslist");
        EntityRef namelist = CoreRegistry.get(EntityManager.class).create(prefab);
        namelist.hasComponent(namesComponent.class);
        namesComponent namecomp = namelist.getComponent(namesComponent.class);
        Random rand = new Random();
        return namecomp.namelist.get(rand.nextInt(namecomp.namelist.size()));
    }

    /**
     * destroys a minion at the end of their dying animation this implies that
     * setting their animation to die will destroy them.
     */
    @ReceiveEvent(components = {SkeletalMeshComponent.class,
            AnimationComponent.class})
    public void onAnimationEnd(AnimEndEvent event, EntityRef entity) {
        AnimationComponent animcomp = entity
                .getComponent(AnimationComponent.class);
        if (animcomp != null && event.getAnimation().equals(animcomp.dieAnim)) {
            entity.destroy();
        }
    }

    // See package org.terasology.world.block.entity.BlockEntitySystem
    // public void onDestroyed(NoHealthEvent event, EntityRef entity) {
    // For some kinds of blocks, the block is automatically added to the instigator's inventory.
    // If this isn't sufficient, then we probably need to try to go find nearby blocks and pick them up
    // since the BlockToItem event types do not record the instigator.

    //        /**
    //         * triggered when a block was destroyed and dropped in the world used to
    //         * intercept gathering by minions and sending the block to their inventory
    //         * the droppedblock in the world then gets destroyed, possible duplication
    //         * exploit
    //         */
    //        @ReceiveEvent(components = { MinionComponent.class })
    //        public void onBlockDropped(BlockDroppedEvent event, EntityRef entity) {
    //                if (entity.hasComponent(MinionComponent.class)) {
    //                        EntityRef item;
    //                        if (event.getOldBlock().getEntityMode() == BlockEntityMode.PERSISTENT) {
    //                                item = blockItemFactory.newInstance(event.getOldBlock()
    //                                                .getBlockFamily(), entity);
    //                        } else {
    //                                item = blockItemFactory.newInstance(event.getOldBlock()
    //                                                .getBlockFamily());
    //                        }
    //                        entity.send(new ReceivedItemEvent(item));
    //                        event.getDroppedBlock().destroy();
    //                }
    //        }

    /**
     * The active minion, to be commanded by the minion command item etc uses a
     * slightly different texture to indicate selection
     * 
     * @param minion
     *            : the new active minion entity
     */
    public static void setActiveMinion(EntityRef minion) {
        SkeletalMeshComponent skelcomp;
        if (activeminion != null) {
            skelcomp = activeminion.getComponent(SkeletalMeshComponent.class);
            if (skelcomp != null) {
                MinionComponent minionComponent = activeminion.getComponent(MinionComponent.class);
                if (null != minionComponent.unselectedSkin) {
                    skelcomp.material = Assets.getMaterial(minionComponent.unselectedSkin);
                }
            }
        }
        skelcomp = minion.getComponent(SkeletalMeshComponent.class);
        if (skelcomp != null) {
            MinionComponent minionComponent = minion.getComponent(MinionComponent.class);
            if (null != minionComponent.selectedSkin) {
                skelcomp.material = Assets.getMaterial(minionComponent.selectedSkin);
            }
        }
        activeminion = minion;
    }

    /**
     * returns the currently active minion
     * 
     * @return : the currently active minion
     */
    public static EntityRef getActiveMinion() {
        return activeminion;
    }

    public static List<MinionRecipe> getRecipesList() {
        if (recipeslist == null) {
            return null;
        }
        return recipeslist;
    }

    /**
     * Returns true if the user set option in minion settings
     */
    public static boolean isActiveMinionShown() {
        return showactiveminion;
    }

    public static void toggleActiveMinionShown() {
        showactiveminion = !showactiveminion;
    }

    /**
     * Returns true if the user set option in minion settings
     */
    public static boolean isSelectionShown() {
        return showSelectionOverlay;
    }

    public static void toggleSelectionShown() {
        showSelectionOverlay = !showSelectionOverlay;
    }

    /**
     * hide / show the active minion
     */
    public void showActiveMinion(boolean show) {
        showactiveminion = show;
    }

    /* TODO: Re-enable if fixed sometime (the use of minion.getId() is invalid and should not be used).
     * The code using this in UIActiveMinion.java is itself commented out pending fixes or eventual module retirement
     *
     * order the minions by id and return the next one, or the first if none were active!
     *
    public static void getNextMinion(boolean deleteactive) {
        EntityManager entman = CoreRegistry.get(EntityManager.class);
        List<Integer> sortedlist = new ArrayList<Integer>();
        for (EntityRef minion : entman.getEntitiesWith(MinionComponent.class)) {
            if (!minion.getComponent(MinionComponent.class).dying) {
                sortedlist.add(minion.getId());
            }
        }
        if (sortedlist.size() == 0) {
            return;
        } else if (deleteactive && sortedlist.size() == 1) {
            activeminion = null;
            return;
        }
        Collections.sort(sortedlist);
        int index = 0;
        if (activeminion != null) {
            index = sortedlist.indexOf(activeminion.getId());
        }
        if (index + 1 == sortedlist.size()) {
            index = 0;
        } else {
            index++;
        }
        index = sortedlist.get(index);
        for (EntityRef minion : entman.getEntitiesWith(MinionComponent.class)) {
            if (minion.getId() == index) {
                setActiveMinion(minion);
            }
        }
    }

    /*
     * order the minions by id and return the previous one, or the first if none were active!

    public static void getPreviousMinion(boolean deleteactive) {
        EntityManager entman = CoreRegistry.get(EntityManager.class);
        List<Integer> sortedlist = new ArrayList<Integer>();
        for (EntityRef minion : entman.getEntitiesWith(MinionComponent.class)) {
            if (!minion.getComponent(MinionComponent.class).dying) {
                sortedlist.add(minion.getId());
            }
        }
        if (sortedlist.size() == 0) {
            return;
        } else if (deleteactive && sortedlist.size() == 1) {
            activeminion = null;
            return;
        }
        Collections.sort(sortedlist);
        int index = 0;
        if (activeminion != null) {
            index = sortedlist.indexOf(activeminion.getId());
        }
        if (index == 0) {
            index = sortedlist.size() - 1;
        } else {
            index--;
        }
        index = sortedlist.get(index);
        for (EntityRef minion : entman.getEntitiesWith(MinionComponent.class)) {
            if (minion.getId() == index) {
                setActiveMinion(minion);
            }
        }
    }
*/
    private void initRecipes() {
        MinionRecipe recipe = new MinionRecipe();

        recipe.Name = "Chocolate";
        recipe.result = "CakeLie:ChocolateBlock";
        recipe.craftRes.add("dirt");
        recipe.craftsteps = 20;
        recipeslist.add(recipe);

        recipe = new MinionRecipe();
        recipe.Name = "Brownie";
        recipe.result = "CakeLie:ChocolateBrownieBlock";
        recipe.craftRes.add("stone");
        recipe.craftsteps = 35;
        recipeslist.add(recipe);

        recipe = new MinionRecipe();
        recipe.Name = "Brownie top";
        recipe.result = "CakeLie:ChocolateBrownieTop";
        recipe.craftRes.add("stone");
        recipe.craftRes.add("snow");
        recipe.craftsteps = 60;
        recipeslist.add(recipe);

        recipe = new MinionRecipe();
        recipe.Name = "Kiwi cake";
        recipe.result = "CakeLie:KiwiCakeBlock";
        recipe.craftRes.add("cactus");
        recipe.craftsteps = 30;
        recipeslist.add(recipe);

        recipe = new MinionRecipe();
        recipe.Name = "Kiwi top";
        recipe.result = "CakeLie:KiwiCakeTop";
        recipe.craftRes.add("cactus");
        recipe.craftRes.add("snow");
        recipe.craftsteps = 50;
        recipeslist.add(recipe);

        recipe = new MinionRecipe();
        recipe.Name = "Corny choco";
        recipe.result = "CakeLie:CornyChokoBlock";
        recipe.craftRes.add("grass");
        recipe.craftsteps = 30;
        recipeslist.add(recipe);

        recipe = new MinionRecipe();
        recipe.Name = "Corny top";
        recipe.result = "CakeLie:CornyChokoTop";
        recipe.craftRes.add("grass");
        recipe.craftRes.add("snow");
        recipe.craftsteps = 50;
        recipeslist.add(recipe);

        recipe = new MinionRecipe();
        recipe.Name = "Orange cake";
        recipe.result = "CakeLie:OrangeCakeBlock";
        recipe.craftRes.add("sand");
        recipe.craftsteps = 30;
        recipeslist.add(recipe);

        recipe = new MinionRecipe();
        recipe.Name = "Orange Top";
        recipe.result = "CakeLie:OrangeCakeTop";
        recipe.craftRes.add("sand");
        recipe.craftRes.add("snow");
        recipe.craftsteps = 50;
        recipeslist.add(recipe);

        recipe = new MinionRecipe();
        recipe.Name = "chest";
        recipe.result = "core:chest";
        recipe.craftRes.add("plank");
        recipe.quantity = 8;
        recipe.craftsteps = 100;
        recipeslist.add(recipe);
    }

    @Override
    public void shutdown() {
        if (activeminion != null) {
            SkeletalMeshComponent skelcomp = activeminion.getComponent(SkeletalMeshComponent.class);
            if (skelcomp != null) {
                MinionComponent minionComponent = activeminion.getComponent(MinionComponent.class);
                if (null != minionComponent.unselectedSkin) {
                    skelcomp.material = Assets.getMaterial(minionComponent.unselectedSkin);
                }
            }
        }
    }

    @Override
    public void preBegin() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void postBegin() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void preSave() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void postSave() {
        // TODO Auto-generated method stub
        
    }

    /**
     * overrides the default attack event if the minion command item is the
     * current helditem only adds gather targets for now, minion command needs
     * popuup to set behaviour
     */
    /*@ReceiveEvent(components = { LocalPlayerComponent.class, MinionControllerComponent.class }, priority = PRIORITY_LOCAL_PLAYER_OVERRIDE)
    public void onAttack(AttackButton event, EntityRef entity) {
    	LocalPlayerComponent locplaycomp = entity
    			.getComponent(LocalPlayerComponent.class);
    	UIItemContainer toolbar = (UIItemContainer) CoreRegistry
    			.get(GUIManager.class).getWindowById("hud")
    			.getElementById("toolbar");
    	int invSlotIndex = localPlayer.getEntity().getComponent(
    			LocalPlayerComponent.class).selectedTool
    			+ toolbar.getSlotStart();
    	EntityRef heldItem = localPlayer.getEntity().getComponent(
    			InventoryComponent.class).itemSlots.get(invSlotIndex);
    	ItemComponent heldItemComp = heldItem.getComponent(ItemComponent.class);

    	if (heldItemComp != null && activeminion != null
    			&& heldItemComp.name.matches("Minion Command")) {
    		SimpleMinionAIComponent aicomp = activeminion
    				.getComponent(SimpleMinionAIComponent.class);
    		LocationComponent loccomp = event.getTarget().getComponent(
    				LocationComponent.class);
    		if(loccomp != null)
    		{
    			aicomp.gatherTargets.add(loccomp.getWorldPosition());
    		}
    		activeminion.saveComponent(aicomp);
    		locplaycomp.handAnimation = 0.5f;
    		entity.saveComponent(locplaycomp);
    		event.consume();
    	}
    }*/
}
