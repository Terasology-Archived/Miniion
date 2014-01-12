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
package org.terasology.miniion.utilities;

import org.terasology.rendering.icons.Icon;

public final class ModIcons {
	
	private static final String MINIONICONS16 = "miniion:minionicon16";

	public static void loadIcons() {
		Icon.registerIcon("minionskull", "miniion:minionicon16", 0, 0);
		Icon.registerIcon("minioncommand", "miniion:minionicon16", 0, 1);
		Icon.registerIcon("emptycard", "miniion:minionicon16", 0, 2);
		Icon.registerIcon("filledcard", "miniion:minionicon16", 0, 3);
		Icon.registerIcon("cardbook", "miniion:minionicon16", 0, 4);
		Icon.registerIcon("oreominionbook", "miniion:minionicon16", 0, 5);
		Icon.registerIcon("zonebook", "miniion:minionicon16", 0, 6);
		Icon.registerIcon("zonetool", "miniion:minionicon16", 0, 7);
		
		Icon.registerIcon("mulch", MINIONICONS16, 1, 0);
		Icon.registerIcon("paper", MINIONICONS16, 1, 1);
		Icon.registerIcon("bookcover", MINIONICONS16, 1, 2);
	}
}
