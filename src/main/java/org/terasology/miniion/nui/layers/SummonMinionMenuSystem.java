/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.miniion.nui.layers;

import javax.vecmath.Vector3f;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.miniion.components.MinionComponent;
import org.terasology.miniion.componentsystem.controllers.MinionSystem;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.registry.Share;

@Share(SummonMinionMenuSystem.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class SummonMinionMenuSystem extends BaseComponentSystem {
    // private static final Logger logger = LoggerFactory.getLogger(MinionMenuSystem.class);

    @In
    private EntityManager entityManager;

    public void createMinion(Prefab prefab) {
        // TODO: provide another way to pick spawn location
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        Vector3f spawnPos = localPlayer.getPosition();
        createMinion(prefab, spawnPos);
    }

    public void createMinion(Prefab prefab, Vector3f spawnPos) {
        
        if ((null != spawnPos) && (prefab != null)) {
            spawnPos.y += 2;
            if (prefab.getComponent(LocationComponent.class) != null) {
                EntityRef minion = entityManager.create(prefab, spawnPos);
                if (minion != null) {
                    CharacterMovementComponent movecomp = minion.getComponent(CharacterMovementComponent.class);
                    movecomp.height = 0.31f;
                    minion.saveComponent(movecomp);
                    MinionComponent minioncomp = minion.getComponent(MinionComponent.class);
                    String[] tempstring = MinionSystem.getName().split(":");
                    if (tempstring.length == 2) {
                        minioncomp.name = tempstring[0];
                        minioncomp.flavortext = tempstring[1];
                    }

                    return;
                }
            }
        }                
    }
}
