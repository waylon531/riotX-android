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

import im.vector.matrix.android.api.auth.data.HomeServerConnectionConfig
import im.vector.matrix.android.api.session.content.ContentUrlResolver
import im.vector.matrix.android.internal.network.NetworkConstants
import javax.inject.Inject

private const val MATRIX_CONTENT_URI_SCHEME = "mxc://"

internal class DefaultContentUrlResolver @Inject constructor(homeServerConnectionConfig: HomeServerConnectionConfig) : ContentUrlResolver {

    private val baseUrl = homeServerConnectionConfig.homeServerUri.toString()
    private val sep = if (baseUrl.endsWith("/")) "" else "/"

    override val uploadUrl = baseUrl + sep + NetworkConstants.URI_API_MEDIA_PREFIX_PATH_R0 + "upload"

    override fun resolveFullSize(contentUrl: String?): String? {
        if (contentUrl?.isValidMatrixContentUrl() == true) {
            val prefix = NetworkConstants.URI_API_MEDIA_PREFIX_PATH_R0 + "download/"
            return resolve(contentUrl, prefix)
        }
        // do not allow non-mxc content URLs
        return null
    }

    override fun resolveThumbnail(contentUrl: String?, width: Int, height: Int, method: ContentUrlResolver.ThumbnailMethod): String? {
        if (contentUrl?.isValidMatrixContentUrl() == true) {
            val prefix = NetworkConstants.URI_API_MEDIA_PREFIX_PATH_R0 + "thumbnail/"
            val params = "?width=$width&height=$height&method=${method.value}"
            return resolve(contentUrl, prefix, params)
        }
        // do not allow non-mxc content URLs
        return null
    }

    private fun resolve(contentUrl: String,
                        prefix: String,
                        params: String? = null): String? {
        var serverAndMediaId = contentUrl.removePrefix(MATRIX_CONTENT_URI_SCHEME)
        val fragmentOffset = serverAndMediaId.indexOf("#")
        var fragment = ""
        if (fragmentOffset >= 0) {
            fragment = serverAndMediaId.substring(fragmentOffset)
            serverAndMediaId = serverAndMediaId.substring(0, fragmentOffset)
        }

        return baseUrl + sep + prefix + serverAndMediaId + (params ?: "") + fragment
    }

    private fun String.isValidMatrixContentUrl(): Boolean {
        return startsWith(MATRIX_CONTENT_URI_SCHEME)
    }
}
