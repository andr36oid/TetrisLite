package com.yet.tetris.data.music

import com.yet.tetris.domain.model.audio.Envelope
import com.yet.tetris.domain.model.audio.MusicSection
import com.yet.tetris.domain.model.audio.MusicSequence
import com.yet.tetris.domain.model.audio.MusicSequencePresets
import com.yet.tetris.domain.model.audio.Note
import com.yet.tetris.domain.model.audio.WaveformType
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KorobeinikiThemeTest {
    private val flat = Envelope(attack = 0f, decay = 0f, sustain = 1f, release = 0f)

    @Test
    fun sectionsPlayOneAfterAnotherEachAtItsOwnTempo() {
        val sequence =
            MusicSequence(
                notes = emptyList(),
                sections =
                    listOf(
                        MusicSection(listOf(Note(Note.A4, Note.HALF)), tempo = 120),
                        MusicSection(listOf(Note(Note.A4, Note.HALF)), tempo = 240),
                    ),
            )

        val pcm = AudioSynthesizer.synthesizeMusicSequence(sequence, WaveformType.SINE, flat)

        // one second at 120 BPM, then half a second at 240
        assertEquals(AudioSynthesizer.SAMPLE_RATE * 3 / 2, pcm.size)
    }

    @Test
    fun bassIsMixedUnderTheMelodyAndCutToItsLength() {
        val melodyOnly =
            MusicSequence(notes = emptyList(), sections = listOf(MusicSection(listOf(Note(Note.A4, Note.QUARTER)))))
        val withBass =
            MusicSequence(
                notes = emptyList(),
                sections =
                    listOf(
                        MusicSection(
                            listOf(Note(Note.A4, Note.QUARTER)),
                            bass = listOf(Note(Note.A2, Note.WHOLE)),
                        ),
                    ),
            )

        val a = AudioSynthesizer.synthesizeMusicSequence(melodyOnly, WaveformType.SINE, flat)
        val b = AudioSynthesizer.synthesizeMusicSequence(withBass, WaveformType.SINE, flat)

        assertEquals(a.size, b.size)
        assertTrue(a.indices.any { abs(a[it] - b[it]) > 0.05f })
        assertTrue(b.all { abs(it) <= 1.0f })
    }

    @Test
    fun korobeinikiThemeIsAboutAMinuteAndBounded() {
        val pcm = AudioSynthesizer.synthesizeMusicSequence(MusicSequencePresets.KOROBEINIKI_THEME, WaveformType.SQUARE)
        val seconds = pcm.size.toFloat() / AudioSynthesizer.SAMPLE_RATE

        // 64 beats at 120 + 32 at 144 + 64 at 168
        assertEquals(32f + 32f * 60 / 144 + 64f * 60 / 168, seconds, absoluteTolerance = 0.1f)
        assertTrue(pcm.all { abs(it) <= 1.0f })
        assertTrue(pcm.any { abs(it) > 0.3f })
    }

    @Test
    fun everyBarOfTheKorobeinikiThemeHasFourBeats() {
        MusicSequencePresets.KOROBEINIKI_THEME.sections.forEach { section ->
            val melodyBeats = section.melody.sumOf { (it.duration / Note.QUARTER).toDouble() }
            val bassBeats = section.bass.sumOf { (it.duration / Note.QUARTER).toDouble() }
            assertEquals(0.0, melodyBeats % 4.0, 0.0001)
            assertEquals(melodyBeats, bassBeats, 0.0001)
        }
    }
}
