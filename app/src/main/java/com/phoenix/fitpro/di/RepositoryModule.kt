package com.phoenix.fitpro.di

import com.phoenix.fitpro.data.repository.AuthRepositoryImpl
import com.phoenix.fitpro.data.repository.NutritionRepositoryImpl
import com.phoenix.fitpro.data.repository.ProgramRepositoryImpl
import com.phoenix.fitpro.data.repository.UserRepositoryImpl
import com.phoenix.fitpro.data.repository.WorkoutRepositoryImpl
import com.phoenix.fitpro.domain.repository.AuthRepository
import com.phoenix.fitpro.domain.repository.NutritionRepository
import com.phoenix.fitpro.domain.repository.ProgramRepository
import com.phoenix.fitpro.domain.repository.UserRepository
import com.phoenix.fitpro.domain.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindWorkoutRepository(impl: WorkoutRepositoryImpl): WorkoutRepository

    @Binds @Singleton
    abstract fun bindNutritionRepository(impl: NutritionRepositoryImpl): NutritionRepository

    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds @Singleton
    abstract fun bindProgramRepository(impl: ProgramRepositoryImpl): ProgramRepository

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}

