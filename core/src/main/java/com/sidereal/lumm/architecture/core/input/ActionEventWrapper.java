/******************************************************************************* Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. ******************************************************************************/

package com.sidereal.lumm.architecture.core.input;

import com.sidereal.lumm.architecture.core.Input;
import com.sidereal.lumm.architecture.core.Input.ActionType;

/**
 * Wrapper for Action events. Are internally created in
 * {@link Input#addOnClickListener(String, int, OnClickListener, ActionType)} from the
 * parameters given, so as to not lose any data. After creation they are
 * attached to the Input processor found with the String parameter as a name
 * 
 * @author Claudiu Bele
 */
public class ActionEventWrapper {

	public int code;

	public OnClickListener event;

	public ActionType inputType;

	public ActionEventWrapper(int code, OnClickListener event, ActionType inputType) {
		this.code = code;
		this.event = event;
		this.inputType = inputType;
	}

}
