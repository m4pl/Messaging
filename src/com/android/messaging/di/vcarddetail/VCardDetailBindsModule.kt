package com.android.messaging.di.vcarddetail

import com.android.messaging.data.vcarddetail.mapper.VCardDetailMapper
import com.android.messaging.data.vcarddetail.mapper.VCardDetailMapperImpl
import com.android.messaging.data.vcarddetail.repository.VCardDetailRepository
import com.android.messaging.data.vcarddetail.repository.VCardDetailRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class VCardDetailBindsModule {

    @Binds
    @Reusable
    abstract fun bindVCardDetailRepository(
        impl: VCardDetailRepositoryImpl,
    ): VCardDetailRepository

    @Binds
    @Reusable
    abstract fun bindVCardDetailMapper(
        impl: VCardDetailMapperImpl,
    ): VCardDetailMapper
}
