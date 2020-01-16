/*
 * Copyright 2020 New Vector Ltd
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
 *
 */

package im.vector.matrix.android.internal.auth.sqlite

import com.squareup.sqldelight.db.SqlDriver
import im.vector.matrix.android.internal.auth.realm.SessionParamsEntity
import im.vector.matrix.android.internal.db.auth.AuthDatabase
import im.vector.matrix.android.internal.db.auth.Session_params
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Inject

internal class AuthSchema @Inject constructor(@im.vector.matrix.android.internal.di.AuthDatabase private val realmConfiguration: RealmConfiguration) : SqlDriver.Schema by AuthDatabase.Schema {

    override fun create(driver: SqlDriver) {
        AuthDatabase.Schema.create(driver)
        AuthDatabase(driver).apply {
            val sessionParamsQueries = this.sessionParamsQueries
            sessionParamsQueries.transaction {
                Realm.getInstance(realmConfiguration).use {
                    it.where(SessionParamsEntity::class.java).findAll().forEach { realmSessionParams ->
                        sessionParamsQueries.insert(realmSessionParams.toSqlModel())
                    }
                }
            }
        }
    }

    private fun SessionParamsEntity.toSqlModel(): Session_params {
        return Session_params.Impl(
                sessionId,
                userId,
                credentialsJson,
                homeServerConnectionConfigJson,
                isTokenValid
        )
    }

}
