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
package org.terasology.miniion.componentsystem.action;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.miniion.components.MinionComponent;
import org.terasology.miniion.components.actions.SpawnMinionActionComponent;
import org.terasology.miniion.componentsystem.controllers.MinionSystem;
import org.terasology.registry.CoreRegistry;

/**
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class SpawnMinionAction implements ComponentSystem {

    private EntityManager entityManager;

    @Override
    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = SpawnMinionActionComponent.class)
    public void onActivate(ActivateEvent event, EntityRef entity) {
        SpawnMinionActionComponent spawnInfo = entity.getComponent(SpawnMinionActionComponent.class);
        Vector3f spawnPos = event.getTargetLocation();
        if ((null != spawnPos) && (spawnInfo.prefab != null)) {
            spawnPos.y += 2;
            Prefab prefab = CoreRegistry.get(PrefabManager.class).getPrefab(
                    spawnInfo.prefab);
            if (prefab != null
                && prefab.getComponent(LocationComponent.class) != null) {
                EntityRef minion = entityManager.create(prefab, spawnPos);
                if (minion != null) {
                    CharacterMovementComponent movecomp = minion
                            .getComponent(CharacterMovementComponent.class);
                    movecomp.height = 0.31f;
                    minion.saveComponent(movecomp);
                    MinionComponent minioncomp = minion
                            .getComponent(MinionComponent.class);
                    String[] tempstring = MinionSystem.getName().split(":");
                    if (tempstring.length == 2) {
                        minioncomp.name = tempstring[0];
                        minioncomp.flavortext = tempstring[1];
                    }

                    return;
                }
            }
        }
        // if this gets here, we didn't actually create a minion
        event.consume();
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
}
