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

import org.terasology.entitySystem.systems.In;
import org.terasology.engine.Time;
import org.terasology.miniion.componentsystem.controllers.MinionSystem;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.rendering.nui.Color;

public class UIScreenSettings extends UIWindow{
	
	@In
    private Time timer;
	
	private final UILabel lblTitle, lblselover, lblWarning;
	private final UIModButtonMenu btnOverlay;
	
    public UIScreenSettings() {
    	lblTitle = new UILabel();
    	lblTitle.setText("Settings");
		lblTitle.setTextShadow(true);
		//lblstatTitle.setBorderSolid(new Vector4f(4f, 4f, 4f, 4f), Color.red);
		lblTitle.setPosition(new Vector2f(150 - (lblTitle.getSize().x /2), 10));
		//lblstatTitle.setPosition(new Vector2f(120, 10));
		lblTitle.setVisible(true);
		lblTitle.setColor(Color.GREEN.toHex());
		addDisplayElement(lblTitle);
		
		lblWarning = new UILabel();
		lblWarning.setText("Warning!!! activating these settings might cause serious performance drops. Use with care");
		lblWarning.setWrap(true);
		lblWarning.setSize(new Vector2f(280, 30));
		lblWarning.setPosition(new Vector2f(10, 300));
		lblWarning.setVisible(true);
		lblWarning.setColor(Color.RED.toHex());
		addDisplayElement(lblWarning);
		
		lblselover = new UILabel();
		lblselover.setText("Selection overlay : renders selection boxes around new zones");
		lblselover.setWrap(true);
		lblselover.setSize(new Vector2f(280, 30));
		lblselover.setPosition(new Vector2f(10, 400));
		lblselover.setVisible(true);
		lblselover.setColor(Color.GREEN.toHex());
		addDisplayElement(lblselover);
		
		btnOverlay = new UIModButtonMenu(new Vector2f(50,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.TOGGLE);
		btnOverlay.setPosition(new Vector2f(125, 430));
		btnOverlay.setId("over");
		if(MinionSystem.isSelectionShown()){
			btnOverlay.setToggleState(true);
    	}else{
    		btnOverlay.setToggleState(false);
    	}
		btnOverlay.addClickListener(new ClickListener() {
			
			@Override
			public void click(UIDisplayElement element, int button) {
				MinionSystem.toggleSelectionShown();
				refreshScreen();
			}
		});
		btnOverlay.setVisible(true);
		addDisplayElement(btnOverlay);
		
		refreshScreen();
    }
    
    public void refreshScreen(){
    	
    	if(MinionSystem.isSelectionShown()){
			btnOverlay.setLabel("On");
    	}else{
    		btnOverlay.setLabel("Off");
    	}

    }
}
