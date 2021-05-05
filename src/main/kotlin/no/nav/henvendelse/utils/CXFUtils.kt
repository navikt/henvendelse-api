package no.nav.henvendelse.utils

import no.nav.common.cxf.CXFClient

inline fun <reified T> CXFClient() = CXFClient(T::class.java)
