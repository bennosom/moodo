package io.engst.moodo.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContract

data class SelectFileParams(
    val mimeType: String = "*/*",
    val suggestedDirectory: String = ""
)

data class CreateFileParams(
    val mimeType: String = "*/*",
    val suggestedName: String,
    val suggestedDirectory: String = ""
)

class SelectFileResultContract : ActivityResultContract<SelectFileParams, Uri?>() {
    override fun createIntent(context: Context, input: SelectFileParams) =
        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            setTypeAndNormalize(input.mimeType)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, input.suggestedDirectory)
        }

    override fun parseResult(resultCode: Int, intent: Intent?) = when (resultCode) {
        Activity.RESULT_OK -> intent?.data
        else -> null
    }
}

class CreateFileResultContract : ActivityResultContract<CreateFileParams, Uri?>() {
    override fun createIntent(context: Context, input: CreateFileParams) =
        Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            setTypeAndNormalize(input.mimeType)
            putExtra(Intent.EXTRA_TITLE, input.suggestedName)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, input.suggestedDirectory)
        }

    override fun parseResult(resultCode: Int, intent: Intent?) = when (resultCode) {
        Activity.RESULT_OK -> intent?.data
        else -> null
    }
}
