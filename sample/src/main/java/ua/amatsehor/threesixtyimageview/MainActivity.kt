package ua.amatsehor.threesixtyimageview

import ua.amatsehor.threesixtyimageview.view.ThreeSixtyImageView
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.SparseArray
import android.widget.SeekBar
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class MainActivity : AppCompatActivity() {

    private val imageArray = SparseArray<Drawable>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sbSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                image.spinSpeedMultiplier = if (progress == 0) 1.0 else progress.toDouble()
            }
        })

        btnLeft.setOnClickListener { image.scrollLeft() }
        btnRight.setOnClickListener { image.scrollRight() }

        for (i in 0 .. maxImageIndex) {
            launch(UI) {
                Glide.with(this@MainActivity)
                    .asDrawable()
                    .load(getImageUrl(i + 1))
                    .into(object : SimpleTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            Log.e("Image loading", "Loaded image #$i at ${System.currentTimeMillis()}")
                            imageArray.put(i, resource)

                            if (imageArray.size() == maxImageIndex + 1) {
                                image.supplier = ThreeSixtyImageView.ImageSupplier(maxImageIndex) { imageArray[it] }
                            }
                        }
                    })
            }
        }
    }

    companion object {
        fun getImageUrl(index: Int): String {
            return "https://cdn.pkwteile.de/uploads/360_photos/8056713/images/1216R0048%20($index).jpg"
        }

        const val maxImageIndex = 34
    }
}