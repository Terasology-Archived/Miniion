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
package org.terasology.miniion.componentsystem.action;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.selection.ApplyBlockSelectionEvent;
import org.terasology.math.Region3i;
import org.terasology.miniion.components.ZoneComponent;
import org.terasology.miniion.componentsystem.controllers.MinionSystem;

@RegisterSystem(RegisterMode.AUTHORITY)
public class SetSelectionAction implements ComponentSystem {

    @In
    private EntityManager entityManager;
    
    @Override
    public void initialise() {

    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent
    public void onSelection(ApplyBlockSelectionEvent event, EntityRef entity) {
        // TODO: this should be done with a new zone event, not method calls
        Region3i selectedRegion = event.getSelection();
        ZoneComponent zoneComponent = new ZoneComponent(selectedRegion);
        // TODO: should include BlockSelectionComponent eventually instead of being part of ZoneComponent
        EntityRef newZone = entityManager.create(zoneComponent);
        // we do not persist the temporary zone selection
        // newZone.saveComponent(zoneComponent);
        MinionSystem.setNewZone(newZone);
    }
}
