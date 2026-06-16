package com.android.messaging.domain.shareintent.usecase

import android.content.ContentResolver
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.android.messaging.util.LogUtil
import com.android.messaging.util.MediaMetadataRetrieverWrapper
import javax.inject.Inject

internal interface ResolveSharedContentType {
    operator fun invoke(uri: Uri?, fallbackType: String?): String?
}

internal class ResolveSharedContentTypeImpl @Inject constructor(
    private val contentResolver: ContentResolver,
) : ResolveSharedContentType {

    override fun invoke(
        uri: Uri?,
        fallbackType: String?,
    ): String? {
        if (uri == null) {
            return fallbackType
        }

        // Prefer the provider-declared type from ContentResolver.getType, falling back to
        // metadata extraction only when it is unavailable. This is recommended by
        // https://developer.android.com/training/secure-file-sharing/retrieve-info.html
        // Some implementations of MediaMetadataRetriever get things horribly wrong for common
        // formats such as jpeg (reports as video/ffmpeg).
        return resolveDeclaredType(uri) ?: extractFromMetadata(uri, fallbackType)
    }

    private fun resolveDeclaredType(uri: Uri): String? {
        return runCatching { contentResolver.getType(uri) }
            .onFailure { LogUtil.i(LogUtil.BUGLE_TAG, "Could not query type of $uri", it) }
            .getOrNull()
    }

    private fun extractFromMetadata(
        uri: Uri,
        fallbackType: String?,
    ): String? {
        val retriever = MediaMetadataRetrieverWrapper()

        return try {
            runCatching {
                retriever.setDataSource(uri)
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            }.onFailure {
                LogUtil.i(LogUtil.BUGLE_TAG, "Could not determine type of $uri", it)
            }.getOrNull() ?: fallbackType
        } finally {
            retriever.release()
        }
    }
}
