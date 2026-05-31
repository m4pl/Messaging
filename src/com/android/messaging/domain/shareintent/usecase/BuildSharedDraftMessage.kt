package com.android.messaging.domain.shareintent.usecase

import android.content.ContentResolver
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.core.content.IntentCompat
import com.android.messaging.datamodel.data.MessageData
import com.android.messaging.datamodel.data.PendingAttachmentData
import com.android.messaging.util.Assert
import com.android.messaging.util.ContentType
import com.android.messaging.util.LogUtil
import com.android.messaging.util.MediaMetadataRetrieverWrapper
import java.io.IOException
import javax.inject.Inject

internal interface BuildSharedDraftMessage {
    operator fun invoke(intent: Intent): MessageData?
}

internal class BuildSharedDraftMessageImpl @Inject constructor(
    private val contentResolver: ContentResolver,
) : BuildSharedDraftMessage {

    override fun invoke(intent: Intent): MessageData? {
        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)

        return when (intent.action) {
            Intent.ACTION_SEND -> buildFromSend(intent, subject)
            Intent.ACTION_SEND_MULTIPLE -> buildFromSendMultiple(intent, subject)

            else -> {
                Assert.fail("Unsupported action type for sharing: ${intent.action}")
                null
            }
        }
    }

    private fun buildFromSend(intent: Intent, subject: String?): MessageData? {
        val contentUri = IntentCompat.getParcelableExtra(
            intent,
            Intent.EXTRA_STREAM,
            Uri::class.java,
        )
        val contentType = extractContentType(contentUri, intent.type)

        if (LogUtil.isLoggable(LogUtil.BUGLE_TAG, LogUtil.DEBUG)) {
            LogUtil.d(
                LogUtil.BUGLE_TAG,
                "onAttachFragment: contentUri=$contentUri, intent.getType()=${intent.type}, " +
                    "inferredType=$contentType",
            )
        }

        return when {
            ContentType.TEXT_PLAIN == contentType -> {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let { messageText ->
                    MessageData.createSharedMessage(messageText, subject)
                }
            }

            ContentType.isMediaType(contentType) && contentUri != null -> {
                MessageData.createSharedMessage(null, subject).apply {
                    addSharedPart(contentType, contentUri)
                }
            }

            else -> {
                Assert.fail(
                    "Unsupported shared content type for $contentUri: $contentType " +
                        "(${intent.type})",
                )
                null
            }
        }
    }

    private fun buildFromSendMultiple(intent: Intent, subject: String?): MessageData? {
        val contentType = intent.type
        if (!ContentType.isImageType(contentType)) {
            Assert.fail("Unsupported shared content type: $contentType")
            return null
        }

        val imageUris = IntentCompat.getParcelableArrayListExtra(
            intent,
            Intent.EXTRA_STREAM,
            Uri::class.java,
        )
        return when {
            imageUris.isNullOrEmpty() -> null

            else -> {
                MessageData.createSharedMessage(null, subject).apply {
                    imageUris.forEach { uri ->
                        addSharedPart(extractContentType(uri, contentType), uri)
                    }
                }
            }
        }
    }

    private fun extractContentType(
        uri: Uri?,
        contentType: String?,
    ): String? {
        if (uri == null) {
            return contentType
        }

        // First try looking at file extension. This is less reliable in some ways but it's
        // recommended by
        // https://developer.android.com/training/secure-file-sharing/retrieve-info.html
        // Some implementations of MediaMetadataRetriever get things horribly wrong for common
        // formats such as jpeg (reports as video/ffmpeg).
        return contentResolver.getType(uri) ?: extractTypeFromMetadata(uri, contentType)
    }

    private fun extractTypeFromMetadata(uri: Uri, contentType: String?): String? {
        val retriever = MediaMetadataRetrieverWrapper()

        return try {
            retriever.setDataSource(uri)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: contentType
        } catch (e: IOException) {
            LogUtil.i(LogUtil.BUGLE_TAG, "Could not determine type of $uri", e)
            contentType
        } finally {
            retriever.release()
        }
    }

    private fun MessageData.addSharedPart(
        contentType: String?,
        uri: Uri,
    ) {
        addPart(PendingAttachmentData.createPendingAttachmentData(contentType, uri))
    }
}
