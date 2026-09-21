package com.ima.alma.util

object BancoReflexiones {
    val frases = listOf(
        // Vínculos y personas
        "¿Con quién tuviste hoy una conversación que valió la pena?",
        "Piensa en alguien que hizo tu rutina más ligera últimamente. ¿Qué hizo?",
        "¿Hubo algún gesto silencioso de alguien hacia ti que no agradeciste en el momento?",
        "¿Qué te dijo alguien hoy que te dejó pensando más de la cuenta?",
        "¿A quién extrañaste hoy de forma inesperada?",
        "¿Qué actitud de otra persona hoy te hizo reflexionar sobre ti mismo?",

        // Momentos y detalles
        "Si pudieras congelar un solo minuto de este día, ¿cuál elegirías?",
        "Describe el momento exacto en que sentiste más tranquilidad hoy.",
        "¿Qué detalle visual o sonido de hoy te llamó la atención sin buscarlo?",
        "¿Qué parte de tu día de hoy no te gustaría olvidar dentro de cinco años?",
        "¿Qué sabor, olor o paisaje de hoy guardas en la memoria?",
        "¿Cuál fue el momento más auténtico y sin filtros que viviste hoy?",

        // Claridad y decisiones
        "¿Qué problema que te preocupaba hace un mes hoy ya no importa tanto?",
        "¿Tomaste hoy alguna decisión incómoda pero necesaria?",
        "¿En qué momento del día sentiste que perdiste el tiempo y por qué?",
        "¿Qué aprendiste hoy por las malas o a través de un error?",
        "¿Qué hábito de tu día de hoy mantendrías para el resto de tu semana?",
        "¿Qué pequeña victoria personal lograste hoy sin que nadie se diera cuenta?",

        // Realidad personal y emociones
        "¿Cómo describirías tu estado mental de hoy en una sola palabra honesta?",
        "¿Qué hiciste hoy exclusivamente por ti y no por cumplir con otros?",
        "¿Cuál fue el reto más pesado que superaste antes de terminar la tarde?",
        "¿Qué es algo que querías decir hoy y preferiste callarte?",
        "¿De qué te sientes orgulloso en la forma en que actuaste hoy?",
        "Si hoy fuera el capítulo de un libro, ¿qué título le pondrías?",
        "¿Qué emoción predominó en tu cuerpo a lo largo de este día?",
        "¿Qué te quitó energía hoy y cómo te recuperaste?",
        "¿Qué pequeña cosa te hizo sonreír sin darte cuenta hoy?",
        "¿Qué aprendizaje te deja la jornada de hoy para arrancar mañana?",
        "¿Con qué sensación en el pecho terminas este día?",
        "¿Qué parte de tu rutina diaria te trajo más paz hoy?"
    )

    fun obtenerAleatoria(excluir: String = ""): String {
        val filtradas = frases.filter { it != excluir }
        return if (filtradas.isNotEmpty()) filtradas.random() else frases.random()
    }
}
