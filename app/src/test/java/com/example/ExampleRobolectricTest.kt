package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DeshExamDatabase
import com.example.data.repository.DeshExamRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("WBBSE Prep", appName)
  }

  @Test
  fun `launch main activity to catch startup crash`() {
    androidx.test.core.app.ActivityScenario.launch(MainActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertTrue(activity != null)
      }
    }
    org.robolectric.shadows.ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
  }

  @Test
  fun `leaderboard showcases mock test scores and boss victories`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val testScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
    val db = DeshExamDatabase.getDatabase(context, testScope)
    val repository = DeshExamRepository(db.deshExamDao())

    val leaderboard = repository.getLeaderboard()
    assertTrue("Leaderboard should have students", leaderboard.isNotEmpty())

    // Check top mock test performer
    val topByMock = leaderboard.maxByOrNull { it.mockScore }
    assertEquals("অনন্যা মুখোপাধ্যায়", topByMock?.name)
    assertEquals(89, topByMock?.mockScore)

    // Check top boss battle victor
    val topByBoss = leaderboard.maxByOrNull { it.bossVictories }
    assertEquals(18, topByBoss?.bossVictories)

    // Check that current user is identified
    val currentUser = leaderboard.find { it.isCurrentUser }
    assertEquals("সৌরভ দাস (আপনি)", currentUser?.name)
    assertTrue(currentUser?.mockScore ?: 0 > 70)
  }

  @Test
  fun `student profile has active learning streak and streak shields`() {
    val profile = com.example.data.model.StudentProfile(
      currentStreak = 7,
      bestStreak = 14,
      streakShields = 2,
      todayStreakGoalMet = true
    )
    assertEquals(7, profile.currentStreak)
    assertEquals(14, profile.bestStreak)
    assertEquals(2, profile.streakShields)
    assertTrue(profile.todayStreakGoalMet)
    assertTrue(profile.streakMultiplier >= 1.5)
  }
}
