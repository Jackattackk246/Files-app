package com.jackattackk246.files.ui.components

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.jackattackk246.files.R

/**
 * Custom Input Pill Component. Hardcodes a highly stylized rounded bottom edge 
 * profile directly onto the search bar layout matrix to prevent text boundary overlaps.
 */
class RoundedBottomSearchPill @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : EditText(context, attrs, defStyleAttr) {

    init {
        // Enforce high-density contrast and padding metrics out of the box
        setHintTextColor(ContextCompat.getColor(context, R.color.text_muted_slate))
        setTextColor(ContextCompat.getColor(context, R.color.text_pure_white))
        
        // Target your explicit low-light graphic canvas layer
        setBackgroundResource(R.drawable.bg_search_pill_rounded_bottom_dark)
        
        // Inject structural boundary space measurements 
        setPaddingRelative(24, 16, 24, 20) 
        
        hint = "Search files or descriptors..."
    }
}

