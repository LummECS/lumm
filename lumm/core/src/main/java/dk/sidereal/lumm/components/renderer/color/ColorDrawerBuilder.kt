package dk.sidereal.lumm.components.renderer.color

import com.badlogic.gdx.graphics.Color
import dk.sidereal.lumm.components.renderer.Drawer
import dk.sidereal.lumm.components.renderer.DrawerBuilder

public class ColorDrawerBuilder() : DrawerBuilder<ColorDrawer>() {

    private var sizeX: Float = 0.toFloat()
    private var sizeY:Float = 0.toFloat()

    private var offsetX: Float = 0.toFloat()
    private var offsetY:Float = 0.toFloat()

    private var originX: Float = 0.toFloat()
    private val originY: Float = 0.toFloat()

    private var rotationDegrees: Float = 0.toFloat()

    private var tintColor: Color? = null

    private var transparency: Float = 0.toFloat()
    
    override fun build(name: String?): ColorDrawer {
        val drawer = ColorDrawer(renderer, null, true)
        if (sizeX != 0f && sizeY != 0f)
            drawer.setSize(sizeX, sizeY)
        drawer.setOffsetPosition(offsetX, offsetY)
        drawer.setOrigin(originX, originY)
        if (tintColor != null)
            drawer.setTintColor(tintColor!!)
        drawer.setRotation(rotationDegrees, false)
        return drawer
    }

    // region setters and getters
    fun setSize(sizeX: Float, sizeY: Float): ColorDrawerBuilder {

        this.sizeX = sizeX
        this.sizeY = sizeY
        return this
    }

    fun setSizeAndCenter(sizeX: Float, sizeY: Float): ColorDrawerBuilder {

        return setSize(sizeX, sizeY).setOffsetPosition(-sizeX / 2f, -sizeY / 2f)
    }

    fun setOffsetPosition(offsetX: Float, offsetY: Float): ColorDrawerBuilder {

        this.offsetX = offsetX
        this.offsetY = offsetY
        return this
    }

    fun setOrigin(originX: Float, originY: Float): ColorDrawerBuilder {

        this.originX = originX
        this.offsetY = originY
        return this
    }

    fun setRotation(rotationDegrees: Float): ColorDrawerBuilder {

        this.rotationDegrees = rotationDegrees
        return this
    }

    fun setColor(tintColor: Color): ColorDrawerBuilder {

        this.tintColor = tintColor
        return this
    }

    fun setTransparency(transparency: Float): ColorDrawerBuilder {

        this.transparency = transparency
        return this
    }

    // endregion

}