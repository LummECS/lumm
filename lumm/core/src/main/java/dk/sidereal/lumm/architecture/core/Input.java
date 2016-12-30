/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ******************************************************************************/

package dk.sidereal.lumm.architecture.core;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import dk.sidereal.lumm.architecture.AbstractEvent;
import dk.sidereal.lumm.architecture.Lumm;
import dk.sidereal.lumm.architecture.LummConfiguration;
import dk.sidereal.lumm.architecture.LummModule;
import dk.sidereal.lumm.architecture.core.input.ActionData;
import dk.sidereal.lumm.architecture.core.input.OnClickListener;
import dk.sidereal.lumm.architecture.core.input.ActionEventWrapper;
import dk.sidereal.lumm.architecture.core.input.OnKeyTypedListener;
import dk.sidereal.lumm.architecture.core.input.OnScrollListener;
import dk.sidereal.lumm.architecture.core.input.TouchData;
import dk.sidereal.lumm.architecture.core.input.OnTouchListener;
import dk.sidereal.lumm.architecture.core.input.TouchEventWrapper;
import dk.sidereal.lumm.util.LummException;
import dk.sidereal.lumm.architecture.LummScene;

/**
 * Configurable {@link LummModule} that is added to the entity system by
 * default. Use {@link Lumm#input} to access it.
 * <p>
 * The values that the Module uses from {@link LummConfiguration} are only the
 * strings found in the {@link LummConfiguration#inputProcessorNames} array,
 * which if not set or if the configuration is not passed, the value
 * {@link #DEFAULT_INPUT_PROCESSOR} will be used. All of the processor names
 * found in the class can be retrieved using {@link #getInputProcessorNames()}.
 *
 * The whole system is based around the use of the values inside the
 * {@link Action} class. Every single key designates either a keyboard key, a
 * mouse button, a finger pressed against a screen that can handle touch, or
 * special cases. Currently, the only special case is {@link Action#ANY_ACTION},
 * where all buttons, keys and fingers are handled.
 * <p>
 * The methods to use for adding events to a processor are the following :
 * {@link #addOnClickListener(String, int, OnClickListener, ActionType)},
 * {@link #addOnKeyTypedListener(String, OnKeyTypedListener)},
 * {@link #addOnScrollListener(String, OnScrollListener)} and
 * {@link #addOnTouchListener(String, int, OnTouchListener, ActionType)}.
 *
 * @author Claudiu Bele
 */
public class Input extends LummModule {

    public static String DEFAULT_INPUT_PROCESSOR = "Default";

    // region external

    /**
     * Represents the key code for individual buttons, keys, fingers and special
     * cases (such as {@link #ANY_ACTION}.
     *
     * @author Claudiu Bele
     */
    public static class Action {

        /** Is used whenever all other actions are used */
        public static final int ANY_ACTION = -1;

        public static final int NO_ACTION = -2;

        public static final int FINGER_1 = -20;

        public static final int FINGER_2 = -19;

        public static final int FINGER_3 = -18;

        public static final int FINGER_4 = -17;

        public static final int FINGER_5 = -16;

        public static final int FINGER_6 = -15;

        public static final int FINGER_7 = -14;

        public static final int FINGER_8 = -13;

        public static final int FINGER_9 = -12;

        public static final int FINGER_10 = -11;

        public static final int MOUSE_LEFT = -10;

        public static final int MOUSE_RIGHT = -9;

        public static final int MOUSE_MIDDLE = -8;

        public static final int MOUSE_BACK = -7;

        public static final int MOUSE_FORWARD = -6;

        public static final int KEY_NUM_0 = 7;

        public static final int KEY_NUM_1 = 8;

        public static final int KEY_NUM_2 = 9;

        public static final int KEY_NUM_3 = 10;

        public static final int KEY_NUM_4 = 11;

        public static final int KEY_NUM_5 = 12;

        public static final int KEY_NUM_6 = 13;

        public static final int KEY_NUM_7 = 14;

        public static final int KEY_NUM_8 = 15;

        public static final int KEY_NUM_9 = 16;

        public static final int KEY_A = 29;

        public static final int KEY_ALT_LEFT = 57;

        public static final int KEY_ALT_RIGHT = 58;

        public static final int KEY_APOSTROPHE = 75;

        public static final int KEY_AT = 77;

        public static final int KEY_B = 30;

        public static final int KEY_BACK = 4;

        public static final int KEY_BACKSLASH = 73;

        public static final int KEY_C = 31;

        public static final int KEY_CALL = 5;

        public static final int KEY_CAMERA = 27;

        public static final int KEY_CLEAR = 28;

        public static final int KEY_COMMA = 55;

        public static final int KEY_D = 32;

        public static final int KEY_DEL = 67;

        public static final int KEY_BACKSPACE = 67;

        public static final int KEY_FORWARD_DEL = 112;

        public static final int KEY_DPAD_CENTER = 23;

        public static final int KEY_DPAD_DOWN = 20;

        public static final int KEY_DPAD_LEFT = 21;

        public static final int KEY_DPAD_RIGHT = 22;

        public static final int KEY_DPAD_UP = 19;

        public static final int KEY_CENTER = 23;

        public static final int KEY_DOWN = 20;

        public static final int KEY_LEFT = 21;

        public static final int KEY_RIGHT = 22;

        public static final int KEY_UP = 19;

        public static final int KEY_E = 33;

        public static final int KEY_ENDCALL = 6;

        public static final int KEY_ENTER = 66;

        public static final int KEY_ENVELOPE = 65;

        public static final int KEY_EQUALS = 70;

        public static final int KEY_EXPLORER = 64;

        public static final int KEY_F = 34;

        public static final int KEY_FOCUS = 80;

        public static final int KEY_G = 35;

        public static final int KEY_GRAVE = 68;

        public static final int KEY_H = 36;

        public static final int KEY_HEADSETHOOK = 79;

        public static final int KEY_HOME = 3;

        public static final int KEY_I = 37;

        public static final int KEY_J = 38;

        public static final int KEY_K = 39;

        public static final int KEY_L = 40;

        public static final int KEY_LEFT_BRACKET = 71;

        public static final int KEY_M = 41;

        public static final int KEY_MEDIA_FAST_FORWARD = 90;

        public static final int KEY_MEDIA_NEXT = 87;

        public static final int KEY_MEDIA_PLAY_PAUSE = 85;

        public static final int KEY_MEDIA_PREVIOUS = 88;

        public static final int KEY_MEDIA_REWIND = 89;

        public static final int KEY_MEDIA_STOP = 86;

        public static final int KEY_MENU = 82;

        public static final int KEY_MINUS = 69;

        public static final int KEY_MUTE = 91;

        public static final int KEY_N = 42;

        public static final int KEY_NOTIFICATION = 83;

        public static final int KEY_NUM = 78;

        public static final int KEY_O = 43;

        public static final int KEY_P = 44;

        public static final int KEY_PERIOD = 56;

        public static final int KEY_PLUS = 81;

        public static final int KEY_POUND = 18;

        public static final int KEY_POWER = 26;

        public static final int KEY_Q = 45;

        public static final int KEY_R = 46;

        public static final int KEY_RIGHT_BRACKET = 72;

        public static final int KEY_S = 47;

        public static final int KEY_SEARCH = 84;

        public static final int KEY_SEMICOLON = 74;

        public static final int KEY_SHIFT_LEFT = 59;

        public static final int KEY_SHIFT_RIGHT = 60;

        public static final int KEY_SLASH = 76;

        public static final int KEY_SOFT_LEFT = 1;

        public static final int KEY_SOFT_RIGHT = 2;

        public static final int KEY_SPACE = 62;

        public static final int KEY_STAR = 17;

        public static final int KEY_SYM = 63;

        public static final int KEY_T = 48;

        public static final int KEY_TAB = 61;

        public static final int KEY_U = 49;

        public static final int KEY_UNKNOWN = 0;

        public static final int KEY_V = 50;

        public static final int KEY_VOLUME_DOWN = 25;

        public static final int KEY_VOLUME_UP = 24;

        public static final int KEY_W = 51;

        public static final int KEY_X = 52;

        public static final int KEY_Y = 53;

        public static final int KEY_Z = 54;

        public static final int KEY_META_ALT_LEFT_ON = 16;

        public static final int KEY_META_ALT_ON = 2;

        public static final int KEY_META_ALT_RIGHT_ON = 32;

        public static final int KEY_META_SHIFT_LEFT_ON = 64;

        public static final int KEY_META_SHIFT_ON = 1;

        public static final int KEY_META_SHIFT_RIGHT_ON = 128;

        public static final int KEY_META_SYM_ON = 4;

        public static final int KEY_CONTROL_LEFT = 129;

        public static final int KEY_CONTROL_RIGHT = 130;

        public static final int KEY_ESCAPE = 131;

        public static final int KEY_END = 132;

        public static final int KEY_INSERT = 133;

        public static final int KEY_PAGE_UP = 92;

        public static final int KEY_PAGE_DOWN = 93;

        public static final int KEY_PICTSYMBOLS = 94;

        public static final int KEY_SWITCH_CHARSET = 95;

        public static final int KEY_BUTTON_CIRCLE = 255;

        public static final int KEY_BUTTON_A = 96;

        public static final int KEY_BUTTON_B = 97;

        public static final int KEY_BUTTON_C = 98;

        public static final int KEY_BUTTON_X = 99;

        public static final int KEY_BUTTON_Y = 100;

        public static final int KEY_BUTTON_Z = 101;

        public static final int KEY_BUTTON_L1 = 102;

        public static final int KEY_BUTTON_R1 = 103;

        public static final int KEY_BUTTON_L2 = 104;

        public static final int KEY_BUTTON_R2 = 105;

        public static final int KEY_BUTTON_THUMBL = 106;

        public static final int KEY_BUTTON_THUMBR = 107;

        public static final int KEY_BUTTON_START = 108;

        public static final int KEY_BUTTON_SELECT = 109;

        public static final int KEY_BUTTON_MODE = 110;

        public static final int KEY_NUMPAD_0 = 144;

        public static final int KEY_NUMPAD_1 = 145;

        public static final int KEY_NUMPAD_2 = 146;

        public static final int KEY_NUMPAD_3 = 147;

        public static final int KEY_NUMPAD_4 = 148;

        public static final int KEY_NUMPAD_5 = 149;

        public static final int KEY_NUMPAD_6 = 150;

        public static final int KEY_NUMPAD_7 = 151;

        public static final int KEY_NUMPAD_8 = 152;

        public static final int KEY_NUMPAD_9 = 153;

        public static final int KEY_COLON = 243;

        public static final int KEY_F1 = 244;

        public static final int KEY_F2 = 245;

        public static final int KEY_F3 = 246;

        public static final int KEY_F4 = 247;

        public static final int KEY_F5 = 248;

        public static final int KEY_F6 = 249;

        public static final int KEY_F7 = 250;

        public static final int KEY_F8 = 251;

        public static final int KEY_F9 = 252;

        public static final int KEY_F10 = 253;

        public static final int KEY_F11 = 254;

        public static final int KEY_F12 = 255;

        public static String toString(int code) {

            String toReturn = "Unknown";
            if (code >= -20 && code <= -11)
                switch (code) {
                    case Action.FINGER_1:
                        return "Finger 1";
                    case Action.FINGER_2:
                        return "Finger 2";
                    case Action.FINGER_3:
                        return "Finger 3";
                    case Action.FINGER_4:
                        return "Finger 4";
                    case Action.FINGER_5:
                        return "Finger 5";
                    case Action.FINGER_6:
                        return "Finger 6";
                    case Action.FINGER_7:
                        return "Finger 7";
                    case Action.FINGER_8:
                        return "Finger 8";
                    case Action.FINGER_9:
                        return "Finger 9";
                    case Action.FINGER_10:
                        return "Finger 10";
                }
            else if (code >= -10 && code <= -6)
                switch (code) {
                    case Action.MOUSE_LEFT:
                        toReturn = "Left Mouse button";
                        break;
                    case Action.MOUSE_RIGHT:
                        toReturn = "Right Mouse button";
                        break;
                    case Action.MOUSE_MIDDLE:
                        toReturn = "Middle Mouse button";
                        break;
                    case Action.MOUSE_BACK:
                        toReturn = "Back Mouse button";
                        break;
                    case Action.MOUSE_FORWARD:
                        toReturn = "Forward Mouse button";
                        break;

                }
            else if (code >= 0 && code <= 255)
                toReturn = com.badlogic.gdx.Input.Keys.toString(code);
            else
                toReturn = "Unknown";

            if (toReturn == null)
                toReturn = "Unknown";

            if (code == Action.ANY_ACTION)
                toReturn = "Any action";
            if (code == Action.NO_ACTION)
                toReturn = "No action";

            return toReturn;
        }
    }

    /**
     * Represents the status of the interraction with a type of input during the
     * last frame.
     *
     * @author Claudiu Bele
     */
    public static enum InputStatus {
        /** There is no interaction with the input type */
        None,

        /**
         * The input has just been pressed. Set in
         * {@link InputProcessor#touchDown(int, int, int, int)},
         * {@link InputProcessor#keyDown(int)} if the current status of any
         * input is {@link #None}
         */
        Down,

        /**
         * The input is being held down. Set in
         * {@link InputProcessor#touchDown(int, int, int, int)},
         * {@link InputProcessor#keyDown(int)} if the current status of any
         * input is {@link #Down}
         */
        Hold,

        /**
         * The input is being released. Set in
         * {@link InputProcessor#touchUp(int, int, int, int)} and
         * {@link InputProcessor#keyUp(int)}.
         */
        Up
    }

    /**
     * The type that listeners can have. Used when adding/removing listeners in
     * {@link Input#addListener(ActionType, AbstractEvent)} and
     * {@link Input#removeListener(ActionType, AbstractEvent)}.
     * <p>
     * {@link Input#listeners} has values of type {@link ActionType} as keys for
     * lists of events to run based on certain {@link InputProcessor} events
     * such as touch down ( {@link InputProcessor#touchDown(int, int, int, int)}
     * ).
     *
     * @author Claudiu
     */
    public static enum ActionType {
        /**
         * The events of this type are called whenever a button or finger or key
         * is pressed, from {@link InputProcessor#touchDown(int, int, int, int)}
         * .
         */
        Down,

        /**
         * The events of this type are called whenever a button or finger or key
         * is pressed, from {@link InputProcessor#touchDown(int, int, int, int)}
         * .
         */
        Hold,

        /**
         * The events of this type are called whenever a button or finger or key
         * is released, from
         * {@link InputProcessor#touchDown(int, int, int, int)}.
         */
        Up,

    }

    // endregion external

    // region fields
    // TODO LIST OF NEW object type
    /**
     * Map containing Input processors that can be retrieved by name. An input
     * processor is added for each entry in
     * {@link LummConfiguration#inputProcessorNames}, in {@link #onCreate()}.
     * <p>
     * Input processors can be retrieved using
     * {@link #getInputProcessor(String)}, whereas the parameter to pass can be
     * selected from the array returned from {@link #getInputProcessorNames()}.
     */
    private final ObjectMap<String, InputProcessor> inputProcessors;

    /**
     * Events to run when a particular key / button / finger is pressed. The
     * actual events are found in the {@link ActionEventWrapper#event} value
     * from the {@link ActionEventWrapper} wrapper created in
     * {@link #addOnClickListener(String, int, OnClickListener, ActionType)}
     */
    private final ObjectMap<InputProcessor, ArrayList<ActionEventWrapper>> actionEvents;

    /**
     * Events to run when a particular finger is pressed. "sorted" by
     * InputProcessor
     */
    private final ObjectMap<InputProcessor, ArrayList<TouchEventWrapper>> touchEvents;

    /**
     * Events to run when the mouse wheel is scrolled, "sorted" by
     * InputProcessor
     */
    private final ObjectMap<InputProcessor, ArrayList<OnScrollListener>> scrollEvents;

    /** Events to run when a key is typed, "sorted" by InputProcessor */
    private final ObjectMap<InputProcessor, ArrayList<OnKeyTypedListener>> keyTypedEvents;

    /**
     * Holds all of the InputProcessors generated by name using
     * {@link LummConfiguration#inputProcessorNames} in {@link #onCreate()}. The
     * order in which InputProcessor instances are added is the same order as
     * the values in the array.
     */
    private final InputMultiplexer inputMultiplexer;

    /**
     * Stores data for individual actions. If an {@link InputData} action does
     * not exist but is currently handled in one of the methods of the
     * inputProcessor, a new instance will be created for the given action code.
     * When calling {@link #getInputData(int)}, if the input data can't be found
     * with the given actioncode, a new one will be added.
     */
    private final ObjectMap<Integer, ActionData> inputData;

    /**
     * Stores data for individual touchActions. If an {@link TouchInputData}
     * action does not exist but is currently handled in one of the methods of
     * the inputProcessor, a new instance will be created for the given action
     * code. When calling {@link #getTouchInputData(int)}, if the input data
     * can't be found with the given touchActioncode, a new one will be added.
     */
    private final ObjectMap<Integer, TouchData> touchInputData;

    private String[] processorNames;

    // endregion fields

    // region constructors

    /**
     * Constructor for input, does not have any connection to the LibGDX backend
     * yet. Is called in
     * {@link Lumm#Lumm(LummScene, LummConfiguration)}
     * .
     */
    public Input(LummConfiguration cfg) {
        // TODO dude cmon
        super(cfg);
        setUpdateFrequency(0);

        inputData = new ObjectMap<Integer, ActionData>();
        touchInputData = new ObjectMap<Integer, TouchData>();

        inputMultiplexer = new InputMultiplexer();
        inputProcessors = new ObjectMap<String, InputProcessor>();

        actionEvents = new ObjectMap<InputProcessor, ArrayList<ActionEventWrapper>>();
        touchEvents = new ObjectMap<InputProcessor, ArrayList<TouchEventWrapper>>();
        scrollEvents = new ObjectMap<InputProcessor, ArrayList<OnScrollListener>>();
        keyTypedEvents = new ObjectMap<InputProcessor, ArrayList<OnKeyTypedListener>>();

        if (cfg.inputProcessorNames == null || cfg.inputProcessorNames.length == 0)
            this.processorNames = new String[]{DEFAULT_INPUT_PROCESSOR};
        else
            this.processorNames = cfg.inputProcessorNames;
    }

    /**
     * Method for creating the Core Module, is called after the LibGDX backend
     * has been created, thus LibGDX constructs can be called in this method
     * without errors. Is called in {@link Lumm#create()}.
     */
    @Override
    public void onCreate() {

        for (int i = 0; i < this.processorNames.length; i++) {

            // create new input processor
            // TODO input processor with name in same object
            final InputProcessor inputProcessor = new InputProcessor() {

                @Override
                public boolean touchUp(int screenX, int screenY, int pointer, int button) {

                    try {

                        // update internal input data
                        // update button data
                        getInputData(button - 10).update(InputStatus.Up);

                        // update touch data
                        getTouchInputData(pointer - 20).update(InputStatus.Up, screenX, screenY);

                        // run listener events

                        boolean handled = false;
                        // go over all action events and run the ones on up if
                        // applicable
                        for (int i = 0; i < actionEvents.get(this).size(); i++) {
                            ActionEventWrapper currEvent = actionEvents.get(this).get(i);
                            // curr event runs when releasing
                            if (currEvent.inputType.equals(ActionType.Up)) {

                                if (currEvent.code == pointer - 20 ||
                                        // if the event runs for a particular touch
                                        currEvent.code == button - 10 ||
                                        // or the event runs on a particular button
                                        currEvent.code == Keys.ANY_KEY)
                                // or the event runs on every key
                                {
                                    handled = handled || currEvent.event.onClick(getInputData(button - 10));
                                }
                            }
                        }

                        // go over all touch event and run the ones on up
                        for (int i = 0; i < touchEvents.get(this).size(); i++) {
                            TouchEventWrapper currEvent = touchEvents.get(this).get(i);
                            // curr event runs when releasing
                            if (currEvent.inputType.equals(ActionType.Up)) {
                                if (currEvent.code == pointer - 20 ||
                                        // if the event runs for a particular touch
                                        currEvent.code == Action.ANY_ACTION)
                                // or the event runs on every key
                                {
                                    handled = handled || currEvent.event.run(getTouchInputData(pointer - 20));
                                }
                            }
                        }

                        return handled;
                    } catch (Exception e) {

                        Lumm.handleException(e);
                        return false;
                    }
                }

                @Override
                public boolean touchDragged(int screenX, int screenY, int pointer) {

                    try {

                        getTouchInputData(pointer - 20).update(InputStatus.Hold, screenX, screenY);

                        boolean handled = false;
                        // go over all touch event and run the ones on up

                        for (int i = 0; i < touchEvents.get(this).size(); i++) {

                            TouchEventWrapper currEvent = touchEvents.get(this).get(i);
                            // curr event runs when releasing
                            if ((currEvent.inputType.equals(ActionType.Down)
                                    && getTouchInputData(pointer - 20).getInputStatus() == InputStatus.Down)
                                    || (currEvent.inputType.equals(ActionType.Hold)
                                    && getTouchInputData(pointer - 20).getInputStatus() == InputStatus.Hold)) {
                                if (currEvent.code == pointer - 20 ||
                                        // if an event runs for a particular touch
                                        currEvent.code == Action.ANY_ACTION)
                                // if an event runs for all touches
                                {
                                    handled = handled || currEvent.event.run(getTouchInputData(pointer - 20));
                                }
                            }
                        }

                        return true;

                    } catch (Exception e) {

                        Lumm.handleException(e);
                        return false;
                    }
                }

                @Override
                public boolean touchDown(int screenX, int screenY, int pointer, int button) {

                    try {

                        // update button data
                        if (getInputData(button - 10).getInputStatus().equals(InputStatus.None))
                            getInputData(button - 10).update(InputStatus.Down);

                        if (getInputData(pointer - 20).getInputStatus().equals(InputStatus.None))
                            getInputData(pointer - 20).update(InputStatus.Down);

                        // update pointer data
                        InputStatus targetInputStatus = getTouchInputData(pointer - 20).getInputStatus();
                        if (getTouchInputData(pointer - 20).getInputStatus().equals(InputStatus.None))
                            targetInputStatus = InputStatus.Down;
                        getTouchInputData(pointer - 20).update(targetInputStatus, screenX, screenY);

                        boolean handled = false;
                        // update internal input data
                        // ...

                        // run listener events
                        // go over all action events and run the ones on up if
                        // applicable
                        for (int i = 0; i < actionEvents.get(this).size(); i++) {
                            ActionEventWrapper currEvent = actionEvents.get(this).get(i);
                            // curr event runs when releasing
                            if ((currEvent.inputType.equals(ActionType.Down)
                                    && getInputData(button - 10).getInputStatus() == InputStatus.Down)
                                    || (currEvent.inputType.equals(ActionType.Hold)
                                    && getInputData(button - 10).getInputStatus() == InputStatus.Hold)) {

                                if (currEvent.code == pointer - 20 ||
                                        // if the event runs for a particular touch
                                        currEvent.code == button - 10 ||
                                        // or the event runs for a particular button
                                        currEvent.code == Action.ANY_ACTION)
                                // or any key is to be handled
                                {
                                    handled = handled || currEvent.event.onClick(getInputData(button - 10));
                                }
                            }
                        }

                        // go over all touch event and run the ones on up
                        for (int i = 0; i < touchEvents.get(this).size(); i++) {

                            TouchEventWrapper currEvent = touchEvents.get(this).get(i);
                            // curr event runs when releasing

                            if ((currEvent.inputType.equals(ActionType.Down)
                                    && getTouchInputData(pointer - 20).getInputStatus() == InputStatus.Down)
                                    || (currEvent.inputType.equals(ActionType.Hold)
                                    && getTouchInputData(pointer - 20).getInputStatus() == InputStatus.Hold)) {
                                if (currEvent.code == pointer - 20 ||
                                        // if the event runs for a particular touch
                                        currEvent.code == Action.ANY_ACTION)
                                // or any key is to be handled

                                {
                                    try {
                                        handled = handled || currEvent.event.run(getTouchInputData(pointer - 20));
                                    } catch (Exception e) {
                                        Lumm.net.logThrowable(e);
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                        return handled;

                    } catch (Exception e) {

                        Lumm.handleException(e);
                        return false;
                    }
                }

                @Override
                public boolean scrolled(int amount) {

                    try {

                        // run listener events
                        boolean handled = false;
                        for (int i = 0; i < scrollEvents.get(this).size(); i++) {
                            handled = handled || scrollEvents.get(this).get(i).run(amount);
                        }
                        return handled;

                    } catch (Exception e) {

                        Lumm.handleException(e);
                        return false;
                    }
                }

                // ignore, can use Gdx.input for mouse position
                @Override
                public boolean mouseMoved(int screenX, int screenY) {

                    return false;
                }

                @Override
                public boolean keyUp(int keycode) {

                    try {

                        // update key press data
                        getInputData(keycode).update(InputStatus.Up);

                        // run listener events
                        boolean handled = false;
                        // go over all action events and run the ones on up if
                        // applicable

                        for (int i = 0; i < actionEvents.get(this).size(); i++) {
                            ActionEventWrapper currEvent = actionEvents.get(this).get(i);
                            // curr event runs when releasing
                            if (currEvent.inputType.equals(ActionType.Up)) {

                                if (currEvent.code == keycode ||
                                        // or the event runs on a particular button
                                        currEvent.code == Action.ANY_ACTION)
                                // or the event runs on every key
                                {
                                    try {
                                        handled = handled || currEvent.event.onClick(getInputData(keycode));
                                    } catch (Exception e) {
                                        Lumm.net.logThrowable(e);
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        return handled;

                    } catch (Exception e) {

                        Lumm.handleException(e);
                        return false;
                    }
                }

                @Override
                public boolean keyTyped(char character) {

                    try {
                        // run listener events
                        boolean handled = false;
                        for (int i = 0; i < keyTypedEvents.get(this).size(); i++) {
                            try {
                                handled = handled || keyTypedEvents.get(this).get(i).run(character);
                            } catch (Exception e) {
                                Lumm.net.logThrowable(e);
                                e.printStackTrace();
                            }
                        }
                        return handled;

                    } catch (Exception e) {

                        Lumm.handleException(e);
                        return false;
                    }
                }

                @Override
                public boolean keyDown(int keycode) {

                    try {
                        // update key press data
                        if (getInputData(keycode).getInputStatus().equals(InputStatus.None))
                            getInputData(keycode).update(InputStatus.Down);

                        // run listener events
                        boolean handled = false;

                        // go over all action events and run the ones on up if
                        // applicable
                        for (int i = 0; i < actionEvents.get(this).size(); i++) {
                            ActionEventWrapper currEvent = actionEvents.get(this).get(i);
                            // curr event runs when releasing
                            if ((currEvent.inputType.equals(ActionType.Down)
                                    && getInputData(keycode).getInputStatus() == InputStatus.Down)
                                    || (currEvent.inputType.equals(ActionType.Hold)
                                    && getInputData(keycode).getInputStatus() == InputStatus.Hold)) {

                                if (currEvent.code == keycode ||
                                        // the event runs for a particular key
                                        currEvent.code == Action.ANY_ACTION)
                                // or any key is to be handled
                                {
                                    try {
                                        handled = handled || currEvent.event.onClick(getInputData(keycode));
                                    } catch (Exception e) {
                                        Lumm.net.logThrowable(e);
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        return handled;

                    } catch (Exception e) {

                        Lumm.handleException(e);
                        return false;
                    }
                }
            };
            inputProcessors.put(processorNames[i], inputProcessor);
            actionEvents.put(inputProcessor, new ArrayList<ActionEventWrapper>());
            touchEvents.put(inputProcessor, new ArrayList<TouchEventWrapper>());
            scrollEvents.put(inputProcessor, new ArrayList<OnScrollListener>());
            keyTypedEvents.put(inputProcessor, new ArrayList<OnKeyTypedListener>());
            inputMultiplexer.addProcessor(inputProcessor);

        }
        // set LibGDX input processor to the one that we are creating
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    // endregion constructors

    // region methods

    /**
     * Updates all of the {@link InputData} and {@link TouchData} instances. If
     * any key/button/finger's status is down, it will become hold. If it is Up,
     * it will become None. if it is hold, it will run keyDown or touchDragged
     * based on the type of action. Is called in {@link Lumm#render()}.
     */
    @Override
    public void onUpdate() {

        // go over all keys, updating their status if necessary
        for (Entry<Integer, ActionData> entry : inputData.entries()) {
            if (entry.value.inputStatus == InputStatus.Down)
                entry.value.inputStatus = InputStatus.Hold;

            if (entry.value.inputStatus == InputStatus.Up) {
                entry.value.inputStatus = InputStatus.None;
            }

            if (entry.value.inputStatus == InputStatus.Hold)
                inputMultiplexer.keyDown(entry.value.code);

        }

        // go over all touch data, updating their status if necessary
        for (Entry<Integer, TouchData> entry : touchInputData.entries()) {
            if (entry.value.inputStatus == InputStatus.Down)
                entry.value.inputStatus = InputStatus.Hold;

            if (entry.value.inputStatus == InputStatus.Up) {
                entry.value.inputStatus = InputStatus.None;
            }

            if (entry.value.inputStatus == InputStatus.Hold) {
                inputMultiplexer.touchDragged((int) entry.value.getPosition().x, (int) entry.value.getPosition().y,
                        entry.value.code + 20);
            }

        }

    }

    /**
     * Returns input data for the requested code. This is for querying action
     * data without having to create events (if it is desired). codes for
     * fingers can be retrieved from {@link Action}.
     *
     * @throws LummException
     *             if <code>actioncode</code> is not linked to any action.
     *
     * @param actionCode
     *            The code for which to get the InputData. Must be the value of
     *            one of the fields in {@link Action} .
     * @return
     */
    public ActionData getInputData(int actionCode) {

        if (Action.toString(actionCode).equals("Unknown") && actionCode < 0 || actionCode > 255)
            throw new LummException("Input requested for an actionCode that is not an action (" + actionCode + ")");

        if (inputData.get(actionCode) == null) {
            ActionData newData = new ActionData(actionCode);
            inputData.put(actionCode, newData);
        }
        return inputData.get(actionCode);

    }

    /**
     * Returns a touch input data for the requested code. This is for querying
     * action data without having to create events (if it is desired). codes for
     * fingers can be retrieved from {@link Action}.
     *
     * @throws LummException
     *             if <code>actioncode</code> is not linked to a finger.
     *
     * @param actionCode
     *            The code for which to get the TouchInputData. Must be between
     *            {@link Action#FINGER_1} and {@link Action#FINGER_10}
     * @return
     */
    public TouchData getTouchInputData(int touchActionCode) {

        // if(touchActionCode == InputAction.MOUSE_LEFT)
        // touchActionCode = InputAction.FINGER_1;

        if (Action.toString(touchActionCode).contains("finger"))
            throw new LummException("Touch input requested for a touchActionCode that is not a finger");

        if (touchInputData.get(touchActionCode) == null) {
            TouchData newData = new TouchData(touchActionCode);
            touchInputData.put(touchActionCode, newData);
        }
        return touchInputData.get(touchActionCode);
    }

    /**
     * Adds an event to run when a key is typed. The only data that the user can
     * process is the char value for the key value passed in
     * {@link OnKeyTypedListener}.
     *
     *
     * @throws NullPointerException
     *             if no processor is found by the requested name, or the event
     *             passed is null.
     *
     *
     * @param inputProcessorName
     *            the name of the input processor to assign the event to. The
     *            input processor can be taken by name using
     *            {@link #getInputProcessorNames()}. The available names are
     *            from the array set in
     *            {@link LummConfiguration#inputProcessorNames}, or if it has
     *            not been set, only {@link #DEFAULT_INPUT_PROCESSOR} can be
     *            found. If null value is provided, the first entry in the array
     *            is entered. The only moment in which an error is thrown
     *            regarding the input processor is if the name can't be found in
     *            the map of input processors.
     *
     *
     * @param event
     *            the event to run when a key is typed.
     */
    public void addOnKeyTypedListener(String inputProcessorName, OnKeyTypedListener listener) {

        if (listener == null)
            throw new NullPointerException("Input.addKeyTypedEvent parameter event of type KeyTypedEvent is null");
        if (inputProcessors.get(inputProcessorName) == null)
            throw new NullPointerException("Unable to find  input processor with the given name " + inputProcessorName);

        keyTypedEvents.get(inputProcessors.get(inputProcessorName)).add(listener);
    }

    /**
     * Adds an event to run when touching the screen with a finger. For choosing
     * a finger to interract with, use the {@link Action} entries, from
     * {@link Action#FINGER_1} to {@link Action#FINGER_10}.
     * <p>
     * The order in which fingers are being pressed is as follows: The first
     * finger is pressed, {@link Action#FINGER_1} is registered. The 2nd finger
     * is pressed, {@link Action#FINGER_2} is registered. When the 1st finger is
     * released, the 2nd finger remains on {@link Action#FINGER_2}, and the next
     * finger that is going to be pressed will be registered at
     * {@link Action#FINGER_1} again, with the 2nd next finger registered at
     * {@link Action#FINGER_3}.
     *
     *
     * @throws LummException
     *             if the <code>finger</code> integer variable is not within
     *             {@link Action#FINGER_1} and {@link Action#FINGER_10}.
     *             <p>
     *
     * @throws NullPointerException
     *             if the <code>event</code> or <code>type</code> parameters are
     *             null, or there is no processor found by the requested name.
     *
     * @see Action InputAction - for finger codes, all of them start with
     *      "Finger_".
     *      <p>
     * @see #addOnClickListener(String, int, OnClickListener, ActionType)
     *      AddActionEvent - for adding a general action, instead of a
     *      touch-only action
     *
     * @param inputProcessorName
     *            the name of the input processor to assign the event to. The
     *            input processor can be taken by name using
     *            {@link #getInputProcessorNames()}. The available names are
     *            from the array set in
     *            {@link LummConfiguration#inputProcessorNames}, or if it has
     *            not been set, only {@link #DEFAULT_INPUT_PROCESSOR} can be
     *            found. If null value is provided, the first entry in the array
     *            is entered. The only moment in which an error is thrown
     *            regarding the input processor is if the name can't be found in
     *            the map of input processors.
     *            <p>
     * @param fingerCode
     *            the code assigned to a finger, to be taken from {@link Action}
     *            , 10 fingers being supported currently, between
     *            {@link Action#FINGER_1} and {@link Action#FINGER_10}.
     *            <p>
     * @param event
     *            the event to run when something happens to the finger.
     *            <p>
     * @param type
     *            The type of action to run the event on. The available options
     *            are {@link ActionType#Hold} for holding the finger down,
     *            {@link ActionType#Down} for the frame in which the finger was
     *            pressed, and {@link ActionType#Up} for the frame in which the
     *            finger is released.
     */
    public void addOnTouchListener(String inputProcessorName, int fingerCode, OnTouchListener onTouchListener,
                                   ActionType type) {

        if (onTouchListener == null)
            throw new NullPointerException("Input.addTouchEvent parameter event of type TouchEvent is null");
        if (type == null)
            throw new NullPointerException("Input.addTouchEvent parameter type of type InputEventType is null");
        if (inputProcessors.get(inputProcessorName) == null)
            throw new NullPointerException("Unable to find  input processor with the given name " + inputProcessorName);

        if ((fingerCode > -11 || fingerCode < -20) && fingerCode != Action.ANY_ACTION)
            throw new LummException(
                    "Input.addTouchEvent parameter finger of type int has to have values within the InputAction.Finger_1 and InputAction.Finger_10 ( -20 and -11");

        TouchEventWrapper wrapper = new TouchEventWrapper(fingerCode, onTouchListener, type);
        touchEvents.get(inputProcessors.get(inputProcessorName)).add(wrapper);
    }

    /**
     * Adds an event to run when a specific action is being accessed. For
     * choosing an action to interract for, use any of the values inside the
     * {@link Action} class, or {@link Action#ANY_ACTION} for listening to any
     * particular action.
     *
     * @throws LummException
     *             if the <code>code</code> integer variable is not a value
     *             found in {@link Action}.
     *
     * @throws NullPointerException
     *             if the <code>event</code> or <code>type</code> parameters are
     *             null, or there is no processor found by the requested name.
     *
     *
     * @param inputProcessorName
     *            the name of the input processor to assign the event to. The
     *            input processor can be taken by name using
     *            {@link #getInputProcessorNames()}. The available names are
     *            from the array set in
     *            {@link LummConfiguration#inputProcessorNames}, or if it has
     *            not been set, only {@link #DEFAULT_INPUT_PROCESSOR} can be
     *            found. If null value is provided, the first entry in the array
     *            is entered. The only moment in which an error is thrown
     *            regarding the input processor is if the name can't be found in
     *            the map of input processors.
     *
     * @param actionCode
     *            the code assigned to an action(key/button/finger), to be taken
     *            from {@link Action}. Use a value from {@link Action} for a
     *            specific value ( like {@link Action#MOUSE_LEFT} ) or
     *            {@link Action#ANY_ACTION} to listen to all of them).
     *
     * @param event
     *            the event to run when something happens to the
     *            action(key,finger, mouse button).
     *
     * @param type
     *            The type of action to run the event on. The available options
     *            are {@link ActionType#Hold} for holding the finger down,
     *            {@link ActionType#Down} for the frame in which the finger was
     *            pressed, and {@link ActionType#Up} for the frame in which the
     *            finger is released.
     */
    public void addOnClickListener(String inputProcessorName, int actionCode, OnClickListener listener,
                                   ActionType type) {

        if (listener == null)
            throw new NullPointerException("Input.addActionEvent parameter event of type ActionEvent is null");
        if (type == null)
            throw new NullPointerException("Input.addActionEvent parameter type of type InputEventType is null");
        if (inputProcessors.get(inputProcessorName) == null)
            throw new NullPointerException("Unable to find  input processor with the given name " + inputProcessorName);

        if (Action.toString(actionCode).equals("Unknown") && actionCode < 0 || actionCode > 255)
            throw new LummException("Input.addActionEvent parameter code of type int can't be found in InputAction");

        ActionEventWrapper wrapper = new ActionEventWrapper(actionCode, listener, type);
        actionEvents.get(inputProcessors.get(inputProcessorName)).add(wrapper);
    }

    /**
     * Adds an event to run when the mouse is scrolled. The only data that the
     * user can process is the integer value for the wheel rotation passed in
     * {@link OnScrollListener#run(int)}.
     *
     *
     * @throws NullPointerException
     *             if no processor is found by the requested name.
     *
     *
     * @param inputProcessorName
     *            the name of the input processor to assign the event to. The
     *            input processor can be taken by name using
     *            {@link #getInputProcessorNames()}. The available names are
     *            from the array set in
     *            {@link LummConfiguration#inputProcessorNames}, or if it has
     *            not been set, only {@link #DEFAULT_INPUT_PROCESSOR} can be
     *            found. If null value is provided, the first entry in the array
     *            is entered. The only moment in which an error is thrown
     *            regarding the input processor is if the name can't be found in
     *            the map of input processors.
     *
     *
     * @param listener
     *            the event to run when something the mouse wheel is scrolled
     */
    public void addOnScrollListener(String inputProcessorName, OnScrollListener listener) {

        if (listener == null)
            throw new NullPointerException("Input.addScrollEvent parameter event of type ScrollEvent is null");
        if (inputProcessors.get(inputProcessorName) == null)
            throw new NullPointerException("Unable to find  input processor with the given name " + inputProcessorName);

        scrollEvents.get(inputProcessors.get(inputProcessorName)).add(listener);

    }

    /**
     * Returns an array of input processor names. The keys are to be used in
     * {@link #addOnTouchListener(String, int, OnTouchListener, ActionType)},
     * {@link #addOnClickListener(String, int, OnClickListener, ActionType)},
     * {@link #addOnKeyTypedListener(String, OnKeyTypedListener)} or
     * {@link #addOnScrollListener(String, OnScrollListener)}.
     * <p>
     * The array contains the data retrieved from the
     * {@link LummConfiguration#inputProcessorNames}. If no
     * {@link LummConfiguration} instance is passed to the {@link Lumm}
     * constructor, The configuration is made automatically,
     * {@link LummConfiguration#inputProcessorNames} containing only the
     * {@link #DEFAULT_INPUT_PROCESSOR} value.
     *
     * @return array of input processor names.
     */
    public String[] getInputProcessorNames() {

        return processorNames;
    }

    /**
     * Attempts to remove an action event tied to a particular input processor,
     * returning a boolean representing whether or not the value was found and
     * removed.
     * <p>
     * If there is no processor tied to the string parameter passed, false will
     * be returned
     * <p>
     *
     * @throws NullPointerException
     *             if the event passed is null
     *
     * @param inputProcessorName
     *            name of the input processor to find the linked event to
     * @param event
     *            the event to remove
     * @return whether the event was removed
     */
    public boolean removeOnClickListener(String inputProcessorName, OnClickListener listener) {

        if (listener == null)
            throw new NullPointerException("Input.removeActionEvent parameter event of type ActionEvent is null");

        InputProcessor targetprocessor = inputProcessors.get(inputProcessorName);

        if (targetprocessor == null)
            return false;

        // go over the events tied to the passed input processor, to find the
        // ActionEvent in one of those wrappers
        for (int i = 0; i < actionEvents.get(targetprocessor).size(); i++) {
            if (actionEvents.get(targetprocessor).get(i).event.equals(listener)) {
                actionEvents.get(targetprocessor).remove(i);
                return true;
            }
        }

        return false;
    }

    /**
     * Attempts to remove a touch event tied to a particular input processor,
     * returning a boolean representing whether or not the value was found and
     * removed.
     * <p>
     * If there is no processor tied to the string parameter passed, false will
     * be returned
     * <p>
     *
     * @throws NullPointerException
     *             if the event passed is null
     *
     * @param inputProcessorName
     *            name of the input processor to find the linked event to
     * @param listener
     *            the listener to remove
     * @return whether the event was removed
     */
    public boolean removeOnTouchListener(String inputProcessorName, OnTouchListener listener) {

        if (listener == null)
            throw new NullPointerException("Input.removeTouchEvent parameter event of type TouchEvent is null");

        InputProcessor targetprocessor = inputProcessors.get(inputProcessorName);

        if (targetprocessor == null)
            return false;

        // go over the events tied to the passed input processor, to find the
        // ActionEvent in one of those wrappers
        for (int i = 0; i < touchEvents.get(targetprocessor).size(); i++) {
            if (touchEvents.get(targetprocessor).get(i).event.equals(listener)) {
                touchEvents.get(targetprocessor).remove(i);
                return true;
            }
        }

        return false;
    }

    /**
     * Attempts to remove a scroll event tied to a particular input processor,
     * returning a boolean representing whether or not the value was found and
     * removed.
     * <p>
     * If there is no processor tied to the string parameter passed, false will
     * be returned
     * <p>
     *
     * @throws NullPointerException
     *             if the event passed is null
     *
     * @param inputProcessorName
     *            name of the input processor to find the linked event to
     * @param listener
     *            the listener to remove
     * @return whether the event was removed
     */
    public boolean removeOnScrollListener(String inputProcessorName, OnScrollListener listener) {

        if (listener == null)
            throw new NullPointerException("Input.removeScrollEvent parameter event of type ScrollEvent is null");

        InputProcessor targetprocessor = inputProcessors.get(inputProcessorName);

        if (targetprocessor == null)
            return false;

        // go over the events tied to the passed input processor, to find the
        // ActionEvent in one of those wrappers
        for (int i = 0; i < scrollEvents.get(targetprocessor).size(); i++) {
            if (scrollEvents.get(targetprocessor).get(i).equals(listener)) {
                scrollEvents.get(targetprocessor).remove(i);
                return true;
            }
        }

        return false;

    }

    /**
     * Attempts to remove a key typed event tied to a particular input
     * processor, returning a boolean representing whether or not the value was
     * found and removed.
     * <p>
     * If there is no processor tied to the string parameter passed, false will
     * be returned
     * <p>
     *
     * @throws NullPointerException
     *             if the event passed is null
     *
     * @param inputProcessorName
     *            name of the input processor to find the linked event to
     * @param event
     *            the event to remove
     * @return whether the event was removed
     */
    public boolean removeOnKeyTypedListener(String inputProcessorName, OnKeyTypedListener event) {

        if (event == null)
            throw new NullPointerException("Input.removeKeyTypedEvent parameter event of type KeyTypedEvent is null");

        InputProcessor targetprocessor = inputProcessors.get(inputProcessorName);

        if (targetprocessor == null)
            return false;

        // go over the events tied to the passed input processor, to find the
        // ActionEvent in one of those wrappers
        for (int i = 0; i < keyTypedEvents.get(targetprocessor).size(); i++) {
            if (keyTypedEvents.get(targetprocessor).get(i).equals(event)) {
                keyTypedEvents.get(targetprocessor).remove(i);
                return true;
            }
        }

        return false;
    }

    /**
     * Attempts to change the action code bound to the event passed. Just like
     * the add and remove methods, NullPointerExceptions and
     * {@link LummException} will be thrown in multiple cases if the parameters
     * passed are either null, using them would harm the system or using them
     * would give no result.
     * <p>
     * The method does not add the ActionEvent if it is not existent, as an
     * {@link ActionType} parameter would be needed for the creation of the
     * internal {@link ActionEventWrapper} used by the system.
     * <p>
     * If you want to be able to bind a specific action to a specific actionCode
     * but you do not want to have a default value for it (not bound ), you can
     * add the event using
     * {@link #addOnClickListener(String, int, OnClickListener, ActionType)} and
     * pass {@link Action#NO_ACTION} so it will not be handled until the code
     * that the event is bound to changes.
     * <p>
     * When successfully rebinding the event, the old action will not handle the
     * event anymore.
     *
     * @param inputProcessorName
     *            name of the input processor in which to find the event
     *            parameter
     * @param event
     *            the event that we want to switch the {@link Action} to.
     * @param newActionCode
     *            the new action code (from {@link Action}) that we want to bind
     *            the event to
     * @return
     */
    public boolean rebindActionEvent(String inputProcessorName, OnClickListener event, int newActionCode) {

        if (event == null)
            throw new NullPointerException("Input.changeActionEventCode parameter event of type ActionEvent is null");

        if (Action.toString(newActionCode).equals("Unknown") && newActionCode < 0 || newActionCode > 255)
            throw new LummException("Input.addActionEvent parameter code of type int can't be found in InputAction");

        InputProcessor targetprocessor = inputProcessors.get(inputProcessorName);

        if (targetprocessor == null)
            throw new NullPointerException("Unable to find  input processor with the given name " + inputProcessorName);

        // go over the events tied to the passed input processor, to find the
        // ActionEvent in one of those wrappers
        for (int i = 0; i < actionEvents.get(targetprocessor).size(); i++) {
            if (actionEvents.get(targetprocessor).get(i).event.equals(event)) {
                actionEvents.get(targetprocessor).get(i).code = newActionCode;
                return true;
            }
        }

        return false;
    }

    @Override
    public List<Class<? extends LummModule>> getDependencies() {
        return null;
    }

    // endregion methods

    // TODO getInputProcessors and getInputProcessor(index)
    // TODO add/remove methods pass new InputProcessor object
    // TODO add Input Processors at runtime
}
