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

import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.miniion.minionenum.ZoneType;
import org.terasology.world.selection.BlockSelectionComponent;

public final class ZoneComponent implements Component {

    private static final int MAX_SELECTED_BOUNDS = 50;

    public EntityRef blockSelectionEntity = EntityRef.NULL;

    public String Name;
    public ZoneType zonetype;

    private ZoneComponent() {
    }
    
    public ZoneComponent(Region3i region) {
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        blockSelectionEntity = entityManager.create(new BlockSelectionComponent());
        BlockSelectionComponent selection = blockSelectionEntity.getComponent(BlockSelectionComponent.class);
        selection.currentSelection = region;
        selection.shouldRender = false;
    }

    public Vector3i getMinBounds() {
        return getBlockSelectionRegion().min();
    }

    public Vector3i getMaxBounds() {
        return getBlockSelectionRegion().max();
    }

    public boolean outofboundselection() {
        boolean retval = false;
        if (getAbsoluteDiff(getMinBounds().x, getMaxBounds().x) > MAX_SELECTED_BOUNDS) {
            retval = true;
        }
        if (getAbsoluteDiff(getMinBounds().y, getMaxBounds().y) > MAX_SELECTED_BOUNDS) {
            retval = true;
        }
        if (getAbsoluteDiff(getMinBounds().z, getMaxBounds().z) > MAX_SELECTED_BOUNDS) {
            retval = true;
        }
        return retval;
    }

    private int getAbsoluteDiff(int val1, int val2) {
        int width;
        if (val1 == val2) {
            width = 1;
        } else if (val1 < 0) {
            if (val2 < 0 && val2 < val1) {
                width = Math.abs(val2) - Math.abs(val1);
            } else if (val2 < 0 && val2 > val1) {
                width = Math.abs(val1) - Math.abs(val2);
            } else {
                width = Math.abs(val1) + val2;
            }
            width++;
        } else {
            if (val2 > -1 && val2 < val1) {
                width = val1 - val2;
            } else if (val2 > -1 && val2 > val1) {
                width = val2 - val1;
            } else {
                width = Math.abs(val2) + val1;
            }
            width++;
        }
        return width;
    }

    public BlockSelectionComponent getBlockSelectionComponent() {
        return blockSelectionEntity.getComponent(BlockSelectionComponent.class);
    }

    public Region3i getBlockSelectionRegion() {
        return getBlockSelectionComponent().currentSelection;
    }

    public Vector3i getStartPosition() {
        return getBlockSelectionRegion().min();
    }

    public int getZoneHeight() {
        Region3i region3i = getBlockSelectionRegion();
        return region3i.size().y;
    }

    public int getZoneDepth() {
        Region3i region3i = getBlockSelectionRegion();
        return region3i.size().x;
    }

    public int getZoneWidth() {
        Region3i region3i = getBlockSelectionRegion();
        return region3i.size().z;
    }

    public void resizeTo(int zoneheight, int zonedepth, int zonewidth) {
        BlockSelectionComponent blockSelectionComponent = getBlockSelectionComponent();
        Region3i region3i = blockSelectionComponent.currentSelection;
        Vector3i min = region3i.min();
        Vector3i newSize = new Vector3i(zonedepth, zoneheight, zonewidth);
        blockSelectionComponent.currentSelection = Region3i.createFromMinAndSize(min, newSize);
    }
}