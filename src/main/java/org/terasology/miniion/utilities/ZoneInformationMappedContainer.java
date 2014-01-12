
package org.terasology.miniion.utilities;

import org.terasology.classMetadata.MappedContainer;
import org.terasology.math.Vector3i;
import org.terasology.miniion.minionenum.ZoneType;

@MappedContainer
public class ZoneInformationMappedContainer {

    public String name;
    public ZoneType zonetype;

    public Vector3i minBounds = new Vector3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    
    public Vector3i startPosition;

    public boolean isTerraformComplete = false;
}
