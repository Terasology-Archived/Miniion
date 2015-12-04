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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.miniion.components.actions.OpenUiActionComponent;
import org.terasology.registry.In;

/**
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class OpenUiAction implements ComponentSystem {

    @In
    private LocalPlayer localPlayer;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {ItemComponent.class, OpenUiActionComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        OpenUiActionComponent uiInfo = entity.getComponent(OpenUiActionComponent.class);
        if (uiInfo != null) {
//            UIWindow uiWindow = guiManager.getWindowById(uiInfo.uiwindowid);
//            if (null == uiWindow) {
//                if (uiInfo.uiwindowid.equals("cardbook")) {
//                    uiWindow = new UICardBook();
//                } else if (uiInfo.uiwindowid.equals("activeminiion")) {
//                    uiWindow = new UIActiveMinion();
//                } else {
//                    throw new RuntimeException("Unsupported window class: '" + uiInfo.uiwindowid + "'");
//                }
//            }
//
//            uiWindow.open();
//
//            if (uiInfo.uiwindowid.matches("cardbook")) {
//                UICardBook cardbookui = (UICardBook)uiWindow;
//                cardbookui.openContainer(entity, localPlayer.getCharacterEntity());
//            }
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
}
