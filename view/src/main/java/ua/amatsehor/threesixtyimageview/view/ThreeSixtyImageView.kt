package ua.amatsehor.threesixtyimageview.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.IntRange
import android.util.AttributeSet
import android.util.Log
import android.view.AbsSavedState
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import kotlin.math.roundToInt

class ThreeSixtyImageView : ImageView {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    var supplier: ImageSupplier? = null
        set(value) {
            field = value
            maxImageIndex = value?.maxImageIndex ?: 0
            askFor(0)
        }

    var spinSpeedMultiplier: Double = 1.0

    private val rightIndex: Int
        get() = if (currentImageIndex == maxImageIndex) 0 else currentImageIndex + 1

    private val leftIndex: Int
        get() = if (currentImageIndex == 0) maxImageIndex else currentImageIndex - 1

    private var currentImageIndex = 0
    private var maxImageIndex = 0

    private var lastXPosition: Float = 0.0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)

        performClick()

        val imageChangeThreshold = width / (spinSpeedMultiplier * (maxImageIndex + 1))

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                lastXPosition = event.x
            }
            MotionEvent.ACTION_MOVE -> {
                val diffWithLast = event.x - lastXPosition
                if (Math.abs(diffWithLast) >= imageChangeThreshold.roundToInt()) {
                    lastXPosition = event.x

                    Log.e(TAG, "lastXPosition: $lastXPosition, diff: $diffWithLast")

                    if (diffWithLast > 0) {
                        askFor(leftIndex)
                    } else {
                        askFor(rightIndex)
                    }
                }
            }
        }

        return true
    }

    fun scrollLeft() {
        askFor(rightIndex)
    }

    fun scrollRight() {
        askFor(leftIndex)
    }

    override fun onSaveInstanceState(): Parcelable {
        val state = super.onSaveInstanceState()
        return SavedState(state).apply {
            this.lastImageIndex = currentImageIndex
        }
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            currentImageIndex = state.lastImageIndex
            askFor(currentImageIndex)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private fun askFor(index: Int) {
        Log.e(TAG, "Requested index: $index")
        val image = supplier?.invoke(index) ?: return
        setImageDrawable(image)
        currentImageIndex = index
    }

    companion object {
        private const val TAG = "ThreeSixteenImageView"
    }

    class ImageSupplier(@IntRange(from = 1) var maxImageIndex: Int, val supply: (imageIndex: Int) -> Drawable) {
        operator fun invoke(imageIndex: Int): Drawable {
            if (maxImageIndex < 1) maxImageIndex = 0
            return supply(imageIndex)
        }
    }

    class SavedState : View.BaseSavedState {

        private var superState: Parcelable? = null
            @JvmName("getOwnSuperState") get() = field

        var lastImageIndex: Int = 0

        constructor(source: Parcel) : super(source) {
            this.superState = source.readParcelable(ImageView::class.java.classLoader)
            lastImageIndex = source.readInt()
        }

        constructor(superState: Parcelable?) : super(AbsSavedState.EMPTY_STATE) {
            this.superState = superState
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(lastImageIndex)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel) = SavedState(parcel)

            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }

    }
}