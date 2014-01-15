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
		for(EntityRef cropEntity : entityManager.getEntitiesWith(OreonCropComponent.class)){
			OreonCropComponent crop = cropEntity.getComponent(OreonCropComponent.class);
			crop.lastgrowthcheck = initTime;
			cropEntity.saveComponent(crop);
		}
	}

	@Override
	public void shutdown() {
		
	}

	@Override
	public void update(float delta) {
		for (EntityRef cropEntity : entityManager.getEntitiesWith(OreonCropComponent.class)){
			if(cropEntity.hasComponent(BlockComponent.class)){
				OreonCropComponent crop = cropEntity.getComponent(OreonCropComponent.class);
				if(crop.fullgrown){
					return;
				}
				if(crop.lastgrowthcheck == -1){
					crop.lastgrowthcheck = timer.getGameTimeInMs();
                                        cropEntity.saveComponent(crop);
					return;
				}
                                if(timer.getGameTimeInMs() - crop.lastgrowthcheck > crop.timeInGameMsToNextStage){
					crop.lastgrowthcheck = timer.getGameTimeInMs();
					if(cropEntity.hasComponent(LocationComponent.class)){
						LocationComponent locComponent = cropEntity.getComponent(LocationComponent.class);
						Block currentBlock = worldprovider.getBlock(locComponent.getWorldPosition());
						String currentBlockFamilyStage = currentBlock.getURI().toString();
						int currentstageIndex = crop.blockFamilyStages.indexOf(currentBlockFamilyStage);
						int lastStageIndex = crop.blockFamilyStages.size() -1;
                                                if(lastStageIndex > currentstageIndex) {
						    currentstageIndex++;
                                                    if(currentstageIndex == lastStageIndex) {
                                                        crop.fullgrown = true;
                                                    }
                                                    String newBlockUri = crop.blockFamilyStages.get(currentstageIndex);
                                                    Block newBlock = blockManager.getBlock(newBlockUri);
                                                    if (newBlockUri.equals(newBlock.getURI().toString())) {
                                                        worldprovider.setBlock(new Vector3i(locComponent.getWorldPosition()), newBlock);
                                                    }
						}
						cropEntity.saveComponent(crop);
					}					
				}
			}
		}
		
	}

}
