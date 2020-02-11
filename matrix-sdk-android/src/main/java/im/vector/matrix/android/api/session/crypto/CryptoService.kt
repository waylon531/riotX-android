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

package im.vector.matrix.android.api.session.crypto

import android.content.Context
import androidx.lifecycle.LiveData
import im.vector.matrix.android.api.MatrixCallback
import im.vector.matrix.android.api.listeners.ProgressListener
import im.vector.matrix.android.api.session.crypto.crosssigning.CrossSigningService
import im.vector.matrix.android.api.session.crypto.keysbackup.KeysBackupService
import im.vector.matrix.android.api.session.crypto.keyshare.RoomKeysRequestListener
import im.vector.matrix.android.api.session.crypto.sas.VerificationService
import im.vector.matrix.android.api.session.events.model.Content
import im.vector.matrix.android.api.session.events.model.Event
import im.vector.matrix.android.internal.crypto.MXEventDecryptionResult
import im.vector.matrix.android.internal.crypto.NewSessionListener
import im.vector.matrix.android.internal.crypto.crosssigning.DeviceTrustLevel
import im.vector.matrix.android.internal.crypto.model.CryptoDeviceInfo
import im.vector.matrix.android.internal.crypto.model.ImportRoomKeysResult
import im.vector.matrix.android.internal.crypto.model.MXDeviceInfo
import im.vector.matrix.android.internal.crypto.model.MXEncryptEventContentResult
import im.vector.matrix.android.internal.crypto.model.MXUsersDevicesMap
import im.vector.matrix.android.internal.crypto.model.rest.DeviceInfo
import im.vector.matrix.android.internal.crypto.model.rest.DevicesListResponse
import im.vector.matrix.android.internal.crypto.model.rest.RoomKeyRequestBody

interface CryptoService {

    suspend fun setDeviceName(deviceId: String, deviceName: String)

    suspend fun deleteDevice(deviceId: String)

    suspend fun deleteDeviceWithUserPassword(deviceId: String, authSession: String?, password: String)

    fun getCryptoVersion(context: Context, longFormat: Boolean): String

    fun isCryptoEnabled(): Boolean

    fun getVerificationService(): VerificationService

    fun getCrossSigningService(): CrossSigningService

    fun getKeysBackupService(): KeysBackupService

    fun isRoomBlacklistUnverifiedDevices(roomId: String?): Boolean

    fun setWarnOnUnknownDevices(warn: Boolean)

    fun setDeviceVerification(trustLevel: DeviceTrustLevel, userId: String, deviceId: String)

    fun getUserDevices(userId: String): MutableList<CryptoDeviceInfo>

    suspend fun setDevicesKnown(devices: List<MXDeviceInfo>)

    fun deviceWithIdentityKey(senderKey: String, algorithm: String): CryptoDeviceInfo?

    fun getMyDevice(): CryptoDeviceInfo

    fun getGlobalBlacklistUnverifiedDevices(): Boolean

    fun setGlobalBlacklistUnverifiedDevices(block: Boolean)

    fun setRoomUnBlacklistUnverifiedDevices(roomId: String)

    fun getDeviceTrackingStatus(userId: String): Int

    suspend fun importRoomKeys(roomKeysAsArray: ByteArray, password: String, progressListener: ProgressListener?): ImportRoomKeysResult

    suspend fun exportRoomKeys(password: String): ByteArray

    fun setRoomBlacklistUnverifiedDevices(roomId: String)

    fun getDeviceInfo(userId: String, deviceId: String?): CryptoDeviceInfo?

    fun reRequestRoomKeyForEvent(event: Event)

    fun cancelRoomKeyRequest(requestBody: RoomKeyRequestBody)

    fun addRoomKeysRequestListener(listener: RoomKeysRequestListener)

    fun removeRoomKeysRequestListener(listener: RoomKeysRequestListener)

    suspend fun getDevicesList(): List<DeviceInfo>

    suspend fun getDeviceInfo(deviceId: String): DeviceInfo

    fun inboundGroupSessionsCount(onlyBackedUp: Boolean): Int

    fun isRoomEncrypted(roomId: String): Boolean

    suspend fun encryptEventContent(eventContent: Content,
                            eventType: String,
                            roomId: String): MXEncryptEventContentResult

    @Throws(MXCryptoError::class)
    fun decryptEvent(event: Event, timeline: String): MXEventDecryptionResult

    suspend fun decryptEventAsync(event: Event, timeline: String): MXEventDecryptionResult

    fun getEncryptionAlgorithm(roomId: String): String?

    fun shouldEncryptForInvitedMembers(roomId: String): Boolean

    suspend fun downloadKeys(userIds: List<String>, forceDownload: Boolean): MXUsersDevicesMap<CryptoDeviceInfo>

    fun getCryptoDeviceInfo(userId: String): List<CryptoDeviceInfo>

    fun getLiveCryptoDeviceInfo(): LiveData<List<CryptoDeviceInfo>>

    fun getLiveCryptoDeviceInfo(userId: String): LiveData<List<CryptoDeviceInfo>>

    fun getLiveCryptoDeviceInfo(userIds: List<String>): LiveData<List<CryptoDeviceInfo>>

    fun addNewSessionListener(newSessionListener: NewSessionListener)

    fun removeSessionListener(listener: NewSessionListener)
}
