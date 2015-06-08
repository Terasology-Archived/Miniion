/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.miniion.components.AssignableTaskComponent;
import org.terasology.miniion.components.AssignedTaskComponent;
import org.terasology.miniion.components.AssignedTaskType;
import org.terasology.miniion.components.MinionComponent;
import org.terasology.miniion.components.TaskStatusType;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureUtil;
import org.terasology.rendering.nui.Color;
import org.terasology.world.selection.BlockSelectionComponent;

@Share(TaskManagementSystem.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class TaskManagementSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(TaskManagementSystem.class);

    @In
    private EntityManager entityManager;

    @In
    private Time timer;

    private int getIndexForTaskStatusTypeData(AssignableTaskComponent assignableTaskComponent, int x, int y, int z) {
        Vector3i areaMin = assignableTaskComponent.area.min();
        Vector3i areaSize = assignableTaskComponent.area.size();
        
        int i = x - areaMin.x;
        int j = y - areaMin.y;
        int k = z - areaMin.z;
        
        int index = k + (j * areaSize.z) + (i * areaSize.y * areaSize.z);
        return index;
    }
    
    private void initializeTaskStatusTypes(AssignableTaskComponent assignableTaskComponent) {
        Vector3i areaSize = assignableTaskComponent.area.size();
        assignableTaskComponent.subtaskStatusData = new TaskStatusType[areaSize.x * areaSize.y * areaSize.z];
    }

    private TaskStatusType getTaskStatusType(AssignableTaskComponent assignableTaskComponent, int x, int y, int z) {
        int index = getIndexForTaskStatusTypeData(assignableTaskComponent, x, y, z);
        return assignableTaskComponent.subtaskStatusData[index];
    }

    private void setTaskStatusType(AssignableTaskComponent assignableTaskComponent, int x, int y, int z, TaskStatusType newTaskStatusType) {
        int index = getIndexForTaskStatusTypeData(assignableTaskComponent, x, y, z);
        assignableTaskComponent.subtaskStatusData[index] = newTaskStatusType;
    }
    
    public void createAssignedTask(AssignedTaskType taskType, Region3i selection) {
        Texture taskSelectionTexture;
        Color taskColor;
        switch (taskType) {
            case Plant:
                taskColor = Color.GREEN.alterAlpha(100);
                taskSelectionTexture = Assets.get(TextureUtil.getTextureUriForColor(taskColor), Texture.class).get();
                break;
            case Dig:
                taskColor = Color.BLUE.alterAlpha(100);
                taskSelectionTexture = Assets.get(TextureUtil.getTextureUriForColor(taskColor), Texture.class).get();
                break;
            default:
                taskColor = Color.RED.alterAlpha(100);
                taskSelectionTexture = Assets.get(TextureUtil.getTextureUriForColor(taskColor), Texture.class).get();
                break;
        }
        
        AssignableTaskComponent assignableTaskComponent = new AssignableTaskComponent();
        assignableTaskComponent.area = selection;
        assignableTaskComponent.creationGameTime = timer.getGameTimeInMs();
        assignableTaskComponent.assignedTaskType = taskType;
        
        // track subtasks
        Vector3i areaMin = assignableTaskComponent.area.min();
        Vector3i areaSize = assignableTaskComponent.area.size();
        initializeTaskStatusTypes(assignableTaskComponent);
        for (int i = areaMin.x; i < (areaSize.x + areaMin.x); i++) {
            for (int j = areaMin.y; j < (areaSize.y + areaMin.y); j++) {
                for (int k = areaMin.z; k < (areaSize.z + areaMin.z); k++) {
                    setTaskStatusType(assignableTaskComponent, i, j, k, TaskStatusType.AVAILABLE);
                }
            }
        }
        assignableTaskComponent.nextSubtaskCoordinatesToAssign = new Vector3i(areaMin);

        BlockSelectionComponent blockSelectionComponent = new BlockSelectionComponent();
        blockSelectionComponent.currentSelection= selection;
        blockSelectionComponent.shouldRender = true;
        blockSelectionComponent.startPosition = selection.min();
        blockSelectionComponent.texture = taskSelectionTexture;

        // Not sure if there's a better way to do it, but this seems the most appropriate?
        EntityRef assignedTaskEntity = entityManager.create(assignableTaskComponent, blockSelectionComponent);
    }

    // Simplistic task assignment
    public void assignTask(EntityRef minionEntity) {
        List<EntityRef> assignableTaskComponentEntityList = new ArrayList<EntityRef>();
        Iterable<EntityRef> assignableTaskIterable = entityManager.getEntitiesWith(AssignableTaskComponent.class);
        for (EntityRef assignableTaskComponentEntity : assignableTaskIterable) {
            AssignableTaskComponent assignableTaskComponent = assignableTaskComponentEntity.getComponent(AssignableTaskComponent.class);
            if (hasAssignableSubtask(assignableTaskComponent)) {
                MinionComponent minionComponent = assignableTaskComponentEntity.getComponent(MinionComponent.class);
                if (null == minionComponent) {
                    assignableTaskComponentEntityList.add(assignableTaskComponentEntity);
                }
            }
        }

        if (assignableTaskComponentEntityList.isEmpty()) {
            return;
        }

        Collections.sort(assignableTaskComponentEntityList, new Comparator<EntityRef>() {
            @Override
            public int compare(EntityRef e1, EntityRef e2) {
                AssignableTaskComponent atc1 = e1.getComponent(AssignableTaskComponent.class);
                AssignableTaskComponent atc2 = e2.getComponent(AssignableTaskComponent.class);
                long create1 = atc1.creationGameTime;
                long create2 = atc2.creationGameTime;
                return (create1 == create2) ? 0 : ((create1 < create2) ? -1 : 1);
            }
        });

        EntityRef assignableTaskComponentEntity = assignableTaskComponentEntityList.remove(0);
        
        AssignedTaskComponent assignedTaskComponent = createAssignedTaskComponent(assignableTaskComponentEntity);
        minionEntity.addComponent(assignedTaskComponent);
    }

    private boolean hasAssignableSubtask(AssignableTaskComponent assignableTaskComponent) {
        Vector3i areaMin = assignableTaskComponent.area.min();
        Vector3i areaSize = assignableTaskComponent.area.size();
        for (int i = areaMin.x; i < (areaSize.x + areaMin.x); i++) {
            for (int j = areaMin.y; j < (areaSize.y + areaMin.y); j++) {
                for (int k = areaMin.z; k < (areaSize.z + areaMin.z); k++) {
                    TaskStatusType taskStatusType = getTaskStatusType(assignableTaskComponent, i, j, k);
                    if (TaskStatusType.AVAILABLE == taskStatusType) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void finishedTask(EntityRef entity, AssignedTaskComponent assignedTaskComponent) {
        EntityRef assignableTaskEntity = assignedTaskComponent.assignableTaskEntity;
        if (TaskStatusType.COMPLETED == assignedTaskComponent.taskStatusType) {
            markAsCompleted(assignableTaskEntity, assignedTaskComponent);
            boolean allDone = isTaskCompleted(assignableTaskEntity, assignedTaskComponent);
            if (allDone) {
                assignableTaskEntity.destroy();
            }
        } else {
            markAsAvailable(assignableTaskEntity, assignedTaskComponent);
        }
    }

    private AssignedTaskComponent createAssignedTaskComponent(EntityRef assignableTaskEntity) {
        AssignableTaskComponent assignableTaskComponent = assignableTaskEntity.getComponent(AssignableTaskComponent.class);
        AssignedTaskComponent assignedTaskComponent = new AssignedTaskComponent();

        assignedTaskComponent.assignableTaskEntity = assignableTaskEntity;
        assignedTaskComponent.subtaskCoordinates = getNextAvailableSubtaskCoordinates(assignableTaskEntity, assignedTaskComponent);

        assignedTaskComponent.assignedTaskType = assignableTaskComponent.assignedTaskType;
        assignedTaskComponent.taskStatusType = TaskStatusType.ASSIGNED;
        assignedTaskComponent.targetLocation = new Vector3i(assignedTaskComponent.subtaskCoordinates);

        markAsAssigned(assignableTaskEntity, assignedTaskComponent);
        
        return assignedTaskComponent;
    }

    
    private void markAsAssigned(EntityRef assignableTaskEntity, AssignedTaskComponent assignedTaskComponent) {
        mark(assignableTaskEntity, assignedTaskComponent, TaskStatusType.ASSIGNED);
    }

    private void markAsCompleted(EntityRef assignableTaskEntity, AssignedTaskComponent assignedTaskComponent) {
        mark(assignableTaskEntity, assignedTaskComponent, TaskStatusType.COMPLETED);
    }

    private void markAsAvailable(EntityRef assignableTaskEntity, AssignedTaskComponent assignedTaskComponent) {
        mark(assignableTaskEntity, assignedTaskComponent, TaskStatusType.AVAILABLE);
    }

    private Vector3i getNextAvailableSubtaskCoordinates(EntityRef assignableTaskEntity, AssignedTaskComponent assignedTaskComponent) {
        AssignableTaskComponent assignableTaskComponent = assignableTaskEntity.getComponent(AssignableTaskComponent.class);
        Vector3i firstCoordinates = assignableTaskComponent.nextSubtaskCoordinatesToAssign;
        Vector3i areaMin = assignableTaskComponent.area.min();
        Vector3i areaSize = assignableTaskComponent.area.size();
        Vector3i nextAvailableSubtaskCoordinates = null;
        int[] xLoopedRangeArray = getLoopedRangeArray(firstCoordinates.x, areaMin.x, areaMin.x + areaSize.x);
        int[] yLoopedRangeArray = getLoopedRangeArray(firstCoordinates.y, areaMin.y, areaMin.y + areaSize.y);
        int[] zLoopedRangeArray = getLoopedRangeArray(firstCoordinates.z, areaMin.z, areaMin.z + areaSize.z);
        for (int x: xLoopedRangeArray) {
            for (int y: yLoopedRangeArray) {
                for (int z: zLoopedRangeArray) {
                    // save when we find an available match,
                    // but then continue on to the next coordinate so we can start there next time
                    if (null == nextAvailableSubtaskCoordinates) {
                        TaskStatusType taskStatusType = getTaskStatusType(assignableTaskComponent, x, y, z);
                        if (TaskStatusType.AVAILABLE == taskStatusType) {
                            nextAvailableSubtaskCoordinates = new Vector3i(x, y, z);
                        }
                    } else {
                        assignableTaskComponent.nextSubtaskCoordinatesToAssign = new Vector3i(x, y, z);
                        return nextAvailableSubtaskCoordinates;
                    }
                }
            }
        }
        
        if (null == nextAvailableSubtaskCoordinates) {
            // It's an error to reach this point since it means there was no available task
            throw new IllegalStateException("Didn't find any available subtask coordinates");
        } else {
            // If we get here, that means we've cycled through all coordinates so
            // No update needed for assignableTaskComponent.nextSubtaskCoordinatesToAssign
            return nextAvailableSubtaskCoordinates;
        }
    }

    private int[] getLoopedRangeArray(int start, int minInclusive, int maxExclusive) {
        int size = maxExclusive - minInclusive;
        int[] loopedRangeArray = new int[size];
        for (int index = 0; index < size; ++index) {
            int value = start + index;
            if (value >= maxExclusive) {
                value = minInclusive + (value - maxExclusive);
            }
            loopedRangeArray[index] = value;
        }
        return loopedRangeArray;
    }

    private boolean isTaskCompleted(EntityRef assignableTaskEntity, AssignedTaskComponent assignedTaskComponent) {
        AssignableTaskComponent assignableTaskComponent = assignableTaskEntity.getComponent(AssignableTaskComponent.class);
        Vector3i areaMin = assignableTaskComponent.area.min();
        Vector3i areaSize = assignableTaskComponent.area.size();
        for (int i = areaMin.x; i < (areaSize.x + areaMin.x); i++) {
            for (int j = areaMin.y; j < (areaSize.y + areaMin.y); j++) {
                for (int k = areaMin.z; k < (areaSize.z + areaMin.z); k++) {
                    TaskStatusType taskStatusType = getTaskStatusType(assignableTaskComponent, i, j, k);
                    if (TaskStatusType.COMPLETED != taskStatusType) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void mark(EntityRef assignableTaskEntity, AssignedTaskComponent assignedTaskComponent, TaskStatusType statusType) {
        Vector3i subtaskCoordinates = assignedTaskComponent.subtaskCoordinates;
        AssignableTaskComponent assignableTaskComponent = assignableTaskEntity.getComponent(AssignableTaskComponent.class);
        setTaskStatusType(assignableTaskComponent, subtaskCoordinates.x, subtaskCoordinates.y, subtaskCoordinates.z, statusType);
    }
}
