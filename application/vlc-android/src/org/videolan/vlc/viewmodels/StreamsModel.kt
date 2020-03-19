/*******************************************************************************
 *  StreamsModel.kt
 * ****************************************************************************
 * Copyright © 2018-2019 VLC authors and VideoLAN
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 ******************************************************************************/

package org.videolan.vlc.viewmodels

import android.content.Context
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.videolan.medialibrary.interfaces.media.MediaWrapper
import org.videolan.resources.util.getFromMl
import org.videolan.tools.CoroutineContextProvider


class StreamsModel(context: Context, coroutineContextProvider: CoroutineContextProvider = CoroutineContextProvider()) : MedialibraryModel<MediaWrapper>(context, coroutineContextProvider) {
    var deletingMedia: MediaWrapper? = null
    val observableSearchText = ObservableField<String>()

    init {
        if (medialibrary.isStarted) refresh()
    }


    override suspend fun updateList() {
        dataset.value = withContext(coroutineContextProvider.Default) { medialibrary.lastStreamsPlayed().toMutableList().also { deletingMedia?.let { remove(it) } } }
    }

    fun rename(position: Int, name: String) {
        val media = dataset.get(position)
        viewModelScope.launch {
            withContext(Dispatchers.IO) { media.rename(name) }
            refresh()
        }
    }

    fun delete() {
        deletingMedia?.let { media ->
            viewModelScope.launch {
                context.getFromMl { removeExternalMedia(media.id) }
                refresh()
            }
        }
    }
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return StreamsModel(context.applicationContext) as T
        }
    }
}