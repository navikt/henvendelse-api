package no.nav.henvendelse.rest.sendsamtalereferat

import io.swagger.annotations.*
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
@Api(description = "APIer for oppretting av samtalereferat")
class SendSamtalereferatController(
    @Autowired val authcontext: AuthContextHolder,
    @Autowired val porttype: SendUtHenvendelsePortType
) {

    @PostMapping("/send")
    @ApiOperation(
        value = "Oppretter samtalereferat til bruker",
        notes = """
            Minimalistisk API for opprettelse av samtalereferat.
        """
    )
    @ApiImplicitParam(name = "X-Correlation-Id", paramType = "header", required = true)
    @ApiResponses(
        value = [
            ApiResponse(code = 200, message = "Returner behandlingsIden for det opprettede samtalereferatet"),
            ApiResponse(code = 401, message = "Ugyldig token"),
            ApiResponse(code = 406, message = "Validering av dataene i requesten feilet, se feilmelding i responsen."),
            ApiResponse(code = 500, message = "Ukjent feil, sannsynligvis fra henvendelse"),
        ]
    )
    fun sendSamtaleReferat(@RequestBody request: SendSamtalereferatRequest): String {
        return withAudit(describe(CREATE, Samtalereferat, FNR to request.fnr, ENHET to request.enhet)) {
            val navIdent = requireOptional(authcontext.navIdent) {
                "Kunne ikke hente ut navIdent fra JWT"
            }
            verifyFnr(request.fnr)
            verifyEnhet(request.enhet)

            porttype.sendUtHenvendelse(request.tilRequest(navIdent)).behandlingsId
        }
    }
}
