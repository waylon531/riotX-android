/*
 * Copyright 2019 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.matrix.android.internal.session.content

import com.squareup.moshi.Moshi
import im.vector.matrix.android.api.session.content.ContentUrlResolver
import im.vector.matrix.android.internal.di.Authenticated
import im.vector.matrix.android.internal.network.ProgressRequestBody
import im.vector.matrix.android.internal.network.awaitResponse
import im.vector.matrix.android.internal.network.toFailure
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.IOException
import javax.inject.Inject

internal class FileUploader @Inject constructor(@Authenticated
                                                private val okHttpClient: OkHttpClient,
                                                private val eventBus: EventBus,
                                                contentUrlResolver: ContentUrlResolver,
                                                moshi: Moshi) {

    private val uploadUrl = contentUrlResolver.uploadUrl
    private val responseAdapter = moshi.adapter(ContentUploadResponse::class.java)

    suspend fun uploadFile(file: File,
                           filename: String?,
                           mimeType: String?,
                           progressListener: ProgressRequestBody.Listener? = null): ContentUploadResponse {
        val uploadBody = file.asRequestBody(mimeType?.toMediaTypeOrNull())
        return upload(uploadBody, filename, progressListener)
    }

    suspend fun uploadByteArray(byteArray: ByteArray,
                                filename: String?,
                                mimeType: String?,
                                progressListener: ProgressRequestBody.Listener? = null): ContentUploadResponse {
        val uploadBody = byteArray.toRequestBody(mimeType?.toMediaTypeOrNull())
        return upload(uploadBody, filename, progressListener)
    }

    private suspend fun upload(uploadBody: RequestBody, filename: String?, progressListener: ProgressRequestBody.Listener?): ContentUploadResponse {
        val urlBuilder = uploadUrl.toHttpUrlOrNull()?.newBuilder() ?: throw RuntimeException()

        val httpUrl = urlBuilder
                .addQueryParameter("filename", filename)
                .build()

        val requestBody = if (progressListener != null) ProgressRequestBody(uploadBody, progressListener) else uploadBody

        val request = Request.Builder()
                .url(httpUrl)
                .post(requestBody)
                .build()

        return okHttpClient.newCall(request).awaitResponse().use { response ->
            if (!response.isSuccessful) {
                throw response.toFailure(eventBus)
            } else {
                response.body?.source()?.let {
                    responseAdapter.fromJson(it)
                }
                        ?: throw IOException()
            }
        }
    }
}
