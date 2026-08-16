package com.example.detection

import java.text.Normalizer
import java.util.Locale

class HeuristicContentDetector : ContentDetector {

    // High confidence adult content indicators (English, Bangla Unicode, and Banglish)
    private val explicitTerms = setOf(
        // English
        "porno", "pornography", "porn", "xvideos", "pornhub", "xhamster", "xnxx",
        "redtube", "youporn", "brazzers", "chaturbate", "onlyfans", "nsfw",
        "hentai", "xxx", "erotic", "sex tape", "adult movie", "nude photo",
        "stripclub", "escort service", "camgirl", "boobs", "topless", "nude", "nudes", "naked", "sex",
        "masturbat", "musterbat", "mastrobat", "amateur", "ameture", "amater",
        "pussy", "dick", "cock", "blowjob", "handjob", "cumshot", "orgasm", "horny",
        "slut", "whore", "milf", "hardcore", "anal", "creampie", "squirt", "deepthroat",
        "threesome", "gangbang", "incest", "bdsm",

        // Bengali Unicode script
        "সেক্স", "পর্ন", "পর্নোগ্রাফি", "চোদা", "চুদা", "চুদাচুডি", "চোদন",
        "মাগি", "খানকি", "ভোদা", "ধোন", "ল্যাংটা", "নগ্ন", "খারাপ ভিডিও",

        // Banglish / Transliterated Bengali
        "choda", "chodachodi", "chodan", "khanki", "voda", "dhon", "langta", "gorom video", "hot chobi"
    )

    // Secondary adult context keywords (weight 0.25f each)
    private val secondaryTerms = setOf(
        // English
        "adults only", "18+", "over 18", "age verification", "sexually explicit",
        "uncensored", "leaked nudes", "sensual", "lingerie", "strip", "fetish",
        "mature content", "adult content",

        // Bengali Unicode script
        "১৮+", "এডাল্ট", "গরম ভিডিও", "গোপন ভিডিও", "সেক্সি", "হট ছবি", "নগ্ন ছবি", "গরম ছবি", "গোপন লিঙ্ক",

        // Banglish / Transliterated Bengali
        "18+", "adolt", "hot video", "leaked photo", "gopon video"
    )

    override fun detectContent(
        nodesText: List<String>,
        packageName: String,
        sensitivity: String
    ): DetectionResult {
        if (nodesText.isEmpty()) {
            return DetectionResult(DetectionLevel.SAFE, 0.0f, "No screen content")
        }

        // Normalize text: NFC normalization, strip invisible characters, lowercase
        val combinedRaw = nodesText.joinToString(" ")
        val combinedText = normalizeText(combinedRaw)

        if (combinedText.isBlank()) {
            return DetectionResult(DetectionLevel.SAFE, 0.0f, "No usable text content")
        }

        // Match explicit terms using reliable rule
        val matchedExplicit = explicitTerms.filter { term ->
            matchTerm(combinedText, term)
        }

        // Match secondary terms
        val matchedSecondary = secondaryTerms.filter { term ->
            matchTerm(combinedText, term)
        }

        var score = 0.0f

        if (matchedExplicit.isNotEmpty()) {
            // Base score for high-confidence explicit matches
            score += 0.70f + (matchedExplicit.size - 1) * 0.15f
        }

        if (matchedSecondary.isNotEmpty()) {
            score += matchedSecondary.size * 0.25f
        }

        val threshold = when (sensitivity.uppercase(Locale.ROOT)) {
            "HIGH" -> 0.50f
            "LOW" -> 0.85f
            else -> 0.65f // MEDIUM
        }

        score = score.coerceAtMost(1.0f)

        return when {
            score >= threshold -> {
                val matchedAll = (matchedExplicit + matchedSecondary).distinct()
                DetectionResult(
                    level = DetectionLevel.ADULT,
                    confidence = score,
                    triggerReason = "Explicit adult terms detected (${matchedAll.take(3).joinToString(", ")})",
                    detectedKeywords = matchedAll
                )
            }
            score >= (threshold - 0.20f) && score > 0.30f -> {
                val matchedAll = (matchedExplicit + matchedSecondary).distinct()
                DetectionResult(
                    level = DetectionLevel.POSSIBLY_ADULT,
                    confidence = score,
                    triggerReason = "Suspicious adult terminology (${matchedAll.take(3).joinToString(", ")})",
                    detectedKeywords = matchedAll
                )
            }
            else -> {
                DetectionResult(
                    level = DetectionLevel.SAFE,
                    confidence = score,
                    triggerReason = "Content cleared on-device analysis"
                )
            }
        }
    }

    private fun normalizeText(text: String): String {
        if (text.isBlank()) return ""
        val nfc = Normalizer.normalize(text, Normalizer.Form.NFC)
        val clean = nfc.replace(Regex("[\u200B\u200C\u200D\uFEFF\u00AD]"), "")
        return clean.lowercase(Locale.ROOT)
    }

    private fun matchTerm(text: String, term: String): Boolean {
        val normTerm = normalizeText(term)
        if (normTerm.isBlank()) return false

        return if (normTerm.length <= 3) {
            // Word boundary match for short terms like "xxx", "nsfw", "porn", "18+", "১৮+"
            Regex("(?i)\\b${Regex.escape(normTerm)}\\b").containsMatchIn(text) || text.contains(normTerm)
        } else {
            text.contains(normTerm)
        }
    }
}

