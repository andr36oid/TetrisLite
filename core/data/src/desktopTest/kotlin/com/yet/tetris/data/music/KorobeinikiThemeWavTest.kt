package com.yet.tetris.data.music

import com.yet.tetris.data.mapper.getSequenceForTheme
import com.yet.tetris.data.mapper.getWaveformForTheme
import com.yet.tetris.domain.model.audio.MusicTheme
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Writes the Korobeiniki theme to the WAV file named in TETRIS_MUSIC_WAV, to listen to it. */
class KorobeinikiThemeWavTest {
    @Test
    fun writeWav() {
        val path = System.getenv("TETRIS_MUSIC_WAV") ?: return
        val started = System.nanoTime()
        val pcm =
            AudioSynthesizer.synthesizeMusicSequence(
                assertNotNull(getSequenceForTheme(MusicTheme.KOROBEINIKI)),
                getWaveformForTheme(MusicTheme.KOROBEINIKI),
            )
        val ms = (System.nanoTime() - started) / 1_000_000
        // Validate before quantizing: clamping must not conceal a broken mix.
        assertTrue(pcm.isNotEmpty())
        assertTrue(pcm.all { it.isFinite() && abs(it) < 1f }, "Non-finite or clipped PCM")
        val peak = pcm.maxOf { abs(it) }
        val rms = sqrt(pcm.sumOf { it.toDouble() * it } / pcm.size)
        val data = ByteBuffer.allocate(pcm.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        pcm.forEach { data.putShort((it * Short.MAX_VALUE).toInt().toShort()) }
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        header.put("RIFF".toByteArray()).putInt(36 + pcm.size * 2).put("WAVE".toByteArray())
        header
            .put("fmt ".toByteArray())
            .putInt(16)
            .putShort(1)
            .putShort(1)
        header
            .putInt(AudioSynthesizer.SAMPLE_RATE)
            .putInt(AudioSynthesizer.SAMPLE_RATE * 2)
            .putShort(2)
            .putShort(16)
        header.put("data".toByteArray()).putInt(pcm.size * 2)
        File(path).outputStream().use { output ->
            output.write(header.array())
            output.write(data.array())
        }
        val seconds = pcm.size.toDouble() / AudioSynthesizer.SAMPLE_RATE
        println("wrote $path: ${pcm.size} samples, $seconds seconds, peak $peak, RMS $rms, synthesized in $ms ms")
    }
}
