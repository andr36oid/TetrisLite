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
        const val D3 = 146.83f
        const val E3 = 164.81f
        const val C4 = 261.63f
        const val D4 = 293.66f
        const val E4 = 329.63f
        const val F4 = 349.23f
        const val G4 = 392.00f
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
 */
data class MusicSequence(
    val notes: List<Note>,
    val tempo: Int = 120, // BPM
    val loop: Boolean = true,
    val sections: List<MusicSection> = emptyList(),
    val bassWaveform: WaveformType = WaveformType.TRIANGLE,
    val bassVolume: Float = 0.45f,
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
 * Predefined music sequences inspired by classic Tetris (but original compositions).
 */
object MusicSequencePresets {
    // Lengths in beats (a quarter note is one beat), for writing the Korobeiniki theme by bars.
    private fun n(
        frequency: Float,
        beats: Float,
    ) = Note(frequency, beats * Note.QUARTER)

    /** One bar of oom-pah bass: root and fifth on the four beats. */
    private fun oomPah(
        root: Float,
        fifth: Float,
    ) = listOf(n(root, 1f), n(fifth, 1f), n(root, 1f), n(fifth, 1f))

    // Korobeiniki, the Russian folk song (melody 19th century, public domain): the first
    // half of the verse ...
    private val VERSE_A =
        listOf(
            n(Note.E5, 1f), n(Note.B4, 0.5f), n(Note.C5, 0.5f), n(Note.D5, 1f), n(Note.C5, 0.5f), n(Note.B4, 0.5f),
            n(Note.A4, 1f), n(Note.A4, 0.5f), n(Note.C5, 0.5f), n(Note.E5, 1f), n(Note.D5, 0.5f), n(Note.C5, 0.5f),
            n(Note.B4, 1.5f), n(Note.C5, 0.5f), n(Note.D5, 1f), n(Note.E5, 1f),
            n(Note.C5, 1f), n(Note.A4, 1f), n(Note.A4, 2f),
        )

    // ... and the second half.
    private val VERSE_B =
        listOf(
            n(Note.D5, 1.5f), n(Note.F5, 0.5f), n(Note.A5, 1f), n(Note.G5, 0.5f), n(Note.F5, 0.5f),
            n(Note.E5, 1.5f), n(Note.C5, 0.5f), n(Note.E5, 1f), n(Note.D5, 0.5f), n(Note.C5, 0.5f),
            n(Note.B4, 1f), n(Note.B4, 0.5f), n(Note.C5, 0.5f), n(Note.D5, 1f), n(Note.E5, 1f),
            n(Note.C5, 1f), n(Note.A4, 1f), n(Note.A4, 2f),
        )

    // The verse again in running eighths, the way dance players vary a tune (written for
    // andr36oid's fork).
    private val VARIATION_A =
        listOf(
            n(Note.E5, 0.5f), n(Note.E5, 0.5f), n(Note.B4, 0.5f), n(Note.C5, 0.5f),
            n(Note.D5, 0.5f), n(Note.E5, 0.25f), n(Note.D5, 0.25f), n(Note.C5, 0.5f), n(Note.B4, 0.5f),
            n(Note.A4, 0.5f), n(Note.A4, 0.5f), n(Note.A4, 0.5f), n(Note.C5, 0.5f),
            n(Note.E5, 0.5f), n(Note.E5, 0.5f), n(Note.D5, 0.5f), n(Note.C5, 0.5f),
            n(Note.B4, 0.5f), n(Note.B4, 0.5f), n(Note.B4, 0.5f), n(Note.C5, 0.5f),
            n(Note.D5, 0.5f), n(Note.D5, 0.5f), n(Note.E5, 0.5f), n(Note.E5, 0.5f),
            n(Note.C5, 0.5f), n(Note.E5, 0.5f), n(Note.C5, 0.5f), n(Note.B4, 0.5f), n(Note.A4, 2f),
        )

    private val VARIATION_B =
        listOf(
            n(Note.D5, 0.5f), n(Note.D5, 0.5f), n(Note.D5, 0.5f), n(Note.F5, 0.5f),
            n(Note.A5, 0.5f), n(Note.A5, 0.5f), n(Note.G5, 0.5f), n(Note.F5, 0.5f),
            n(Note.E5, 0.5f), n(Note.E5, 0.5f), n(Note.E5, 0.5f), n(Note.C5, 0.5f),
            n(Note.E5, 0.5f), n(Note.F5, 0.25f), n(Note.E5, 0.25f), n(Note.D5, 0.5f), n(Note.C5, 0.5f),
            n(Note.B4, 0.5f), n(Note.B4, 0.5f), n(Note.B4, 0.5f), n(Note.C5, 0.5f),
            n(Note.D5, 0.5f), n(Note.D5, 0.5f), n(Note.E5, 0.5f), n(Note.E5, 0.5f),
            n(Note.C5, 0.5f), n(Note.E5, 0.5f), n(Note.C5, 0.5f), n(Note.B4, 0.5f), n(Note.A4, 2f),
        )

    // A plain folk accompaniment (written for andr36oid's fork): A minor and E for the first
    // half, D minor, A minor and E for the second.
    private val BASS_A =
        oomPah(Note.A2, Note.E3) + oomPah(Note.A2, Note.E3) + oomPah(Note.E2, Note.B2) + oomPah(Note.A2, Note.E3)
    private val BASS_B =
        oomPah(Note.D3, Note.A2) + oomPah(Note.A2, Note.E3) + oomPah(Note.E2, Note.B2) + oomPah(Note.A2, Note.E3)

    private val KOROBEINIKI_SECTIONS =
        listOf(
            // the whole verse, both halves twice, at a walking pace
            MusicSection(VERSE_A + VERSE_A + VERSE_B + VERSE_B, BASS_A + BASS_A + BASS_B + BASS_B, tempo = 120),
            // the variation, quicker
            MusicSection(VARIATION_A + VARIATION_B, BASS_A + BASS_B, tempo = 144),
            // and the verse once more at dance speed, before it starts over slowly
            MusicSection(VERSE_A + VERSE_A + VERSE_B + VERSE_B, BASS_A + BASS_A + BASS_B + BASS_B, tempo = 168),
        )

    /**
     * Korobeiniki, the Russian folk song: the melody is from the 19th century and in the
     * public domain. The arrangement (a variation, the bass and the faster rounds) was made
     * for andr36oid: the verse, the variation and the verse again, faster each round, with an
     * oom-pah bass. About 68 seconds before it loops.
     */
    val KOROBEINIKI_THEME =
        MusicSequence(
            notes = KOROBEINIKI_SECTIONS.flatMap { it.melody },
            tempo = 120,
            loop = true,
            sections = KOROBEINIKI_SECTIONS,
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
