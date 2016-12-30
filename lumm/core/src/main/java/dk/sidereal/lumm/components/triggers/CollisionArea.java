package dk.sidereal.lumm.components.triggers;

import com.badlogic.gdx.math.Rectangle;

/**
 * Represents a rectangle that designates an area in which collision events are
 * handled. All areas in a {@link Collider} can be queried using
 * {@link Collider#getCollisionAreas()}.
 * <p>
 * A {@link CollisionArea} object is tied to a Collider when
 * {@link Collider#addCollisionArea(CollisionArea)} is called.
 * <p>
 * A listener can be added to a Collider object for ColliderArea collisions,
 * which is {@link OnCollisionAreaEventListener}.
 *
 * @author Claudiu
 */
public class CollisionArea extends TriggerArea {

    Collider collider;

    /**
     * Constructor in which the size designates the width and height, also being
     * used for changing the offset values to center the collider area.
     * <p>
     * This constructor is to be used for simple colliders, but is not limited
     * in any way, being adjustable using the setters for custom sizes and
     * offsets.
     * <p>
     * For custom offsets without having to use setters, use
     * {@link #Collider(String, float, float, float, float)}.
     *
     * @param name name of collision area
     * @param size size of the area, designates the width and height.
     */
    public CollisionArea(String name, float size) {
        this(name, size, size);
    }

    /**
     * Constructor which requires both the width and height, which are also
     * being used for changing the offset values to center the collision area
     * around the object that the {@link Collider} is tied to.
     * <p>
     * For custom offsets without having to use setters, use
     * {@link #Collider(String, float, float, float, float)}.
     *
     * @param name   name of collision area
     * @param width  width of the area
     * @param height height of the area
     */
    public CollisionArea(String name, float width, float height) {
        this(name, width, height, -width / 2f, -height / 2f);
    }

    /**
     * ColliderArea constructor that uses parameters for custom x and y-axis
     * offsets in relation to the origin, which is the bottom left corner, which
     * at offsets 0,0 points at the object's position
     *
     * @param name    name of collision area
     * @param width   width of the area
     * @param height  height of the area
     * @param offsetX x-axis offset
     * @param offsetY y-axis offset
     */
    public CollisionArea(String name, float width, float height, float offsetX, float offsetY) {
        this.name = name;
        rect = new Rectangle(0, 0, width, height);
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.enabled = true;
    }

    @Override
    public void setWidth(float width, boolean centerAroundObject) {
        super.setWidth(width, centerAroundObject);
        if (collider == null)
            return;
        rect.setX(collider.objectToFocus.position.getX() + offsetX);

    }

    @Override
    public void setHeight(float height, boolean centerAroundObject) {
        super.setHeight(height, centerAroundObject);
        if (collider == null)
            return;
        rect.setY(collider.objectToFocus.position.getY() + offsetY);
    }

    @Override
    public void setOffsetX(float offsetX) {
        super.setOffsetX(offsetX);
        if (collider == null)
            return;
        rect.setX(collider.objectToFocus.position.getX() + offsetX);
    }

    @Override
    public void setOffsetY(float offsetY) {
        super.setOffsetY(offsetY);
        if (collider == null)
            return;
        rect.setY(collider.objectToFocus.position.getY() + offsetY);

    }

    /**
     * Returns the collider tied to the object. The value is set in
     * {@link Collider#addCollisionArea(CollisionArea)} to the target parameter
     * value.
     *
     * @return The collider that the area is part of.
     */
    public Collider getCollider() {
        return collider;
    }
}
