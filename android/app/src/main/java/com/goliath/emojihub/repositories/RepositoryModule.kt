package com.goliath.emojihub.repositories

import com.goliath.emojihub.repositories.remote.UserRepository
import com.goliath.emojihub.repositories.remote.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindsUserRepository(impl: UserRepositoryImpl): UserRepository
}