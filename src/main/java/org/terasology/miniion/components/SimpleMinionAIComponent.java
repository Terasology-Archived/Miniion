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
package org.terasology.miniion.components;

import java.util.ArrayList;
import java.util.List;

import org.terasology.entitySystem.Component;
import org.terasology.math.geom.Vector3f;

/**
 *         really used
 */
public final class SimpleMinionAIComponent implements Component {

    public long lastChangeOfDirectionAt;
    public Vector3f lastPosition;
    public double previousdistanceToTarget = Double.NEGATIVE_INFINITY;

    public long lastAttacktime;
    public long lastDistancecheck;
    public long lastPathtime;
    public long lastHungerCheck;
    public int patrolCounter;
    public int craftprogress;

    public Vector3f movementTarget = new Vector3f();
    public Vector3f previousTarget = new Vector3f();

    public List<Vector3f> movementTargets = new ArrayList<>();
    public List<Vector3f> gatherTargets = new ArrayList<>();
    public List<Vector3f> patrolTargets = new ArrayList<>();
    public List<Vector3f> pathTargets = new ArrayList<>();

    public boolean followingPlayer = true;
    public boolean locked;

    public void clearCommands() {
        movementTargets.removeAll(movementTargets);
        gatherTargets.removeAll(gatherTargets);
        patrolTargets.removeAll(patrolTargets);
    }

}
