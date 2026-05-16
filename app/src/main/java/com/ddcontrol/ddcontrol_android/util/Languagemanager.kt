package com.ddcontrol.ddcontrol_android.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object LanguageManager {

    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANG   = "lang"

    private val _lang = MutableStateFlow("es")
    val lang: StateFlow<String> = _lang

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _lang.value = prefs.getString(KEY_LANG, "es") ?: "es"
    }

    fun setLang(context: Context, newLang: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, newLang).apply()
        _lang.value = newLang

    }

    fun t(key: String): String {
        val translations = if (_lang.value == "en") EN else ES
        return translations[key] ?: key
    }

    private val ES = mapOf(
        "nav.inicio"                 to "Inicio",
        "nav.fichajes"               to "Fichajes",
        "nav.solicitudes"            to "Solicitudes",
        "nav.incidencias"            to "Incidencias",

        "dashboard.hola"             to "Hola,",
        "dashboard.sin_turno"        to "Sin turno asignado",
        "dashboard.mi_horario"       to "Mi horario",
        "dashboard.mes"              to "Mes",
        "dashboard.semana"           to "Semana",
        "dashboard.estado_actual"    to "ESTADO ACTUAL",
        "dashboard.fichajes_hoy"     to "Fichajes hoy",
        "dashboard.solicitudes"      to "Solicitudes",
        "dashboard.incidencias"      to "Incidencias",
        "dashboard.fichado"          to "Fichado",
        "dashboard.pendiente"        to "Pendiente",
        "dashboard.no_laboral"       to "No laboral",
        "dashboard.sin_fichar"       to "Sin fichar",
        "dashboard.hoy"              to "Hoy",
        "dashboard.dia_laboral"      to "Día laboral",
        "dashboard.estado_dentro"    to "Dentro - entrada registrada",
        "dashboard.estado_fuera"     to "Fuera - salida registrada",
        "dashboard.estado_pausa"     to "En pausa",
        "dashboard.estado_pausa_fin" to "Dentro - pausa finalizada",
        "dashboard.estado_sin_reg"   to "Sin registros hoy",
        "dashboard.cerrar_sesion"    to "Cerrar sesión",

        "fichajes.titulo"            to "FICHAJES DE HOY",
        "fichajes.sin_fichajes"      to "Sin fichajes hoy",
        "fichajes.registrar_entrada" to "Registrar entrada",
        "fichajes.registrar_salida"  to "Registrar salida",
        "fichajes.en_pausa"          to "En pausa - reanuda primero",
        "fichajes.iniciar_pausa"     to "Iniciar pausa",
        "fichajes.finalizar_pausa"   to "Finalizar pausa",
        "fichajes.sin_sede"          to "No tienes sede asignada",
        "fichajes.sin_gps"           to "Sin ubicación GPS",
        "fichajes.dentro"            to "Dentro",
        "fichajes.fuera"             to "Fuera",
        "fichajes.entrada"           to "Entrada",
        "fichajes.salida"            to "Salida",
        "fichajes.aviso"             to "Aviso",
        "fichajes.fichar_igual"      to "Fichar igualmente",
        "fichajes.cancelar"          to "Cancelar",
        "fichajes.aviso_no_laboral"  to "Hoy no es tu día de trabajo según tu turno. ¿Seguro que quieres fichar?",
        "fichajes.fuera_radio"       to "Fichaje fuera de radio",
        "fichajes.fuera_radio_msg"   to "Has fichado fuera del radio permitido de tu sede.",

        "solicitudes.titulo"         to "MIS SOLICITUDES",
        "solicitudes.nueva"          to " Nueva solicitud",
        "solicitudes.sin_datos"      to "No tienes solicitudes registradas",
        "solicitudes.tipo"           to "Tipo",
        "solicitudes.fecha_inicio"   to "Fecha inicio",
        "solicitudes.fecha_fin"      to "Fecha fin",
        "solicitudes.selecciona"     to "Selecciona fecha",
        "solicitudes.motivo"         to "Motivo (opcional)",
        "solicitudes.enviar"         to "Enviar",
        "solicitudes.cancelar"       to "Cancelar",
        "solicitudes.nueva_titulo"   to "Nueva solicitud",
        "solicitudes.sel_inicio"     to "Selecciona primero la fecha de inicio",
        "solicitudes.sel_fecha_ini"  to "Selecciona la fecha de inicio",
        "solicitudes.sel_fecha_fin"  to "Selecciona la fecha de fin",
        "solicitudes.fecha_invalida" to "La fecha de fin no puede ser anterior a la de inicio",
        "solicitudes.aceptar"        to "Aceptar",
        "solicitudes.respuesta"      to "Respuesta: ",
        "solicitudes.pendiente"      to "Pendiente",
        "solicitudes.aprobada"       to "Aprobada",
        "solicitudes.denegada"       to "Denegada",

        "incidencias.titulo"         to "MIS INCIDENCIAS",
        "incidencias.nueva"          to "Nueva incidencia",
        "incidencias.sin_datos"      to "No tienes incidencias registradas",
        "incidencias.tipo"           to "Tipo",
        "incidencias.descripcion"    to "Descripción (opcional)",
        "incidencias.enviar"         to "Enviar",
        "incidencias.cancelar"       to "Cancelar",
        "incidencias.nueva_titulo"   to "Nueva incidencia",
        "incidencias.resuelta"       to "Resuelta",
        "incidencias.abierta"        to "Abierta",

        "login.titulo"               to "D&D Control",
        "login.subtitulo"            to "Panel de empleado",
        "login.email"                to "Correo electrónico",
        "login.password"             to "Contraseña",
        "login.btn"                  to "Iniciar sesión",
        "login.error_campos"         to "Email y contraseña son obligatorios",

        "lang.es"                    to "ES",
        "lang.en"                    to "EN",
    )

    private val EN = mapOf(
        "nav.inicio"                 to "Home",
        "nav.fichajes"               to "Clock-ins",
        "nav.solicitudes"            to "Requests",
        "nav.incidencias"            to "Incidents",

        "dashboard.hola"             to "Hello,",
        "dashboard.sin_turno"        to "No shift assigned",
        "dashboard.mi_horario"       to "My schedule",
        "dashboard.mes"              to "Month",
        "dashboard.semana"           to "Week",
        "dashboard.estado_actual"    to "CURRENT STATUS",
        "dashboard.fichajes_hoy"     to "Clock-ins today",
        "dashboard.solicitudes"      to "Requests",
        "dashboard.incidencias"      to "Incidents",
        "dashboard.fichado"          to "Clocked in",
        "dashboard.pendiente"        to "Pending",
        "dashboard.no_laboral"       to "Non-working",
        "dashboard.sin_fichar"       to "Not clocked",
        "dashboard.hoy"              to "Today",
        "dashboard.dia_laboral"      to "Working day",
        "dashboard.estado_dentro"    to "In - clock-in registered",
        "dashboard.estado_fuera"     to "Out - clock-out registered",
        "dashboard.estado_pausa"     to "On break",
        "dashboard.estado_pausa_fin" to "In - break ended",
        "dashboard.estado_sin_reg"   to "No records today",
        "dashboard.cerrar_sesion"    to "Log out",

        "fichajes.titulo"            to "TODAY'S CLOCK-INS",
        "fichajes.sin_fichajes"      to "No clock-ins today",
        "fichajes.registrar_entrada" to "Clock in",
        "fichajes.registrar_salida"  to "Clock out",
        "fichajes.en_pausa"          to "On break - resume first",
        "fichajes.iniciar_pausa"     to "Start break",
        "fichajes.finalizar_pausa"   to "End break",
        "fichajes.sin_sede"          to "You have no assigned branch",
        "fichajes.sin_gps"           to "No GPS location",
        "fichajes.dentro"            to "Inside",
        "fichajes.fuera"             to "Outside",
        "fichajes.entrada"           to "Clock-in",
        "fichajes.salida"            to "Clock-out",
        "fichajes.aviso"             to "Warning",
        "fichajes.fichar_igual"      to "Clock in anyway",
        "fichajes.cancelar"          to "Cancel",
        "fichajes.aviso_no_laboral"  to "Today is not your working day according to your shift. Are you sure you want to clock in?",
        "fichajes.fuera_radio"       to "Clock-in outside radius",
        "fichajes.fuera_radio_msg"   to "You clocked in outside your branch's allowed radius.",

        "solicitudes.titulo"         to "MY REQUESTS",
        "solicitudes.nueva"          to " New request",
        "solicitudes.sin_datos"      to "You have no requests registered",
        "solicitudes.tipo"           to "Type",
        "solicitudes.fecha_inicio"   to "Start date",
        "solicitudes.fecha_fin"      to "End date",
        "solicitudes.selecciona"     to "Select date",
        "solicitudes.motivo"         to "Reason (optional)",
        "solicitudes.enviar"         to "Send",
        "solicitudes.cancelar"       to "Cancel",
        "solicitudes.nueva_titulo"   to "New request",
        "solicitudes.sel_inicio"     to "Select the start date first",
        "solicitudes.sel_fecha_ini"  to "Select the start date",
        "solicitudes.sel_fecha_fin"  to "Select the end date",
        "solicitudes.fecha_invalida" to "End date cannot be before start date",
        "solicitudes.aceptar"        to "Accept",
        "solicitudes.respuesta"      to "Reply: ",
        "solicitudes.pendiente"      to "Pending",
        "solicitudes.aprobada"       to "Approved",
        "solicitudes.denegada"       to "Rejected",

        "incidencias.titulo"         to "MY INCIDENTS",
        "incidencias.nueva"          to "New incident",
        "incidencias.sin_datos"      to "You have no incidents registered",
        "incidencias.tipo"           to "Type",
        "incidencias.descripcion"    to "Description (optional)",
        "incidencias.enviar"         to "Send",
        "incidencias.cancelar"       to "Cancel",
        "incidencias.nueva_titulo"   to "New incident",
        "incidencias.resuelta"       to "Resolved",
        "incidencias.abierta"        to "Open",

        "login.titulo"               to "D&D Control",
        "login.subtitulo"            to "Employee panel",
        "login.email"                to "Email address",
        "login.password"             to "Password",
        "login.btn"                  to "Sign in",
        "login.error_campos"         to "Email and password are required",

        "lang.es"                    to "ES",
        "lang.en"                    to "EN",
    )
}