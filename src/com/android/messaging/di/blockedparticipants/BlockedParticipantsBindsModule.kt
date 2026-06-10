package com.android.messaging.di.blockedparticipants

import com.android.messaging.data.blockedparticipants.repository.BlockedParticipantsRepository
import com.android.messaging.data.blockedparticipants.repository.BlockedParticipantsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class BlockedParticipantsBindsModule {

    @Binds
    @Reusable
    abstract fun bindBlockedParticipantsRepository(
        impl: BlockedParticipantsRepositoryImpl,
    ): BlockedParticipantsRepository
}
