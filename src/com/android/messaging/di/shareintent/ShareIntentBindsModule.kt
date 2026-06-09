package com.android.messaging.di.shareintent

import com.android.messaging.data.shareintent.repository.SharedAttachmentRepository
import com.android.messaging.data.shareintent.repository.SharedAttachmentRepositoryImpl
import com.android.messaging.domain.shareintent.usecase.BuildSharedConversationDraft
import com.android.messaging.domain.shareintent.usecase.BuildSharedConversationDraftImpl
import com.android.messaging.domain.shareintent.usecase.ResolveSharedContentType
import com.android.messaging.domain.shareintent.usecase.ResolveSharedContentTypeImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ShareIntentBindsModule {

    @Binds
    @Reusable
    abstract fun bindSharedAttachmentRepository(
        impl: SharedAttachmentRepositoryImpl,
    ): SharedAttachmentRepository

    @Binds
    @Reusable
    abstract fun bindResolveSharedContentType(
        impl: ResolveSharedContentTypeImpl,
    ): ResolveSharedContentType

    @Binds
    @Reusable
    abstract fun bindBuildSharedConversationDraft(
        impl: BuildSharedConversationDraftImpl,
    ): BuildSharedConversationDraft
}
