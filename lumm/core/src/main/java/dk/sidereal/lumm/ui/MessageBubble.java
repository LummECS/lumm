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

package dk.sidereal.lumm.ui;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import dk.sidereal.lumm.architecture.Lumm;
import dk.sidereal.lumm.architecture.LummObject;
import dk.sidereal.lumm.architecture.LummScene;
import dk.sidereal.lumm.architecture.concrete.ConcreteLummObject;
import dk.sidereal.lumm.architecture.core.input.ActionData;
import dk.sidereal.lumm.architecture.core.input.OnClickListener;
import dk.sidereal.lumm.architecture.listeners.OnResizeListener;
import dk.sidereal.lumm.components.input.Clickable;
import dk.sidereal.lumm.components.renderer.Renderer;
import dk.sidereal.lumm.components.renderer.sprite.SpriteBuilder;
import dk.sidereal.lumm.components.renderer.sprite.SpriteDrawer;
import dk.sidereal.lumm.architecture.core.Input;

/**
 * Class resembling a message bubble. Messages can be sent to the object,
 * displaying them and dissapearing or waiting to be clicked until closing.
 *
 * @author Claudiu Bele
 */
public class MessageBubble extends ConcreteLummObject {

    // region fields

    private String targetInputProcessor;

    public TextBuilder text;

    public Vector2 size;

    public Color bgColor;

    public float textSize;

    public Renderer renderer;

    public Clickable clickable;

    public ArrayList<MessageBubbleText> texts;

    public boolean inTransition;

    private ShrinkType shrinkType;

    /**
     * The general offset of the text, compared to the top of the Message
     * bubble, initially set to -40
     */
    public float textPositionOffset;

    /**
     * The amount we reduce from the size for the word wrap. Being 0 would
     * possibly make the text overlap the frame texture.
     * <p>
     * Set to -40 in {@link MessageBubble#onCreate(Object...) }.
     */
    public float textWrapOffset;

    /**
     * The static hashmap in which we will hold valuable message bubbles ( the
     * ones that when created are being passed a name )
     */
    public static HashMap<String, MessageBubble> messageBubbles;

    class MessageBubbleText {

        public ArrayList<TextBuilder.Paragraph> paragraphs;

        public float fadeTime;

        public float upTime;

        public float timeRemaining;

        public DisplayStatus displayStatus;

        public boolean closeOnClick;

        public MessageBubbleText(ArrayList<TextBuilder.Paragraph> paragraphs) {

            displayStatus = DisplayStatus.APPEARING;
            fadeTime = 0.4f;
            closeOnClick = false;
            upTime = timeRemaining = 2;
            textSize = 1.4f;
            this.paragraphs = paragraphs;
        }

        public void set(MessageBubble bubble) {

            displayStatus = DisplayStatus.APPEARING;
            timeRemaining = fadeTime;

            bubble.text.clearText();
            for (int i = 0; i < paragraphs.size(); i++) {
                bubble.text.addText(paragraphs.get(i).text, paragraphs.get(i).color);
            }
            if (closeOnClick) {
                bubble.text.addText(" ", Color.BLACK);
                bubble.text.addText("Click window to close.", Color.WHITE);
            }
        }

        public void update(MessageBubble bubble) {

            timeRemaining -= Lumm.time.getDeltaTime();
            timeRemaining = Math.max(0, timeRemaining);

            float progress = 0;
            if (displayStatus.equals(DisplayStatus.APPEARING)) {

                progress = 1 - timeRemaining / fadeTime;
                if (timeRemaining == 0) {
                    timeRemaining = upTime;
                    displayStatus = DisplayStatus.DISPLAYED;
                    if (closeOnClick) {
                        bubble.clickable.setEnabled(true);
                    }
                    inTransition = false;
                }

            }

            if (displayStatus.equals(DisplayStatus.DISPLAYED)) {
                progress = 1;
                if (timeRemaining == 0 && !closeOnClick) {
                    timeRemaining = fadeTime;
                    displayStatus = DisplayStatus.DISSAPEARING;
                }

            }

            if (displayStatus.equals(DisplayStatus.DISSAPEARING)) {
                if (texts.size() == 1) {
                    bubble.inTransition = true;
                }
                progress = timeRemaining / fadeTime;
                if (timeRemaining == 0) {
                    displayStatus = DisplayStatus.HIDDEN;
                    texts.remove(this);
                    if (texts.size() > 0) {
                        texts.get(0).set(bubble);
                    }

                }

            }

            // if has to become smaller or bigger in size, will do that
            // becomes bigger by default initially, becomes smaller when
            // removing the last fade element.
            if (bubble.inTransition) {
                renderer.getDrawer("BG", SpriteDrawer.class).setTransparency(progress, false);

                if (bubble.shrinkType.equals(ShrinkType.Full)) {
                    renderer.getDrawer("BG", SpriteDrawer.class).setSizeAndCenter(size.x * progress, size.y * progress);

                    bubble.text.setScale(textSize * progress);
                }
            }

            bubble.text.alpha = progress;

        }

        public MessageBubbleText addParagraph(String text, Color color) {

            paragraphs.add(new TextBuilder.Paragraph(text, color));
            return this;
        }

        public MessageBubbleText setFadeTime(float fadeTime) {

            this.fadeTime = fadeTime;
            return this;
        }

        public MessageBubbleText setUpTime(float upTime) {

            this.upTime = upTime;
            return this;
        }

        public MessageBubbleText setCloseOnClick(boolean value) {

            closeOnClick = value;
            return this;
        }

    }

    enum DisplayStatus {
        APPEARING, DISPLAYED, DISSAPEARING, HIDDEN
    }

    public static enum ShrinkType {
        Full, AlphaChannel
    }

    // endregion fields

    // region constructors

    public MessageBubble(String name, LummScene scene, ShrinkType shrinkType, String targetInputProcessor) {

        super(scene, targetInputProcessor);

        if (messageBubbles == null) {
            messageBubbles = new HashMap<String, MessageBubble>();
        }

        if (name != null) {
            messageBubbles.put(name, this);
        }

        this.shrinkType = shrinkType;

    }

    @Override
    public void onCreate(Object... params) {

        this.targetInputProcessor = (String) params[0];

        this.position.setRelative(0, 0, 10);
        this.textPositionOffset = 0;
        this.textWrapOffset = 0;
        size = new Vector2(Gdx.graphics.getHeight() * 0.2f, Gdx.graphics.getHeight() * 0.4f);
        bgColor = new Color(0, 0, 0, 0.5f);
        renderer = new Renderer(this);
        clickable = new Clickable(this);
        clickable.setEnabled(false);
        clickable.setAreaSize(size.x, size.y);

        clickable.addOnClickListener(targetInputProcessor, Input.Action.ANY_ACTION, new OnClickListener() {

            @Override
            public boolean onClick(ActionData inputData) {

                if (texts.size() == 0)
                    return false;

                if (texts.get(0).closeOnClick && texts.get(0).displayStatus.equals(DisplayStatus.DISPLAYED)) {
                    texts.get(0).displayStatus = DisplayStatus.DISSAPEARING;
                    texts.get(0).timeRemaining = texts.get(0).fadeTime;
                    clickable.setEnabled(false);
                    return true;
                }
                return false;
            }

        }, Input.ActionType.Down, true);

        setSceneLayer("UI");

        Lumm.assets.load(Lumm.assets.frameworkAssetsFolder + "White.png", Texture.class);

        renderer.addDrawer("BG", new SpriteBuilder(Lumm.assets.frameworkAssetsFolder + "White.png")
                .setSizeAndCenter(size.x, size.y).setTransparency(0).setColor(bgColor));

        text = new TextBuilder(getScene(), true);
        text.setSceneLayer("UI");
        text.setAnchor(TextBuilder.Anchor.Middle);
        text.setParent(this);
        text.position.setLocal(0, 0);
        text.position.setRelative(0, 0, 1);
        texts = new ArrayList<MessageBubble.MessageBubbleText>();
        inTransition = true;

        onResizeListener = new OnResizeListener<LummObject>() {

            @Override
            public void onResize(LummObject caller, float x, float y, float oldX, float oldY) {

                setSize(size.x * (x / oldX), size.y * (y / oldY));
                position.set(getSceneLayer().camera.position.x, getSceneLayer().camera.position.y, position.getZ());

            }
        };
    }

    // endregion constructors

    // region methods

    public void setColor(float r, float g, float b, float a) {

        bgColor.set(r, g, b, a);
        renderer.getDrawer("BG", SpriteDrawer.class).setColor(bgColor);
    }

    public void setSize(float width, float height) {

        size = new Vector2(width, height);
        text.setWindowSize(width + textWrapOffset);

        renderer.getDrawer("BG", SpriteDrawer.class).setSizeAndCenter(size.x, size.y);

        clickable.setAreaSize(width, height);

        text.position.setLocal(0, textPositionOffset);

    }

    public void setTextWrapOffset(float value) {

        this.textWrapOffset = value;
        text.setWindowSize(size.x + textWrapOffset);
    }

    public void setTextPositionOffset(float value) {

        this.textPositionOffset = value;
        text.position.setLocal(0, textPositionOffset);
    }

    @Override
    public void onUpdate() {

        if (texts.size() > 0) {
            texts.get(0).update(this);
        }
    }

    public void add(MessageBubbleText text) {

        texts.add(text);
        // if numbers of texts is 1 after adding our own text, we make it
        // appear. IF it isn't we just add it to the queue
        if (texts.size() == 1) {
            texts.get(0).set(this);
        }

    }

    public void add(String text) {

        add(text, new Color(Color.WHITE));
    }

    public void add(String text, Color color) {

        add(text, color, false);
    }

    public void add(String text, Color color, boolean closeOnClick) {

        add(text, color, closeOnClick, 2f, 0.5f);
    }

    public void add(String text, Color color, boolean closeOnClick, float upTime, float fadeTime) {

        ArrayList<TextBuilder.Paragraph> paragraphs = new ArrayList<TextBuilder.Paragraph>();
        paragraphs.add(new TextBuilder.Paragraph(text, color));

        add(paragraphs, closeOnClick, upTime, fadeTime);
    }

    public void add(ArrayList<TextBuilder.Paragraph> paragraphs, boolean closeOnClick, float upTime, float fadeTime) {

        add(new MessageBubbleText(paragraphs).setCloseOnClick(closeOnClick).setUpTime(upTime).setFadeTime(fadeTime));
    }

    public boolean isDisplayingMessage() {

        return texts.size() != 0;
    }

    public void clear() {

        texts.clear();
    }


    // endregion methods
}
