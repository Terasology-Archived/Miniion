package org.terasology.miniion.componentsystem.controllers;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Vector3i;
import org.terasology.miniion.components.OreonCropComponent;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;

@RegisterSystem(RegisterMode.AUTHORITY)
public class OreonCropSystem implements ComponentSystem, UpdateSubscriberSystem {
	
        @In
        private BlockManager blockManager;
        @In
	private EntityManager entityManager;
        @In
	private Time timer;
        @In
	private WorldProvider worldprovider;

	@Override
	public void initialise() {
	}
	
	 @ReceiveEvent(components = {OreonCropComponent.class})
	    public void onSpawn(OnAddedComponent event, EntityRef entity) {
		 initCrops();
	    }
	      
	private void initCrops(){
		//add 3000 to init to create  bit of a delay before first check
		long initTime = timer.getGameTimeInMs();
		for(EntityRef minion : entityManager.getEntitiesWith(OreonCropComponent.class)){
			OreonCropComponent crop = minion.getComponent(OreonCropComponent.class);
			crop.lastgrowthcheck = initTime;
			minion.saveComponent(crop);
		}
	}

	@Override
	public void shutdown() {
		
	}

	@Override
	public void update(float delta) {
		for (EntityRef entity : entityManager.getEntitiesWith(OreonCropComponent.class)){
			if(entity.hasComponent(BlockComponent.class)){
				OreonCropComponent crop = entity.getComponent(OreonCropComponent.class);
				if(crop.fullgrown){
					return;
				}
				if(crop.lastgrowthcheck == -1){
					crop.lastgrowthcheck = timer.getGameTimeInMs();
                                        entity.saveComponent(crop);
					return;
				}
				if(timer.getGameTimeInMs() - crop.lastgrowthcheck > 54000000){
					crop.lastgrowthcheck = timer.getGameTimeInMs();
					if(entity.hasComponent(LocationComponent.class)){
						LocationComponent locComponent = entity.getComponent(LocationComponent.class);
						Block oldblock = worldprovider.getBlock(locComponent.getWorldPosition());
						String oldUri = oldblock.getURI().getFamilyName();
						byte currentstage = Byte.parseByte(oldUri.substring(oldUri.length() - 1, oldUri.length()));
						if(crop.stages -1 > currentstage){
							currentstage++;
							if(currentstage == crop.stages -1){
								crop.fullgrown = true;
							}
							oldUri = oldUri.substring(0, oldUri.length()-1) + currentstage;
							Block newBlock = blockManager.getBlock(oldblock.getURI().getModuleName() + ":" + oldUri);
							worldprovider.setBlock(new Vector3i(locComponent.getWorldPosition()), newBlock);
						}
						entity.saveComponent(crop);
					}					
				}
			}
		}
		
	}

}
