package com.yet.tetris.data.music

import com.yet.tetris.data.mapper.getSequenceForTheme
import com.yet.tetris.data.mapper.getWaveformForTheme
import com.yet.tetris.domain.model.audio.Envelope
import com.yet.tetris.domain.model.audio.MusicSection
import com.yet.tetris.domain.model.audio.MusicSequence
import com.yet.tetris.domain.model.audio.MusicSequencePresets
import com.yet.tetris.domain.model.audio.MusicTheme
import com.yet.tetris.domain.model.audio.Note
import com.yet.tetris.domain.model.audio.WaveformType
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
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
    fun korobeinikiHasHeadroomQuietInterludeAndSmoothVoiceBoundaries() {
        // Use the same selection path as playback, including the theme's softer timbre.
        val sequence = assertNotNull(getSequenceForTheme(MusicTheme.KOROBEINIKI))
        val pcm = AudioSynthesizer.synthesizeMusicSequence(sequence, getWaveformForTheme(MusicTheme.KOROBEINIKI))
        val sectionLength = 16 * AudioSynthesizer.SAMPLE_RATE

        assertEquals(64 * AudioSynthesizer.SAMPLE_RATE, pcm.size)
        assertTrue(pcm.all { it.isFinite() && abs(it) < 0.56f })
        assertTrue(pcm.any { abs(it) > 0.3f })
        assertEquals(0f, pcm.first(), absoluteTolerance = 0.00001f)
        assertEquals(0f, pcm.last(), absoluteTolerance = 0.00001f)

        fun energy(
            start: Int,
            end: Int,
        ): Double = (start until end).sumOf { pcm[it].toDouble() * pcm[it] } / (end - start)

        assertTrue(energy(2 * sectionLength, 3 * sectionLength) < energy(0, sectionLength) * 0.85)
        assertTrue(abs(pcm.sumOf { it.toDouble() } / pcm.size) < 0.001, "No DC offset")

        sequence.sections.forEachIndexed { index, section ->
            fun boundaries(voice: List<Note>): Set<Int> {
                var boundary = index * sectionLength
                return voice
                    .map { note ->
                        boundary += (note.duration * AudioSynthesizer.SAMPLE_RATE).toInt()
                        boundary
                    }.toSet()
            }
            // Where both voices articulate, neither should be cut off mid-wave.
            (boundaries(section.melody) intersect boundaries(section.bass)).filter { it < pcm.size }.forEach { boundary ->
                assertTrue(abs(pcm[boundary] - pcm[boundary - 1]) < 0.002f, "Click at sample $boundary")
            }
        }
    }

    @Test
    fun fourEightBarSectionsKeepBothVoicesOnTheBarGrid() {
        val theme = MusicSequencePresets.KOROBEINIKI_THEME
        assertEquals(4, theme.sections.size)
        assertTrue(theme.loop)
        assertEquals(theme.notes, theme.sections.flatMap { it.melody })
        theme.sections.forEach { section ->
            assertEquals(120, section.tempo)
            listOf(section.melody, section.bass).forEach { voice ->
                var beat = 0.0
                voice.forEach { note ->
                    val duration = (note.duration / Note.QUARTER).toDouble()
                    assertTrue(duration >= 0.5)
                    assertTrue(beat % 4.0 + duration <= 4.0, "Note crosses a bar at beat $beat")
                    assertTrue(note.volume > 0f && note.volume <= 1f)
                    beat += duration
                }
                assertEquals(32.0, beat)
            }
        }
    }

    @Test
    fun accompanimentSpellsTheHarmonyAndRepeatChangesTexture() {
        val sections = MusicSequencePresets.KOROBEINIKI_THEME.sections
        val am = setOf(Note.A3, Note.C3, Note.C4, Note.E3, Note.E4)
        val dm = setOf(Note.D3, Note.F3, Note.A3)
        val e7 = setOf(Note.E3, Note.G_SHARP3, Note.B3, Note.D4)
        val f = setOf(Note.F3, Note.A3, Note.C4)
        val c = setOf(Note.C3, Note.E3, Note.G3, Note.C4)
        val verseChords = listOf(am, am, e7, am, dm, am, e7, am)
        val bridgeChords = listOf(f, c, dm, e7, f, c, dm, e7)
        sections.forEachIndexed { index, section ->
            val chords = if (index == 2) bridgeChords else verseChords
            var beat = 0.0
            section.bass.forEach { note ->
                val chord = if (index == 3 && beat >= 30.0) e7 else chords[(beat / 4).toInt()]
                assertTrue(note.frequency in chord, "Wrong chord tone in section $index at beat $beat")
                beat += note.duration / Note.QUARTER
            }
        }
        assertNotEquals(sections[0].melody.map { it.frequency }, sections[1].melody.map { it.frequency })
        assertTrue(sections[1].bass.size > sections[0].bass.size)
        assertTrue(sections[2].melody.map { it.frequency }.average() < sections[0].melody.map { it.frequency }.average())
    }

    @Test
    fun themeEnvelopesFinishBeforeTheShortestNote() {
        val theme = MusicSequencePresets.KOROBEINIKI_THEME
        listOf(assertNotNull(theme.melodyEnvelope), assertNotNull(theme.bassEnvelope)).forEach { envelope ->
            val shortest =
                theme.sections.minOf { section ->
                    (section.melody + section.bass).minOf { it.duration * 120f / section.tempo }
                }
            assertTrue(envelope.attack > 0 && envelope.release > 0)
            assertTrue(envelope.attack + envelope.decay + envelope.release <= shortest)
        }
    }

    @Test
    fun voiceEnvelopesOverrideTheCallerIndependently() {
        val silent = Envelope(attack = 0f, decay = 0f, sustain = 0f, release = 0f)
        val melodyOnly = MusicSequence(listOf(Note(Note.A4, Note.QUARTER, volume = 0.3f)))
        val withBass =
            melodyOnly.copy(
                sections = listOf(MusicSection(melodyOnly.notes, listOf(Note(Note.A3, Note.QUARTER)))),
                melodyEnvelope = flat,
                bassEnvelope = silent,
            )
        val reference = AudioSynthesizer.synthesizeMusicSequence(melodyOnly, WaveformType.SINE, flat)
        val melody = AudioSynthesizer.synthesizeMusicSequence(withBass, WaveformType.SINE, silent)
        assertContentEquals(reference, melody)

        val bassOnly = withBass.copy(melodyEnvelope = silent, bassEnvelope = flat)
        val bass = AudioSynthesizer.synthesizeMusicSequence(bassOnly, WaveformType.SINE, silent)
        assertTrue(bass.any { abs(it) > 0.1f })
        assertTrue(bass.indices.any { abs(bass[it] - melody[it]) > 0.1f })
    }
}
