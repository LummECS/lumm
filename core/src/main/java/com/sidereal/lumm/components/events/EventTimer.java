/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.sidereal.lumm.components.events;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;

import com.sidereal.lumm.architecture.AbstractEvent;
import com.sidereal.lumm.architecture.Lumm;
import com.sidereal.lumm.architecture.LummObject;
import com.sidereal.lumm.architecture.concrete.ConcreteLummComponent;

/**
 * Schedule {@link AbstractEvent} objects to run after a specific amount of
 * time.
 * 
 * @author Claudiu Bele
 */
public class EventTimer extends ConcreteLummComponent {

	// region fields

	// it is the other way around because you can change values but not keys
	// the Float is the timeRemaining, which will be changed with each update,
	// so we want it as the value
	private Hashtable<AbstractEvent, Float> timedEvents;

	private ArrayList<AbstractEvent> eventsToRemove;

	private Hashtable<AbstractEvent, Float> eventsToAdd;

	// endregion fields

	// region constructors

	public EventTimer(LummObject obj) {

		super(obj);
		timedEvents = new Hashtable<AbstractEvent, Float>();
		eventsToAdd = new Hashtable<AbstractEvent, Float>();
		eventsToRemove = new ArrayList<AbstractEvent>();
	}

	// endregion constructors

	// region methods

	@Override
	public void onUpdate() {

		for (Entry<AbstractEvent, Float> entry : eventsToAdd.entrySet()) {
			timedEvents.put(entry.getKey(), entry.getValue());
		}
		eventsToAdd.clear();

		for (Entry<AbstractEvent, Float> entry : timedEvents.entrySet()) {
			entry.setValue(entry.getValue() - Lumm.time.getDeltaTime());

			if (entry.getValue() < 0) {

				entry.getKey().run();
				eventsToRemove.add(entry.getKey());

			}
		}

		for (int i = 0; i < eventsToRemove.size(); i++) {
			timedEvents.remove(eventsToRemove.get(i));
		}

		eventsToRemove.clear();
	}

	public void setEvent(float seconds, AbstractEvent event) {

		eventsToAdd.put(event, seconds);
	}

	public void setEvent(float seconds, AbstractEvent event, AbstractEvent firstEvent) {

		firstEvent.run();
		eventsToAdd.put(event, seconds);
	}

	public void removeEvent(AbstractEvent event) {

		timedEvents.remove(event);
	}

	// endregion methods
}
