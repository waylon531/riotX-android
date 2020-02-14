/*
 * Copyright 2019 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.riotx.features.workers.signout

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import im.vector.matrix.android.api.session.Session
import im.vector.matrix.android.api.session.crypto.keysbackup.KeysBackupState
import im.vector.matrix.android.api.session.crypto.keysbackup.KeysBackupStateListener
import javax.inject.Inject

class SignOutViewModel @Inject constructor(private val session: Session) : ViewModel(), KeysBackupStateListener {
    // Keys exported manually
    var keysExportedToFile = MutableLiveData<Boolean>()

    var keysBackupState = MutableLiveData<KeysBackupState>()

    init {
        session.getCryptoService().getKeysBackupService().addListener(this)

        keysBackupState.value = session.getCryptoService().getKeysBackupService().state
    }

    /**
     * Safe way to get the current KeysBackup version
     */
    fun getCurrentBackupVersion(): String {
        return session.getCryptoService().getKeysBackupService().currentBackupVersion ?: ""
    }

    /**
     * Safe way to get the number of keys to backup
     */
    fun getNumberOfKeysToBackup(): Int {
        return session.getCryptoService().inboundGroupSessionsCount(false)
    }

    /**
     * Safe way to tell if there are more keys on the server
     */
    fun canRestoreKeys(): Boolean {
        return session.getCryptoService().getKeysBackupService().canRestoreKeys()
    }

    override fun onCleared() {
        super.onCleared()

        session.getCryptoService().getKeysBackupService().removeListener(this)
    }

    override fun onStateChange(newState: KeysBackupState) {
        keysBackupState.value = newState
    }

    fun refreshRemoteStateIfNeeded() {
        if (keysBackupState.value == KeysBackupState.Disabled) {
            session.getCryptoService().getKeysBackupService().checkAndStartKeysBackup()
        }
    }
}
