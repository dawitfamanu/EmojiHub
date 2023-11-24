package com.goliath.emojihub.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.goliath.emojihub.NavigationDestination
import com.goliath.emojihub.R
import com.goliath.emojihub.viewmodels.EmojiViewModel
import com.goliath.emojihub.viewmodels.PostViewModel
import com.goliath.emojihub.views.components.CreatedEmojiListView
import com.goliath.emojihub.views.components.CreatedPostListView
import com.goliath.emojihub.views.components.PlayEmojiView
import com.goliath.emojihub.views.components.SavedEmojiListView

@Composable
fun BottomNavigationBar(
    navController: NavHostController
) {
    NavHost(navController, startDestination = PageItem.Feed.screenRoute) {
        composable(PageItem.Feed.screenRoute) {
            FeedPage()
        }

        composable(PageItem.Emoji.screenRoute) {
            EmojiPage()
        }

        composable(PageItem.Profile.screenRoute) {
            ProfilePage()
        }

        composable(NavigationDestination.TransformVideo) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(PageItem.Emoji.screenRoute)
            }
            val emojiViewModel = hiltViewModel<EmojiViewModel>(parentEntry)
            TransformVideoPage(emojiViewModel)
        }

        composable(NavigationDestination.PlayEmojiVideo) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(PageItem.Emoji.screenRoute)
            }
            val emojiViewModel = hiltViewModel<EmojiViewModel>(parentEntry)
            PlayEmojiView(emojiViewModel)
        }

        composable(NavigationDestination.CreatePost) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(PageItem.Feed.screenRoute)
            }
            val postViewModel = hiltViewModel<PostViewModel>(parentEntry)
            CreatePostPage(postViewModel)
        }

        composable(NavigationDestination.MyPostList) {
            CreatedPostListView()
        }

        composable(NavigationDestination.MyEmojiList) {
            CreatedEmojiListView()
        }

        composable(NavigationDestination.MySavedEmojiList) {
            SavedEmojiListView()
        }
    }
}

sealed class PageItem(
    val icon: Int, val screenRoute: String
) {
    object Feed: PageItem(R.drawable.round_home_24, "FEED")
    object Emoji: PageItem(R.drawable.round_add_reaction_24, "EMOJI")
    object Profile: PageItem(R.drawable.round_settings_24, "PROFILE")
}

val pageItemList = listOf(
    PageItem.Feed,
    PageItem.Emoji,
    PageItem.Profile
)