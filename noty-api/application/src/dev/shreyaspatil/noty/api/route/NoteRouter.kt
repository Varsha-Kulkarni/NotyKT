/*
 * Copyright 2020 Shreyas Patil
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

package dev.shreyaspatil.noty.api.route

import dagger.Lazy
import dev.shreyaspatil.noty.api.auth.principal.UserPrincipal
import dev.shreyaspatil.noty.api.controller.NotesController
import dev.shreyaspatil.noty.api.exception.BadRequestException
import dev.shreyaspatil.noty.api.exception.FailureMessages
import dev.shreyaspatil.noty.api.exception.UnauthorizedActivityException
import dev.shreyaspatil.noty.api.model.request.NoteRequest
import dev.shreyaspatil.noty.api.model.request.PinRequest
import dev.shreyaspatil.noty.api.model.response.generateHttpResponse
import dev.shreyaspatil.noty.api.plugin.controllers
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*

@KtorExperimentalAPI
fun Route.NoteApi(notesController: Lazy<NotesController> = controllers.notesController()) {

    authenticate {
        get("/notes") {
            val principal = call.principal<UserPrincipal>()
                ?: throw UnauthorizedActivityException(FailureMessages.MESSAGE_ACCESS_DENIED)

            val notesResponse = notesController.get().getNotesByUser(principal.user)
            val response = generateHttpResponse(notesResponse)

            call.respond(response.code, response.body)
        }

        route("/note/") {

            post("/new") {
                val noteRequest = runCatching { call.receive<NoteRequest>() }.getOrElse {
                    throw BadRequestException(FailureMessages.MESSAGE_MISSING_NOTE_DETAILS)
                }

                val principal = call.principal<UserPrincipal>()
                    ?: throw UnauthorizedActivityException(FailureMessages.MESSAGE_ACCESS_DENIED)

                val noteResponse = notesController.get().addNote(principal.user, noteRequest)
                val response = generateHttpResponse(noteResponse)

                call.respond(response.code, response.body)
            }

            put("/{id}") {
                val noteId = call.parameters["id"] ?: return@put
                val noteRequest = runCatching { call.receive<NoteRequest>() }.getOrElse {
                    throw BadRequestException(FailureMessages.MESSAGE_MISSING_NOTE_DETAILS)
                }

                val principal = call.principal<UserPrincipal>()
                    ?: throw UnauthorizedActivityException(FailureMessages.MESSAGE_ACCESS_DENIED)

                val noteResponse = notesController.get().updateNote(principal.user, noteId, noteRequest)
                val response = generateHttpResponse(noteResponse)

                call.respond(response.code, response.body)
            }

            delete("/{id}") {
                val noteId = call.parameters["id"] ?: return@delete
                val principal = call.principal<UserPrincipal>()
                    ?: throw UnauthorizedActivityException(FailureMessages.MESSAGE_ACCESS_DENIED)

                val noteResponse = notesController.get().deleteNote(principal.user, noteId)
                val response = generateHttpResponse(noteResponse)

                call.respond(response.code, response.body)
            }

            put("/{id}/pin") {
                val noteId = call.parameters["id"] ?: return@put
                val pinRequest = runCatching { call.receive<PinRequest>() }.getOrElse {
                    throw BadRequestException(FailureMessages.MESSAGE_MISSING_PIN_DETAILS)
                }

                val principal = call.principal<UserPrincipal>()
                    ?: throw UnauthorizedActivityException(FailureMessages.MESSAGE_ACCESS_DENIED)

                val noteResponse = notesController.get().pinNote(principal.user, noteId, pinRequest)
                val response = generateHttpResponse(noteResponse)

                call.respond(response.code, response.body)
            }
        }
    }
}