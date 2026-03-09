package com.ozpods.di
import com.ozpods.data.parser.ProximityPairingParser
import com.ozpods.data.repository.AirPodsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideProximityPairingParser(): ProximityPairingParser = ProximityPairingParser()
    @Provides @Singleton
    fun provideAirPodsRepository(): AirPodsRepository = AirPodsRepository()
}
