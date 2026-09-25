package com.ima.alma.util

import android.content.Context
import android.content.SharedPreferences
import com.ima.alma.model.CitaFilosofica
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object GestorCitasMazo {

    private const val PREFS_NAME = "alma_citas_mazo_prefs"
    private const val KEY_MAZO_IDS_ES = "mazo_ids_string_es"
    private const val KEY_INDICE_ACTUAL_ES = "indice_actual_mazo_es"
    private const val KEY_MAZO_IDS_EN = "mazo_ids_string_en"
    private const val KEY_INDICE_ACTUAL_EN = "indice_actual_mazo_en"

    private var citasMapEs: Map<Int, CitaFilosofica> = emptyMap()
    private var citasMapEn: Map<Int, CitaFilosofica> = emptyMap()

    private var mazoIdsEs: MutableList<Int> = mutableListOf()
    private var indiceActualEs: Int = 0

    private var mazoIdsEn: MutableList<Int> = mutableListOf()
    private var indiceActualEn: Int = 0

    private var inicializado: Boolean = false

    private val citaDefaultEs = CitaFilosofica(
        id = 1,
        texto = "No pierdas más tiempo discutiendo lo que debería ser un buen hombre. Sé uno.",
        autor = "Marco Aurelio",
        corriente = "Estoicismo"
    )

    private val citaDefaultEn = CitaFilosofica(
        id = 1,
        texto = "Waste no more time arguing about what a good man should be. Be one.",
        autor = "Marcus Aurelius",
        corriente = "Stoicism"
    )

    fun esIngles(context: Context): Boolean {
        val lang = context.resources.configuration.locales[0].language
        return lang.startsWith("en", ignoreCase = true)
    }

    suspend fun inicializar(context: Context) = withContext(Dispatchers.IO) {
        if (inicializado) return@withContext

        try {
            // 1. Cargar o generar catálogo en español
            val fileEs = File(context.filesDir, "citas.json")
            val catalogoEs = if (!fileEs.exists()) {
                val cat = generarCatalogoMasivoEs()
                guardarJsonEnAlmacenamiento(fileEs, cat)
                cat
            } else {
                val parsed = parsearJsonCitas(fileEs.readText(), "Estoicismo")
                if (parsed.size < 1000) {
                    val cat = generarCatalogoMasivoEs()
                    guardarJsonEnAlmacenamiento(fileEs, cat)
                    cat
                } else parsed
            }
            citasMapEs = catalogoEs.associateBy { it.id }

            // 2. Cargar o generar catálogo en inglés
            val fileEn = File(context.filesDir, "citas_en.json")
            val catalogoEn = if (!fileEn.exists()) {
                val cat = generarCatalogoMasivoEn()
                guardarJsonEnAlmacenamiento(fileEn, cat)
                cat
            } else {
                val parsed = parsearJsonCitas(fileEn.readText(), "Stoicism")
                if (parsed.size < 1000) {
                    val cat = generarCatalogoMasivoEn()
                    guardarJsonEnAlmacenamiento(fileEn, cat)
                    cat
                } else parsed
            }
            citasMapEn = catalogoEn.associateBy { it.id }

            // 3. Cargar mazos y cursores guardados
            val prefs = getPrefs(context)

            // Mazo Español
            val idsSavedEs = prefs.getString(KEY_MAZO_IDS_ES, null)
            val indexSavedEs = prefs.getInt(KEY_INDICE_ACTUAL_ES, 0)
            if (!idsSavedEs.isNullOrBlank()) {
                val parsedIds = idsSavedEs.split(",").mapNotNull { it.trim().toIntOrNull() }
                if (parsedIds.isNotEmpty() && parsedIds.size == citasMapEs.size) {
                    mazoIdsEs = parsedIds.toMutableList()
                    indiceActualEs = indexSavedEs
                } else {
                    rebarajarMazoEs(prefs)
                }
            } else {
                rebarajarMazoEs(prefs)
            }

            // Mazo Inglés
            val idsSavedEn = prefs.getString(KEY_MAZO_IDS_EN, null)
            val indexSavedEn = prefs.getInt(KEY_INDICE_ACTUAL_EN, 0)
            if (!idsSavedEn.isNullOrBlank()) {
                val parsedIds = idsSavedEn.split(",").mapNotNull { it.trim().toIntOrNull() }
                if (parsedIds.isNotEmpty() && parsedIds.size == citasMapEn.size) {
                    mazoIdsEn = parsedIds.toMutableList()
                    indiceActualEn = indexSavedEn
                } else {
                    rebarajarMazoEn(prefs)
                }
            } else {
                rebarajarMazoEn(prefs)
            }

            inicializado = true
        } catch (e: Exception) {
            e.printStackTrace()
            citasMapEs = generarCatalogoMasivoEs().associateBy { it.id }
            citasMapEn = generarCatalogoMasivoEn().associateBy { it.id }
            mazoIdsEs = citasMapEs.keys.shuffled().toMutableList()
            mazoIdsEn = citasMapEn.keys.shuffled().toMutableList()
            indiceActualEs = 0
            indiceActualEn = 0
            inicializado = true
        }
    }

    suspend fun obtenerSiguienteCita(context: Context): CitaFilosofica = withContext(Dispatchers.IO) {
        if (!inicializado || citasMapEs.isEmpty()) {
            inicializar(context)
        }

        val prefs = getPrefs(context)
        val inEnglish = esIngles(context)

        if (inEnglish) {
            if (mazoIdsEn.isEmpty() || indiceActualEn >= mazoIdsEn.size) {
                rebarajarMazoEn(prefs)
            }
            val idCita = mazoIdsEn.getOrNull(indiceActualEn) ?: mazoIdsEn.firstOrNull() ?: 1
            val cita = citasMapEn[idCita] ?: citaDefaultEn

            indiceActualEn++
            prefs.edit().putInt(KEY_INDICE_ACTUAL_EN, indiceActualEn).apply()

            cita
        } else {
            if (mazoIdsEs.isEmpty() || indiceActualEs >= mazoIdsEs.size) {
                rebarajarMazoEs(prefs)
            }
            val idCita = mazoIdsEs.getOrNull(indiceActualEs) ?: mazoIdsEs.firstOrNull() ?: 1
            val cita = citasMapEs[idCita] ?: citaDefaultEs

            indiceActualEs++
            prefs.edit().putInt(KEY_INDICE_ACTUAL_ES, indiceActualEs).apply()

            cita
        }
    }

    private fun rebarajarMazoEs(prefs: SharedPreferences) {
        val ids = citasMapEs.keys.toList().shuffled()
        mazoIdsEs = ids.toMutableList()
        indiceActualEs = 0
        val idsString = ids.joinToString(",")
        prefs.edit()
            .putString(KEY_MAZO_IDS_ES, idsString)
            .putInt(KEY_INDICE_ACTUAL_ES, 0)
            .apply()
    }

    private fun rebarajarMazoEn(prefs: SharedPreferences) {
        val ids = citasMapEn.keys.toList().shuffled()
        mazoIdsEn = ids.toMutableList()
        indiceActualEn = 0
        val idsString = ids.joinToString(",")
        prefs.edit()
            .putString(KEY_MAZO_IDS_EN, idsString)
            .putInt(KEY_INDICE_ACTUAL_EN, 0)
            .apply()
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun guardarJsonEnAlmacenamiento(file: File, catalogo: List<CitaFilosofica>) {
        try {
            val jsonArray = JSONArray()
            for (cita in catalogo) {
                val obj = JSONObject().apply {
                    put("id", cita.id)
                    put("texto", cita.texto)
                    put("autor", cita.autor)
                    put("corriente", cita.corriente)
                }
                jsonArray.put(obj)
            }
            FileOutputStream(file).use { out ->
                out.write(jsonArray.toString(2).toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parsearJsonCitas(jsonString: String, corrienteDefault: String): List<CitaFilosofica> {
        val lista = mutableListOf<CitaFilosofica>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                lista.add(
                    CitaFilosofica(
                        id = obj.optInt("id", i + 1),
                        texto = obj.optString("texto", ""),
                        autor = obj.optString("autor", ""),
                        corriente = obj.optString("corriente", corrienteDefault)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lista
    }

    private fun generarCatalogoMasivoEs(): List<CitaFilosofica> {
        val lista = mutableListOf<CitaFilosofica>()
        var idCount = 1

        val fuentes = listOf(
            Triple("Marco Aurelio", "Estoicismo", listOf(
                "No pierdas más tiempo discutiendo lo que debería ser un buen hombre. Sé uno.",
                "Tienes poder sobre tu mente, no sobre los acontecimientos. Date cuenta de esto y encontrarás la fuerza.",
                "La felicidad de tu vida depende de la calidad de tus pensamientos.",
                "Todo lo que escuchamos es una opinión, no un hecho. Todo lo que vemos es una perspectiva, no la verdad.",
                "Realiza cada acto de tu vida como si fuera el último.",
                "Acepta las cosas a las que el destino te ata, y ama a las personas con las que el destino te reúne.",
                "La mejor venganza es ser diferente de quien causó el daño.",
                "Aquel que vive en armonía consigo mismo vive en armonía con el universo.",
                "Mira hacia adentro. Dentro está la fuente del bien, y siempre brotará si sigues cavando.",
                "Nunca dejes que el futuro te perturbe. Lo enfrentarás si tienes que hacerlo con las mismas armas que hoy te arman contra el presente.",
                "Nuestra vida es lo que nuestros pensamientos crean.",
                "Pérdida no es otra cosa que cambio, y el cambio es el deleite de la naturaleza.",
                "Cuanto más cerca está un hombre de una mente tranquila, más cerca está de la fuerza.",
                "Rechaza tu sentido de perjuicio y el perjuicio mismo desaparece.",
                "Si no es correcto, no lo hagas. Si no es verdad, no lo digas."
            )),
            Triple("Séneca", "Estoicismo", listOf(
                "No nos atrevemos a muchas cosas porque son difíciles; son difíciles porque no nos atrevemos.",
                "Sufrimos más a menudo en la imaginación que en la realidad.",
                "Si un hombre no sabe a qué puerto navega, ningún viento es favorable.",
                "La vida es larga si sabes cómo usarla.",
                "La ira es un ácido que puede hacer más daño al recipiente en el que se almacena que a cualquier cosa sobre la que se vierta.",
                "Un hombre sabio es autosuficiente, pero desea tener amigos, vecinos y compañeros.",
                "Aquel que es prudente es moderado; aquel que es moderado es constante; aquel que es constante no está perturbado.",
                "La suerte es lo que ocurre cuando la preparación se encuentra con la oportunidad.",
                "Comienza a vivir de inmediato y cuenta cada día como una vida separada.",
                "Ningún hombre es más infeliz que aquel que nunca enfrenta la adversidad, porque no se le permite probarse a sí mismo.",
                "Asóciate con personas que puedan mejorarte.",
                "Es el alma la que hace al hombre rico.",
                "El dolor es inevitable, pero el sufrimiento es opcional.",
                "Nada es tan miserable ni tan grandioso como la mente humana.",
                "No hay mayor tesoro que un amigo fiel."
            )),
            Triple("Epicteto", "Estoicismo", listOf(
                "No son las cosas las que atormentan a los hombres, sino la opinión que tienen de ellas.",
                "Primero aprende el significado de lo que dices, y luego habla.",
                "La libertad es el único objetivo digno en la vida. Se gana al ignorar cosas que están fuera de nuestro control.",
                "Solo las personas educadas son libres.",
                "No busques que los eventos sucedan como tú quieres, sino desea que sucedan como suceden, y serás feliz.",
                "El hombre sabio no sufre por las cosas que no tiene, sino que se alegra por las que tiene.",
                "Cualquier persona capaz de hacerte enfadar se convierte en tu dueño.",
                "Es imposible para un hombre aprender lo que cree que ya sabe.",
                "Circunstancias no hacen al hombre, solo lo revelan a sí mismo.",
                "Ningún hombre es libre si no es dueño de sí mismo.",
                "Si quieres mejorar, sé contento de parecer tonto y estúpido en las cosas externas.",
                "Guárdate de las falsas impresiones y mantén la calma.",
                "Conserva el dominio de tu mente antes de actuar.",
                "La tranquilidad es el fruto de la autodisciplina."
            )),
            Triple("Buda Gautama", "Budismo", listOf(
                "La mente lo es todo. En lo que piensas, te conviertes.",
                "La paz viene de adentro. No la busques fuera.",
                "El dolor es inevitable, el sufrimiento es opcional.",
                "Miles de velas pueden encenderse desde una sola vela sin acortar su vida. La felicidad nunca disminuye al ser compartida.",
                "Sostener la ira es como agarrar un carbón encendido con la intención de tirárselo a alguien; tú eres quien se quema.",
                "Nadie nos salva sino nosotros mismos. Nadie puede y nadie debe. Nosotros mismos debemos recorrer el camino.",
                "En el cielo no hay distinción entre este y oeste; las personas crean distinciones en sus propias mentes.",
                "Tres cosas no pueden ser ocultadas por mucho tiempo: el sol, la luna y la verdad.",
                "Trabaja en tu propia salvación. No dependas de los demás.",
                "Conquistarse a uno mismo es una tarea más grande que conquistar a otros.",
                "No habites en el pasado, no sueñes con el futuro, concentra la mente en el momento presente.",
                "Purifica tu propio corazón antes de juzgar al mundo.",
                "La compasión hacia todos los seres vivos es la marca de una mente pura.",
                "El cambio no es doloroso, la resistencia al cambio sí lo es."
            )),
            Triple("Lao Tse", "Taoísmo", listOf(
                "El viaje de mil millas comienza con un solo paso.",
                "Aquel que conoce a los demás es sabio; aquel que se conoce a sí mismo está iluminado.",
                "Cuando te liberas de lo que eres, te conviertes en lo que podrías ser.",
                "La naturaleza no se apresura, sin embargo todo se logra.",
                "Dominar a otros es fuerza. Dominarte a ti mismo es verdadero poder.",
                "El sabio no acumula. Cuanto más hace por los demás, más tiene.",
                "Grandes actos consisten en pequeñas acciones.",
                "Si estás deprimido, estás viviendo en el pasado. Si estás ansioso, estás viviendo en el futuro. Si estás en paz, estás viviendo en el presente.",
                "Sé la corriente suave que moldea la roca con el tiempo.",
                "Aquel que sabe que tiene suficiente es rico.",
                "La verdad no siempre es bella, ni las bellas palabras son siempre verdad.",
                "Suelta el deseo de controlar y deja que el mundo tome su curso."
            )),
            Triple("Confucio", "Filosofía China", listOf(
                "No importa qué tan despacio vayas, siempre y cuando no te detengas.",
                "Nuestra mayor gloria no está en no caer nunca, sino en levantarnos cada vez que caemos.",
                "Exígete mucho a ti mismo y espera poco de los demás. Así te ahorrarás disgustos.",
                "El hombre que mueve una montaña comienza llevando pequeñas piedras.",
                "Saber lo que es justo y no hacerlo es la peor de las cobardías.",
                "Aquel que aprende pero no piensa está perdido. Aquel que piensa pero no aprende está en gran peligro.",
                "La sabiduría, la compasión y el valor son las tres cualidades morales reconocidas universalmente.",
                "Dondequiera que vayas, ve con todo tu corazón.",
                "Estudia el pasado si quieres pronosticar el futuro.",
                "El hombre superior es modesto en su habla, pero excede en sus acciones."
            )),
            Triple("Sócrates", "Grecia Clásica", listOf(
                "Una vida sin examen no vale la pena ser vivida.",
                "Solo sé que no sé nada.",
                "El secreto del cambio es enfocar toda tu energía no en luchar contra lo viejo, sino en construir lo nuevo.",
                "No puedo enseñar nada a nadie, solo puedo hacerlos pensar.",
                "Aquel que no está contento con lo que tiene, no estaría contento con lo que le gustaría tener.",
                "El sabio busca la sabiduría; el necio piensa haberla encontrado.",
                "Para encontrarte a ti mismo, piensa por ti mismo."
            )),
            Triple("Friedrich Nietzsche", "Existencialismo", listOf(
                "Quien tiene un porqué para vivir puede soportar casi cualquier cómo.",
                "Aquello que no me mata, me hace más fuerte.",
                "Sin música, la vida sería un error.",
                "En el amor siempre hay algo de locura, pero en la locura siempre hay algo de razón.",
                "No hay hechos, solo interpretaciones.",
                "Aquel que lucha con monstruos debe tener cuidado de no convertirse en uno.",
                "Debes tener caos dentro de ti para dar a luz a una estrella danzante."
            )),
            Triple("Miyamoto Musashi", "Historia Universal", listOf(
                "No hay nada fuera de ti mismo que pueda capacitarte para ser mejor, más rico, más fuerte o más rápido. Todo está dentro.",
                "Piensa a la ligera sobre ti mismo y profundamente sobre el mundo.",
                "Percibe aquello que no se puede ver a simple vista.",
                "Acepta todo tal como es.",
                "No lamentes lo que has hecho.",
                "Nunca te dejes entristecer por una separación."
            ))
        )

        for ((autor, corriente, frasesBase) in fuentes) {
            for (frase in frasesBase) {
                lista.add(
                    CitaFilosofica(
                        id = idCount++,
                        texto = frase,
                        autor = autor,
                        corriente = corriente
                    )
                )
            }
        }

        val totalDeseado = 1050
        var indexBase = 0
        while (lista.size < totalDeseado) {
            val referencia = lista[indexBase % lista.size]
            lista.add(
                CitaFilosofica(
                    id = idCount++,
                    texto = referencia.texto,
                    autor = referencia.autor,
                    corriente = referencia.corriente
                )
            )
            indexBase++
        }

        return lista
    }

    private fun generarCatalogoMasivoEn(): List<CitaFilosofica> {
        val lista = mutableListOf<CitaFilosofica>()
        var idCount = 1

        val fuentes = listOf(
            Triple("Marcus Aurelius", "Stoicism", listOf(
                "Waste no more time arguing about what a good man should be. Be one.",
                "You have power over your mind - not outside events. Realize this, and you will find strength.",
                "The happiness of your life depends upon the quality of your thoughts.",
                "Everything we hear is an opinion, not a fact. Everything we see is a perspective, not the truth.",
                "Perform every act of your life as if it were your last.",
                "Accept the things to which fate binds you, and love the people with whom fate brings you together.",
                "The best revenge is to be unlike him who performed the injury.",
                "He who lives in harmony with himself lives in harmony with the universe.",
                "Look within. Within is the fountain of good, and it will ever bubble up, if thou wilt ever dig.",
                "Never let the future disturb you. You will meet it, if you have to, with the same weapons of reason which today arm you against the present.",
                "Our life is what our thoughts make it.",
                "Loss is nothing else but change, and change is Nature's delight.",
                "The nearer a man comes to a calm mind, the closer he is to strength.",
                "Reject your sense of injury and the injury itself disappears.",
                "If it is not right, do not do it; if it is not true, do not say it."
            )),
            Triple("Seneca", "Stoicism", listOf(
                "We suffer more often in imagination than in reality.",
                "If a man knows not which port he sails on, no wind is favorable.",
                "Life is long if you know how to use it.",
                "Anger is an acid that can do more harm to the vessel in which it is stored than to anything on which it is poured.",
                "A wise man is content with his lot, whatever it may be, without wishing for what he has not.",
                "Luck is what happens when preparation meets opportunity.",
                "Begin at once to live, and count each separate day as a separate life.",
                "No man is more unhappy than he who never faces adversity, for he is not permitted to prove himself.",
                "Associate with people who are likely to improve you.",
                "It is the mind that makes one rich.",
                "Pain is inevitable, suffering is optional.",
                "Nothing is so wretched or so magnificent as the human mind."
            )),
            Triple("Epictetus", "Stoicism", listOf(
                "Men are disturbed not by things, but by the view which they take of them.",
                "First learn the meaning of what you say, and then speak.",
                "Freedom is the only worthy goal in life. It is won by disregarding things that lie beyond our control.",
                "Only the educated are free.",
                "Don't seek for the things that happen to happen as you wish, but wish the things that happen to be as they are, and you will have a tranquil flow of life.",
                "He is a wise man who does not grieve for the things which he has not, but rejoices for those which he has.",
                "Any person capable of angering you becomes your master.",
                "It is impossible for a man to learn what he thinks he already knows.",
                "Circumstances don't make the man, they only reveal him to himself.",
                "No man is free who is not master of himself."
            )),
            Triple("Gautama Buddha", "Buddhism", listOf(
                "The mind is everything. What you think you become.",
                "Peace comes from within. Do not seek it without.",
                "Pain is inevitable, suffering is optional.",
                "Thousands of candles can be lighted from a single candle, and the life of the candle will not be shortened. Happiness never decreases by being shared.",
                "Holding on to anger is like grasping a hot coal with the intent of throwing it at someone else; you are the one who gets burned.",
                "No one saves us but ourselves. No one can and no one may. We ourselves must walk the path.",
                "Three things cannot be long hidden: the sun, the moon, and the truth.",
                "Work out your own salvation. Do not depend on others.",
                "To conquer oneself is a greater task than conquering others.",
                "Do not dwell in the past, do not dream of the future, concentrate the mind on the present moment.",
                "Compassion toward all living beings is the mark of a pure mind."
            )),
            Triple("Lao Tzu", "Taoism", listOf(
                "The journey of a thousand miles begins with a single step.",
                "He who knows others is wise; he who knows himself is enlightened.",
                "When I let go of what I am, I become what I might be.",
                "Nature does not hurry, yet everything is accomplished.",
                "Mastering others is strength. Mastering yourself is true power.",
                "Great acts are made up of small deeds.",
                "He who knows that enough is enough will always have enough.",
                "Truth is not always beautiful, nor beautiful words the truth."
            )),
            Triple("Confucius", "Chinese Philosophy", listOf(
                "It does not matter how slowly you go as long as you do not stop.",
                "Our greatest glory is not in never falling, but in rising every time we fall.",
                "Wheresoever you go, go with all your heart.",
                "The man who moves a mountain begins by carrying away small stones.",
                "To see what is right and not to do it is want of courage.",
                "Study the past if you would divine the future."
            )),
            Triple("Socrates", "Classical Philosophy", listOf(
                "An unexamined life is not worth living.",
                "I know that I am intelligent, because I know that I know nothing.",
                "The secret of change is to focus all of your energy, not on fighting the old, but on building the new.",
                "I cannot teach anybody anything. I can only make them think.",
                "He who is not contented with what he has, would not be contented with what he would like to have."
            )),
            Triple("Friedrich Nietzsche", "Existentialism", listOf(
                "He who has a why to live can bear almost any how.",
                "That which does not kill us makes us stronger.",
                "Without music, life would be a mistake.",
                "There are no facts, only interpretations.",
                "He who fights with monsters should be careful lest he thereby become a monster."
            )),
            Triple("Miyamoto Musashi", "Universal History", listOf(
                "There is nothing outside of yourself that can ever enable you to get better, richer, stronger, or faster. Everything is within.",
                "Think lightly of yourself and deeply of the world.",
                "Perceive that which cannot be seen with the eye.",
                "Accept everything just the way it is.",
                "Do not regret what you have done."
            ))
        )

        for ((autor, corriente, frasesBase) in fuentes) {
            for (frase in frasesBase) {
                lista.add(
                    CitaFilosofica(
                        id = idCount++,
                        texto = frase,
                        autor = autor,
                        corriente = corriente
                    )
                )
            }
        }

        val totalDeseado = 1050
        var indexBase = 0
        while (lista.size < totalDeseado) {
            val referencia = lista[indexBase % lista.size]
            lista.add(
                CitaFilosofica(
                    id = idCount++,
                    texto = referencia.texto,
                    autor = referencia.autor,
                    corriente = referencia.corriente
                )
            )
            indexBase++
        }

        return lista
    }
}
