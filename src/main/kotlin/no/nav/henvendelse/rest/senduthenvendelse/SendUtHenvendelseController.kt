package no.nav.henvendelse.rest.senduthenvendelse

import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.senduthenvendelse.SendUtHenvendelsePortType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/senduthenvendelse")
class SendUtHenvendelseController : SendUtHenvendelseApi {
    @Autowired
    lateinit var porttype: SendUtHenvendelsePortType

    @PostMapping("/oppretthenvendelse")
    override fun opprettHenvendelse(@RequestBody request: OpprettHenvendelseRequest): String {
        return porttype.opprettHenvendelse(
            request.type.name,
            request.fodselsnummer,
            request.behandlingskjedeId
        )
    }

    @PostMapping("/ferdigstillhenvendelse")
    override fun ferdigstillHenvendelse(@RequestBody request: FerdigstillHenvendelseRequest): FerdigstillHenvendelseResponse {
        return porttype.ferdigstillHenvendelse(request.toWS()).fromWS()
    }

    @PostMapping("/senduthenvendelse")
    override fun sendUtHenvendelse(@RequestBody request: SendUtHenvendelseRequest): SendUtHenvendelseResponse {
        return porttype.sendUtHenvendelse(request.toWS()).fromWS()
    }

    @GetMapping("/ping")
    override fun ping() {
        return porttype.ping()
    }
}
