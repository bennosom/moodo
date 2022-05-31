package io.engst.moodo.ui.tasks.edit

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Filter
import androidx.annotation.ColorInt
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip
import io.engst.moodo.R
import io.engst.moodo.model.types.Tag
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.dp
import io.engst.moodo.shared.injectLogger
import java.util.*
import kotlin.random.Random

class AutoCompleteChipGroupView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FlexboxLayout(context, attrs, defStyle) {

    private var callback: ((tags: List<Tag>) -> Unit)? = null
    private var currentTags: List<Tag> = emptyList()

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

    // TODO: try MaterialAutoCompleteTextView + AutoCompleteAdapter
    private val autoCompleteTextView: AutoCompleteTextView = AutoCompleteTextView(context)
    private lateinit var autoCompleteTagAdapter: AutoCompleteTagAdapter

    init {
        initEditTextView()
    }

    fun setCurrentTags(tags: List<Tag>) {
        currentTags = tags
        getAllChips().forEach { removeView(it) }
        currentTags.forEach { tag ->
            addChipView(tag.name, tag.color)
        }
    }

    fun setAvailableTags(tags: List<Tag>) {
        autoCompleteTagAdapter.tags = tags
    }

    fun setOnTagsChanged(block: (tags: List<Tag>) -> Unit) {
        callback = block
    }

    private fun initEditTextView() {
        val layoutParams = LayoutParams(WRAP_CONTENT, 48.dp).apply {
            //flexGrow = 1f
        }
        autoCompleteTextView.layoutParams = layoutParams
        autoCompleteTextView.setBackgroundResource(android.R.color.transparent)
        autoCompleteTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        autoCompleteTextView.threshold = 1
        autoCompleteTextView.hint = resources.getText(R.string.task_tag_input_hint)
        autoCompleteTextView.imeOptions = EditorInfo.IME_ACTION_DONE
        autoCompleteTextView.inputType = EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS
        autoCompleteTextView.maxLines = 1
        autoCompleteTextView.setSingleLine()

        autoCompleteTagAdapter = AutoCompleteTagAdapter(context) { position ->
            val tag = autoCompleteTagAdapter.getItem(position) as Tag
            autoCompleteTextView.text = null
            addChipView(name = tag.name)
            currentTags = currentTags + tag
            callback?.invoke(currentTags)
            autoCompleteTextView.dismissDropDown()
        }
        autoCompleteTextView.setAdapter(autoCompleteTagAdapter)

        autoCompleteTextView.setOnItemClickListener { adapterView, _, position, _ ->
            val tag = adapterView.getItemAtPosition(position) as Tag
            autoCompleteTextView.text = null
            addChipView(name = tag.name)
            currentTags = currentTags + tag
            callback?.invoke(currentTags)
        }

        autoCompleteTextView.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val text = textView.text.toString()
                if (text.isNotBlank()) {
                    textView.text = null
                    addChipView(name = text)
                    currentTags = currentTags + Tag(name = text, color = Color.LTGRAY)
                    callback?.invoke(currentTags)
                }
                true
            } else false
        }

        /*doAfterTextChanged { text ->
            if (text != null && text.isEmpty()) {
                return@doAfterTextChanged
            }

            // comma is detected
            if (text?.trim()?.last() == ',') {
                val name = text.substring(0, text.length - 1)
                block(name)
            }
        }*/

        addView(autoCompleteTextView)
    }

    private fun addChipView(name: String, @ColorInt color: Int = Color.LTGRAY) {
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
        addView(chip, childCount - 1, layoutParams)
    }
}

fun FlexboxLayout.getAllChips(): List<Chip> = (0 until childCount).mapNotNull { index -> getChildAt(index) as? Chip }
