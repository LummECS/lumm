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

package dk.sidereal.lumm.components.pathfinding;

import java.util.HashMap;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import dk.sidereal.lumm.architecture.Lumm;
import dk.sidereal.lumm.architecture.LummObject;
import dk.sidereal.lumm.architecture.concrete.ConcreteLummComponent;

/**
 * Handles the creation of {@link PathfindingMap} objects.
 *
 * @author Claudiu Bele
 */
public class PathfindingHandler extends ConcreteLummComponent {

    // region fields

    private static HashMap<String, PathfindingMap> maps;

    public PathfindingMap map;

    public static Sprite debugClosed;

    public static Sprite debugOpen;

    public static float nodeSizeDebug = 0.9f;

    public static float lineThickness = 10;

    public static BitmapFont font;

    public static GlyphLayout glyphLayout;

    // endregion fields

    // region constructors

    public PathfindingHandler(LummObject obj) {

        super(obj);

        setDebugToggleKeys(Keys.CONTROL_LEFT, Keys.X);
    }

    @Override
    protected void initialiseClass() {

        if (maps == null) {
            maps = new HashMap<String, PathfindingMap>();
        }

        if (!Lumm.debug.isEnabled())
            return;
        if (font == null) {
            font = Lumm.assets.get(Lumm.assets.frameworkAssetsFolder + "Blocks.fnt", BitmapFont.class);
            font.setColor(Color.BLACK);
            font.getData().setScale(2);
            // font.setScale(2);
        }
        if (glyphLayout == null) {
            glyphLayout = new GlyphLayout();
        }

        if (debugOpen == null) {
            debugOpen = new Sprite();
            debugOpen.setColor(new Color(0, 1, 0, 0.5f));
        }

        if (debugClosed == null) {
            debugClosed = new Sprite();
            debugClosed.setColor(new Color(1, 0, 0, 0.5f));
        }
    }

    // endregion constructors

    // region methods

    @Override
    public void onUpdate() {

    }

    public void addMap(String name, int width, int height) {

        map = new PathfindingMap(width, height);
        maps.put(name, map);
    }

    @Override
    public void onDebug() {

        if (map != null) {
            for (int i = 0; i < map.nodesX; i++) {
                for (int j = 0; j < map.nodesY; j++) {

                    float nodeX = map.getNodeX(i);
                    float nodeY = map.getNodeY(j);
                    glyphLayout.setText(font, i + ", " + j);

                    font.draw(object.getSceneLayer().spriteBatch, glyphLayout, (int) nodeX - glyphLayout.width / 2f,
                            (int) nodeY + glyphLayout.height / 2f);

                    if (map.nodes[i][j].access[0] == false) {

                        debugClosed.setBounds(nodeX - map.getNodeSize().x / 2 + 1,
                                nodeY - (map.getNodeSize().y * nodeSizeDebug) / 2, lineThickness,
                                map.getNodeSize().y * nodeSizeDebug);
                        debugClosed.draw(object.getSceneLayer().spriteBatch);
                    } else {
                        debugOpen.setBounds(nodeX - map.getNodeSize().x / 2 + 1,
                                nodeY - (map.getNodeSize().y * nodeSizeDebug) / 2, lineThickness,
                                map.getNodeSize().y * nodeSizeDebug);
                        debugOpen.draw(object.getSceneLayer().spriteBatch);
                    }

                    if (map.nodes[i][j].access[1] == false) {

                        debugClosed.setBounds(nodeX + map.getNodeSize().x / 2 - lineThickness - 1,
                                nodeY - (map.getNodeSize().y * nodeSizeDebug) / 2, lineThickness,
                                map.getNodeSize().y * nodeSizeDebug);
                        debugClosed.draw(object.getSceneLayer().spriteBatch);
                    } else {
                        debugOpen.setBounds(nodeX + map.getNodeSize().x / 2 - lineThickness - 1,
                                nodeY - (map.getNodeSize().y * nodeSizeDebug) / 2, lineThickness,
                                map.getNodeSize().y * nodeSizeDebug);
                        debugOpen.draw(object.getSceneLayer().spriteBatch);
                    }

                    if (map.nodes[i][j].access[2] == false) {

                        debugClosed.setBounds(nodeX - (map.getNodeSize().x * nodeSizeDebug) / 2,
                                nodeY - map.getNodeSize().y / 2 + 1, map.getNodeSize().x * nodeSizeDebug,
                                lineThickness);
                        debugClosed.draw(object.getSceneLayer().spriteBatch);
                    } else {
                        debugOpen.setBounds(nodeX - (map.getNodeSize().x * nodeSizeDebug) / 2,
                                nodeY - map.getNodeSize().y / 2 + 1, map.getNodeSize().x * nodeSizeDebug,
                                lineThickness);
                        debugOpen.draw(object.getSceneLayer().spriteBatch);
                    }

                    if (map.nodes[i][j].access[3] == false) {

                        debugClosed.setBounds(nodeX - (map.getNodeSize().x * nodeSizeDebug) / 2,
                                nodeY + map.getNodeSize().y / 2 - lineThickness - 1,
                                map.getNodeSize().x * nodeSizeDebug, lineThickness);
                        debugClosed.draw(object.getSceneLayer().spriteBatch);
                    } else {
                        debugOpen.setBounds(nodeX - (map.getNodeSize().x * nodeSizeDebug) / 2,
                                nodeY + map.getNodeSize().y / 2 - lineThickness - 1,
                                map.getNodeSize().x * nodeSizeDebug, lineThickness);
                        debugOpen.draw(object.getSceneLayer().spriteBatch);
                    }

                }
            }
        }

    }

    public static PathfindingMap getMap(String mapName) {

        if (!maps.containsKey(mapName))
            return null;

        return maps.get(mapName);
    }

    // endregion methods
}
