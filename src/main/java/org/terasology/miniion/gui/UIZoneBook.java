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

import java.util.List;

import javax.vecmath.Vector2f;

import org.lwjgl.input.Keyboard;
import org.terasology.asset.Assets;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.miniion.components.ZoneComponent;
import org.terasology.miniion.componentsystem.controllers.MinionSystem;
import org.terasology.miniion.gui.UIModButton.ButtonType;
import org.terasology.miniion.minionenum.ZoneType;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureUtil;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.widgets.UIComboBox;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIList;
import org.terasology.rendering.gui.widgets.UIListItem;
import org.terasology.rendering.gui.widgets.UIText;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.rendering.nui.Color;
import org.terasology.world.selection.BlockSelectionComponent;

public class UIZoneBook extends UIWindow {
    private static final int MAX_SELECTED_BOUNDS = 50;

    /*
     * @In private LocalPlayer localPlayer;
     * 
     * @In private EntityManager entityManager;
     */
    private final UIImage background;
    private final UILabel lblzonename, lblheight, lbldepth, lblwidth, lblzonetype, lblError;
    private final UIText txtzonename, txtheight, txtdepth, txtwidth;
    private final UIComboBox cmbType;
    private UIList uizonelistgroup, uizonelist;
    private UIModButton btnSave, btnDelete, btnBack;

    private EntityRef lastSelectedZone = EntityRef.NULL;

    private ClickListener zoneTypeSelectionListener = new ClickListener() {
        @Override
        public void click(UIDisplayElement element, int button) {
            UIListItem listitem = (UIListItem) element;
            List<EntityRef> currentZoneList = null;
            switch (((ZoneType) listitem.getValue())) {
                case Gather: {
                    currentZoneList = MinionSystem.getGatherZoneList();
                    break;
                }
                case Terraform: {
                    currentZoneList = MinionSystem.getTerraformZoneList();
                    break;
                }
                case Work: {
                    currentZoneList = MinionSystem.getWorkZoneList();
                    break;
                }
                case Storage: {
                    currentZoneList = MinionSystem.getStorageZoneList();
                    break;
                }
                case OreonFarm: {
                    currentZoneList = MinionSystem.getOreonFarmZoneList();
                    break;
                }
                default: {
                    break;
                }
            }
            
            if (null != currentZoneList) {
                uizonelist.removeAll();
                for (EntityRef zone : currentZoneList) {
                    ZoneComponent zoneComponent = zone.getComponent(ZoneComponent.class);
                    String zoneName = zoneComponent.Name;
                    UIListItem newlistitem = new UIListItem(zoneName, zone);
                    newlistitem.setTextColor(Color.toColorString(Color.BLACK));
                    newlistitem.addClickListener(zoneSelectionlistener);
                    uizonelist.addItem(newlistitem);
                }
                uizonelistgroup.setVisible(false);
                uizonelist.setVisible(true);
                btnBack.setVisible(true);
            }
        }
    };

    private ClickListener zoneSelectionlistener = new ClickListener() {
        @Override
        public void click(UIDisplayElement element, int button) {
            UIListItem listitem = (UIListItem) element;
            if (cmbType.isVisible()) {
                cmbType.setVisible(false);
            }
            lblError.setText("");
            hideSelectedZone();
            EntityRef zone = (EntityRef) listitem.getValue();
            ZoneComponent zoneComponent = zone.getComponent(ZoneComponent.class);
            txtzonename.setText(zoneComponent.Name);
            txtheight.setText("" + zoneComponent.getZoneHeight());
            txtwidth.setText("" + zoneComponent.getZoneWidth());
            txtdepth.setText("" + zoneComponent.getZoneDepth());
            switch (zoneComponent.zonetype) {
                case Gather: {
                    lblzonetype.setText("Zonetype : Gather");
                    break;
                }
                case Terraform: {
                    lblzonetype.setText("Zonetype : Terraform");
                    break;
                }
                case Work: {
                    lblzonetype.setText("Zonetype : Work");
                    break;
                }
                default: {
                    lblzonetype.setText("label wasn't set");
                    break;
                }
            }

            btnSave.setVisible(false);
            btnDelete.setVisible(true);
            lastSelectedZone = zone;
            showSelectedZone();
        }
    };

    public UIZoneBook() {

        setId("zonebook");
        setModal(true);
        maximize();
        setCloseKeys(new int[]{Keyboard.KEY_ESCAPE});

        background = new UIImage();
        background.setTexture(Assets.getTexture("miniion:openbook"));
        background.setHorizontalAlign(EHorizontalAlign.CENTER);
        background.setVerticalAlign(EVerticalAlign.CENTER);
        background.setSize(new Vector2f(500, 300));
        background.setVisible(true);
        addDisplayElement(background);

        uizonelist = new UIList();
        uizonelist.setSize(new Vector2f(200, 220));
        uizonelist.setPosition(new Vector2f(40, 20));
        uizonelist.setVisible(true);
        background.addDisplayElement(uizonelist);

        uizonelistgroup = new UIList();
        uizonelistgroup.setSize(new Vector2f(200, 250));
        uizonelistgroup.setPosition(new Vector2f(40, 20));
        uizonelistgroup.setVisible(true);
        background.addDisplayElement(uizonelistgroup);

        lblzonename = new UILabel("Zone name :");
        lblzonename.setPosition(new Vector2f(260, 20));
        lblzonename.setColor(Color.toColorString(Color.BLACK));
        lblzonename.setVisible(true);
        background.addDisplayElement(lblzonename);

        txtzonename = new UIText();
        txtzonename.setPosition(new Vector2f(350, 20));
        txtzonename.setColor(Color.toColorString(Color.BLACK));
        txtzonename.setSize(new Vector2f(80, 20));
        txtzonename.setVisible(true);
        background.addDisplayElement(txtzonename);

        lblheight = new UILabel("Height :");
        lblheight.setPosition(new Vector2f(260, 40));
        lblheight.setColor(Color.toColorString(Color.BLACK));
        lblheight.setVisible(true);
        background.addDisplayElement(lblheight);

        txtheight = new UIText();
        txtheight.setPosition(new Vector2f(350, 40));
        txtheight.setColor(Color.toColorString(Color.BLACK));
        txtheight.setSize(new Vector2f(80, 20));
        txtheight.setVisible(true);
        background.addDisplayElement(txtheight);

        lblwidth = new UILabel("Width :");
        lblwidth.setPosition(new Vector2f(260, 60));
        lblwidth.setColor(Color.toColorString(Color.BLACK));
        lblwidth.setVisible(true);
        background.addDisplayElement(lblwidth);

        txtwidth = new UIText();
        txtwidth.setPosition(new Vector2f(350, 60));
        txtwidth.setColor(Color.toColorString(Color.BLACK));
        txtwidth.setSize(new Vector2f(80, 20));
        txtwidth.setVisible(true);
        background.addDisplayElement(txtwidth);

        lbldepth = new UILabel("Depth :");
        lbldepth.setPosition(new Vector2f(260, 80));
        lbldepth.setColor(Color.toColorString(Color.BLACK));
        lbldepth.setVisible(true);
        background.addDisplayElement(lbldepth);

        txtdepth = new UIText();
        txtdepth.setPosition(new Vector2f(350, 80));
        txtdepth.setColor(Color.toColorString(Color.BLACK));
        txtdepth.setSize(new Vector2f(80, 20));
        txtdepth.setVisible(true);
        background.addDisplayElement(txtdepth);

        lblzonetype = new UILabel("");
        lblzonetype.setPosition(new Vector2f(260, 100));
        lblzonetype.setColor(Color.toColorString(Color.BLACK));
        lblzonetype.setVisible(true);
        background.addDisplayElement(lblzonetype);

        cmbType = new UIComboBox(new Vector2f(80, 20), new Vector2f(80, 200));
        cmbType.setPosition(new Vector2f(350, 100));
        cmbType.setVisible(false);
        background.addDisplayElement(cmbType);
        initTypes();

        lblError = new UILabel("");
        lblError.setWrap(true);
        lblError.setSize(new Vector2f(200, 80));
        lblError.setPosition(new Vector2f(260, 130));
        lblError.setColor(Color.toColorString(Color.RED));
        lblError.setVisible(true);
        background.addDisplayElement(lblError);

        btnSave = new UIModButton(new Vector2f(50, 20), ButtonType.NORMAL);
        btnSave.setPosition(new Vector2f(260, 230));
        btnSave.setLabel("Save");
        btnSave.setId("btnSave");
        btnSave.setVisible(true);
        btnSave.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                saveMinion(element, button);
            }
        });
        background.addDisplayElement(btnSave);

        btnDelete = new UIModButton(new Vector2f(50, 20), ButtonType.NORMAL);
        btnDelete.setPosition(new Vector2f(260, 230));
        btnDelete.setLabel("Delete");
        btnDelete.setId("btnDelZone");
        btnDelete.setVisible(false);
        btnDelete.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                deleteZone(element, button, (EntityRef) uizonelist.getSelection().getValue());
            }
        });
        background.addDisplayElement(btnDelete);

        btnBack = new UIModButton(new Vector2f(50, 20), ButtonType.NORMAL);
        btnBack.setPosition(new Vector2f(40, 240));
        btnBack.setLabel("Back");
        btnBack.setId("btnBack");
        btnBack.setVisible(false);
        btnBack.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                initList();
                btnBack.setVisible(false);
            }
        });
        background.addDisplayElement(btnBack);

    }

    private void saveMinion(UIDisplayElement element, int id) {
        lblError.setText("");
        if (null == MinionSystem.getCurrentBlockSelectionRegion()) {
            lblError.setText("Something went wrong. Please close the book and recreate the selection.");
        }
        if ((!cmbType.isVisible())) {
            this.close();
        }
        if (cmbType.isVisible() && cmbType.getSelection() == null) {
            lblError.setText("Please select a zone type");
            return;
        }
        if (cmbType.isVisible() && cmbType.getSelection() != null && (null == cmbType.getSelection().getText() || cmbType.getSelection().getText().isEmpty())) {
            lblError.setText("Please select a zone type");
            return;
        }
        if (cmbType.isVisible() && cmbType.getSelection() != null) {
            ZoneType zoneType = ZoneType.valueOf(cmbType.getSelection().getText());
            if (zoneType == ZoneType.OreonFarm) {
                Region3i region = MinionSystem.getCurrentBlockSelectionRegion();
                if (region.min().y != region.max().y) {
                    lblError.setText("A farm zone needs to be level. Please select a flat zone and try again");
                    return;
                }
            }
        }
        String newZoneName = txtzonename.getText().trim();
        if (newZoneName.length() < 0) {
            lblError.setText("Zone name must be specified");
            return;
        }
        
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        for (EntityRef zone : entityManager.getEntitiesWith(ZoneComponent.class)) {
            ZoneComponent zoneComponent = zone.getComponent(ZoneComponent.class);
            if (newZoneName.equalsIgnoreCase(zoneComponent.Name)) {
                lblError.setText("Zone name already exists!");
                return;
            }
        }

        int zoneheight;
        try {
            zoneheight = Integer.parseInt(txtheight.getText().trim());
        } catch (NumberFormatException e1) {
            lblError.setText("zone height needs to be an number");
            return;
        }
        int zonewidth;
        try {
            zonewidth = Integer.parseInt(txtwidth.getText());
        } catch (NumberFormatException e1) {
            lblError.setText("zone width needs to be an number");
            return;
        }
        int zonedepth;
        try {
            zonedepth = Integer.parseInt(txtdepth.getText());
        } catch (NumberFormatException e1) {
            lblError.setText("zone depth needs to be an number");
            return;
        }

        Region3i newZoneRegion = MinionSystem.getCurrentBlockSelectionRegion();
        Vector3i min = newZoneRegion.min();
        Vector3i newSize = new Vector3i(zonedepth, zoneheight, zonewidth);
        newZoneRegion = Region3i.createFromMinAndSize(min, newSize);

        ZoneComponent zoneComponent = new ZoneComponent(newZoneRegion);

        BlockSelectionComponent blockSelectionComponent = new BlockSelectionComponent();
        blockSelectionComponent.currentSelection = newZoneRegion;
        blockSelectionComponent.shouldRender = false;

        zoneComponent.Name = newZoneName;
        zoneComponent.zonetype = ZoneType.valueOf(cmbType.getSelection().getText());
        
        EntityRef newzone = entityManager.create(zoneComponent, blockSelectionComponent);

        newzone.saveComponent(zoneComponent);
        newzone.saveComponent(blockSelectionComponent);
        
        MinionSystem.addZone(newzone);
        lblzonetype.setText("");
        MinionSystem.setCurrentBlockSelectionRegion(null);
        lastSelectedZone = newzone;
        showSelectedZone();
        this.close();
    }

    private void deleteZone(UIDisplayElement element, int id, EntityRef deletezone) {
        ZoneComponent zoneComponent = deletezone.getComponent(ZoneComponent.class);
        switch (zoneComponent.zonetype) {
            case Gather: {
                hideSelectedZone(deletezone);
                MinionSystem.getGatherZoneList().remove(deletezone);
                break;
            }
            case Work: {
                hideSelectedZone(deletezone);
                MinionSystem.getWorkZoneList().remove(deletezone);
                break;
            }
            case Terraform: {
                hideSelectedZone(deletezone);
                MinionSystem.getTerraformZoneList().remove(deletezone);
                break;
            }
            case Storage: {
                hideSelectedZone(deletezone);
                MinionSystem.getStorageZoneList().remove(deletezone);
                break;
            }
            case OreonFarm: {
                hideSelectedZone(deletezone);
                MinionSystem.getOreonFarmZoneList().remove(deletezone);
                break;
            }
        }
        fillUI();
    }

    @Override
    public void open() {
        super.open();
        fillUI();
    }

    public boolean outofboundselection(Region3i region) {
        boolean retval = false;
        if (getAbsoluteDiff(region.min().x, region.max().x) > MAX_SELECTED_BOUNDS) {
            retval = true;
        }
        if (getAbsoluteDiff(region.min().y, region.max().y) > MAX_SELECTED_BOUNDS) {
            retval = true;
        }
        if (getAbsoluteDiff(region.min().z, region.max().z) > MAX_SELECTED_BOUNDS) {
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

    private void fillUI() {
        initList();
        resetInput();

        Region3i currentSelectedRegion = MinionSystem.getCurrentBlockSelectionRegion();
        if (null != currentSelectedRegion) {
            EntityManager entityManager = CoreRegistry.get(EntityManager.class);
            // TODO: this should really be a count of active zones owned by this player
            int zoneCount = entityManager.getCountOfEntitiesWith(ZoneComponent.class);
            
            txtzonename.setText("Zone" + String.valueOf(zoneCount));
            lblzonetype.setText("ZoneType :");
            cmbType.setVisible(true);
            txtwidth.setText(String.valueOf(ZoneComponent.getZoneWidth(currentSelectedRegion)));
            txtdepth.setText(String.valueOf(ZoneComponent.getZoneDepth(currentSelectedRegion)));
            txtheight.setText(String.valueOf(ZoneComponent.getZoneHeight(currentSelectedRegion)));

            if (outofboundselection(currentSelectedRegion)) {
                btnSave.setVisible(true);
                lblError.setText("The zone is to big to be saved, depth, width, height should not exceed 50");
            }
            else {
                btnSave.setVisible(true);
            }
            btnDelete.setVisible(false);
        }
    }

    private void initList() {
        //clear and init the list
        uizonelistgroup.setVisible(true);
        uizonelist.setVisible(false);
        uizonelistgroup.removeAll();
        for (ZoneType zonetype : ZoneType.values()) {
            UIListItem listitem = new UIListItem(zonetype.toString(), zonetype);
            listitem.setTextColor(Color.toColorString(Color.BLACK));
            listitem.addClickListener(zoneTypeSelectionListener);
            uizonelistgroup.addItem(listitem);
        }
    }

    private void initTypes() {
        UIListItem listitem = new UIListItem(ZoneType.Gather.toString(), ZoneType.Gather);
        listitem.setTextColor(Color.toColorString(Color.BLACK));
        cmbType.addItem(listitem);
        listitem = new UIListItem(ZoneType.Terraform.toString(), ZoneType.Terraform);
        listitem.setTextColor(Color.toColorString(Color.BLACK));
        cmbType.addItem(listitem);
        listitem = new UIListItem(ZoneType.OreonFarm.toString(), ZoneType.OreonFarm);
        listitem.setTextColor(Color.toColorString(Color.BLACK));
        cmbType.addItem(listitem);
    }

    private void resetInput() {
        //clear the textbowes
        txtzonename.setText("");
        txtheight.setText("");
        txtwidth.setText("");
        txtdepth.setText("");
        lblzonetype.setText("");
        lblError.setText("");
        btnSave.setVisible(false);
        btnDelete.setVisible(false);
    }

    private void hideSelectedZone() {
        hideSelectedZone(lastSelectedZone);
    }

    private void hideSelectedZone(EntityRef zone) {
        if (EntityRef.NULL != zone) {
            BlockSelectionComponent blockSelectionComponent = zone.getComponent(BlockSelectionComponent.class);
            blockSelectionComponent.shouldRender = false;
            zone.saveComponent(blockSelectionComponent);
        }
    }

    private void showSelectedZone() {
        if (EntityRef.NULL != lastSelectedZone) {
            BlockSelectionComponent blockSelectionComponent = lastSelectedZone.getComponent(BlockSelectionComponent.class);
            blockSelectionComponent.texture = Assets.get(TextureUtil.getTextureUriForColor(new java.awt.Color(255, 255, 0, 100)), Texture.class);
            blockSelectionComponent.shouldRender = true;
            // we probably don't want to save a selected zone rendering state as on
            // zoneComponent.blockSelectionEntity.saveComponent(blockSelectionComponent);
        }
    }

    public void shutdown() {
        hideSelectedZone();
        lastSelectedZone = EntityRef.NULL;
        super.shutdown();
    }
}
