package com.android.messaging.di.shareintent

import com.android.messaging.ui.shareintent.screen.delegate.ShareDraftDelegate
import com.android.messaging.ui.shareintent.screen.delegate.ShareDraftDelegateImpl
import com.android.messaging.ui.shareintent.screen.delegate.ShareTargetsDelegate
import com.android.messaging.ui.shareintent.screen.delegate.ShareTargetsDelegateImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
internal abstract class ShareIntentViewModelBindsModule {

    @Binds
    @ViewModelScoped
    abstract fun bindShareTargetsDelegate(
        impl: ShareTargetsDelegateImpl,
    ): ShareTargetsDelegate

    @Binds
    @ViewModelScoped
    abstract fun bindShareDraftDelegate(
        impl: ShareDraftDelegateImpl,
    ): ShareDraftDelegate
}
