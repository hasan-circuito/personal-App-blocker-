package com.example

import com.example.detection.DetectionLevel
import com.example.detection.HeuristicContentDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HeuristicContentDetectorTest {

    private val detector = HeuristicContentDetector()

    @Test
    fun `test English safe content is classified as SAFE`() {
        val nodes = listOf("Kotlin documentation", "How to build Jetpack Compose apps", "Android Studio")
        val result = detector.detectContent(nodes, "com.android.chrome", "MEDIUM")
        assertEquals(DetectionLevel.SAFE, result.level)
    }

    @Test
    fun `test English explicit terms trigger ADULT level`() {
        val nodes = listOf("Search results for xvideos and free adult porno clips", "watch online")
        val result = detector.detectContent(nodes, "com.android.chrome", "MEDIUM")
        assertEquals(DetectionLevel.ADULT, result.level)
        assertTrue(result.confidence >= 0.65f)
    }

    @Test
    fun `test English ambiguous or educational text is SAFE`() {
        val nodes = listOf("Human anatomy and biology textbook", "Reproductive health education lesson")
        val result = detector.detectContent(nodes, "com.android.chrome", "MEDIUM")
        assertEquals(DetectionLevel.SAFE, result.level)
    }

    @Test
    fun `test Bangla Unicode high-confidence prohibited text triggers ADULT level`() {
        val nodes = listOf("এখানে ফ্রী চোদাচুডি এবং খারাপ ভিডিও লিংক আছে", "দেখুন")
        val result = detector.detectContent(nodes, "org.telegram.messenger", "MEDIUM")
        assertEquals(DetectionLevel.ADULT, result.level)
        assertTrue(result.detectedKeywords.contains("চোদাচুডি") || result.detectedKeywords.contains("খারাপ ভিডিও"))
    }

    @Test
    fun `test Bangla Unicode contextual adult content triggers ADULT level`() {
        val nodes = listOf("১৮+ এডাল্ট সেক্সি গরম ভিডিও লিঙ্ক")
        val result = detector.detectContent(nodes, "org.telegram.messenger", "MEDIUM")
        assertEquals(DetectionLevel.ADULT, result.level)
    }

    @Test
    fun `test Banglish prohibited content triggers ADULT level`() {
        val nodes = listOf("watch hot choda video on telegram channel now")
        val result = detector.detectContent(nodes, "org.telegram.messenger", "MEDIUM")
        assertEquals(DetectionLevel.ADULT, result.level)
        assertTrue(result.detectedKeywords.contains("choda"))
    }

    @Test
    fun `test Mixed Bangla-English prohibited text triggers ADULT level`() {
        val nodes = listOf("download hot video এবং সেক্স ছবি on telegram link")
        val result = detector.detectContent(nodes, "org.telegram.messenger", "MEDIUM")
        assertEquals(DetectionLevel.ADULT, result.level)
        assertTrue(result.detectedKeywords.contains("সেক্স"))
    }

    @Test
    fun `test Normal Bangla conversation is SAFE`() {
        val nodes = listOf("বাংলাদেশ একটি সুন্দর ও স্বাধীন দেশ। আজকের খবর শুনুন।", "আমি কাল স্কুলে যাব")
        val result = detector.detectContent(nodes, "org.telegram.messenger", "MEDIUM")
        assertEquals(DetectionLevel.SAFE, result.level)
    }

    @Test
    fun `test Educational Bangla context is SAFE`() {
        val nodes = listOf("অ্যান্ড্রয়েড টিউটোরিয়াল এবং অ্যান্ড্রয়েড মোবাইল অ্যাপ ডেভেলপমেন্ট কোর্স শিখুন")
        val result = detector.detectContent(nodes, "com.android.chrome", "MEDIUM")
        assertEquals(DetectionLevel.SAFE, result.level)
    }

    @Test
    fun `test exact typo strings individually match adult detection`() {
        val r1 = detector.detectContent(listOf("musterbating"), "org.telegram.messenger", "MEDIUM")
        assertEquals(DetectionLevel.ADULT, r1.level)

        val r2 = detector.detectContent(listOf("ameture"), "org.telegram.messenger", "MEDIUM")
        assertEquals(DetectionLevel.ADULT, r2.level)

        val r3 = detector.detectContent(listOf("musterbating girl 18+"), "org.telegram.messenger", "MEDIUM")
        assertEquals(DetectionLevel.ADULT, r3.level)
    }

    @Test
    fun `test Telegram title musterbating girl 18+ slash ameture girl 18+ triggers ADULT`() {
        val nodes = listOf("musterbating girl 18+ / ameture girl 18+")
        val result = detector.detectContent(nodes, "org.telegram.messenger", "MEDIUM")
        assertEquals(DetectionLevel.ADULT, result.level)
        assertTrue(result.confidence >= 0.85f)
        assertTrue(result.detectedKeywords.contains("musterbat"))
        assertTrue(result.detectedKeywords.contains("ameture"))
    }

    @Test
    fun `test known working adult telegram channel triggers ADULT`() {
        val nodes = listOf("Pornhub Videos HD Official Channel", "Join for daily updates")
        val result = detector.detectContent(nodes, "org.telegram.messenger", "MEDIUM")
        assertEquals(DetectionLevel.ADULT, result.level)
    }

    @Test
    fun `test safe telegram channel is ALLOWED`() {
        val nodes = listOf("Kotlin Developers Discussion Group", "Latest news and updates about Android")
        val result = detector.detectContent(nodes, "org.telegram.messenger", "MEDIUM")
        assertEquals(DetectionLevel.SAFE, result.level)
    }
}
