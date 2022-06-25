package io.engst.moodo.ui.tasks.edit

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Filter
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import io.engst.moodo.R
import io.engst.moodo.model.types.Tag
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.dp
import io.engst.moodo.shared.injectLogger
import java.util.*
import kotlin.random.Random

class AutoCompleteChipGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ChipGroup(context, attrs, defStyle) {

    private var callback: ((tags: List<Tag>) -> Unit)? = null
    private var currentTags: List<Tag> = emptyList()
    private var availableTags: List<Tag> = emptyList()

    class AutoCompleteTagAdapter(context: Context, private val itemClickListener: (position: Int) -> Unit) :
        ArrayAdapter<Tag>(context, R.layout.tag_autocomplete_dropdown_item, mutableListOf()) {

        private val logger: Logger by injectLogger("tag_autocomplete")
        private val inflater = LayoutInflater.from(context)

        var tags: List<Tag> = emptyList()
        var filteredTags: ArrayList<Tag> = arrayListOf()

        override fun getCount(): Int {
            return filteredTags.size
        }

        override fun getItem(position: Int): Tag? {
            return filteredTags[position]
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val tag = getItem(position)
            val layout = (convertView as? ViewGroup) ?: inflater.inflate(R.layout.tag_autocomplete_dropdown_item, parent, false)
            layout.findViewById<Chip>(R.id.tag_autocomplete_dropdown_chip).apply {
                text = tag?.name
                chipBackgroundColor = tag?.let { ColorStateList.valueOf(it.color) }
                setOnClickListener {
                    itemClickListener.invoke(position)
                }
                // setOnCloseIconClickListener { /* TODO: remove entire tag with confirmation dialog */ }
            }
            logger.debug { "getView for $position" }
            return layout
        }

        override fun getFilter(): Filter = object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredItems: List<Tag> = if (constraint == null || constraint.isEmpty()) {
                    tags
                } else {
                    val filterPattern = constraint.toString().trim().lowercase(Locale.getDefault())
                    tags.filter { it.name.lowercase().contains(filterPattern) }
                }

                logger.debug { "filtered results: $filteredItems" }

                return FilterResults().apply {
                    values = filteredItems
                    count = filteredItems.size
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredTags.clear()
                if (results?.count!! > 0) {
                    for (result in results.values as List<*>) {
                        filteredTags.add(result as Tag)
                    }
                    notifyDataSetChanged()
                } else {
                    filteredTags.addAll(tags)
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    private val editText: EditText = EditText(context)
    private val chipBackground = ChipDrawable.createFromResource(context, R.xml.task_edit_tag_input_chip)

    init {
        initEditTextView()
    }

    fun setCurrentTags(tags: List<Tag>) {
        currentTags = tags
        getAllChips().forEach { removeView(it) }
        currentTags.forEach { tag ->
            addLeftChip(tag.name, tag.color)
        }
    }

    fun setAvailableTags(tags: List<Tag>) {
        availableTags = tags
    }

    fun setOnTagsChanged(block: (tags: List<Tag>) -> Unit) {
        callback = block
    }

    private fun initEditTextView() {
        val containerLayout = FrameLayout(context)
        val outerLayoutParams = LayoutParams(WRAP_CONTENT, 48.dp)
        containerLayout.layoutParams = outerLayoutParams

        val backgroundView = View(context)
        backgroundView.layoutParams = MarginLayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
            topMargin = 8.dp
            bottomMargin = 8.dp
        }
        backgroundView.background = chipBackground
        containerLayout.addView(backgroundView)

        chipBackground.isCloseIconVisible = false

        editText.updatePadding(left = 36.dp, right = 16.dp)
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        editText.lineHeight = 16.dp
        editText.hint = resources.getText(R.string.task_tag_input_hint)
        editText.imeOptions = EditorInfo.IME_ACTION_DONE
        editText.inputType = EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS
        editText.maxLines = 1
        editText.setSingleLine()
        editText.background = null

        editText.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val text = textView.text.toString()
                if (text.isNotBlank()) {
                    textView.text = null
                    addLeftChip(name = text)
                    currentTags = currentTags + Tag(name = text, color = Color.LTGRAY)
                    callback?.invoke(currentTags)
                }
                true
            } else false
        }

        editText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                updateSuggestedTags((view as EditText).text.toString())
            } else {
                getRightChips().onEach { removeView(it) }
            }
        }

        editText.doAfterTextChanged { text ->
            val textEntered = text?.isNotBlank() ?: false
            chipBackground.isCloseIconVisible = textEntered // show or hide "add" icon

            // make EditText as small as input text given (otherwise hint text will be taken for minimum width)
            if (textEntered) {
                editText.updatePadding(left = 36.dp, right = 36.dp)
                editText.hint = null
            } else {
                editText.updatePadding(left = 36.dp, right = 16.dp)
                editText.hint = resources.getText(R.string.task_tag_input_hint)
            }

            updateSuggestedTags(text?.toString())
        }

        containerLayout.addView(editText, LayoutParams(WRAP_CONTENT, MATCH_PARENT))
        addView(containerLayout, outerLayoutParams)
    }

    private fun updateSuggestedTags(searchText: String? = null) {
        getRightChips().onEach { removeView(it) }
        searchText?.let { text ->
            if (text.isNotBlank()) {
                availableTags
                    .filter { tag ->
                        !currentTags.any {
                            it.id == tag.id
                        }
                    }
                    .onEach { tag ->
                        if (tag.name.contains(text, ignoreCase = true)) {
                            addRightChip(name = tag.name, color = tag.color)
                        }
                    }
            }
        }
    }

    private fun addRightChip(name: String, @ColorInt color: Int = Color.LTGRAY) {
        val inflater = LayoutInflater.from(context)
        val chip = inflater.inflate(R.layout.task_edit_tag_suggestion_chip, null) as Chip
        chip.apply {
            text = name
            chipBackgroundColor = ColorStateList.valueOf(color)
            setOnClickListener {
                editText.text = null
                currentTags = currentTags + Tag(name = name, color = color)
                callback?.invoke(currentTags)
            }
        }

        val layoutParams = MarginLayoutParams(MarginLayoutParams.WRAP_CONTENT, MarginLayoutParams.WRAP_CONTENT)
        layoutParams.rightMargin = 4.dp
        addView(chip, layoutParams)
    }

    private fun addLeftChip(name: String, @ColorInt color: Int = Color.LTGRAY) {
        val inflater = LayoutInflater.from(context)
        val chip = inflater.inflate(R.layout.task_edit_tag_chip, null) as Chip
        chip.apply {
            text = name
            chipBackgroundColor = ColorStateList.valueOf(color)
            setOnClickListener { view ->
                val nextColor: Int = Color.argb(255, Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
                chipBackgroundColor = ColorStateList.valueOf(nextColor)
                currentTags = currentTags.map {
                    if (it.name == (view as Chip).text) {
                        it.copy(color = chipBackgroundColor!!.defaultColor)
                    } else {
                        it
                    }
                }
                callback?.invoke(currentTags)
            }
            setOnCloseIconClickListener { view ->
                removeView(view)
                currentTags = currentTags.filter { it.name != (view as Chip).text }
                callback?.invoke(currentTags)
            }
        }

        val layoutParams = MarginLayoutParams(MarginLayoutParams.WRAP_CONTENT, MarginLayoutParams.WRAP_CONTENT)
        layoutParams.rightMargin = 4.dp
        val editTextIndex = indexOfChild(editText.parent as View)
        addView(chip, editTextIndex, layoutParams)
    }

    private fun getRightChips(): List<Chip> = (indexOfChild(editText.parent as View) until childCount).mapNotNull { index ->
        getChildAt(index) as? Chip
    }

    private fun getAllChips(): List<Chip> = (0 until childCount).mapNotNull { index -> getChildAt(index) as? Chip }
}

