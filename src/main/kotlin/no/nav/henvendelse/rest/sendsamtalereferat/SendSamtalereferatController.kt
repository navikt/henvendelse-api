package no.nav.henvendelse.rest.sendsamtalereferat

import io.swagger.annotations.ApiImplicitParam
import no.nav.common.auth.context.AuthContextHolder
import no.nav.henvendelse.naudit.Audit.Action.CREATE
import no.nav.henvendelse.naudit.Audit.Companion.describe
import no.nav.henvendelse.naudit.Audit.Companion.withAudit
import no.nav.henvendelse.naudit.AuditIdentifier.ENHET
import no.nav.henvendelse.naudit.AuditIdentifier.FNR
import no.nav.henvendelse.naudit.AuditResources.Henvendelse.Companion.Samtalereferat
import no.nav.henvendelse.rest.common.Verification.requireOptional
import no.nav.henvendelse.rest.common.Verification.verifyEnhet
import no.nav.henvendelse.rest.common.Verification.verifyFnr
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.senduthenvendelse.SendUtHenvendelsePortType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/sendsamtalereferat")
class SendSamtalereferatController {
    @Autowired
    lateinit var authcontext: AuthContextHolder

    @Autowired
    lateinit var porttype: SendUtHenvendelsePortType

    @PostMapping("/send")
    @ApiImplicitParam(name = "X-Correlation-Id", paramType = "header", required = true)
    fun sendSamtaleReferat(@RequestBody request: SendSamtalereferatRequest): String {
        return withAudit(describe(CREATE, Samtalereferat, FNR to request.fnr, ENHET to request.enhet)) {
            val navIdent = requireOptional(authcontext.navIdent)
            verifyFnr(request.fnr) { "Fødselsnummer ikke gyldig: ${request.fnr}" }
            verifyEnhet(request.enhet) { "EnhetId ikke gyldig: ${request.enhet}" }

            porttype.sendUtHenvendelse(request.tilRequest(navIdent)).behandlingsId
        }
    }
}
