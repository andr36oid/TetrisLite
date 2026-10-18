package com.yet.tetris.data.music

import com.yet.tetris.domain.model.audio.MusicSequencePresets
import com.yet.tetris.domain.model.audio.WaveformType
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test

/** Writes the Korobeiniki theme to the WAV file named in TETRIS_MUSIC_WAV, to listen to it. */
class KorobeinikiThemeWavTest {
    @Test
    fun writeWav() {
        val path = System.getenv("TETRIS_MUSIC_WAV") ?: return
        val started = System.nanoTime()
        val pcm = AudioSynthesizer.synthesizeMusicSequence(MusicSequencePresets.KOROBEINIKI_THEME, WaveformType.SQUARE)
        val ms = (System.nanoTime() - started) / 1_000_000
        val data = ByteBuffer.allocate(pcm.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        pcm.forEach { data.putShort((it.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()) }
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        header.put("RIFF".toByteArray()).putInt(36 + pcm.size * 2).put("WAVE".toByteArray())
        header.put("fmt ".toByteArray()).putInt(16).putShort(1).putShort(1)
        header.putInt(AudioSynthesizer.SAMPLE_RATE).putInt(AudioSynthesizer.SAMPLE_RATE * 2).putShort(2).putShort(16)
        header.put("data".toByteArray()).putInt(pcm.size * 2)
        File(path).writeBytes(header.array() + data.array())
        println("wrote $path: ${pcm.size} samples, peak ${pcm.maxOf { kotlin.math.abs(it) }}, synthesized in $ms ms")
    }
}
