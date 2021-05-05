package no.nav.henvendelse.rest.nais

import no.nav.common.health.selftest.SelfTestUtils
import no.nav.common.health.selftest.SelftTestCheckResult
import no.nav.common.health.selftest.SelftestHtmlGenerator
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal")
class NaisController {
    @GetMapping("/isReady")
    fun isReady(): ResponseEntity<Void> = ResponseEntity.status(200).build()

    @GetMapping("/isAlive")
    fun isAlive(): ResponseEntity<Void> = ResponseEntity.status(200).build()

    @GetMapping("/selftest")
    fun selftest(): ResponseEntity<String> {
        val result = emptyList<SelftTestCheckResult>()
        return ResponseEntity
            .status(SelfTestUtils.findHttpStatusCode(result))
            .contentType(MediaType.TEXT_HTML)
            .body(SelftestHtmlGenerator.generate(result))
    }
}