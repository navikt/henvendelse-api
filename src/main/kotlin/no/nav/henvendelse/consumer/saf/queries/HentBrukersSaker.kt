package no.nav.henvendelse.consumer.saf.queries

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue
import no.nav.henvendelse.consumer.GraphQLRequest
import no.nav.henvendelse.consumer.GraphQLResult
import no.nav.henvendelse.consumer.GraphQLVariables
import no.nav.henvendelse.consumer.saf.SafService

class HentBrukersSaker(override val variables: Variables) :
    GraphQLRequest<HentBrukersSaker.Variables, HentBrukersSaker.Result> {
    override val query: String = SafService.lastQueryFraFil("hentBrukersSaker")
    override val expectedReturnType: Class<Result> = Result::class.java

    data class Variables(val brukerId: BrukerIdInput) : GraphQLVariables
    data class BrukerIdInput(
        val id: String,
        val type: BrukerIdType
    )
    data class Result(val saker: List<Sak?>) : GraphQLResult
    data class Sak(val arkivsaksnummer: String?, val tema: Tema?)

    enum class BrukerIdType {
        AKTOERID,

        FNR,

        ORGNR,

        /**
         * This is a default enum value that will be used when attempting to deserialize unknown value.
         */
        @JsonEnumDefaultValue
        __UNKNOWN_VALUE
    }

    enum class Tema {
        AAP,

        AAR,

        AGR,

        BAR,

        BID,

        BIL,

        DAG,

        ENF,

        ERS,

        FAR,

        FEI,

        FOR,

        FOS,

        FRI,

        FUL,

        GEN,

        GRA,

        GRU,

        HEL,

        HJE,

        IAR,

        IND,

        KON,

        KTR,

        MED,

        MOB,

        OMS,

        OPA,

        OPP,

        PEN,

        PER,

        REH,

        REK,

        RPO,

        RVE,

        SAA,

        SAK,

        SAP,

        SER,

        SIK,

        STO,

        SUP,

        SYK,

        SYM,

        TIL,

        TRK,

        TRY,

        TSO,

        TSR,

        UFM,

        UFO,

        UKJ,

        VEN,

        YRA,

        YRK,

        /**
         * This is a default enum value that will be used when attempting to deserialize unknown value.
         */
        @JsonEnumDefaultValue
        __UNKNOWN_VALUE
    }
}
