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

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.terasology.asset.Assets;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.miniion.components.MinionComponent;
import org.terasology.miniion.components.actions.SpawnMinionActionComponent;
import org.terasology.miniion.gui.UIModButton.ButtonType;
import org.terasology.rendering.gui.animation.AnimationMove;
import org.terasology.rendering.gui.animation.AnimationRotate;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.VisibilityListener;
import org.terasology.rendering.gui.widgets.UIComboBox;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UIInventoryGrid;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIListItem;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.rendering.nui.Color;

/**
 * Displays two inventories, and allows moving items between them
 * 
 * @author Immortius <immortius@gmail.com>
 */
public class UICardBook extends UIWindow {

	EntityRef container = EntityRef.NULL;
	EntityRef creature = EntityRef.NULL;
	EntityManager entityManager;

	private final UIInventoryGrid playerInventory;
	private final UIInventoryGrid playerToolbar;
	private final UIInventoryGrid containerInventory;

	private final UIImage leftGearWheel;
	private final UIImage rightGearWheel;
	private final UIImage background;
	private final UILabel page1label;
	private final UIComboBox minioncombo;
	private final UIModButton buttoncreatecard;

	public UICardBook() {
		setId("cardbook");
		entityManager = CoreRegistry.get(EntityManager.class);
		setBackgroundColor(new Color(0, 0, 0, 200).toHex());
		setModal(true);
		maximize();
		// setCloseBinds(new String[] {"engine:useHeldItem"});
		setCloseKeys(new int[] { Keyboard.KEY_ESCAPE });

		addVisibilityListener(new VisibilityListener() {
			@Override
			public void changed(UIDisplayElement element, boolean visibility) {
				if (!visibility) {
					getGUIManager().getWindowById("hud")
							.getElementById("leftGearWheel").setVisible(true);
					getGUIManager().getWindowById("hud")
							.getElementById("rightGearWheel").setVisible(true);
				}
			}
		});

		background = new UIImage();
		background.setTexture(Assets.getTexture("miniion:openbook"));
		background.setHorizontalAlign(EHorizontalAlign.CENTER);
		background.setVerticalAlign(EVerticalAlign.CENTER);
		background.setSize(new Vector2f(500, 300));
		background.setVisible(true);
		addDisplayElement(background);

		page1label = new UILabel();
		page1label.setPosition(new Vector2f(40, 20));
		page1label.setSize(new Vector2f(190, 60));
		page1label.setWrap(true);
		page1label.setText("Insert an empty card into this page!");
		page1label.setColor(Color.BLACK.toHex());
		page1label.setVisible(true);
		background.addDisplayElement(page1label);

		minioncombo = new UIComboBox(new Vector2f(190, 20), new Vector2f(190,
				120));
		minioncombo.setPosition(new Vector2f(40, 120));
		minioncombo.setVisible(false);
		background.addDisplayElement(minioncombo);

		buttoncreatecard = new UIModButton(new Vector2f(140, 20),
				ButtonType.NORMAL);
		buttoncreatecard.setPosition(new Vector2f(280, 60));
		buttoncreatecard.setSize(new Vector2f(180, 180));
		buttoncreatecard.setLabel("Create card");
		buttoncreatecard.setVisible(false);
		buttoncreatecard.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
				executeCreate(element, button);
			}
		});

		background.addDisplayElement(buttoncreatecard);

		playerToolbar = new UIInventoryGrid(10);
		playerToolbar.setVisible(true);
		playerToolbar.setHorizontalAlign(EHorizontalAlign.CENTER);
		playerToolbar.setVerticalAlign(EVerticalAlign.BOTTOM);
		playerToolbar.setCellMargin(new Vector2f(0f, 0f));
		playerToolbar.setBorderImage("engine:inventory", new Vector2f(0f, 84f),
				new Vector2f(169f, 83f), new Vector4f(4f, 4f, 4f, 4f));

		playerInventory = new UIInventoryGrid(10);
		playerInventory.setVisible(true);
		playerInventory.setCellMargin(new Vector2f(0f, 0f));
		playerInventory.setBorderImage("engine:inventory",
				new Vector2f(0f, 84f), new Vector2f(169f, 61f), new Vector4f(
						5f, 4f, 3f, 4f));

		containerInventory = new UIInventoryGrid(8);
		containerInventory.setVisible(true);
		containerInventory.setPosition(new Vector2f(40, 60));
		containerInventory.setCellMargin(new Vector2f(0f, 0f));
		background.addDisplayElement(containerInventory);
		// containerInventory.setBorderImage("engine:inventory", new
		// Vector2f(0f, 84f), new Vector2f(169f, 61f), new Vector4f(5f, 4f, 3f,
		// 4f));

		leftGearWheel = new UIImage(Assets.getTexture("engine:inventory"));
		leftGearWheel.setSize(new Vector2f(36f, 36f));
		leftGearWheel.setTextureOrigin(new Vector2f(121.0f, 168.0f));
		leftGearWheel.setTextureSize(new Vector2f(27.0f, 27.0f));
		leftGearWheel.setVisible(true);

		leftGearWheel.setHorizontalAlign(EHorizontalAlign.CENTER);
		leftGearWheel.setVerticalAlign(EVerticalAlign.BOTTOM);
		leftGearWheel.setPosition(new Vector2f(
				leftGearWheel.getPosition().x - 240f, leftGearWheel
						.getPosition().y - 4f));

		rightGearWheel = new UIImage(Assets.getTexture("engine:inventory"));
		rightGearWheel.setSize(new Vector2f(36f, 36f));
		rightGearWheel.setTextureOrigin(new Vector2f(121.0f, 168.0f));
		rightGearWheel.setTextureSize(new Vector2f(27.0f, 27.0f));
		rightGearWheel.setVisible(true);

		rightGearWheel.setHorizontalAlign(EHorizontalAlign.CENTER);
		rightGearWheel.setVerticalAlign(EVerticalAlign.BOTTOM);
		rightGearWheel.setPosition(new Vector2f(
				rightGearWheel.getPosition().x + 240f, rightGearWheel
						.getPosition().y - 4f));

		addDisplayElement(rightGearWheel);
		addDisplayElement(leftGearWheel);

		addDisplayElement(playerInventory);
		addDisplayElement(playerToolbar);
		// addDisplayElement(containerInventory);

		layout();
	}

	public void openContainer(EntityRef container, EntityRef creature) {

		// empty and fill combo box, just in case
		minioncombo.removeAll();
		PrefabManager prefMan = CoreRegistry.get(PrefabManager.class);
		for (Prefab prefab : prefMan.listPrefabs(MinionComponent.class)) {
			UIListItem listitem = new UIListItem();
			listitem.setTextColor(Color.BLACK.toHex());
			String[] tempstring = prefab.getName().split(":");
			if (tempstring.length == 2) {
				listitem.setText(tempstring[1]);
				minioncombo.addItem(listitem);
			}
		}

		this.container = container;
		this.creature = creature;

                playerToolbar.linkToEntity(creature, 0, 9);
                playerInventory.linkToEntity(creature, 10);
                containerInventory.linkToEntity(container);

                // Pretty sure that setConnected is automatic by this point.
                
//		playerToolbar.setConnected(container);
//		playerInventory.setConnected(container);
//		containerInventory.setConnected(creature);
//		// TODO connect toolbar <-> inventory somehow to allow fast transfer.

		getGUIManager().getWindowById("hud").getElementById("leftGearWheel")
				.setVisible(false);
		getGUIManager().getWindowById("hud").getElementById("rightGearWheel")
				.setVisible(false);
		layout();

		playerInventory.setPosition(new Vector2f(Display.getWidth() / 2
				- playerInventory.getSize().x / 2, Display.getHeight() + 5f));
		playerInventory.addAnimation(new AnimationMove(new Vector2f(Display
				.getWidth() / 2 - playerInventory.getSize().x / 2, Display
				.getHeight() - 192f), 20f));
		playerInventory.getAnimation(AnimationMove.class).start();
		leftGearWheel.addAnimation(new AnimationRotate(-120f, 10f));
		leftGearWheel.getAnimation(AnimationRotate.class).start();
		rightGearWheel.addAnimation(new AnimationRotate(120f, 10f));
		rightGearWheel.getAnimation(AnimationRotate.class).start();
	}

	private void executeCreate(UIDisplayElement element, int button) {
	    SlotBasedInventoryManager inventoryManager = CoreRegistry.get(SlotBasedInventoryManager.class);

		PrefabManager prefMan = CoreRegistry.get(PrefabManager.class);
		for (Prefab prefab : prefMan.listPrefabs(MinionComponent.class)) {
			if (minioncombo.getSelection() != null
					&& prefab.getName().contains(
							minioncombo.getSelection().getText())) {
				if (this.container != null) {
					InventoryComponent invcomp = this.container
							.getComponent(InventoryComponent.class);
					if (invcomp != null) {
					        // previously was invcomp.itemSlots.get(0)
                                                EntityRef itemInSlot0Entity = inventoryManager.getItemInSlot(this.container, 0);
						if ((itemInSlot0Entity != null) && (itemInSlot0Entity != EntityRef.NULL)) {
							EntityRef itemstack = itemInSlot0Entity;
							ItemComponent item = itemstack.getComponent(ItemComponent.class);
							if(item.stackCount == 1){
								itemstack.destroy();
							}else{
								item.stackCount--;
							}
							EntityRef filledcard = entityManager
									.create("miniion:filledcard");
							filledcard.getComponent(ItemComponent.class).name = minioncombo
									.getSelection().getText() + " card";
							filledcard
									.getComponent(SpawnMinionActionComponent.class).prefab = prefab
									.getName();
							EntityRef player = CoreRegistry.get(
									LocalPlayer.class).getCharacterEntity();
                                                        inventoryManager.giveItem(player, filledcard);
							// TODO: player should be listening for a ReceivedItemEvent rather than directly using inventory managerm
						        // but that isn't happening yet.
						        // player.send(new ReceivedItemEvent(filledcard));
							buttoncreatecard.setVisible(false);
							minioncombo.setVisible(false);
						}
					}
				}
			}
		}
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		super.update();
	            SlotBasedInventoryManager inventoryManager = CoreRegistry.get(SlotBasedInventoryManager.class);
		if (this.container != null) {
			InventoryComponent invcomp = this.container
					.getComponent(InventoryComponent.class);
			if (invcomp != null) {
                            // previously was invcomp.itemSlots.get(0)
                            EntityRef itemInSlot0Entity = inventoryManager.getItemInSlot(this.container, 0);
                            if ((itemInSlot0Entity != null) && (itemInSlot0Entity != EntityRef.NULL)) {
					EntityRef itemstack = itemInSlot0Entity;
					ItemComponent stackComp = itemstack
							.getComponent(ItemComponent.class);
					if (stackComp != null) {
						if (stackComp.name.matches("empty card")) {
							buttoncreatecard.setVisible(true);
							minioncombo.setVisible(true);
						} else {
							buttoncreatecard.setVisible(false);
							minioncombo.setVisible(false);
						}
					}
				} else {
					buttoncreatecard.setVisible(false);
					minioncombo.setVisible(false);
				}
			}
		}
	}
}
