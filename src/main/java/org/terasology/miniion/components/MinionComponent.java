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
package org.terasology.miniion.components;

import org.terasology.entitySystem.Component;
import org.terasology.miniion.utilities.MinionRecipe;

/**
 * Allows an entity to store items
 * 
 * @author Immortius <immortius@gmail.com>
 */
public final class MinionComponent implements Component {

    // simple uri defining an icon
    public String icon;
    // simple uri defining an icon for the filled card in inventory
    public String filledCardIcon;
    
    // personal name for the minion, not set in prefab!
    public String name = "unknown";
    // minion type, eg : oreoBuilder, needs to be defined in prefab
    public String flavortext = "unknown";
    // skin to shown when minion is selected, eg : Oreons:OreonSkinSelected, needs to be defined in prefab
    public String selectedSkin = null;
    // skin to shown when minion is not selected, eg : Oreons:OreonSkin, needs to be defined in prefab
    public String unselectedSkin = null;

    public boolean dying = false;

    //stats
    public int Health;
    public int HealthTotal;
    public int Hunger;
    public int Hungertotal;
    public int Stamina;
    public int Staminatotal;

    //the recipe to craft when working
    public MinionRecipe assignedrecipe;

    public MinionComponent() {

    }

}
