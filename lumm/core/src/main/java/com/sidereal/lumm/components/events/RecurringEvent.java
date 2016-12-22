/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.sidereal.lumm.components.events;

import java.util.ArrayList;

import com.sidereal.lumm.architecture.AbstractEvent;
import com.sidereal.lumm.architecture.Lumm;
import com.sidereal.lumm.architecture.LummObject;
import com.sidereal.lumm.architecture.concrete.ConcreteLummComponent;

/**
 * Schedule {@link AbstractEvent} objects to run repeatedly over a number of
 * seconds or times.
 *
 * @author Claudiu Bele
 */
public class RecurringEvent extends ConcreteLummComponent {

    // region fields

    private ArrayList<AbstractEvent> events;

    protected float overallTimer = 0, frequencyTimer = 0;

    // endregion fields

    // region constructors

    public RecurringEvent(LummObject obj) {

        super(obj);
        events = new ArrayList<AbstractEvent>();
    }

    // endregion costructors

    // region methods

    @Override
    public void onUpdate() {

        for (int i = 0; i < events.size(); i++) {
            events.get(i).run();
        }
    }

    /**
     * Adds an event that will be ran at a certain frequency, until the end time
     * ends.
     *
     * @param event
     *            An event that will run at a certain frequency
     * @param frequency
     *            How often (in seconds) the action will happen
     * @param endTime
     *            The seconds it will last, if set to -1, it will be permanent.
     */
    public void setEvent(final AbstractEvent event, final float frequency, final float endTime,
                         final AbstractEvent endEvent) {

        events.add(new AbstractEvent() {

            private float freq = frequency, end = endTime, freqTimer = 0, endTimer = 0;

            private AbstractEvent eventToRun = event;

            @Override
            public void run(Object... params) {

                freqTimer += Lumm.time.getDeltaTime();
                endTimer += Lumm.time.getDeltaTime();

                // if end is -1, we want it to run all the time
                if (end != -1 && endTimer - end >= 0) {
                    if (endEvent != null) {
                        endEvent.run();
                    }
                    events.remove(events.indexOf(this));
                }
                if (freqTimer > freq) {
                    eventToRun.run();
                    freqTimer -= freq;
                }
            }
        });
    }

    public void setEvent(final AbstractEvent event, final float frequency, final int times,
                         final AbstractEvent endEvent) {

        events.add(new AbstractEvent() {

            private float freq = frequency, end = times * frequency + 0.05f, freqTimer = 0, endTimer = 0;

            private AbstractEvent eventToRun = event;

            @Override
            public void run(Object... params) {

                freqTimer += Lumm.time.getDeltaTime();
                endTimer += Lumm.time.getDeltaTime();

                // if end is -1, we want it to run all the time
                if (end != -1 && endTimer - end >= 0 && times != -1) {
                    if (endEvent != null) {
                        endEvent.run();
                    }
                    events.remove(events.indexOf(this));
                }
                if (freqTimer > freq) {
                    eventToRun.run();
                    freqTimer -= freq;
                }
            }
        });
    }

    public ArrayList<AbstractEvent> getEvents() {

        return events;
    }

    public void setEvents(ArrayList<AbstractEvent> events) {

        this.events = events;
    }

    // endregion methods

}
