package com.github.se.orator.ui.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq

class NavigationTest {

  private lateinit var navHostController: NavHostController
  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp(): Unit {
    navHostController = mock(NavHostController::class.java)
    navigationActions = NavigationActions(navHostController)
  }

  @Test
  fun navigateToDestinationWorks(): Unit {
    navigationActions.navigateTo(TopLevelDestinations.FRIENDS)
    verify(navHostController)
        .navigate(eq(TopLevelDestinations.FRIENDS.route), any<NavOptionsBuilder.() -> Unit>())
  }

  @Test
  fun navigateToDestinationClearsBackStack(): Unit {
    navigationActions.navigateTo(TopLevelDestinations.FRIENDS)
    verify(navHostController)
        .navigate(eq(TopLevelDestinations.FRIENDS.route), any<NavOptionsBuilder.() -> Unit>())
  }

  @Test
  fun goBackWorks(): Unit {
    navigationActions.goBack()
    verify(navHostController).popBackStack()
  }

  @Test
  fun getCurrentRouteReturnsCorrectRoute(): Unit {
    val argumentCaptor = argumentCaptor<String>()

    val dest: NavDestination = NavDestination("friends")

    navigationActions.navigateTo(TopLevelDestinations.FRIENDS)
    verify(navHostController)
        .navigate(argumentCaptor.capture(), any<NavOptionsBuilder.() -> Unit>())
    dest.route = argumentCaptor.firstValue

    `when`(navHostController.currentDestination).thenReturn(dest)

    assert(navigationActions.currentRoute() == TopLevelDestinations.FRIENDS.route)
  }
}
