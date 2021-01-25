package org.isoron.uhabits

import dagger.Module
import dagger.Provides
import org.isoron.uhabits.core.AppScope
import org.isoron.uhabits.core.tasks.SingleThreadTaskRunner
import org.isoron.uhabits.core.tasks.TaskRunner

@Module
internal object SingleThreadModule {
    @JvmStatic
    @Provides
    @AppScope
    fun provideTaskRunner(): TaskRunner {
        return SingleThreadTaskRunner()
    }
}
