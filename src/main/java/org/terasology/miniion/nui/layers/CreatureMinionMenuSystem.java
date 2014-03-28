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
package org.terasology.miniion.nui.layers;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.miniion.components.MinionComponent;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.UIButton;

@Share(CreatureMinionMenuSystem.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class CreatureMinionMenuSystem extends BaseComponentSystem {
    // private static final Logger logger = LoggerFactory.getLogger(MinionMenuSystem.class);

    @In
    private EntityManager entityManager;

    // TODO: eventually this needs to be done by listening for minion create/destroy events,
    // probably in ManagerInterfaceSystem

    public void populateCreatureMenus(ColumnLayout summonTabColumnLayout) {
        Iterable<EntityRef> entityIterable = entityManager.getEntitiesWith(MinionComponent.class);
        for (EntityRef entityRef : entityIterable) {
            
            MinionComponent minionComponent = entityRef.getComponent(MinionComponent.class);

            String minionIdentity = minionComponent.flavortext + " - " + minionComponent.name;
                    
            UIButton existingMinionMenuItem = new UIButton(minionIdentity, minionIdentity);
            existingMinionMenuItem.subscribe(new ActivateEventListener() {
                @Override
                public void onActivated(UIWidget widget) {
                    // Go to minion? select minion?
                }
            });

            summonTabColumnLayout.addWidget(existingMinionMenuItem);
        }
    }
}
