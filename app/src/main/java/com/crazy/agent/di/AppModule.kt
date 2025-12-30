package com.crazy.agent.di

import android.content.Context
import com.crazy.agent.action.ActionExecutor
import com.crazy.agent.agent.AgentPlanBuilder
import com.crazy.agent.agent.CommandParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt module for providing dependencies */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideCommandParser(): CommandParser {
        return CommandParser()
    }

    @Provides
    @Singleton
    fun provideAgentPlanBuilder(commandParser: CommandParser): AgentPlanBuilder {
        return AgentPlanBuilder(commandParser)
    }

    @Provides
    @Singleton
    fun provideActionExecutor(@ApplicationContext context: Context): ActionExecutor {
        return ActionExecutor(context)
    }
}
