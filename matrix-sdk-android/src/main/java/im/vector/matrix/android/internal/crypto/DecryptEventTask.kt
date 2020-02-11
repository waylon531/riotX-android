/*
 * Copyright (c) 2020 New Vector Ltd
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

package im.vector.matrix.android.internal.crypto

import im.vector.matrix.android.api.session.crypto.MXCryptoError
import im.vector.matrix.android.api.session.events.model.Event
import timber.log.Timber
import javax.inject.Inject

internal class DecryptEventTask @Inject constructor(private val roomDecryptorProvider: RoomDecryptorProvider) {

    suspend fun decryptEvent(event: Event, timeline: String): MXEventDecryptionResult {
        val eventContent = event.content
        if (eventContent == null) {
            Timber.e("## decryptEvent : empty event content")
            throw MXCryptoError.Base(MXCryptoError.ErrorType.BAD_ENCRYPTED_MESSAGE, MXCryptoError.BAD_ENCRYPTED_MESSAGE_REASON)
        } else {
            val algorithm = eventContent["algorithm"]?.toString()
            val alg = roomDecryptorProvider.getOrCreateRoomDecryptor(event.roomId, algorithm)
            if (alg == null) {
                val reason = String.format(MXCryptoError.UNABLE_TO_DECRYPT_REASON, event.eventId, algorithm)
                Timber.e("## decryptEvent() : $reason")
                throw MXCryptoError.Base(MXCryptoError.ErrorType.UNABLE_TO_DECRYPT, reason)
            } else {
                return alg.decryptEvent(event, timeline)
            }
        }
    }
}
