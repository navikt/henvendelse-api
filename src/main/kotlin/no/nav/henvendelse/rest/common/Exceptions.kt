package no.nav.henvendelse.rest.common

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class RestOperationNotSupportedException(message: String) : ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, message)
class RestInvalidDataException(message: String) : ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, message)
