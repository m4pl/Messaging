package com.android.messaging.data.vcarddetail.mapper

import android.content.Intent
import com.android.messaging.data.vcarddetail.model.VCardContact
import com.android.messaging.data.vcarddetail.model.VCardField
import com.android.messaging.data.vcarddetail.model.VCardFieldAction
import com.android.messaging.datamodel.data.VCardContactItemData
import com.android.messaging.datamodel.media.VCardResourceEntry
import com.android.messaging.datamodel.media.VCardResourceEntry.VCardResourceEntryDestinationItem
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

internal interface VCardDetailMapper {
    fun map(vCardContactItemData: VCardContactItemData): ImmutableList<VCardContact>
}

internal class VCardDetailMapperImpl @Inject constructor() : VCardDetailMapper {

    override fun map(
        vCardContactItemData: VCardContactItemData,
    ): ImmutableList<VCardContact> {
        val entries = vCardContactItemData.vCardResource?.vCards ?: return persistentListOf()

        return entries
            .map(::mapContact)
            .toImmutableList()
    }

    private fun mapContact(entry: VCardResourceEntry): VCardContact {
        return VCardContact(
            displayName = entry.displayName?.takeIf(String::isNotBlank),
            avatarUri = entry.avatarUri?.toString()?.takeIf(String::isNotBlank),
            fields = entry.contactInfo
                .mapNotNull(::mapField)
                .toImmutableList(),
        )
    }

    private fun mapField(item: VCardResourceEntryDestinationItem): VCardField? {
        val value = item.displayDestination?.takeIf(String::isNotBlank) ?: return null

        return VCardField(
            value = value,
            label = item.destinationType?.takeIf(String::isNotBlank),
            action = mapAction(item.clickIntent, value),
        )
    }

    private fun mapAction(clickIntent: Intent?, displayValue: String): VCardFieldAction {
        if (clickIntent == null) {
            return VCardFieldAction.None
        }

        val scheme = clickIntent.data?.scheme?.lowercase()

        return when (clickIntent.action) {
            Intent.ACTION_DIAL if scheme == SCHEME_TEL -> {
                VCardFieldAction.Dial(displayValue)
            }

            Intent.ACTION_SENDTO if scheme == SCHEME_MAILTO -> {
                VCardFieldAction.Email(displayValue)
            }

            Intent.ACTION_VIEW if scheme == SCHEME_GEO -> {
                VCardFieldAction.OpenMap(displayValue)
            }

            Intent.ACTION_VIEW if (scheme == SCHEME_HTTP || scheme == SCHEME_HTTPS) -> {
                VCardFieldAction.OpenUrl(clickIntent.dataString ?: displayValue)
            }

            else -> VCardFieldAction.None
        }
    }

    private companion object {
        private const val SCHEME_TEL = "tel"
        private const val SCHEME_MAILTO = "mailto"
        private const val SCHEME_GEO = "geo"
        private const val SCHEME_HTTP = "http"
        private const val SCHEME_HTTPS = "https"
    }
}
