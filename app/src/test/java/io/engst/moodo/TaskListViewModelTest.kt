package io.engst.moodo

import io.engst.moodo.model.TaskRepository
import io.engst.moodo.model.types.Task
import io.engst.moodo.ui.tasks.Group
import io.engst.moodo.ui.tasks.TaskListViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale

class TaskListViewModelTest {
    private val coroutineDispatcher = TestCoroutineDispatcher()

    private lateinit var testClock: Clock
    private lateinit var testLocale: Locale
    private lateinit var mockRepository: TaskRepository

    @Test
    fun `At 30-10-2021 - Sunday, 31-10-2021 is Tomorrow while Monday, 01-11-2021 is NextWeek (US)`() =
        coroutineDispatcher.runBlockingTest {
            testClock = Clock.fixed(
                Instant.parse("2021-10-30T12:00:00Z"),
                ZoneId.of("UTC")
            )
            testLocale = Locale(Locale.US.language, Locale.US.country)

            val taskTomorrow = Task(
                id = 0L,
                createdDate = LocalDateTime.now(testClock),
                dueDate = LocalDateTime.of(
                    LocalDate.of(2021, 10, 31),
                    LocalTime.NOON
                ),
                isDue = false
            )
            val taskNextWeek = Task(
                id = 1L,
                createdDate = LocalDateTime.now(testClock),
                dueDate = LocalDateTime.of(
                    LocalDate.of(2021, 11, 1),
                    LocalTime.NOON
                ),
                isDue = false
            )

            mockRepository = mockk {
                every { tasks } returns flow {
                    emit(
                        listOf(
                            taskTomorrow,
                            taskNextWeek
                        )
                    )
                }
            }

            val uut = TaskListViewModel(
                coroutineDispatcher,
                mockRepository,
                testClock,
                testLocale
            )

            assertThat(
                uut.tasks.first().map { it.id }
            ).isEqualTo(
                listOf(
                    Group.Today.name,
                    Group.Tomorrow.name,
                    taskTomorrow.id.toString(),
                    Group.NextWeek.name,
                    taskNextWeek.id.toString()
                )
            )
        }

    @Test
    fun `At 02-11-2021 - Sunday, 07-11-2021 should be only part of NextWeek (US)`() =
        coroutineDispatcher.runBlockingTest {
            testClock = Clock.fixed(
                Instant.parse("2021-11-02T12:00:00Z"),
                ZoneId.of("UTC")
            )
            testLocale = Locale(Locale.US.language, Locale.US.country)

            val testTask = Task(
                id = 0L,
                createdDate = LocalDateTime.now(testClock),
                dueDate = LocalDateTime.of(
                    LocalDate.of(2021, 11, 7),
                    LocalTime.NOON
                ),
                isDue = false
            )

            mockRepository = mockk {
                every { tasks } returns flow {
                    emit(listOf(testTask))
                }
            }

            val uut = TaskListViewModel(
                coroutineDispatcher,
                mockRepository,
                testClock,
                testLocale
            )

            assertThat(
                uut.tasks.first().map { it.id }
            ).isEqualTo(
                listOf(
                    Group.Today.name,
                    Group.NextWeek.name,
                    testTask.id.toString()
                )
            )
        }


    @Test
    fun `At 30-10-2021 - Sunday, 07-11-2021 should be only part of NextWeek (DE)`() =
        coroutineDispatcher.runBlockingTest {
            testClock = Clock.fixed(
                Instant.parse("2021-10-30T12:00:00Z"),
                ZoneId.of("UTC")
            )
            testLocale = Locale(Locale.GERMAN.language, Locale.GERMANY.country)

            val testTask = Task(
                id = 0L,
                createdDate = LocalDateTime.now(testClock),
                dueDate = LocalDateTime.of(
                    LocalDate.of(2021, 10, 31),
                    LocalTime.NOON
                ),
                isDue = false
            )

            mockRepository = mockk {
                every { tasks } returns flow {
                    emit(listOf(testTask))
                }
            }

            val uut = TaskListViewModel(
                coroutineDispatcher,
                mockRepository,
                testClock,
                testLocale
            )

            assertThat(
                uut.tasks.first().map { it.id }
            ).isEqualTo(
                listOf(
                    Group.Today.name,
                    Group.Tomorrow.name,
                    testTask.id.toString()
                )
            )
        }
}