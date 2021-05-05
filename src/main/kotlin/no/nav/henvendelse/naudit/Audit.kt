package no.nav.henvendelse.naudit

import no.nav.common.auth.context.AuthContextHolderThreadLocal
import no.nav.henvendelse.naudit.AuditIdentifier.DENY_REASON
import no.nav.henvendelse.naudit.AuditIdentifier.FAIL_REASON
import org.slf4j.LoggerFactory

private val tjenestekallLogg = LoggerFactory.getLogger("SecureLog")
class Audit {
    open class AuditResource(val resource: String)
    enum class Action {
        CREATE, READ, UPDATE, DELETE
    }

    interface AuditDescriptor<T> {
        fun log(subject: String?, resource: T?)
        fun denied(subject: String?, reason: String)
        fun failed(subject: String?, exception: Throwable)

        fun Throwable.getFailureReason(): String = this.message ?: this.toString()
    }

    internal class ParameterizedDescriptor<T>(
        private val action: Action,
        private val resourceType: AuditResource,
        private val extractIdentifiers: (T?) -> List<Pair<AuditIdentifier, String?>>
    ) : AuditDescriptor<T> {
        override fun log(subject: String?, resource: T?) {
            val identifiers = extractIdentifiers(resource).toTypedArray()
            logInternal(subject, action, resourceType, identifiers)
        }

        override fun denied(subject: String?, reason: String) {
            logInternal(subject, action, resourceType, arrayOf(DENY_REASON to reason))
        }

        override fun failed(subject: String?, exception: Throwable) {
            logInternal(subject, action, resourceType, arrayOf(FAIL_REASON to exception.getFailureReason()))
        }
    }

    internal class NoopDescriptor<T> : AuditDescriptor<T> {
        override fun log(subject: String?, resource: T?) {}
        override fun denied(subject: String?, reason: String) {}
        override fun failed(subject: String?, exception: Throwable) {}
    }

    internal class Descriptor(
        private val action: Action,
        private val resourceType: AuditResource,
        private val identifiers: Array<out Pair<AuditIdentifier, String?>>
    ) : AuditDescriptor<Any> {
        override fun log(subject: String?, resource: Any?) {
            logInternal(subject, action, resourceType, identifiers)
        }

        override fun denied(subject: String?, reason: String) {
            logInternal(subject, action, resourceType, arrayOf(DENY_REASON to reason))
        }

        override fun failed(subject: String?, exception: Throwable) {
            logInternal(subject, action, resourceType, arrayOf(FAIL_REASON to exception.getFailureReason()))
        }
    }

    companion object {
        val skipAuditLog: AuditDescriptor<Any> = NoopDescriptor()

        @JvmStatic
        fun <T> skipAuditLog(): AuditDescriptor<T> = NoopDescriptor()

        @JvmStatic
        fun describe(action: Action, resourceType: AuditResource, vararg identifiers: Pair<AuditIdentifier, String?>): AuditDescriptor<Any> {
            return Descriptor(action, resourceType, identifiers)
        }

        @JvmStatic
        fun <T> describe(action: Action, resourceType: AuditResource, extractIdentifiers: (T?) -> List<Pair<AuditIdentifier, String?>>): AuditDescriptor<T> {
            return ParameterizedDescriptor(action, resourceType, extractIdentifiers)
        }

        @JvmStatic
        fun <S> withAudit(descriptor: AuditDescriptor<in S>, supplier: () -> S): S {
            val authcontext = AuthContextHolderThreadLocal.instance()
            val subject = authcontext.navIdent.map { it.get() }.orElse(null)
            return runCatching(supplier)
                .onSuccess { descriptor.log(subject, it) }
                .onFailure { descriptor.failed(subject, it) }
                .getOrThrow()
        }

        private fun logInternal(subject: String?, action: Action, resourceType: AuditResource, identifiers: Array<out Pair<AuditIdentifier, String?>>) {
            val logline = listOfNotNull(
                "action='$action'",
                subject?.let { "subject='$it'" },
                "resource='${resourceType.resource}'",
                *identifiers
                    .map { "${it.first}='${it.second ?: "-"}'" }
                    .toTypedArray()
            )
                .joinToString(" ")

            tjenestekallLogg.info(logline)
        }
    }
}