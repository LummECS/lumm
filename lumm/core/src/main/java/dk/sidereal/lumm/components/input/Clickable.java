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

package dk.sidereal.lumm.components.input;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import dk.sidereal.lumm.architecture.Lumm;
import dk.sidereal.lumm.architecture.LummComponent;
import dk.sidereal.lumm.architecture.LummObject;
import dk.sidereal.lumm.architecture.concrete.ConcreteLummComponent;
import dk.sidereal.lumm.architecture.core.Input;
import dk.sidereal.lumm.architecture.core.Input.ActionType;
import dk.sidereal.lumm.architecture.core.input.ActionData;
import dk.sidereal.lumm.architecture.core.input.OnClickListener;
import dk.sidereal.lumm.architecture.core.input.TouchData;
import dk.sidereal.lumm.architecture.core.input.OnTouchListener;
import dk.sidereal.lumm.architecture.listeners.OnDisposeListener;

/**
 * Behavior used for creating events that are to be triggered when the user
 * clicks in a certain area. The area is saved in {@link #area}, which gets
 * updated each frame based on the location of the AbstractObject. The event to
 * run is {@link #eventOnHold}
 *
 * @see {@link Input}
 * @author Claudiu Bele
 */

// TODO handle multiple area types
public class Clickable extends ConcreteLummComponent {

    // region external

    private class ActionEventEntry {

        String inputProcessor;

        OnClickListener event;

        public ActionEventEntry(String inputProcessor, OnClickListener event) {
            this.inputProcessor = inputProcessor;
            this.event = event;
        }
    }

    private class TouchEventEntry {

        String inputProcessor;

        OnTouchListener event;

        public TouchEventEntry(String inputProcessor, OnTouchListener event) {
            this.inputProcessor = inputProcessor;
            this.event = event;
        }
    }

    // endregion external

    // region fields

    /**
     * The amount of time to wait so as to not trigger multiple events in a row
     */
    public float cooldown;

    /** The time remaining until we can record an event again */
    public float timeRemaining;

    /** Sprite used for debugging */
    public static Sprite debugSpriteSource;

    /**
     * Events that were added from an instance of Clickable to the Input, to
     * remove from Input in the dispose method
     */
    private List<ActionEventEntry> registeredActionEvents;

    /**
     * Events that were added from an instance of Clickable to the Input, to
     * remove from Input in the dispose method
     */
    private List<TouchEventEntry> registeredTouchEvents;

    /**
     * The area which if clicked, triggers an event. Set in
     * {@link #setAreaSize(float, float)} or {@link #setArea(Rectangle)}
     */
    private Rectangle area;

    /** Debugging sprite */
    private Sprite debugSprite;

    private float offsetX, offsetY;

    // endregion fields

    // region constructors

    public Clickable(LummObject obj) {

        super(obj);

        if (Lumm.debug.isEnabled())
            this.debugSprite = new Sprite(debugSpriteSource);
        this.cooldown = 1f;
        this.timeRemaining = 0f;

        registeredActionEvents = new ArrayList<Clickable.ActionEventEntry>();
        registeredTouchEvents = new ArrayList<Clickable.TouchEventEntry>();

        setDebugToggleKeys(Keys.CONTROL_LEFT, Keys.Z);

        onDisposeListener = new OnDisposeListener<LummComponent>() {

            @Override
            public void onDispose(LummComponent caller) {

                for (int i = 0; i < registeredActionEvents.size(); i++) {
                    Lumm.input.removeOnClickListener(registeredActionEvents.get(i).inputProcessor,
                            registeredActionEvents.get(i).event);
                }

                for (int i = 0; i < registeredTouchEvents.size(); i++) {
                    Lumm.input.removeOnTouchListener(registeredTouchEvents.get(i).inputProcessor,
                            registeredTouchEvents.get(i).event);
                }
            }
        };

    }

    @Override
    protected void initialiseClass() {

        if (!Lumm.debug.isEnabled())
            return;
        if (debugSpriteSource == null) {
            debugSpriteSource = new Sprite(
                    Lumm.assets.get(Lumm.assets.frameworkAssetsFolder + "White.png", Texture.class));
            debugSpriteSource.setColor(new Color(218 / 255f, 165 / 255f, 32 / 255f, 0.5f));
        }
    }

    // endregion constructors

    // region methods

    @Override
    public void onDebug() {

        if (isEnabled()) {
            debugSprite.setBounds(area.x, area.y, area.width, area.height);
            debugSprite.draw(object.getSceneLayer().spriteBatch);
        }
    }

    @Override
    public void onUpdate() {

        // adapt the area based on the position of the object
        area.x = object.position.getX() + offsetX;
        area.y = object.position.getY() + offsetY;

        timeRemaining -= Lumm.time.getDeltaTime();

    }

    public Rectangle getArea() {

        return area;
    }

    public void setAreaSize(float width, float height) {

        offsetX = -width / 2;
        offsetY = -height / 2;
        this.area = new Rectangle(object.position.getX() - width / 2, object.position.getY() - height / 2, width,
                height);

    }

    public void setAreaSize(float width, float height, float offsetX, float offsetY) {

        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.area = new Rectangle(object.position.getX() + offsetX, object.position.getY() + offsetY, width, height);

    }

    public void setArea(Rectangle area) {

        this.area = area;
    }

    /**
     * Adds an action event to {@link Lumm#input}. To read more on what each
     * parameter does, visit
     * {@link Input#addOnClickListener(String, int, OnClickListener, ActionType)}, as
     * that is the method that will be accessed from this one after some
     * tweaking. For adding events related to individual fingers other than the
     * first one, use
     * {@link #addTouchEvent(String, int, OnTouchListener, ActionType)}
     *
     * @param inputProcessorName
     * @param action
     * @param event
     * @param eventType
     * @param inside
     *            Whether to run the action when the action happens inside or
     *            outside of clickable area
     */
    public void addOnClickListener(String inputProcessorName, int action, final OnClickListener event, ActionType eventType,
                                   final boolean inside) {

        // make event that encapsulates the passed event
        OnClickListener clickableEvent = new OnClickListener() {

            @Override
            public boolean onClick(ActionData inputData) {
                // event will not run if mouse position is not within bounds.
                if (area.contains(object.getSceneLayer().mousePosition) == inside)
                    return event.onClick(inputData);
                else
                    return false;

            }
        };

        Lumm.input.addOnClickListener(inputProcessorName, action, clickableEvent, eventType);
        registeredActionEvents.add(new ActionEventEntry(inputProcessorName, clickableEvent));

    }

    public void addTouchEvent(String inputProcessorName, int action, final OnTouchListener touchEvent, ActionType eventType,
                              final boolean inside) {
        // make event that encapsulates the passed event
        OnTouchListener clickableEvent = new OnTouchListener() {

            @Override
            public boolean run(TouchData inputData) {
                // translate touch input to a mouse position
                Vector3 translatedTouchPosition = new Vector3(inputData.getPosition(), 0);
                object.getSceneLayer().camera.unproject(translatedTouchPosition);

                if (area.contains(translatedTouchPosition.x, translatedTouchPosition.y) == inside)
                    return touchEvent.run(inputData);
                else
                    return false;
            }
        };

        Lumm.input.addOnTouchListener(inputProcessorName, action, clickableEvent, eventType);
        registeredTouchEvents.add(new TouchEventEntry(inputProcessorName, clickableEvent));
    }


    // endregion methods
}
