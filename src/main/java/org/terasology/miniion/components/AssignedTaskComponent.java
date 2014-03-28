
package org.terasology.miniion.components;

import org.terasology.entitySystem.Component;
import org.terasology.math.Region3i;

public final class AssignedTaskComponent implements Component {
    public AssignedTaskType assignedTaskType;
    public Region3i area;
}
