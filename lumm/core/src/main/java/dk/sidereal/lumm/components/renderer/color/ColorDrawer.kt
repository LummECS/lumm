package dk.sidereal.lumm.components.renderer.color

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import dk.sidereal.lumm.architecture.Lumm
import dk.sidereal.lumm.architecture.LummObject
import dk.sidereal.lumm.components.renderer.Drawer
import dk.sidereal.lumm.components.renderer.Renderer
import javax.xml.soap.Text

class ColorDrawer(renderer: Renderer?, name: String?, useRawDelta: Boolean) : Drawer(renderer, name, useRawDelta) {

    /** File path to the texture  */
    private var filepath: String? = null

    /** Angle at which to rotate. Will rotate around [.origin].  */
    private var degrees: Float = 0.toFloat()

    /** Texture to manipulate and draw.  */
    private var sprite: Sprite = Sprite()

    /** Color for tinting the texture  */
    private var tintColor: Color? = null

    /**
     * Texture transparency. Object transparency and [.tintColor] also
     * taken into account.
     */
    private var transparency: Float = 0.toFloat()

    /** image size  */
    private val size: Vector2 = Vector2()

    /** offset position from [LummObject].  */
    private val positionOffset: Vector2 = Vector2()

    /** the origin of the texture, used for rotation  */
    private val origin: Vector2 = Vector2()


    override fun draw(delta: Float) {
        val targetX = renderer.`object`.position.x + positionOffset.x
        if (sprite.x != targetX)
            sprite.x = targetX

        val targetY = renderer.`object`.position.y + positionOffset.y
        if (sprite.y != targetY)
            sprite.y = targetY

        sprite.draw(renderer.`object`.sceneLayer.spriteBatch)
    }

    override fun isOutOfBounds(): Boolean {
        return false
    }

    override fun dispose() {
    }

    // region setters
    fun setTintColor(c: Color): ColorDrawer {
        tintColor = c
        sprite.color = tintColor

        return this
    }

    fun setSizeAndCenter(x: Float, y: Float): ColorDrawer {

        setSize(x, y).setOffsetPosition(-x / 2f, -y / 2f)
        return this
    }

    fun setOffsetPosition(x: Float, y: Float): ColorDrawer {

        if (this.positionOffset.x == x && this.positionOffset.y == y)
            return this

        this.positionOffset.set(x, y)
        return this
    }

    fun setOrigin(x: Float, y: Float): ColorDrawer {

        if (this.origin.x == x && this.origin.y == y)
            return this

        this.origin.set(x, y)
        sprite.setOrigin(origin.x, origin.y)
        return this
    }

    fun setRotation(degrees: Float, forced: Boolean): ColorDrawer {

        if (this.degrees == degrees && !forced)
            return this
        this.degrees = degrees
        sprite.rotation = degrees
        return this
    }


    fun setSize(x: Float, y: Float): ColorDrawer {
        if (this.size.x == x && this.size.y == y)
            return this
        this.size.set(x, y)
        sprite.setSize(this.size.x, this.size.y)
        return this
    }


}