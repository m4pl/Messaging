package com.android.messaging.di.shareintent

import com.android.messaging.data.shareintent.repository.ShareTargetsRepository
import com.android.messaging.data.shareintent.repository.ShareTargetsRepositoryImpl
import com.android.messaging.domain.shareintent.usecase.BuildSharedDraftMessage
import com.android.messaging.domain.shareintent.usecase.BuildSharedDraftMessageImpl
import com.android.messaging.ui.shareintent.screen.mapper.ShareTargetUiStateMapper
import com.android.messaging.ui.shareintent.screen.mapper.ShareTargetUiStateMapperImpl
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
    abstract fun bindShareTargetUiStateMapper(
        impl: ShareTargetUiStateMapperImpl,
    ): ShareTargetUiStateMapper

    @Binds
    @Reusable
    abstract fun bindShareTargetsRepository(
        impl: ShareTargetsRepositoryImpl,
    ): ShareTargetsRepository

    @Binds
    @Reusable
    abstract fun bindBuildSharedDraftMessage(
        impl: BuildSharedDraftMessageImpl,
    ): BuildSharedDraftMessage
}
