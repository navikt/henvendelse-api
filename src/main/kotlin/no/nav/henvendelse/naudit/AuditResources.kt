package no.nav.henvendelse.naudit

class AuditResources {
    class Henvendelse {
        companion object {
            val Henvendelse = Audit.AuditResource("henvendelse")
            val Samtalereferat = Audit.AuditResource("henvendelse.samtalereferat")


            val FerdigstillUtenSvar = Audit.AuditResource("henvendelse.ferdigstillutensvar")
            val OppdaterKontorsperre = Audit.AuditResource("henvendelse.oppdaterkontorsperre")
            val OppdaterTilKassering = Audit.AuditResource("henvendelse.oppdatertilkassering")
            val OppdaterTemagruppe = Audit.AuditResource("henvendelse.oppdatertemagruppe")
            val KnyttTilSak = Audit.AuditResource("henvendelse.knytttilsak")
        }
    }
}
