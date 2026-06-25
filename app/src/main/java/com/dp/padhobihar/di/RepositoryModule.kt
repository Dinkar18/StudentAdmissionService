package com.dp.padhobihar.di

import com.dp.padhobihar.data.repository.FirebaseAuthRepository
import com.dp.padhobihar.data.repository.FirebaseCollegeRepository
import com.dp.padhobihar.data.repository.FirebaseCommissionRepository
import com.dp.padhobihar.data.repository.FirebaseStudentRepository
import com.dp.padhobihar.data.repository.FirebaseUserRepository
import com.dp.padhobihar.domain.repository.AuthRepository
import com.dp.padhobihar.domain.repository.CollegeRepository
import com.dp.padhobihar.domain.repository.CommissionRepository
import com.dp.padhobihar.domain.repository.StudentRepository
import com.dp.padhobihar.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: FirebaseUserRepository): UserRepository

    @Binds
    @Singleton
    abstract fun bindStudentRepository(impl: FirebaseStudentRepository): StudentRepository

    @Binds
    @Singleton
    abstract fun bindCollegeRepository(impl: FirebaseCollegeRepository): CollegeRepository

    @Binds
    @Singleton
    abstract fun bindCommissionRepository(impl: FirebaseCommissionRepository): CommissionRepository
}
