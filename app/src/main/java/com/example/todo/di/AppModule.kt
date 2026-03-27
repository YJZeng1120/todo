package com.example.todo.di

import android.content.Context
import com.example.todo.data.dao.TodoDao
import com.example.todo.data.database.TodoDatabase
import com.example.todo.data.repository.TodoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TodoDatabase =
        TodoDatabase.getInstance(context)

    @Provides
    fun provideTodoDao(db: TodoDatabase): TodoDao = db.todoDao()

    @Provides
    @Singleton
    fun provideRepository(dao: TodoDao): TodoRepository = TodoRepository(dao)
}
