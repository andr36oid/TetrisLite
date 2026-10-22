package com.yet.tetris.domain.model.audio

/**
 * Represents a musical note with frequency and duration.
 * Used for procedural music generation.
 */
data class Note(
    val frequency: Float, // Hz
    val duration: Float, // seconds
    val volume: Float = 1.0f,
) {
    companion object {
        // Musical note frequencies (A4 = 440Hz standard)
        const val E2 = 82.41f
        const val A2 = 110.00f
        const val B2 = 123.47f
        const val C3 = 130.81f
        const val D3 = 146.83f
        const val E3 = 164.81f
        const val F3 = 174.61f
        const val G3 = 196.00f
        const val G_SHARP3 = 207.65f
        const val A3 = 220.00f
        const val B3 = 246.94f
        const val C4 = 261.63f
        const val D4 = 293.66f
        const val E4 = 329.63f
        const val F4 = 349.23f
        const val G4 = 392.00f
        const val G_SHARP4 = 415.30f
        const val A4 = 440.00f
        const val B4 = 493.88f
        const val C5 = 523.25f
        const val D5 = 587.33f
        const val E5 = 659.25f
        const val F5 = 698.46f
        const val G5 = 783.99f
        const val A5 = 880.00f
        const val B5 = 987.77f
        const val C6 = 1046.50f
        const val D6 = 1174.66f
        const val E6 = 1318.51f
        const val F6 = 1396.91f

        // Common note durations (at 120 BPM)
        const val WHOLE = 2.0f
        const val HALF = 1.0f
        const val QUARTER = 0.5f
        const val EIGHTH = 0.25f
        const val SIXTEENTH = 0.125f
    }
}

/**
 * Waveform types for sound synthesis.
 */
enum class WaveformType {
    SINE, // Pure tone, smooth
    SQUARE, // Retro, harsh
    TRIANGLE, // Softer than square
    SAWTOOTH, // Bright, buzzy
    NOISE, // White noise for percussion
}

/**
 * ADSR envelope for shaping sound over time.
 * Attack, Decay, Sustain, Release
 */
data class Envelope(
    val attack: Float = 0.01f, // seconds
    val decay: Float = 0.1f, // seconds
    val sustain: Float = 0.7f, // level (0-1)
    val release: Float = 0.2f, // seconds
)

/**
 * Parameters for procedural sound effect generation.
 */
data class SoundEffectParams(
    val waveform: WaveformType,
    val startFrequency: Float,
    val endFrequency: Float = startFrequency,
    val duration: Float,
    val envelope: Envelope = Envelope(),
    val volume: Float = 1.0f,
)

/**
 * One part of a longer piece: a melody, an optional bass voice under it and the tempo for
 * both. The bass is cut to the melody's length.
 */
data class MusicSection(
    val melody: List<Note>,
    val bass: List<Note> = emptyList(),
    val tempo: Int = 120, // BPM
)

/**
 * Music sequence definition for procedural generation.
 *
 * A sequence with [sections] plays them one after another, each at its own tempo and with
 * its bass voice; [notes] and [tempo] then aren't used for the sound.
 * Optional [melodyEnvelope] and [bassEnvelope] override the renderer's fallback envelope.
 */
data class MusicSequence(
    val notes: List<Note>,
    val tempo: Int = 120, // BPM
    val loop: Boolean = true,
    val sections: List<MusicSection> = emptyList(),
    val bassWaveform: WaveformType = WaveformType.TRIANGLE,
    val bassVolume: Float = 0.45f,
    // Null preserves the envelope supplied by the caller for existing themes.
    val melodyEnvelope: Envelope? = null,
    val bassEnvelope: Envelope? = null,
)

/**
 * Predefined sound effect parameters for common game events.
 */
object SoundEffectPresets {
    val PIECE_MOVE =
        SoundEffectParams(
            waveform = WaveformType.SQUARE,
            startFrequency = 200f,
            duration = 0.05f,
            envelope = Envelope(attack = 0.01f, decay = 0.02f, sustain = 0.5f, release = 0.02f),
            volume = 0.3f,
        )

    val PIECE_ROTATE =
        SoundEffectParams(
            waveform = WaveformType.TRIANGLE,
            startFrequency = 400f,
            endFrequency = 600f,
            duration = 0.08f,
            envelope = Envelope(attack = 0.01f, decay = 0.03f, sustain = 0.6f, release = 0.04f),
            volume = 0.4f,
        )

    val PIECE_DROP =
        SoundEffectParams(
            waveform = WaveformType.SQUARE,
            startFrequency = 150f,
            endFrequency = 80f,
            duration = 0.15f,
            envelope = Envelope(attack = 0.01f, decay = 0.05f, sustain = 0.4f, release = 0.09f),
            volume = 0.5f,
        )

    val LINE_CLEAR =
        SoundEffectParams(
            waveform = WaveformType.SAWTOOTH,
            startFrequency = 300f,
            endFrequency = 800f,
            duration = 0.3f,
            envelope = Envelope(attack = 0.02f, decay = 0.1f, sustain = 0.7f, release = 0.18f),
            volume = 0.6f,
        )

    val TETRIS =
        SoundEffectParams(
            waveform = WaveformType.SAWTOOTH,
            startFrequency = 400f,
            endFrequency = 1200f,
            duration = 0.5f,
            envelope = Envelope(attack = 0.02f, decay = 0.15f, sustain = 0.8f, release = 0.33f),
            volume = 0.7f,
        )

    val GAME_OVER =
        SoundEffectParams(
            waveform = WaveformType.TRIANGLE,
            startFrequency = 440f,
            endFrequency = 110f,
            duration = 1.0f,
            envelope = Envelope(attack = 0.05f, decay = 0.3f, sustain = 0.5f, release = 0.65f),
            volume = 0.6f,
        )

    val LEVEL_UP =
        SoundEffectParams(
            waveform = WaveformType.SAWTOOTH,
            startFrequency = 500f,
            endFrequency = 1000f,
            duration = 0.5f,
            envelope = Envelope(attack = 0.05f, decay = 0.15f, sustain = 0.7f, release = 0.25f),
            volume = 0.7f,
        )
}

/**
 * Game music: original compositions and an original arrangement of a public-domain folk melody.
 */
object MusicSequencePresets {
    // Lengths in beats (a quarter note is one beat), for writing the Korobeiniki theme by bars.
    private fun n(
        frequency: Float,
        beats: Float,
    ) = Note(frequency, beats * Note.QUARTER)

    /** Light downbeats, softer upbeats and a breath at the end of each eight-bar phrase. */
    private fun phrased(
        notes: List<Note>,
        level: Float,
    ): List<Note> {
        val beats = notes.sumOf { (it.duration / Note.QUARTER).toDouble() }
        var beat = 0.0
        return notes.map { note ->
            val accent =
                when (beat % 4.0) {
                    0.0 -> 1.0f
                    2.0 -> 0.94f
                    1.0, 3.0 -> 0.88f
                    else -> 0.80f
                }
            val cadence = if (beat >= beats - 4.0) 0.88f else 1.0f
            val upperRegister = if (note.frequency >= Note.G5) 0.88f else 1.0f
            beat += note.duration / Note.QUARTER
            note.copy(volume = note.volume * level * accent * cadence * upperRegister)
        }
    }

    // Korobeiniki, the Russian folk song (melody 19th century, public domain): the first
    // half of the verse ...
    private val VERSE_A =
        listOf(
            // Bar 1: Am
            n(Note.E5, 1f),
            n(Note.B4, 0.5f),
            n(Note.C5, 0.5f),
            n(Note.D5, 1f),
            n(Note.C5, 0.5f),
            n(Note.B4, 0.5f),
            // Bar 2: Am/C
            n(Note.A4, 1f),
            n(Note.A4, 0.5f),
            n(Note.C5, 0.5f),
            n(Note.E5, 1f),
            n(Note.D5, 0.5f),
            n(Note.C5, 0.5f),
            // Bar 3: E7/B
            n(Note.B4, 1.5f),
            n(Note.C5, 0.5f),
            n(Note.D5, 1f),
            n(Note.E5, 1f),
            // Bar 4: Am
            n(Note.C5, 1f),
            n(Note.A4, 1f),
            n(Note.A4, 2f),
        )

    // ... and the second half.
    private val VERSE_B =
        listOf(
            // Bar 5: Dm
            n(Note.D5, 1.5f),
            n(Note.F5, 0.5f),
            n(Note.A5, 1f),
            n(Note.G5, 0.5f),
            n(Note.F5, 0.5f),
            // Bar 6: Am/C
            n(Note.E5, 1.5f),
            n(Note.C5, 0.5f),
            n(Note.E5, 1f),
            n(Note.D5, 0.5f),
            n(Note.C5, 0.5f),
            // Bar 7: E7
            n(Note.B4, 1f),
            n(Note.B4, 0.5f),
            n(Note.C5, 0.5f),
            n(Note.D5, 1f),
            n(Note.E5, 1f),
            // Bar 8: Am
            n(Note.C5, 1f),
            n(Note.A4, 1f),
            n(Note.A4, 2f),
        )

    // Original answering figures around the folk melody. Eighths leave space for the
    // accompaniment; G sharp gives the E7 bars a clear pull back to A minor.
    private val VARIATION =
        listOf(
            // Bar 1: Am
            n(Note.E5, 1f),
            n(Note.B4, 0.5f),
            n(Note.C5, 0.5f),
            n(Note.D5, 0.5f),
            n(Note.E5, 0.5f),
            n(Note.C5, 0.5f),
            n(Note.B4, 0.5f),
            // Bar 2: Am/C
            n(Note.A4, 0.5f),
            n(Note.B4, 0.5f),
            n(Note.C5, 0.5f),
            n(Note.A4, 0.5f),
            n(Note.E5, 1f),
            n(Note.D5, 0.5f),
            n(Note.C5, 0.5f),
            // Bar 3: E7/B
            n(Note.B4, 1f),
            n(Note.G_SHARP4, 0.5f),
            n(Note.B4, 0.5f),
            n(Note.D5, 0.5f),
            n(Note.C5, 0.5f),
            n(Note.B4, 0.5f),
            n(Note.E5, 0.5f),
            // Bar 4: Am
            n(Note.C5, 0.5f),
            n(Note.B4, 0.5f),
            n(Note.A4, 1f),
            n(Note.E5, 0.5f),
            n(Note.C5, 0.5f),
            n(Note.A4, 1f),
            // Bar 5: Dm
            n(Note.D5, 1f),
            n(Note.F5, 0.5f),
            n(Note.E5, 0.5f),
            n(Note.A5, 0.5f),
            n(Note.G5, 0.5f),
            n(Note.F5, 1f),
            // Bar 6: Am/C
            n(Note.E5, 0.5f),
            n(Note.G5, 0.5f),
            n(Note.E5, 0.5f),
            n(Note.C5, 0.5f),
            n(Note.E5, 1f),
            n(Note.D5, 0.5f),
            n(Note.C5, 0.5f),
            // Bar 7: E7
            n(Note.B4, 1f),
            n(Note.G_SHARP4, 0.5f),
            n(Note.B4, 0.5f),
            n(Note.D5, 0.5f),
            n(Note.B4, 0.5f),
            n(Note.E5, 1f),
            // Bar 8: Am
            n(Note.C5, 0.5f),
            n(Note.B4, 0.5f),
            n(Note.A4, 1f),
            n(Note.C5, 0.5f),
            n(Note.B4, 0.5f),
            n(Note.A4, 1f),
        )

    // New eight-bar interlude: a lower, quieter response, not another folk tune or a
    // transcription of a game arrangement. F | C/E | Dm | E7 | F | C | Dm | E7.
    private val INTERLUDE =
        listOf(
            // Bar 1: F
            n(Note.A4, 1.5f),
            n(Note.C5, 0.5f),
            n(Note.A4, 1f),
            n(Note.G4, 1f),
            // Bar 2: C/E
            n(Note.G4, 1f),
            n(Note.E4, 1f),
            n(Note.G4, 0.5f),
            n(Note.C5, 0.5f),
            n(Note.B4, 1f),
            // Bar 3: Dm
            n(Note.A4, 1f),
            n(Note.F4, 0.5f),
            n(Note.A4, 0.5f),
            n(Note.D5, 1f),
            n(Note.C5, 1f),
            // Bar 4: E7
            n(Note.B4, 1.5f),
            n(Note.G_SHARP4, 0.5f),
            n(Note.E4, 2f),
            // Bar 5: F
            n(Note.C5, 1f),
            n(Note.A4, 0.5f),
            n(Note.C5, 0.5f),
            n(Note.F5, 1f),
            n(Note.E5, 1f),
            // Bar 6: C
            n(Note.E5, 1f),
            n(Note.C5, 1f),
            n(Note.G4, 1f),
            n(Note.E4, 1f),
            // Bar 7: Dm
            n(Note.F4, 1f),
            n(Note.A4, 0.5f),
            n(Note.C5, 0.5f),
            n(Note.D5, 1f),
            n(Note.F4, 1f),
            // Bar 8: E7
            n(Note.E4, 1f),
            n(Note.G_SHARP4, 1f),
            n(Note.B4, 1f),
            n(Note.D5, 1f),
        )

    // One monophonic accompaniment supplies bass AND harmony, rather than adding more
    // oscillators. Keep most of it in the audible 165–330 Hz range of a small speaker.
    // Am | Am/C | E7/B | Am | Dm | Am/C | E7 | Am.
    private val WALKING_BASS_A =
        listOf(
            // Bar 1: Am
            n(Note.A3, 1f),
            n(Note.C4, 1f),
            n(Note.E3, 1f),
            n(Note.C4, 1f),
            // Bar 2: Am/C
            n(Note.C3, 1f),
            n(Note.E3, 1f),
            n(Note.A3, 1f),
            n(Note.C4, 1f),
            // Bar 3: E7/B
            n(Note.B3, 1f),
            n(Note.D4, 1f),
            n(Note.E3, 1f),
            n(Note.G_SHARP3, 1f),
            // Bar 4: Am
            n(Note.A3, 1f),
            n(Note.E3, 1f),
            n(Note.A3, 2f),
        )
    private val WALKING_BASS_B =
        listOf(
            // Bar 5: Dm
            n(Note.D3, 1f),
            n(Note.F3, 1f),
            n(Note.A3, 1f),
            n(Note.F3, 1f),
            // Bar 6: Am/C
            n(Note.C3, 1f),
            n(Note.E3, 1f),
            n(Note.A3, 1f),
            n(Note.C4, 1f),
            // Bar 7: E7
            n(Note.E3, 1f),
            n(Note.G_SHARP3, 1f),
            n(Note.B3, 1f),
            n(Note.D4, 1f),
            // Bar 8: Am
            n(Note.A3, 1f),
            n(Note.C4, 1f),
            n(Note.A3, 2f),
        )

    // Broken chords on the repeat; the bass landing lasts a beat, with lighter eighths
    // above it. Each line is one complete 4/4 bar, including the cadential breath.
    private val BROKEN_CHORDS_A =
        listOf(
            // Bar 1: Am
            n(Note.A3, 1f),
            n(Note.E4, 0.5f),
            n(Note.C4, 0.5f),
            n(Note.E3, 1f),
            n(Note.C4, 0.5f),
            n(Note.E4, 0.5f),
            // Bar 2: Am/C
            n(Note.C3, 1f),
            n(Note.E3, 0.5f),
            n(Note.A3, 0.5f),
            n(Note.E3, 1f),
            n(Note.C4, 0.5f),
            n(Note.A3, 0.5f),
            // Bar 3: E7/B
            n(Note.B3, 1f),
            n(Note.D4, 0.5f),
            n(Note.G_SHARP3, 0.5f),
            n(Note.E3, 1f),
            n(Note.B3, 0.5f),
            n(Note.D4, 0.5f),
            // Bar 4: Am
            n(Note.A3, 1f),
            n(Note.C4, 0.5f),
            n(Note.E4, 0.5f),
            n(Note.E3, 1f),
            n(Note.A3, 1f),
        )
    private val BROKEN_CHORDS_B =
        listOf(
            // Bar 5: Dm
            n(Note.D3, 1f),
            n(Note.F3, 0.5f),
            n(Note.A3, 0.5f),
            n(Note.A3, 1f),
            n(Note.F3, 0.5f),
            n(Note.A3, 0.5f),
            // Bar 6: Am/C
            n(Note.C3, 1f),
            n(Note.E3, 0.5f),
            n(Note.A3, 0.5f),
            n(Note.E3, 1f),
            n(Note.C4, 0.5f),
            n(Note.A3, 0.5f),
            // Bar 7: E7
            n(Note.E3, 1f),
            n(Note.G_SHARP3, 0.5f),
            n(Note.B3, 0.5f),
            n(Note.D4, 1f),
            n(Note.B3, 0.5f),
            n(Note.G_SHARP3, 0.5f),
        )
    private val BROKEN_CHORD_CADENCE =
        listOf(
            // Bar 8: Am
            n(Note.A3, 1f),
            n(Note.E4, 0.5f),
            n(Note.C4, 0.5f),
            n(Note.E3, 1f),
            n(Note.A3, 1f),
        )

    private val INTERLUDE_BASS =
        listOf(
            // Bar 1: F
            n(Note.F3, 2f),
            n(Note.A3, 1f),
            n(Note.C4, 1f),
            // Bar 2: C/E
            n(Note.E3, 2f),
            n(Note.G3, 1f),
            n(Note.C4, 1f),
            // Bar 3: Dm
            n(Note.D3, 2f),
            n(Note.F3, 1f),
            n(Note.A3, 1f),
            // Bar 4: E7
            n(Note.E3, 2f),
            n(Note.B3, 1f),
            n(Note.G_SHARP3, 1f),
            // Bar 5: F
            n(Note.F3, 2f),
            n(Note.C4, 1f),
            n(Note.A3, 1f),
            // Bar 6: C
            n(Note.C3, 2f),
            n(Note.E3, 1f),
            n(Note.G3, 1f),
            // Bar 7: Dm
            n(Note.D3, 2f),
            n(Note.A3, 1f),
            n(Note.F3, 1f),
            // Bar 8: E7
            n(Note.E3, 2f),
            n(Note.G_SHARP3, 1f),
            n(Note.B3, 1f),
        )

    // The return alternates the walking and broken-chord textures. Its last two beats
    // turn to E7, leading naturally into A minor at the start of the loop.
    private val RETURN_MELODY =
        VERSE_A + VERSE_B.dropLast(3) +
            listOf(n(Note.C5, 1f), n(Note.A4, 1f), n(Note.E4, 1f), n(Note.G_SHARP4, 1f))
    private val RETURN_BASS =
        WALKING_BASS_A + BROKEN_CHORDS_B +
            listOf(n(Note.A3, 1f), n(Note.C4, 1f), n(Note.E3, 1f), n(Note.G_SHARP3, 1f))

    private val KOROBEINIKI_SECTIONS =
        listOf(
            MusicSection(phrased(VERSE_A + VERSE_B, 0.88f), phrased(WALKING_BASS_A + WALKING_BASS_B, 0.80f), tempo = 120),
            MusicSection(phrased(VARIATION, 0.82f), phrased(BROKEN_CHORDS_A + BROKEN_CHORDS_B + BROKEN_CHORD_CADENCE, 0.74f), tempo = 120),
            MusicSection(phrased(INTERLUDE, 0.72f), phrased(INTERLUDE_BASS, 0.66f), tempo = 120),
            MusicSection(phrased(RETURN_MELODY, 0.86f), phrased(RETURN_BASS, 0.76f), tempo = 120),
        )

    /**
     * Public-domain 19th-century Korobeiniki melody, retained from this fork's note data.
     * Original 32-bar arrangement for andr36oid: eight-bar verse, ornamented repeat,
     * quieter new interlude and returning verse with a dominant turnaround. No published
     * game arrangement or external transcription was used. Steady 120 BPM, 64-second loop.
     * Two soft triangle voices: a plucked lead and bass/broken chords, without effects or
     * extra oscillators. Envelopes finish within the shortest (eighth) note.
     */
    val KOROBEINIKI_THEME =
        MusicSequence(
            notes = KOROBEINIKI_SECTIONS.flatMap { it.melody },
            tempo = 120,
            loop = true,
            sections = KOROBEINIKI_SECTIONS,
            bassVolume = 0.38f,
            melodyEnvelope = Envelope(attack = 0.012f, decay = 0.075f, sustain = 0.60f, release = 0.055f),
            bassEnvelope = Envelope(attack = 0.008f, decay = 0.09f, sustain = 0.35f, release = 0.045f),
        )

    /**
     * Classic chiptune-inspired melody.
     * Original composition inspired by Russian folk music style.
     */
    val CLASSIC_THEME =
        MusicSequence(
            notes =
                listOf(
                    // Phrase 1
                    Note(Note.E5, Note.QUARTER),
                    Note(Note.B4, Note.EIGHTH),
                    Note(Note.C5, Note.EIGHTH),
                    Note(Note.D5, Note.QUARTER),
                    Note(Note.C5, Note.EIGHTH),
                    Note(Note.B4, Note.EIGHTH),
                    Note(Note.A4, Note.QUARTER),
                    Note(Note.A4, Note.EIGHTH),
                    Note(Note.C5, Note.EIGHTH),
                    Note(Note.E5, Note.QUARTER),
                    Note(Note.D5, Note.EIGHTH),
                    Note(Note.C5, Note.EIGHTH),
                    Note(Note.B4, Note.QUARTER + Note.EIGHTH),
                    Note(Note.C5, Note.EIGHTH),
                    Note(Note.D5, Note.QUARTER),
                    Note(Note.E5, Note.QUARTER),
                    Note(Note.C5, Note.QUARTER),
                    Note(Note.A4, Note.QUARTER),
                    Note(Note.A4, Note.HALF),
                    // Phrase 2
                    Note(Note.D5, Note.QUARTER + Note.EIGHTH),
                    Note(Note.F5, Note.EIGHTH),
                    Note(Note.A5, Note.QUARTER),
                    Note(Note.G5, Note.EIGHTH),
                    Note(Note.F5, Note.EIGHTH),
                    Note(Note.E5, Note.QUARTER + Note.EIGHTH),
                    Note(Note.C5, Note.EIGHTH),
                    Note(Note.E5, Note.QUARTER),
                    Note(Note.D5, Note.EIGHTH),
                    Note(Note.C5, Note.EIGHTH),
                    Note(Note.B4, Note.QUARTER),
                    Note(Note.B4, Note.EIGHTH),
                    Note(Note.C5, Note.EIGHTH),
                    Note(Note.D5, Note.QUARTER),
                    Note(Note.E5, Note.QUARTER),
                    Note(Note.C5, Note.QUARTER),
                    Note(Note.A4, Note.QUARTER),
                    Note(Note.A4, Note.HALF),
                ),
            tempo = 144,
            loop = true,
        )

    /**
     * Modern electronic style melody.
     */
    val MODERN_THEME =
        MusicSequence(
            notes =
                listOf(
                    Note(Note.C5, Note.EIGHTH),
                    Note(Note.E5, Note.EIGHTH),
                    Note(Note.G5, Note.EIGHTH),
                    Note(Note.E5, Note.EIGHTH),
                    Note(Note.C5, Note.EIGHTH),
                    Note(Note.E5, Note.EIGHTH),
                    Note(Note.G5, Note.QUARTER),
                    Note(Note.A4, Note.EIGHTH),
                    Note(Note.C5, Note.EIGHTH),
                    Note(Note.E5, Note.EIGHTH),
                    Note(Note.C5, Note.EIGHTH),
                    Note(Note.A4, Note.EIGHTH),
                    Note(Note.C5, Note.EIGHTH),
                    Note(Note.E5, Note.QUARTER),
                ),
            tempo = 128,
            loop = true,
        )

    val MINIMAL_THEME =
        MusicSequence(
            notes =
                listOf(
                    Note(Note.C4, Note.QUARTER),
                    Note(Note.E4, Note.QUARTER),
                    Note(Note.G4, Note.HALF),
                    Note(Note.A4, Note.QUARTER),
                    Note(Note.G4, Note.QUARTER),
                    Note(Note.E4, Note.HALF),
                ),
            tempo = 100,
            loop = true,
        )

    val ARCADE_THEME =
        MusicSequence(
            notes =
                listOf(
                    Note(Note.E5, Note.SIXTEENTH, volume = 0.85f),
                    Note(Note.G5, Note.SIXTEENTH, volume = 0.85f),
                    Note(Note.B5, Note.SIXTEENTH, volume = 0.85f),
                    Note(Note.E6, Note.EIGHTH, volume = 0.9f),
                    Note(Note.D6, Note.SIXTEENTH, volume = 0.82f),
                    Note(Note.B5, Note.SIXTEENTH, volume = 0.82f),
                    Note(Note.G5, Note.SIXTEENTH, volume = 0.82f),
                    Note(Note.A5, Note.EIGHTH, volume = 0.84f),
                    Note(Note.E5, Note.SIXTEENTH, volume = 0.85f),
                    Note(Note.G5, Note.SIXTEENTH, volume = 0.85f),
                    Note(Note.C6, Note.SIXTEENTH, volume = 0.88f),
                    Note(Note.E6, Note.EIGHTH, volume = 0.92f),
                    Note(Note.D6, Note.SIXTEENTH, volume = 0.82f),
                    Note(Note.C6, Note.SIXTEENTH, volume = 0.82f),
                    Note(Note.A5, Note.SIXTEENTH, volume = 0.82f),
                    Note(Note.G5, Note.EIGHTH, volume = 0.8f),
                    Note(Note.F5, Note.SIXTEENTH, volume = 0.78f),
                    Note(Note.A5, Note.SIXTEENTH, volume = 0.8f),
                    Note(Note.C6, Note.SIXTEENTH, volume = 0.84f),
                    Note(Note.F6, Note.EIGHTH, volume = 0.9f),
                    Note(Note.E6, Note.SIXTEENTH, volume = 0.82f),
                    Note(Note.C6, Note.SIXTEENTH, volume = 0.82f),
                    Note(Note.A5, Note.SIXTEENTH, volume = 0.82f),
                    Note(Note.B5, Note.EIGHTH, volume = 0.84f),
                ),
            tempo = 156,
            loop = true,
        )

    val DUSK_THEME =
        MusicSequence(
            notes =
                listOf(
                    Note(Note.A4, Note.QUARTER, volume = 0.55f),
                    Note(Note.C5, Note.EIGHTH, volume = 0.5f),
                    Note(Note.E5, Note.QUARTER, volume = 0.58f),
                    Note(Note.G5, Note.EIGHTH, volume = 0.48f),
                    Note(Note.E5, Note.QUARTER, volume = 0.56f),
                    Note(Note.C5, Note.EIGHTH, volume = 0.5f),
                    Note(Note.D5, Note.QUARTER, volume = 0.54f),
                    Note(Note.F5, Note.EIGHTH, volume = 0.5f),
                    Note(Note.A5, Note.QUARTER, volume = 0.58f),
                    Note(Note.G5, Note.EIGHTH, volume = 0.48f),
                    Note(Note.E5, Note.QUARTER, volume = 0.56f),
                    Note(Note.C5, Note.QUARTER, volume = 0.5f),
                ),
            tempo = 92,
            loop = true,
        )

    val BATTLE_THEME =
        MusicSequence(
            notes =
                listOf(
                    Note(Note.E4, Note.EIGHTH, volume = 0.8f),
                    Note(Note.E4, Note.EIGHTH, volume = 0.76f),
                    Note(Note.G4, Note.SIXTEENTH, volume = 0.82f),
                    Note(Note.A4, Note.SIXTEENTH, volume = 0.84f),
                    Note(Note.B4, Note.EIGHTH, volume = 0.88f),
                    Note(Note.A4, Note.SIXTEENTH, volume = 0.8f),
                    Note(Note.G4, Note.SIXTEENTH, volume = 0.78f),
                    Note(Note.E4, Note.EIGHTH, volume = 0.82f),
                    Note(Note.D4, Note.EIGHTH, volume = 0.76f),
                    Note(Note.E4, Note.EIGHTH, volume = 0.8f),
                    Note(Note.G4, Note.SIXTEENTH, volume = 0.82f),
                    Note(Note.A4, Note.SIXTEENTH, volume = 0.84f),
                    Note(Note.C5, Note.EIGHTH, volume = 0.9f),
                    Note(Note.B4, Note.SIXTEENTH, volume = 0.82f),
                    Note(Note.G4, Note.SIXTEENTH, volume = 0.8f),
                    Note(Note.E4, Note.EIGHTH, volume = 0.84f),
                ),
            tempo = 138,
            loop = true,
        )
}
