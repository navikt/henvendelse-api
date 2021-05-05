package no.nav.henvendelse.rest.sendsamtalereferat

import io.swagger.annotations.ApiImplicitParam
import no.nav.common.auth.context.AuthContextHolder
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
    lateinit var porttype: SendUtHenvendelsePortType
    @Autowired
    lateinit var authcontext: AuthContextHolder

    @PostMapping("/send")
    @ApiImplicitParam(name = "X-Correlation-Id", paramType = "header", required = true)
    fun sendSamtaleReferat(@RequestBody request: SendSamtalereferatRequest): String {
        verifyFnr(request.fnr) { "Fødselsnummer ikke gyldig: ${request.fnr}" }
        verifyEnhet(request.enhet) { "EnhetId ikke gyldig: ${request.enhet}" }

        val navIdent = requireOptional(authcontext.navIdent)
        return porttype.sendUtHenvendelse(request.tilRequest(navIdent)).behandlingsId
    }
}
