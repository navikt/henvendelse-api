package no.nav.henvendelse.rest.common

import java.util.*

object Verification {
    fun verifyFnr(fnr: String, lazyMessage: () -> Any) {
        var valid = true
        valid = valid && fnr.length == 11
        valid = valid && fnr.isNumerical()

        verify(valid, lazyMessage)
    }

    fun verifyEnhet(enhet: String, lazyMessage: () -> Any) {
        var valid = true
        valid = valid && enhet.length == 4
        valid = valid && enhet.isNumerical()

        verify(valid, lazyMessage)
    }

    fun verify(valid: Boolean, lazyMessage: () -> Any) {
        if (!valid) {
            throw RestInvalidDataException(lazyMessage().toString())
        }
    }

    fun <T> requireOptional(optional: Optional<T>): T {
        return optional.orElseThrow {
            RestInvalidDataException("Kunne ikke hente ut navIdent fra JWT")
        }
    }

    private fun String.isNumerical(): Boolean {
        return this.toIntOrNull() != null
    }
}
