
package org.terasology.miniion.components;

import org.terasology.entitySystem.Component;
import org.terasology.math.Region3i;

public final class AssignableTaskComponent implements Component {
    public AssignedTaskType assignedTaskType;
    public Region3i area;
    public long creationGameTime;
}
