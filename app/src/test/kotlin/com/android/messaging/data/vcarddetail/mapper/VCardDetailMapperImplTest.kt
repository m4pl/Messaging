package com.android.messaging.data.vcarddetail.mapper

import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.android.messaging.data.vcarddetail.model.VCardField
import com.android.messaging.data.vcarddetail.model.VCardFieldAction
import com.android.messaging.datamodel.data.VCardContactItemData
import com.android.messaging.datamodel.media.VCardResource
import com.android.messaging.datamodel.media.VCardResourceEntry
import com.android.messaging.datamodel.media.VCardResourceEntry.VCardResourceEntryDestinationItem
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class VCardDetailMapperImplTest {

    private val mapper = VCardDetailMapperImpl()

    @Test
    fun map_nullVCardResource_returnsEmptyList() {
        val data = mockk<VCardContactItemData>()
        every { data.vCardResource } returns null

        val result = mapper.map(data)

        assertTrue(result.isEmpty())
    }

    @Test
    fun map_singleContactWithField_mapsValueLabelAndAction() {
        val entry = contactEntry(
            displayName = "Ada Lovelace",
            avatarUri = "content://avatar".toUri(),
            items = listOf(
                destination(
                    value = "+1 555 0001",
                    label = "Mobile",
                    clickIntent = Intent(Intent.ACTION_DIAL, "tel:+15550001".toUri()),
                ),
            ),
        )

        val contact = mapper.map(vCardData(entry)).single()

        assertEquals("Ada Lovelace", contact.displayName)
        assertEquals("content://avatar", contact.avatarUri)
        assertEquals(
            VCardField(
                value = "+1 555 0001",
                label = "Mobile",
                action = VCardFieldAction.Dial("+1 555 0001"),
            ),
            contact.fields.single(),
        )
    }

    @Test
    fun map_blankDisplayNameAndAvatar_areNormalizedToNull() {
        val entry = contactEntry(
            displayName = "   ",
            avatarUri = "   ".toUri(),
            items = emptyList(),
        )

        val contact = mapper.map(vCardData(entry)).single()

        assertNull(contact.displayName)
        assertNull(contact.avatarUri)
        assertTrue(contact.fields.isEmpty())
    }

    @Test
    fun map_nullDisplayNameAndAvatar_areMappedToNull() {
        val entry = contactEntry(
            displayName = null,
            avatarUri = null,
            items = emptyList(),
        )

        val contact = mapper.map(vCardData(entry)).single()

        assertNull(contact.displayName)
        assertNull(contact.avatarUri)
    }

    @Test
    fun map_blankFieldValue_isSkipped() {
        val entry = contactEntry(
            displayName = "Ada",
            avatarUri = null,
            items = listOf(
                destination(
                    value = "   ",
                    label = null,
                    clickIntent = null,
                ),
            ),
        )

        val contact = mapper.map(vCardData(entry)).single()

        assertTrue(contact.fields.isEmpty())
    }

    @Test
    fun map_blankFieldLabel_isMappedToNull() {
        val field = singleField(
            value = "value",
            label = "  ",
            clickIntent = null,
        )

        assertNull(field.label)
    }

    @Test
    fun map_multipleContacts_arePreservedInOrder() {
        val result = mapper.map(
            vCardData(
                contactEntry(
                    displayName = "First",
                    avatarUri = null,
                    items = emptyList(),
                ),
                contactEntry(
                    displayName = "Second",
                    avatarUri = null,
                    items = emptyList(),
                ),
            ),
        )

        assertEquals(listOf("First", "Second"), result.map { it.displayName })
    }

    @Test
    fun mapAction_nullIntent_isNone() {
        val field = singleField(
            value = "value",
            label = null,
            clickIntent = null,
        )

        assertEquals(VCardFieldAction.None, field.action)
    }

    @Test
    fun mapAction_dialTelIntent_isDial() {
        val action = actionFor(Intent(Intent.ACTION_DIAL, "tel:+15550001".toUri()))

        assertEquals(VCardFieldAction.Dial("value"), action)
    }

    @Test
    fun mapAction_sendtoMailtoIntent_isEmail() {
        val action = actionFor(Intent(Intent.ACTION_SENDTO, "mailto:".toUri()))

        assertEquals(VCardFieldAction.Email("value"), action)
    }

    @Test
    fun mapAction_viewGeoIntent_isOpenMap() {
        val action = actionFor(Intent(Intent.ACTION_VIEW, "geo:0,0?q=Main%20Street".toUri()))

        assertEquals(VCardFieldAction.OpenMap("value"), action)
    }

    @Test
    fun mapAction_viewHttpIntent_isOpenUrlFromIntentData() {
        val action = actionFor(Intent(Intent.ACTION_VIEW, "http://example.com".toUri()))

        assertEquals(VCardFieldAction.OpenUrl("http://example.com"), action)
    }

    @Test
    fun mapAction_viewHttpsIntent_isOpenUrlFromIntentData() {
        val action = actionFor(Intent(Intent.ACTION_VIEW, "https://example.com/path".toUri()))

        assertEquals(VCardFieldAction.OpenUrl("https://example.com/path"), action)
    }

    @Test
    fun mapAction_uppercaseScheme_isNormalized() {
        val action = actionFor(Intent(Intent.ACTION_DIAL, "TEL:+15550001".toUri()))

        assertEquals(VCardFieldAction.Dial("value"), action)
    }

    @Test
    fun mapAction_dialActionWithNonTelScheme_isNone() {
        val action = actionFor(Intent(Intent.ACTION_DIAL, "http://example.com".toUri()))

        assertEquals(VCardFieldAction.None, action)
    }

    @Test
    fun mapAction_unknownActionWithKnownScheme_isNone() {
        val action = actionFor(Intent(Intent.ACTION_EDIT, "tel:+15550001".toUri()))

        assertEquals(VCardFieldAction.None, action)
    }

    @Test
    fun mapAction_intentWithoutData_isNone() {
        val action = actionFor(Intent(Intent.ACTION_VIEW))

        assertEquals(VCardFieldAction.None, action)
    }

    private fun actionFor(clickIntent: Intent?): VCardFieldAction {
        return singleField(
            value = "value",
            label = null,
            clickIntent = clickIntent,
        ).action
    }

    private fun singleField(
        value: String,
        label: String?,
        clickIntent: Intent?,
    ): VCardField {
        val entry = contactEntry(
            displayName = "Ada",
            avatarUri = null,
            items = listOf(
                destination(
                    value = value,
                    label = label,
                    clickIntent = clickIntent,
                ),
            ),
        )
        return mapper.map(vCardData(entry)).single().fields.single()
    }

    private fun vCardData(
        vararg entries: VCardResourceEntry,
    ): VCardContactItemData {
        val resource = mockk<VCardResource>()
        every { resource.vCards } returns entries.toList()
        val data = mockk<VCardContactItemData>()
        every { data.vCardResource } returns resource
        return data
    }

    private fun contactEntry(
        displayName: String?,
        avatarUri: Uri?,
        items: List<VCardResourceEntryDestinationItem>,
    ): VCardResourceEntry {
        val entry = mockk<VCardResourceEntry>()
        every { entry.displayName } returns displayName
        every { entry.avatarUri } returns avatarUri
        every { entry.contactInfo } returns items
        return entry
    }

    private fun destination(
        value: String?,
        label: String?,
        clickIntent: Intent?,
    ): VCardResourceEntryDestinationItem {
        return VCardResourceEntryDestinationItem(value, label, clickIntent)
    }
}
