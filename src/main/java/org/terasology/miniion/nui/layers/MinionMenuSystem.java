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

import java.util.Arrays;
import java.util.List;

import javax.vecmath.Vector3f;

import org.terasology.common.nui.MenuHUDElement;
import org.terasology.common.nui.UIMenuItem;
import org.terasology.common.nui.UISingleClickList;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
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

import com.google.common.collect.Lists;

@Share(MinionMenuSystem.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class MinionMenuSystem extends BaseComponentSystem {
    // private static final Logger logger = LoggerFactory.getLogger(MinionMenuSystem.class);

    @In
    private EntityManager entityManager;

    @Override
    public void initialise() {
        MenuHUDElement menuHUDElement = MenuHUDElement.getMenuHudElement();
        
        final UISingleClickList<UIMenuItem> menu = menuHUDElement.getMenu();
        final UISingleClickList<UIMenuItem> submenu = menuHUDElement.getSubmenu();
        final UISingleClickList<UIMenuItem> subSubMenu = menuHUDElement.getSubSubMenu();

        final UIMenuItem selectMinionMenu = new UIMenuItem("Select current", new Runnable() {
            @Override
            public void run() {
                submenu.setVisible(false);
            }
        });
        
        final UIMenuItem showMinionMenu = new UIMenuItem("Show Minions", new Runnable() {
            @Override
            public void run() {
                submenu.setVisible(false);
            }
        });
        
        final UIMenuItem createMinionMenu = new UIMenuItem("Create", new Runnable() {
            @Override
            public void run() {
                PrefabManager prefMan = CoreRegistry.get(PrefabManager.class);
                
                List<UIMenuItem> minionMenuItemList = Lists.newArrayList();
                for (final Prefab prefab : prefMan.listPrefabs(MinionComponent.class)) {
                    
                    String[] tempstring = prefab.getName().split(":");
                    if (tempstring.length == 2) {
                        String minionName = tempstring[1];
                        UIMenuItem selectMinionMenu = new UIMenuItem(minionName, new Runnable() {
                            @Override
                            public void run() {
                                subSubMenu.setVisible(false);
                                createMinion(prefab);
                            }
                        });
                        minionMenuItemList.add(selectMinionMenu);
                    }
                }
                
                subSubMenu.setList(minionMenuItemList);
                subSubMenu.setVisible(true);
            }
        });
        
        final UIMenuItem minionsMenu = new UIMenuItem("Minions", new Runnable() {
            @Override
            public void run() {
                submenu.setList(Arrays.asList(new UIMenuItem[] {
                        selectMinionMenu, showMinionMenu, createMinionMenu
                }));
                submenu.setVisible(true);
            }
        });

        menu.getList().add(minionsMenu);
    }

    private void createMinion(Prefab prefab) {
        // TODO: provide another way to pick spawn location
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        Vector3f spawnPos = localPlayer.getPosition();
        createMinion(prefab, spawnPos);
    }

    private void createMinion(Prefab prefab, Vector3f spawnPos) {
        
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
