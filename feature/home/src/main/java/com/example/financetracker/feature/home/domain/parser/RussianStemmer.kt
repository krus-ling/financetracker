package com.example.financetracker.feature.home.domain.parser

/**
 * Стеммер русского языка для отсечения падежных окончаний.
 */
object RussianStemmer {
    private val PERFECTIVE_GERUND = Regex("(ив|ивши|ившись|ыв|ывши|ывшись)$")
    private val ADJECTIVE = Regex("(ее|ие|ые|ое|ими|ыми|ей|ий|ый|ой|ем|им|ым|ом|его|ого|ему|ому|их|ых|ую|юю|ая|яя|ою|ею)$")
    private val NOUN = Regex("(а|ев|ов|ие|ье|е|иями|ями|ами|еи|ии|и|ией|ей|ой|ий|й|иям|ям|ием|ем|ам|ом|о|у|ах|иях|ях|ы|ь|ию|ью|ю|ия|ья|я)$")

    fun stem(word: String): String {
        var w = word.lowercase().trim()

        // Короткие слова не трогаем
        if (w.length <= 3) return w

        // Заменяем ё на е
        w = w.replace('ё', 'е')

        // Убираем деепричастные и глагольные окончания
        w = PERFECTIVE_GERUND.replace(w, "")

        // Убираем окончания прилагательных
        w = ADJECTIVE.replace(w, "")

        // Убираем окончания существительных
        w = NOUN.replace(w, "")

        return w
    }
}