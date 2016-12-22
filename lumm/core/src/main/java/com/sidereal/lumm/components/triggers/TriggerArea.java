package com.sidereal.lumm.components.triggers;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.sidereal.lumm.architecture.LummObject;

public class TriggerArea {

    public static class TriggerAreaConnection {

        TriggerArea own;
        TriggerArea target;

        public TriggerAreaConnection(TriggerArea t1, TriggerArea t2) {
            own = t1;
            target = t2;
        }

        public boolean equals(TriggerAreaConnection obj) {

            return own.equals(obj.own) && target.equals(obj.target);
        }
    }

    public Rectangle rect;
    protected float offsetX;
    protected float offsetY;
    public String name;
    public boolean enabled;

    /**
     * Returns the collision area's width. The width is initially set in the
     * constructors, and can be changed using {@link #setWidth(float, boolean)}
     * or {@link #setSize(float, float, boolean)}.
     *
     * @return collision area's width
     * @see {@link #setWidth(float, boolean)} to set the width of the collision
     * area, as well as potentially center it on the x-axis
     * @see {@link #setSize(float, float, boolean)} to set both the width and
     * height of the collision area
     */
    public float getWidth() {
        return rect.width;
    }

    /**
     * Sets the width of the collision area, updating the x-axis offset in order
     * to center the collision area around the object if the 2nd parameter is
     * set to true.
     *
     * @param width              The new width of the collision area.
     * @param centerAroundObject Whether to center the object on the x axis, updating the value
     *                           returned by {@link #getOffsetX()}.
     * @see {@link #setSize(float, float, boolean)} for setting both the width
     * and height, as well as updating both
     * <p>
     * {@link #setOffsetX(float)} for manually updating the x-axis offset.
     * <p>
     * {@link #getWidth()} for getting the width of the area.
     * <p>
     * {@link #getOffsetX()} for getting the x-axis offset of the area.
     */
    public void setWidth(float width, boolean centerAroundObject) {

        rect.setWidth(width);
        if (centerAroundObject) {
            offsetX = -width / 2f;
        }
    }

    /**
     * Returns the collision area's height. The height is initially set in the
     * constructor, and can be changed using {@link #setHeight(float, boolean)}
     * or {@link #setSize(float, float, boolean)}.
     *
     * @return collision area's height
     * @see {@link #setHeight(float, boolean)} to set the height of the
     * collision area, as well as potentially center it on the y-axis
     * @see {@link #setSize(float, float, boolean)} to set both the width and
     * height of the collision area
     */
    public float getHeight() {
        return rect.getHeight();
    }

    /**
     * Sets the height of the collision area, updating the y-axis offset in
     * order to center the collision area around the object if the 2nd parameter
     * is set to true.
     *
     * @param height             The new height of the collision area.
     * @param centerAroundObject Whether to center the object on the x axis, updating the value
     *                           returned by {@link #getOffsetX()}.
     * @see {@link #setSize(float, float, boolean)} for setting both the width
     * and height, as well as updating both
     * @see {@link #setOffsetY(float)} for manually updating the y-axis offset.
     * @see {@link #getHeight()} for getting the height of the area.
     * @see {@link #getOffsetY()} for getting the y-axis offset of the area.
     */
    public void setHeight(float height, boolean centerAroundObject) {
        rect.setWidth(height);
        if (centerAroundObject) {
            offsetY = -height / 2f;

        }
    }

    /**
     * Sets the with and height of the collision area, updating both the x-axis
     * and y-axis offset in order to center the collision area around the object
     * if the 2nd parameter is set to true.
     *
     * @param width              The new width of the collision area.
     * @param height             The new height of the collision area
     * @param centerAroundObject Whether to center the object on the x and y axis, updating the
     *                           value returned by {@link #getOffsetX()} and
     *                           {@link #getHeight()}.
     * @see {@link #setWidth(float, boolean)} to set the width of the collision
     * area, as well as potentially center it on the x-axis
     * @see {@link #setHeight(float, boolean)} to set the height of the
     * collision area, as well as potentially center it on the y-axis
     * @see {@link #setOffsetX(float)} for manually updating the x-axis offset.
     * @see {@link #setOffsetY(float)} for manually updating the y-axis offset.
     * @see {@link #getWidth()} for getting the width of the area.
     * @see {@link #getHeight()} for getting the height of the area.
     * @see {@link #getOffsetX()} for getting the x-axis offset of the area.
     * @see {@link #getOffsetY()} for getting the y-axis offset of the area.
     */
    public void setSize(float width, float height, boolean centerAroundObject) {
        setWidth(width, centerAroundObject);
        setHeight(height, centerAroundObject);
    }

    /**
     * Returns the x-axis offset at which to place the collision area, in
     * relation to the {@link LummObject}'s position. An x and y-axis offset
     * value of 0 leads to the collision area having its bottom-left corner at
     * the object's position.
     *
     * @return the x-axis offset of the collision area rectangle.
     */
    public float getOffsetX() {
        return offsetX;
    }

    /**
     * Sets the x-axis offset of the rectangle that represents the collision
     * area.
     * <p>
     * An x and y-axis offset value of 0 leads to the collision area having its
     * bottom-left corner at the object's position.
     *
     * @param offsetX x-axis collision area offset from the object's position
     */
    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;

    }

    /**
     * Returns the y-axis offset at which to place the collision area, in
     * relation to the {@link LummObject}'s position. An x and y-axis offset
     * value of 0 leads to the collision area having its bottom-left corner at
     * the object's position.
     *
     * @return the x-axis offset of the collision area rectangle.
     */
    public float getOffsetY() {
        return offsetY;
    }

    /**
     * Sets the y-axis offset of the rectangle that represents the collision
     * area.
     * <p>
     * An x and y-axis offset value of 0 leads to the collision area having its
     * bottom-left corner at the object's position.
     *
     * @param offsetY y-axis collision area offset from the object's position
     */
    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    public static boolean doTriggerAreasIntersect(List<TriggerArea> c1, List<TriggerArea> c2) {

        for (int i = 0; i < c1.size(); i++) {
            if (!c1.get(i).enabled)
                continue;

            for (int j = 0; j < c2.size(); j++) {
                if (!c2.get(i).enabled)
                    continue;

                if (c1.get(i).rect.overlaps(c2.get(j).rect))
                    return true;
            }
        }
        return false;
    }

    public static List<TriggerAreaConnection> getIntersectingTriggerAreas(List<TriggerArea> c1, List<TriggerArea> c2) {
        List<TriggerAreaConnection> list = new ArrayList<TriggerAreaConnection>();

        for (int i = 0; i < c1.size(); i++) {
            if (!c1.get(i).enabled)
                continue;

            for (int j = 0; j < c2.size(); j++) {
                if (!c2.get(i).enabled)
                    continue;

                if (c1.get(i).rect.overlaps(c2.get(j).rect))
                    list.add(new TriggerAreaConnection(c1.get(i), c2.get(j)));
            }
        }

        return list;

    }

}