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
package org.terasology.miniion.gui;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.terasology.engine.Time;
import org.terasology.entitySystem.systems.In;
import org.terasology.miniion.components.MinionComponent;
import org.terasology.miniion.componentsystem.controllers.MinionSystem;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.rendering.nui.Color;

public class UIScreenStats extends UIWindow {

    @In
    private Time timer;

    private final UILabel lblstatTitle, lblstatHealth, lblstatHunger, lblstatStamina;

    /*
     * delete this comment when no longer needed.
     * the way components work : you define a template in the component
     * you set inital values in the prefab
     * after that you can access those values ingame and edit them
     * You have your own screen, add all elements you need here, much easier to determine coordinates
     * hope this explains some things
     * for the timer, check crafting mod, has a good example, is what I did.
     * and for some reason the prefabs don't seem to initialize correctly, but it's a start.
     * */

    public UIScreenStats() {
        lblstatTitle = new UILabel();
        lblstatTitle.setTextShadow(true);
        lblstatTitle.setBorderSolid(new Vector4f(4f, 4f, 4f, 4f), Color.toColorString(Color.RED));
        lblstatTitle.setPosition(new Vector2f(100, 10));
        lblstatTitle.setVisible(true);
        lblstatTitle.setColor(Color.toColorString(Color.GREEN));
        addDisplayElement(lblstatTitle);

        lblstatHealth = new UILabel();
        lblstatHealth.setText("Health Placeholder");
        lblstatHealth.setPosition(new Vector2f(10, 40));
        lblstatHealth.setVisible(true);
        lblstatHealth.setColor(Color.toColorString(Color.GREEN));
        addDisplayElement(lblstatHealth);

        lblstatHunger = new UILabel();
        lblstatHunger.setPosition(new Vector2f(10, 70));
        lblstatHunger.setVisible(true);
        lblstatHunger.setColor(Color.toColorString(Color.YELLOW));
        addDisplayElement(lblstatHunger);

        lblstatStamina = new UILabel();
        lblstatStamina.setText("Stamina Placeholder");
        lblstatStamina.setPosition(new Vector2f(10, 100));
        lblstatStamina.setVisible(true);
        lblstatStamina.setColor(Color.toColorString(Color.CYAN));
        addDisplayElement(lblstatStamina);
    }

    public void refreshScreen() {

        if (MinionSystem.getActiveMinion() == null) {
            lblstatTitle.setText("Please Select A Minion!");
        } else {
            MinionComponent minioncomp = MinionSystem.getActiveMinion().getComponent(MinionComponent.class);
            if (minioncomp != null) {
                lblstatTitle.setText(minioncomp.name + "'s Stats");
                lblstatTitle.setPosition(new Vector2f((this.getSize().x / 2) - (lblstatTitle.getSize().x / 2), 10));
                lblstatHunger.setText("Hunger: " + minioncomp.Hunger + "/" + minioncomp.Hungertotal);
            }
        }
    }

    private void refreshStats() {
        if (MinionSystem.getActiveMinion() != null) {
            MinionComponent minioncomp = MinionSystem.getActiveMinion().getComponent(MinionComponent.class);
            if (minioncomp != null) {
                lblstatHunger.setText("Hunger: " + minioncomp.Hunger + "/" + minioncomp.Hungertotal);
            }
        }
    }

    @Override
    public void update() {
        super.update();
        //to see the changes in hunger update
        //for performance, might create a seperate refresh function 
        //with only the stats that change over time
        refreshStats();
    }
}
