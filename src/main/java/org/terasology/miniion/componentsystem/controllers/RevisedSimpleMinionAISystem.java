/*
 * Copyright 2015 MovingBlocks
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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.events.HorizontalCollisionEvent;
import org.terasology.logic.health.DoDamageEvent;
import org.terasology.logic.health.EngineDamageTypes;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3i;
import org.terasology.math.geom.Vector3f;
import org.terasology.miniion.components.AnimationComponent;
import org.terasology.miniion.components.AssignedTaskComponent;
import org.terasology.miniion.components.AssignedTaskType;
import org.terasology.miniion.components.MinionComponent;
import org.terasology.miniion.components.MinionFarmerComponent;
import org.terasology.miniion.components.NPCMovementInputComponent;
import org.terasology.miniion.components.SimpleMinionAIComponent;
import org.terasology.miniion.components.TaskStatusType;
import org.terasology.miniion.events.MinionMessageEvent;
import org.terasology.miniion.minionenum.MinionMessagePriority;
import org.terasology.miniion.pathfinder.AStarPathing;
import org.terasology.miniion.utilities.MinionMessage;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.selection.BlockSelectionComponent;

/**
 * Created with IntelliJ IDEA. User: Overdhose Date: 7/05/12 Time: 18:25 first
 * evolution of the minion AI, could probably use a lot of improvements
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class RevisedSimpleMinionAISystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(RevisedSimpleMinionAISystem.class);
    private static final String DEFAULT_TERRAFORM_FINAL_BLOCK_TYPE_NAME = "CakeLie:ChocolateBlock";
    private static final String DEFAULT_CROP_BLOCK_NAME = "core:plant";

    private static final int MINIMUM_WORK_DISTANCE = 4;

    @In
    private BlockManager blockManager;

    //    @In
    //    private SlotBasedInventoryManager inventoryManager;

    @In
    private TaskManagementSystem taskManager;

    @In
    private EntityManager entityManager;
    @In
    private WorldProvider worldProvider;
    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private Time timer;

    private AStarPathing aStarPathing;

    @Override
    public void initialise() {
        aStarPathing = new AStarPathing(worldProvider);

    }

    @ReceiveEvent(components = {SimpleMinionAIComponent.class})
    public void onSpawn(OnAddedComponent event, EntityRef entity) {
        initMinionAI();
    }

    private void initMinionAI() {
        //add 3000 to init to create  bit of a delay before first check
        long initTime = timer.getGameTimeInMs() + 3000;
        for (EntityRef minion : entityManager.getEntitiesWith(SimpleMinionAIComponent.class)) {
            SimpleMinionAIComponent ai = minion.getComponent(SimpleMinionAIComponent.class);
            ai.lastAttacktime = initTime;
            ai.lastDistancecheck = initTime;
            ai.lastHungerCheck = initTime;
            minion.saveComponent(ai);
        }
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void update(float delta) {
        assignTasksToIdleMinions();

        for (EntityRef entity : entityManager.getEntitiesWith(
                SimpleMinionAIComponent.class,
                NPCMovementInputComponent.class, LocationComponent.class,
                MinionComponent.class, SkeletalMeshComponent.class,
                AnimationComponent.class)) {

            MinionComponent minioncomp = entity
                    .getComponent(MinionComponent.class);
            AnimationComponent animcomp = entity
                    .getComponent(AnimationComponent.class);
            SimpleMinionAIComponent ai = entity
                    .getComponent(SimpleMinionAIComponent.class);
            AssignedTaskComponent assignedTaskComponent = entity.getComponent(AssignedTaskComponent.class);

            //hunger system, increase the delay by increasing > 10000
            if (timer.getGameTimeInMs() - ai.lastHungerCheck > 10000) {
                ai.lastHungerCheck = timer.getGameTimeInMs();
                if (minioncomp.Hunger > 0) {
                    minioncomp.Hunger--;
                    //need to save components for data to persist when game restarts
                    entity.saveComponent(minioncomp);
                } else {
                    //die? reset for now so you see effect
                    minioncomp.Hunger = 100;
                }
            }

            if (null == assignedTaskComponent) {
                // TODO:  minion is doing nothing
                changeAnimation(entity, animcomp.idleAnim, false);
                return;
            }

            switch (assignedTaskComponent.assignedTaskType) {
                case Follow: {
                    executeFollowAI(entity);
                    break;
                }
                case Gather: {
                    executeGatherAI(entity);
                    break;
                }
                case Dig: {
                    executeDigAI(entity, assignedTaskComponent);
                    break;
                }
                case Plant:
                case Work: {
                    executeWorkAI(entity, assignedTaskComponent);
                    break;
                }
                case Terraform: {
                    executeTerraformAI(entity, assignedTaskComponent, "");
                    break;
                }
                case Move: {
                    executeMoveAI(entity);
                    break;
                }
                case Patrol: {
                    executePatrolAI(entity);
                    break;
                }
                case Attack: {
                    changeAnimation(entity, animcomp.attackAnim, true);
                    break;
                }
                case Die: {
                    changeAnimation(entity, animcomp.dieAnim, false);
                    break;
                }
                case Stay: {
                    executeStayAI(entity);
                    break;
                }
                case Test: {
                    executeTestAI(entity);
                    break;
                }
                default: {
                    changeAnimation(entity, animcomp.idleAnim, false);
                    break;
                }
            }
        }
    }

    private void executeStayAI(EntityRef entity) {
        NPCMovementInputComponent movementInput = entity
                .getComponent(NPCMovementInputComponent.class);
        movementInput.directionToMove = new Vector3f(0, 0, 0);
        entity.saveComponent(movementInput);
        AnimationComponent animcomp = entity
                .getComponent(AnimationComponent.class);
        changeAnimation(entity, animcomp.idleAnim, false);
    }

    private void changeAnimation(EntityRef entity, MeshAnimation animation,
                                 boolean loop) {
        SkeletalMeshComponent skeletalcomp = entity
                .getComponent(SkeletalMeshComponent.class);
        if (skeletalcomp.animation != animation) {
            skeletalcomp.animation = animation;
            skeletalcomp.loop = loop;
            entity.saveComponent(skeletalcomp);
        }
    }

    private void executeFollowAI(EntityRef entity) {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        if (localPlayer == null) {
            return;
        }
        LocationComponent location = entity
                .getComponent(LocationComponent.class);
        SimpleMinionAIComponent ai = entity
                .getComponent(SimpleMinionAIComponent.class);
        Vector3f worldPos = new Vector3f(location.getWorldPosition());

        Vector3f dist = new Vector3f(worldPos);
        dist.sub(localPlayer.getPosition());
        double distanceToPlayer = dist.lengthSquared();

        if (distanceToPlayer > 8) {
            // Head to player
            Vector3f target = localPlayer.getPosition();
            ai.movementTarget.set(target);
            ai.followingPlayer = true;
            entity.saveComponent(ai);
        }

        setMovement(ai.movementTarget, entity);
    }

    private void executeGatherAI(EntityRef entity) {
        //        MinionComponent minioncomp = entity.getComponent(MinionComponent.class);
        //        LocationComponent location = entity
        //                .getComponent(LocationComponent.class);
        //        SimpleMinionAIComponent ai = entity
        //                .getComponent(SimpleMinionAIComponent.class);
        //        AnimationComponent animcomp = entity
        //                .getComponent(AnimationComponent.class);
        //        Vector3f worldPos = new Vector3f(location.getWorldPosition());
        //        FarmZoneComponent assignedZoneComponent = minioncomp.assignedZoneEntity.getComponent(ZoneComponent.class);
        //
        //        if ((ai.gatherTargets.size() == 0) && (minioncomp.assignedZoneEntity != EntityRef.NULL) && (assignedZoneComponent.zonetype == ZoneType.Gather)) {
        //            getTargetsfromZone(minioncomp, ai);
        //        }
        //
        //        if ((ai.gatherTargets == null) || (ai.gatherTargets.size() < 1)) {
        //            return;
        //        }
        //        Vector3f currentTarget = ai.gatherTargets.get(0);
        //        if (currentTarget == null) {
        //            ai.gatherTargets.remove(currentTarget);
        //            changeAnimation(entity, animcomp.idleAnim, true);
        //            entity.saveComponent(ai);
        //            return;
        //        }
        //        Vector3f dist = new Vector3f(worldPos);
        //        dist.sub(currentTarget);
        //        double distanceToTarget = dist.lengthSquared();
        //
        //        if (distanceToTarget < 4) {
        //            // switch animation
        //            changeAnimation(entity, animcomp.workAnim, false);
        //            // gather the block
        //            if (timer.getGameTimeInMs() - ai.lastAttacktime > 500) {
        //                ai.lastAttacktime = timer.getGameTimeInMs();
        //                boolean attacked = attack(entity, currentTarget);
        //                if (!attacked) {
        //                    changeAnimation(entity, animcomp.idleAnim, true);
        //                    ai.gatherTargets.remove(currentTarget);
        //                }
        //            }
        //        }
        //
        //        entity.saveComponent(ai);
        //        setMovement(currentTarget, entity);
    }

    //
    //    private void getTargetsfromZone(MinionComponent minioncomp,
    //                                    SimpleMinionAIComponent ai) {
    //        EntityRef zone = minioncomp.assignedTaskEntity;
    //        FarmZoneComponent zoneComponent = zone.getComponent(ZoneComponent.class);
    //        // first loop at highest blocks (y)
    //        for (int y = zoneComponent.getMaxBounds().y; y >= zoneComponent.getMinBounds().y; y--) {
    //            for (int x = zoneComponent.getMinBounds().x; x <= zoneComponent.getMaxBounds().x; x++) {
    //                for (int z = zoneComponent.getMinBounds().z; z <= zoneComponent.getMaxBounds().z; z++) {
    //                    Block tmpblock = worldProvider.getBlock(x, y, z);
    //                    if (!tmpblock.isInvisible()) {
    //                        ai.gatherTargets.add(new Vector3f(x, y + 0.5f, z));
    //                    }
    //                }
    //            }
    //        }
    //    }

    private void executeWorkAI(EntityRef entity, AssignedTaskComponent assignedTaskComponent) {
        MinionFarmerComponent minionFarmer = entity.getComponent(MinionFarmerComponent.class);

        if (assignedTaskComponent.assignedTaskType == AssignedTaskType.Plant) {
            if (isTerraformComplete(assignedTaskComponent.targetLocation, minionFarmer.farmFieldBlockName)) {
                //farming
                executeFarmAI(entity, assignedTaskComponent);
            } else {
                executeTerraformAI(entity, assignedTaskComponent, minionFarmer.farmFieldBlockName);
            }
            return;
        }
    }

    private boolean isTerraformComplete(Vector3i targetLocation, String blockTypeName) {
        Block block = worldProvider.getBlock(targetLocation);
        if (!block.getURI().toString().toLowerCase().equals(blockTypeName.toLowerCase())) {
            return false;
        } else {
            return true;
        }
    }

//    private boolean isAreaComposedOnlyOfBlockType(Region3i area, String blockTypeName) {
//        for (int y = area.max().y; y >= area.min().y; y--) {
//            for (int x = area.min().x; x <= area.max().x; x++) {
//                for (int z = area.min().z; z <= area.max().z; z++) {
//                    Block block = worldProvider.getBlock(x, y, z);
//                    if (!block.getURI().toString().toLowerCase().equals(blockTypeName.toLowerCase())) {
//                        return false;
//                    }
//                }
//            }
//        }
//
//        return true;
//    }

    private Vector3i[] getCardinalNeighborLocations(Vector3i targetLocation) {
        return new Vector3i[] {
                new Vector3i(targetLocation.x - 1, targetLocation.y, targetLocation.z),
                new Vector3i(targetLocation.x + 1, targetLocation.y, targetLocation.z),
                new Vector3i(targetLocation.x, targetLocation.y - 1, targetLocation.z),
                new Vector3i(targetLocation.x, targetLocation.y + 1, targetLocation.z),
                new Vector3i(targetLocation.x, targetLocation.y, targetLocation.z - 1),
                new Vector3i(targetLocation.x, targetLocation.y, targetLocation.z + 1)
        };
    }

    private boolean isReachable(EntityRef minionEntity, Vector3i targetLocation) {
        // TODO: This method is going to require a lot more work
        // TODO: All of these need to be done with a pathfinding algorithm, but here's a temporary placeholder
        boolean reachable = true;


        // Not reachable if surrounded by dirt and we can't burrow/ghost -

        boolean surrounded = true;
        Vector3i[] cardinalNeighborLocations = getCardinalNeighborLocations(targetLocation);

        for (Vector3i neighborLocation : cardinalNeighborLocations) {
            Block tmpblock = worldProvider.getBlock(neighborLocation);

            if (tmpblock.isPenetrable()) {              ///this does not work properly
                surrounded = false;
            }
        }
        if (surrounded) {
            reachable = false;
        }


        // Not reachable if up in the air and we can't fly, jump down to it, or climb up to it
        LocationComponent location = minionEntity.getComponent(LocationComponent.class);
        Vector3f entityLocation = location.getLocalPosition();

        Vector3f currentTarget = new Vector3f(targetLocation.x, 0, targetLocation.z);
        Vector3f dist = new Vector3f(entityLocation.x, 0, entityLocation.z);
        dist.sub(currentTarget);
        double horizontalDistanceToTarget = dist.lengthSquared();
        if (horizontalDistanceToTarget < MINIMUM_WORK_DISTANCE) {
            if (Math.abs(entityLocation.y - targetLocation.y) > MINIMUM_WORK_DISTANCE) {
                reachable = false;
            }
        }

        // Consider it unreachable if we're not making progress moving toward it after a period of time?

        return reachable;
    }

    private boolean isDiggable(Vector3i targetLocation) {
        Block tmpblock = worldProvider.getBlock(targetLocation);
        if (!tmpblock.isInvisible()) {
            if (!(blockManager.getBlock(BlockManager.AIR_ID).equals(tmpblock))) {
                if (tmpblock.isDestructible()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void executeDigAI(EntityRef minionEntity, AssignedTaskComponent assignedTaskComponent) {
        MinionComponent minioncomp = minionEntity.getComponent(MinionComponent.class);
        LocationComponent location = minionEntity.getComponent(LocationComponent.class);
        SimpleMinionAIComponent ai = minionEntity.getComponent(SimpleMinionAIComponent.class);
        AnimationComponent animcomp = minionEntity.getComponent(AnimationComponent.class);
        Vector3f worldPos = new Vector3f(location.getWorldPosition());

        if (!isDiggable(assignedTaskComponent.targetLocation)) {
            assignedTaskComponent.taskStatusType = TaskStatusType.COMPLETED;
            endTask(minionEntity, ai, animcomp);
        }

        if (!isReachable(minionEntity, assignedTaskComponent.targetLocation)) {
            assignedTaskComponent.taskStatusType = TaskStatusType.UNREACHABLE;
            endTask(minionEntity, ai, animcomp);
        }

        Vector3f currentTarget = assignedTaskComponent.targetLocation.toVector3f();

        Vector3f dist = new Vector3f(worldPos);
        dist.sub(currentTarget);
        double distanceToTarget = dist.lengthSquared();

        int damageAmount = 1;

        if (distanceToTarget < MINIMUM_WORK_DISTANCE) {
            changeAnimation(minionEntity, animcomp.workAnim, true);
            if (timer.getGameTimeInMs() - ai.lastAttacktime > 200) {
                ai.lastAttacktime = timer.getGameTimeInMs();
                Block tmpblock = worldProvider.getBlock(assignedTaskComponent.targetLocation);
                if (!tmpblock.isInvisible() && tmpblock.isDestructible() && !tmpblock.equals(blockManager.getBlock(BlockManager.AIR_ID))) {
                    EntityRef blockEntity = blockEntityRegistry.getEntityAt(assignedTaskComponent.targetLocation);
                    DoDamageEvent doDamageEvent = new DoDamageEvent(damageAmount, EngineDamageTypes.PHYSICAL.get(), minionEntity);
                    blockEntity.send(doDamageEvent);
                    Block newTmpblock = worldProvider.getBlock(assignedTaskComponent.targetLocation);
                    if (!newTmpblock.equals(tmpblock)) {
                        assignedTaskComponent.taskStatusType = TaskStatusType.COMPLETED;
                        endTask(minionEntity, ai, animcomp);
                    }
                } else {
                    assignedTaskComponent.taskStatusType = TaskStatusType.IMPOSSIBLE;
                    endTask(minionEntity, ai, animcomp);
                }

            }
        } else {
            setMovement(currentTarget, minionEntity);
        }
    }

    private boolean isTerraformable(Vector3i targetLocation) {
        Block tmpblock = worldProvider.getBlock(targetLocation);
        if (!tmpblock.isInvisible()) {
            if (!(blockManager.getBlock(BlockManager.AIR_ID).equals(tmpblock))) {
                if (tmpblock.isDestructible()) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Terraforms a zone into a set recipe, by default chocolate.
     * @param minionEntity the minion that is terraforming
     * @param assignedTaskComponent set to empty string by default for normal terraforming
     * @param terraformFinalBlockType can override the default recipe for farming
     */
    private void executeTerraformAI(EntityRef minionEntity, AssignedTaskComponent assignedTaskComponent, String terraformFinalBlockType) {
        MinionComponent minioncomp = minionEntity.getComponent(MinionComponent.class);
        LocationComponent location = minionEntity.getComponent(LocationComponent.class);
        SimpleMinionAIComponent ai = minionEntity.getComponent(SimpleMinionAIComponent.class);
        AnimationComponent animcomp = minionEntity.getComponent(AnimationComponent.class);
        Vector3f worldPos = new Vector3f(location.getWorldPosition());

        if (!isTerraformable(assignedTaskComponent.targetLocation)) {
            assignedTaskComponent.taskStatusType = TaskStatusType.COMPLETED;
            endTask(minionEntity, ai, animcomp);
        }

        if (!isReachable(minionEntity, assignedTaskComponent.targetLocation)) {
            assignedTaskComponent.taskStatusType = TaskStatusType.UNREACHABLE;
            endTask(minionEntity, ai, animcomp);
        }

        Vector3f currentTarget = assignedTaskComponent.targetLocation.toVector3f();

        Vector3f dist = new Vector3f(worldPos);
        dist.sub(currentTarget);
        double distanceToTarget = dist.lengthSquared();


        if (distanceToTarget < MINIMUM_WORK_DISTANCE) {
            // terraform
            changeAnimation(minionEntity, animcomp.terraformAnim, true);
            if (timer.getGameTimeInMs() - ai.lastAttacktime > 200) {
                ai.lastAttacktime = timer.getGameTimeInMs();

                Block tmpblock = worldProvider.getBlock(assignedTaskComponent.targetLocation);
                if (!tmpblock.isInvisible() && tmpblock.isDestructible() && !tmpblock.equals(blockManager.getBlock(BlockManager.AIR_ID))) {
                    String moduleName = tmpblock.getBlockFamily().getURI().getModuleName().toString();
                    // TODO: why do we care about what kinds of blocks we terraform?
                    if ((moduleName.equals("engine")) || (moduleName.equals("core"))) {
                        ai.craftprogress++;
                        if (ai.craftprogress > 20) {
                            Block newBlock = getBlockForTerraformBlockType(terraformFinalBlockType);
                            worldProvider.setBlock(assignedTaskComponent.targetLocation, newBlock);
                            ai.craftprogress = 0;
                            assignedTaskComponent.taskStatusType = TaskStatusType.COMPLETED;
                            endTask(minionEntity, ai, animcomp);
                        }
                    } else {
                        assignedTaskComponent.taskStatusType = TaskStatusType.IMPOSSIBLE;
                        endTask(minionEntity, ai, animcomp);
                    }
                } else {
                    assignedTaskComponent.taskStatusType = TaskStatusType.IMPOSSIBLE;
                    endTask(minionEntity, ai, animcomp);
                }
            }
        } else {
            setMovement(currentTarget, minionEntity);
        }
    }

    private Block getBlockForTerraformBlockType(String terraformFinalBlockType) {
        Block newBlock;
        if ((null != terraformFinalBlockType) && !terraformFinalBlockType.isEmpty()) {
            newBlock = blockManager.getBlock(terraformFinalBlockType);
            if (!newBlock.getURI().toString().toLowerCase().equals(terraformFinalBlockType.toLowerCase())) {
                // Not sure what we should do if block read fails, but we don't want air as default
                newBlock = blockManager.getBlock(DEFAULT_TERRAFORM_FINAL_BLOCK_TYPE_NAME);
            }
        } else {
            newBlock = blockManager.getBlock(DEFAULT_TERRAFORM_FINAL_BLOCK_TYPE_NAME);
        }
        return newBlock;
    }

    private boolean isFarmable(MinionFarmerComponent minionFarmer, Vector3i targetLocation) {
        Block farmFieldBlock = getBlockForTerraformBlockType(minionFarmer.farmFieldBlockName);

        Block cropBlock = worldProvider.getBlock(targetLocation.x, targetLocation.y + 1, targetLocation.z);
        // TODO: are there other acceptable values other than air here?
        if (!blockManager.getBlock(BlockManager.AIR_ID).equals(cropBlock)) {
            return false;
        }

        Block groundBlock = worldProvider.getBlock(targetLocation);
        if (groundBlock.getURI().toString().toLowerCase().equals(farmFieldBlock.getURI().toString().toLowerCase())) {
            return true;
        }
        return false;
    }

    /**
     * plants crops
     * @param minionEntity the minion that is terraforming
     * @param assignedTaskComponent set to empty string by default for normal terraforming,
     */
    private void executeFarmAI(EntityRef minionEntity, AssignedTaskComponent assignedTaskComponent) {
        MinionFarmerComponent minionFarmer = minionEntity.getComponent(MinionFarmerComponent.class);
        LocationComponent location = minionEntity.getComponent(LocationComponent.class);
        SimpleMinionAIComponent ai = minionEntity.getComponent(SimpleMinionAIComponent.class);
        AnimationComponent animcomp = minionEntity.getComponent(AnimationComponent.class);
        Vector3f worldPos = new Vector3f(location.getWorldPosition());

        if (!isFarmable(minionFarmer, assignedTaskComponent.targetLocation)) {
            assignedTaskComponent.taskStatusType = TaskStatusType.COMPLETED;
            endTask(minionEntity, ai, animcomp);
        }

        if (!isReachable(minionEntity, assignedTaskComponent.targetLocation)) {
            assignedTaskComponent.taskStatusType = TaskStatusType.UNREACHABLE;
            endTask(minionEntity, ai, animcomp);
        }

        Vector3f currentTarget = assignedTaskComponent.targetLocation.toVector3f();

        Vector3f dist = new Vector3f(worldPos);
        dist.sub(currentTarget);
        double distanceToTarget = dist.lengthSquared();


        if (distanceToTarget < MINIMUM_WORK_DISTANCE) {
            // farm
            changeAnimation(minionEntity, animcomp.terraformAnim, true);
            if (timer.getGameTimeInMs() - ai.lastAttacktime > 200) {
                ai.lastAttacktime = timer.getGameTimeInMs();
                Block currentBlock = worldProvider.getBlock(new Vector3i(currentTarget.x, currentTarget.y + 1, currentTarget.z));
                Block plantedBlock = null;
                if (null != minionFarmer.blockNameToPlantAboveFarmField) {
                    plantedBlock = blockManager.getBlock(minionFarmer.blockNameToPlantAboveFarmField);
                    if (!plantedBlock.getURI().toString().toLowerCase().equals(minionFarmer.blockNameToPlantAboveFarmField.toLowerCase())) {
                        // We didn't get what we asked for and probably got air instead
                        plantedBlock = null;
                    }
                }
                if (null == plantedBlock) {
                    // Not sure what we should do if block read fails, but we don't want air as default
                    plantedBlock = blockManager.getBlock(DEFAULT_CROP_BLOCK_NAME);
                }

                if (plantedBlock.getPrefab().equals(currentBlock.getPrefab())) {
                    assignedTaskComponent.taskStatusType = TaskStatusType.COMPLETED;
                    endTask(minionEntity, ai, animcomp);
                }
                ai.craftprogress++;
                if (ai.craftprogress > 20) {
                    worldProvider.setBlock(new Vector3i(currentTarget.x, currentTarget.y + 1, currentTarget.z), plantedBlock);
                    ai.craftprogress = 0;
                    assignedTaskComponent.taskStatusType = TaskStatusType.COMPLETED;
                    endTask(minionEntity, ai, animcomp);
                }
            }
        } else {
            setMovement(currentTarget, minionEntity);
        }

    }

    private void executeMoveAI(EntityRef entity) {
        LocationComponent location = entity
                .getComponent(LocationComponent.class);
        SimpleMinionAIComponent ai = entity
                .getComponent(SimpleMinionAIComponent.class);
        Vector3f worldPos = new Vector3f(location.getWorldPosition());
        NPCMovementInputComponent movementInput = entity.getComponent(NPCMovementInputComponent.class);

        // get targets, break if none
        List<Vector3f> targets = ai.movementTargets;
        if ((targets == null) || (targets.size() < 1)) {
            movementInput.directionToMove = new Vector3f(0, 0, 0);
            entity.saveComponent(movementInput);
            return;
        }
        Vector3f currentTarget = targets.get(0);
        // trying to solve distance calculation with some simple trick of
        // reducing the height to 0.5, might not work for taller entities
        worldPos.y = worldPos.y - (worldPos.y % 1) + 0.5f;

        // calc distance to current Target
        Vector3f dist = new Vector3f(worldPos);
        dist.sub(currentTarget);
        double distanceToTarget = dist.length();

        // used 1.0 here as a check, should be lower to have the minion jump on
        // the last block, TODO need to calc middle of block
        if (distanceToTarget < 0.1d) {
            ai.movementTargets.remove(0);
            entity.saveComponent(ai);
            currentTarget = null;
            return;
        }

        setMovement(currentTarget, entity);
    }

    private void executePatrolAI(EntityRef entity) {
        LocationComponent location = entity
                .getComponent(LocationComponent.class);
        SimpleMinionAIComponent ai = entity
                .getComponent(SimpleMinionAIComponent.class);
        Vector3f worldPos = new Vector3f(location.getWorldPosition());

        // get targets, break if none
        List<Vector3f> targets = ai.patrolTargets;
        if ((targets == null) || (targets.size() < 1)) {
            return;
        }
        int patrolCounter = ai.patrolCounter;
        Vector3f currentTarget = null;

        // get the patrol point
        if (patrolCounter < targets.size()) {
            currentTarget = targets.get(patrolCounter);
        }

        if (currentTarget == null) {
            return;
        }

        // calc distance to current Target
        Vector3f dist = new Vector3f(worldPos);
        dist.sub(currentTarget);
        double distanceToTarget = dist.length();

        if (distanceToTarget < 0.1d) {
            patrolCounter++;
            if (!(patrolCounter < targets.size())) {
                patrolCounter = 0;
            }
            ai.patrolCounter = patrolCounter;
            entity.saveComponent(ai);
            return;
        }

        setMovement(currentTarget, entity);
    }

    private void executeTestAI(EntityRef entity) {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        if (localPlayer == null) {
            return;
        }
        LocationComponent location = entity
                .getComponent(LocationComponent.class);
        SimpleMinionAIComponent ai = entity
                .getComponent(SimpleMinionAIComponent.class);
        Vector3f worldPos = new Vector3f(location.getWorldPosition());

        if (!ai.locked) {
            // get targets, break if none
            List<Vector3f> targets = ai.movementTargets;
            List<Vector3f> pathTargets = ai.pathTargets;
            if ((targets == null) || (targets.size() < 1)) {
                return;
            }

            Vector3f currentTarget; // check if currentTarget target is a path
                                    // or not
            if ((pathTargets != null) && (pathTargets.size() > 0)) {
                currentTarget = pathTargets.get(0);
            } else {
                currentTarget = targets.get(0);
            }
            if (ai.previousTarget != ai.movementTargets.get(0)) {
                ai.locked = true;
                ai.pathTargets = aStarPathing.findPath(worldPos, new Vector3f(
                        currentTarget));
                if (ai.pathTargets == null) {
                    // MinionSystem minionSystem = new MinionSystem();
                    MinionMessage messagetosend = new MinionMessage(
                            MinionMessagePriority.Debug, "test", "testdesc",
                            "testcont", entity, localPlayer.getCharacterEntity());
                    entity.send(new MinionMessageEvent(messagetosend));
                    ai.movementTargets.remove(0);
                }
            }
            ai.locked = false;
            if ((ai.pathTargets != null) && (ai.pathTargets.size() > 0)) {
                pathTargets = ai.pathTargets;
                ai.previousTarget = targets.get(0); // used to check if the
                                                    // final target changed
                currentTarget = pathTargets.get(0);
            }

            // trying to solve distance calculation with some simple trick of
            // reducing the height to a round int, might not work for taller
            // entities
            worldPos.y = worldPos.y - (worldPos.y % 1) + 0.5f;
            // calc distance to current Target
            Vector3f dist = new Vector3f(worldPos);
            dist.sub(currentTarget);
            double distanceToTarget = dist.length();

            if (distanceToTarget < 0.1d) {
                if ((ai.pathTargets != null) && (ai.pathTargets.size() > 0)) {
                    ai.pathTargets.remove(0);
                    entity.saveComponent(ai);
                } else {
                    if (ai.movementTargets.size() > 0) {
                        ai.movementTargets.remove(0);
                    }
                    ai.previousTarget = null;
                    entity.saveComponent(ai);
                }
                return;
            }

            setMovement(currentTarget, entity);
        }
    }

    private void setMovement(Vector3f currentTarget, EntityRef entity) {
        LocationComponent location = entity
                .getComponent(LocationComponent.class);
        SimpleMinionAIComponent ai = entity
                .getComponent(SimpleMinionAIComponent.class);
        NPCMovementInputComponent movementInput = entity
                .getComponent(NPCMovementInputComponent.class);
        AnimationComponent animcomp = entity
                .getComponent(AnimationComponent.class);
        SkeletalMeshComponent skeletalcomp = entity
                .getComponent(SkeletalMeshComponent.class);
        Vector3f worldPos = new Vector3f(location.getWorldPosition());

        Vector3f dist = new Vector3f(worldPos);
        dist.sub(currentTarget);
        double distanceToTarget = dist.length();

        Vector3f targetDirection = new Vector3f();
        targetDirection.sub(currentTarget, worldPos);
        if (targetDirection.x * targetDirection.x + targetDirection.z
            * targetDirection.z > 0.03f) {
            if (timer.getGameTimeInMs() - ai.lastDistancecheck > 2000) {
                ai.lastDistancecheck = timer.getGameTimeInMs();
                if (ai.lastPosition == null) {
                    ai.lastPosition = location.getWorldPosition();
                } else if (ai.lastPosition.x == location.getLocalPosition().x
                           && ai.lastPosition.z == location.getWorldPosition().z) {
                    // minion has been stuck at same position => teleport
                    if (skeletalcomp.animation == animcomp.walkAnim) {
                        changeAnimation(entity, animcomp.idleAnim, true);
                    }
                    location.setWorldPosition(currentTarget);
                    movementInput.directionToMove = new Vector3f(0, 0, 0);
                    entity.saveComponent(location);
                    entity.saveComponent(movementInput);
                    ai.lastPosition = location.getWorldPosition();
                } else {
                    ai.lastPosition = location.getWorldPosition();
                }
            }
            changeAnimation(entity, animcomp.walkAnim, true);
            targetDirection.normalize();
            movementInput.directionToMove = targetDirection;

            float yaw = (float) Math
                    .atan2(targetDirection.x, targetDirection.z);
            Vector3f axis = new Vector3f(0, 1, 0);
            location.getLocalRotation().set(axis, yaw);
        } else if (distanceToTarget > 1) {
            // the minion arrived at right x and z but is standing below / above
            // it => teleport
            location.setWorldPosition(currentTarget);
        } else {
            if (skeletalcomp.animation == animcomp.walkAnim) {
                changeAnimation(entity, animcomp.idleAnim, true);
            }
            movementInput.directionToMove = new Vector3f(0, 0, 0);
        }
        entity.saveComponent(ai);
        entity.saveComponent(movementInput);
        entity.saveComponent(location);
    }

    private boolean attack(EntityRef minion, Vector3f position) {

        int damage = 1;
        Block block = worldProvider.getBlock(new Vector3f(position.x,
                position.y - 0.5f, position.z));
        if ((block.isDestructible()) && (block.isTargetable())) {
            EntityRef blockEntity = blockEntityRegistry
                    // originally was getOrCreateEntityAt
                    .getEntityAt(new Vector3i(position));
            if (blockEntity == EntityRef.NULL) {
                return false;
            } else {
                blockEntity.send(new DoDamageEvent(damage, EngineDamageTypes.DIRECT.get(), minion));
                return true;
            }
        }
        return false;
    }

    @ReceiveEvent(components = {SimpleMinionAIComponent.class})
    public void onBump(HorizontalCollisionEvent event, EntityRef entity) {
        NPCMovementInputComponent movementInput = entity
                .getComponent(NPCMovementInputComponent.class);
        CharacterMovementComponent chracterMovement = entity
                .getComponent(CharacterMovementComponent.class);
        if ((movementInput != null) && (chracterMovement.grounded)) {
            movementInput.jumpingRequested = true;
            logger.warn("#toomchsugar#####");
            entity.saveComponent(movementInput);
        }
    }

    private void endTask(EntityRef entity, SimpleMinionAIComponent ai, AnimationComponent animcomp) {
        AssignedTaskComponent assignedTaskComponent = entity.getComponent(AssignedTaskComponent.class);
        if (null != assignedTaskComponent) {
            taskManager.finishedTask(entity, assignedTaskComponent);
        }
        entity.removeComponent(BlockSelectionComponent.class);
        entity.removeComponent(AssignedTaskComponent.class);
        changeAnimation(entity, animcomp.idleAnim, true);
        NPCMovementInputComponent movementInput = entity.getComponent(NPCMovementInputComponent.class);
        movementInput.directionToMove = new Vector3f(0, 0, 0);
        entity.saveComponent(ai);
    }

    private void assignTasksToIdleMinions() {
        Iterable<EntityRef> minionEntityIterable = entityManager.getEntitiesWith(MinionComponent.class);
        for (EntityRef minionEntity : minionEntityIterable) {
            AssignedTaskComponent assignedTaskComponent = minionEntity.getComponent(AssignedTaskComponent.class);
            if (null == assignedTaskComponent) {
                // TODO: send event
                taskManager.assignTask(minionEntity);
            }
        }
    }
}
