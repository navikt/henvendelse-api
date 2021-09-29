package no.nav.henvendelse.consumer.pdl.queries

import no.nav.henvendelse.consumer.pdl.GraphQLRequest
import no.nav.henvendelse.consumer.pdl.GraphQLResult
import no.nav.henvendelse.consumer.pdl.GraphQLVariables
import no.nav.henvendelse.consumer.pdl.PdlService

class HentAktorIder(override val variables: Variables) :
    GraphQLRequest<HentAktorIder.Variables, HentAktorIder.Result> {
    override val query: String = PdlService.lastQueryFraFil("hentAktorIder")
    override val expectedReturnType: Class<Result> = Result::class.java

    data class Variables(val ident: String) : GraphQLVariables
    data class Result(val hentIdenter: Identliste?) : GraphQLResult
    data class Identliste(val identer: List<IdentInformasjon>)
    data class IdentInformasjon(val ident: String, val historisk: Boolean)
}
