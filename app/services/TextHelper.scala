package services

import java.security.SecureRandom
import java.text.Normalizer
import java.text.Normalizer.Form.NFD
import java.util.Locale

import scala.util.Try

object TextHelper {

    val EmptyMailboxName = "default"

    private val threadLocalSecureRandom = ThreadLocal.withInitial[SecureRandom] { () =>
        new SecureRandom()
    }

    val SafeCharset: Array[Char] = (('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9') ++ Seq('_', '-')).toArray

    private val manualCharacterMap = Map(
        'ä' -> "ae",
        'Ä' -> "Ae",
        'ö' -> "oe",
        'Ö' -> "Oe",
        'ü' -> "ue",
        'Ü' -> "Ue",
        'ß' -> "sz",
        'ẞ' -> "Sz"
    )

    private val FilenameReservedChars = Set('/', '\\', '?', '%', '*', ':', '|', '"', '<', '>')
    private val MailboxUnsafeChars = Set('#', '!', '`', '´', '\'', '$', '(', ')', '[', ']', '{', '}', '=', '&', '@', '|', '~', '+', '^', '¬')

    private val UnsafeChars = FilenameReservedChars ++ MailboxUnsafeChars

    private val Spacer = "-"

    private val DiacriticsStripper = "\\p{InCombiningDiacriticalMarks}+".r
    private val NonAsciiStripper = "[^\\p{ASCII}]".r
    private val ControlCharStripper = "\\p{C}".r
    private val SpacerStripper = s"[\\s$Spacer]+".r

    private val normalizationPipeline: Seq[String => String] = Seq(
        stripControlChars,
        manuallyMapCharacters,
        stripDiacritics,
        stripNonAscii(" "),
        stripFilenameReservedChars(" "),
        normalizeSpacers(" "),
        _.trim,
        _.replaceAll(" ", Spacer),
        _.toLowerCase(Locale.US)
    )

    private def manuallyMapCharacters(s: String): String =
        s.flatMap(c => manualCharacterMap.getOrElse(c, c.toString))

    private def stripDiacritics(s: String): String =
        DiacriticsStripper.replaceAllIn(Normalizer.normalize(s, NFD), "")

    private def stripNonAscii(replacement: String)(s: String): String =
        NonAsciiStripper.replaceAllIn(s, replacement)

    private def stripControlChars(s: String): String =
        ControlCharStripper.replaceAllIn(s, "")

    private def normalizeSpacers(replacement: String)(s: String): String = SpacerStripper.replaceAllIn(s, replacement)

    private def stripFilenameReservedChars(replacement: String)(s: String): String = s.flatMap {
        case c if UnsafeChars.contains(c) => replacement
        case c => c.toString
    }

    def normalizeString(s: String, orElse: String): String = {
        val normalized = normalizationPipeline.foldLeft(s)((s, f) => f(s))
        if (normalized.isEmpty) orElse
        else normalized
    }

    def normalizeMailboxName(s: String): String = normalizeString(s, EmptyMailboxName)

    def binarySplitAt(input: String, separator: String, at: Int): Option[(String, String)] = {
        if (at == -1) None
        else {
            val prefix = input.substring(0, at)
            val suffix = input.substring(at + separator.length)
            Some((prefix, suffix))
        }
    }

    def rightMostBinarySplit(input: String, separator: String): Option[(String, String)] =
        binarySplitAt(input, separator, input.lastIndexOf(separator))

    def leftMostBinarySplit(input: String, separator: String): Option[(String, String)] =
        binarySplitAt(input, separator, input.indexOf(separator))

    def splitEmail(s: String): Option[(String, String)] = rightMostBinarySplit(s, "@")

    def makeEmail(name: String, domain: String): String = s"$name@$domain"

    def randomString(len: Int): String = {
        val random = threadLocalSecureRandom.get()
        (1 to len).map(_ => SafeCharset(random.nextInt(SafeCharset.length))).mkString
    }

    def parseInt(str: String): Option[Int] = Try(Integer.parseInt(str)).toOption
}
