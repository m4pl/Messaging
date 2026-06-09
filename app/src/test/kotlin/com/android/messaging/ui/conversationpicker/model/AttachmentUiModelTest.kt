package com.android.messaging.ui.conversationpicker.model

import com.android.messaging.data.conversation.model.draft.ConversationDraftAttachment
import com.android.messaging.ui.common.components.attachment.VCardAttachmentKind
import com.android.messaging.util.ContentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class AttachmentUiModelTest {

    @Test
    fun toAttachmentUiModel_mapsVideoToMediaMarkedAsVideo() {
        val model = attachment(contentType = "video/mp4").toAttachmentUiModel()

        val media = model as AttachmentUiModel.Media
        assertEquals("content://uri", media.id)
        assertEquals("video/mp4", media.contentType)
        assertTrue(media.isVideo)
    }

    @Test
    fun toAttachmentUiModel_mapsImageToMediaNotMarkedAsVideo() {
        val model = attachment(contentType = "image/jpeg").toAttachmentUiModel()

        val media = model as AttachmentUiModel.Media
        assertEquals("image/jpeg", media.contentType)
        assertFalse(media.isVideo)
    }

    @Test
    fun toAttachmentUiModel_mapsAudioWithDuration() {
        val model = attachment(
            contentType = "audio/mpeg",
            durationMillis = 4200L,
        ).toAttachmentUiModel()

        val audio = model as AttachmentUiModel.Audio
        assertEquals("content://uri", audio.id)
        assertEquals(4200L, audio.durationMillis)
    }

    @Test
    fun toAttachmentUiModel_mapsAudioWithoutDurationToZero() {
        val model = attachment(
            contentType = "audio/mpeg",
            durationMillis = null,
        ).toAttachmentUiModel()

        val audio = model as AttachmentUiModel.Audio
        assertEquals(0L, audio.durationMillis)
    }

    @Test
    fun toAttachmentUiModel_mapsVCardStrippingFileExtensionFromTitle() {
        val model = attachment(
            contentType = ContentType.TEXT_VCARD,
            displayName = "Jane.vcf",
        ).toAttachmentUiModel()

        val vcard = model as AttachmentUiModel.VCard
        assertEquals("Jane", vcard.title)
        assertEquals(VCardAttachmentKind.Contact, vcard.kind)
    }

    @Test
    fun toAttachmentUiModel_mapsVCardWithNullDisplayNameToNullTitle() {
        val model = attachment(
            contentType = ContentType.TEXT_VCARD,
            displayName = null,
        ).toAttachmentUiModel()

        assertEquals(null, (model as AttachmentUiModel.VCard).title)
    }

    private fun attachment(
        contentType: String,
        contentUri: String = "content://uri",
        durationMillis: Long? = null,
        displayName: String? = null,
    ): ConversationDraftAttachment {
        return ConversationDraftAttachment(
            contentType = contentType,
            contentUri = contentUri,
            durationMillis = durationMillis,
            displayName = displayName,
        )
    }
}
