package com.yet.tetris.data.music

import com.yet.tetris.domain.model.audio.Envelope
import com.yet.tetris.domain.model.audio.MusicSection
import com.yet.tetris.domain.model.audio.MusicSequence
import com.yet.tetris.domain.model.audio.Note
import com.yet.tetris.domain.model.audio.SoundEffectParams
import com.yet.tetris.domain.model.audio.WaveformType
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

/**
 * A singleton object responsible for generating raw audio data (PCM) from descriptive parameters.
 * This synthesizer is platform-agnostic and operates entirely in the commonMain module.
 * It creates waveforms, applies envelopes, and constructs sound effects and music sequences.
 */
object AudioSynthesizer {
    /**
     * The sample rate in Hertz (samples per second). 44100 Hz is CD quality and standard for audio processing.
     */
    const val SAMPLE_RATE = 44100
    private const val NYQUIST_FREQUENCY = SAMPLE_RATE / 2.0
    private const val MAX_HARMONICS = 32
    private const val TARGET_PEAK = 0.92f
    private const val EDGE_FADE_SAMPLES = 32
    private const val WAVETABLE_SIZE = 4096

    /**
     * Generates PCM audio data for a single sound effect.
     *
     * @param params The descriptive parameters for the sound effect to generate.
     * @return A FloatArray containing the raw PCM data, with values ranging from -1.0f to 1.0f.
     */
    fun synthesizeSoundEffect(params: SoundEffectParams): FloatArray {
        val numSamples = (params.duration * SAMPLE_RATE).toInt()
        if (numSamples <= 0) return FloatArray(0)

        val pcmData = FloatArray(numSamples)
        val envelope = params.envelope

        // Calculate the duration of each envelope phase in samples.
        val attackSamples = (envelope.attack * SAMPLE_RATE).toInt()
        val decaySamples = (envelope.decay * SAMPLE_RATE).toInt()
        val releaseSamples = (envelope.release * SAMPLE_RATE).toInt()

        // Ensure sustain phase is not negative if the other phases are too long.
        val sustainSamples = max(0, numSamples - attackSamples - decaySamples - releaseSamples)

        val sustainAmplitude = params.volume * envelope.sustain

        var phase = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples

            // 1. Calculate the amplitude multiplier based on the ADSR envelope.
            val amplitude =
                envelopeAmplitude(
                    sampleIndex = i,
                    attackSamples = attackSamples,
                    decaySamples = decaySamples,
                    sustainSamples = sustainSamples,
                    releaseSamples = releaseSamples,
                    peakAmplitude = params.volume,
                    sustainAmplitude = sustainAmplitude,
                    totalSamples = numSamples,
                )

            // 2. Calculate the current frequency, allowing for pitch slides.
            val currentFrequency = params.startFrequency + (params.endFrequency - params.startFrequency) * progress

            // 3. Generate the raw sample value based on the chosen waveform.
            val rawValue = oscillatorSample(params.waveform, phase, currentFrequency)
            phase = advancePhase(phase, currentFrequency)

            // 4. Combine the raw waveform value with the final calculated amplitude.
            pcmData[i] = rawValue * amplitude
        }
        return finalizePcm(pcmData)
    }

    /**
     * Generates PCM audio data for a complete musical sequence.
     * It synthesizes each note individually and concatenates them into a single audio buffer.
     *
     * @param sequence The music sequence to synthesize.
     * @param waveform The waveform to use for all notes in the sequence (e.g., SQUARE for chiptune).
     * @param noteEnvelope An optional ADSR envelope to apply to each note, creating a "plucking" or "fading" effect.
     * @return A FloatArray containing the raw PCM data for the entire sequence.
     */
    fun synthesizeMusicSequence(
        sequence: MusicSequence,
        waveform: WaveformType = WaveformType.SQUARE,
        noteEnvelope: Envelope = Envelope(attack = 0.01f, decay = 0.2f, sustain = 0.5f, release = 0.05f),
    ): FloatArray {
        val sections =
            sequence.sections.ifEmpty { listOf(MusicSection(sequence.notes, tempo = sequence.tempo)) }
        val sectionSamples = sections.map { voiceSamples(it.melody, 120.0f / it.tempo) }
        val totalSamples = sectionSamples.sum()
        if (totalSamples <= 0) return FloatArray(0)

        // Every voice is added into the one buffer, so a long piece needs no second copy.
        val finalPcm = FloatArray(totalSamples)
        val tables = mutableMapOf<Pair<WaveformType, Float>, FloatArray?>()
        var offset = 0
        sections.forEachIndexed { index, section ->
            val tempoFactor = 120.0f / section.tempo
            val end = offset + sectionSamples[index]
            addVoice(finalPcm, offset, end, section.melody, tempoFactor, waveform, noteEnvelope, 1.0f, tables)
            if (section.bass.isNotEmpty()) {
                addVoice(
                    finalPcm, offset, end, section.bass, tempoFactor,
                    sequence.bassWaveform, noteEnvelope, sequence.bassVolume, tables,
                )
            }
            offset = end
        }
        return finalizePcm(finalPcm)
    }

    private fun voiceSamples(
        notes: List<Note>,
        tempoFactor: Float,
    ): Int = notes.sumOf { note -> max(0, (note.duration * tempoFactor * SAMPLE_RATE).toInt()) }

    /** Synthesizes [notes] one after another and adds them into [pcm] from [start], up to [end]. */
    private fun addVoice(
        pcm: FloatArray,
        start: Int,
        end: Int,
        notes: List<Note>,
        tempoFactor: Float,
        waveform: WaveformType,
        noteEnvelope: Envelope,
        voiceVolume: Float,
        tables: MutableMap<Pair<WaveformType, Float>, FloatArray?>,
    ) {
        var writeIndex = start
        for (note in notes) {
            if (writeIndex >= end) return
            val noteDuration = note.duration * tempoFactor

            // Generate audio for a single note using a simplified version of the SFX synthesizer.
            val numSamples = (noteDuration * SAMPLE_RATE).toInt()
            if (numSamples <= 0) continue

            val attackSamples = (noteEnvelope.attack * SAMPLE_RATE).toInt()
            val decaySamples = (noteEnvelope.decay * SAMPLE_RATE).toInt()
            val releaseSamples = (noteEnvelope.release * SAMPLE_RATE).toInt()
            val sustainSamples = max(0, numSamples - attackSamples - decaySamples - releaseSamples)

            val peakAmplitude = note.volume * voiceVolume
            val sustainAmplitude = peakAmplitude * noteEnvelope.sustain
            val table = tables.getOrPut(waveform to note.frequency) { wavetable(waveform, note.frequency) }
            var phase = 0.0

            for (i in 0 until numSamples) {
                if (writeIndex >= end) return
                val amplitude =
                    envelopeAmplitude(
                        sampleIndex = i,
                        attackSamples = attackSamples,
                        decaySamples = decaySamples,
                        sustainSamples = sustainSamples,
                        releaseSamples = releaseSamples,
                        peakAmplitude = peakAmplitude,
                        sustainAmplitude = sustainAmplitude,
                        totalSamples = numSamples,
                    )

                val rawValue =
                    if (table != null) {
                        tableSample(table, phase)
                    } else {
                        oscillatorSample(waveform = waveform, phase = phase, frequency = note.frequency)
                    }
                pcm[writeIndex++] += rawValue * amplitude
                phase = advancePhase(phase, note.frequency)
            }
        }
    }

    /**
     * One period of the band-limited [waveform] at [frequency]. A note reads its samples
     * from here instead of summing up to [MAX_HARMONICS] sines for every sample, which made
     * a minute of music take seconds on a slow phone. Noise has no period: null.
     */
    private fun wavetable(
        waveform: WaveformType,
        frequency: Float,
    ): FloatArray? {
        if (waveform == WaveformType.NOISE) return null
        return FloatArray(WAVETABLE_SIZE) { i ->
            oscillatorSample(waveform, 2.0 * PI * i / WAVETABLE_SIZE, frequency)
        }
    }

    private fun tableSample(
        table: FloatArray,
        phase: Double,
    ): Float {
        val position = phase / (2.0 * PI) * WAVETABLE_SIZE
        val index = position.toInt() % WAVETABLE_SIZE
        val next = (index + 1) % WAVETABLE_SIZE
        val fraction = (position - position.toInt()).toFloat()
        return table[index] + (table[next] - table[index]) * fraction
    }

    private fun envelopeAmplitude(
        sampleIndex: Int,
        attackSamples: Int,
        decaySamples: Int,
        sustainSamples: Int,
        releaseSamples: Int,
        peakAmplitude: Float,
        sustainAmplitude: Float,
        totalSamples: Int,
    ): Float =
        when {
            attackSamples > 0 && sampleIndex < attackSamples -> {
                val attackProgress = sampleIndex.toFloat() / attackSamples
                attackProgress * peakAmplitude
            }

            decaySamples > 0 && sampleIndex < attackSamples + decaySamples -> {
                val decayProgress = (sampleIndex - attackSamples).toFloat() / decaySamples
                peakAmplitude * (1.0f - decayProgress) + sustainAmplitude * decayProgress
            }

            sampleIndex < attackSamples + decaySamples + sustainSamples -> sustainAmplitude

            releaseSamples > 0 -> {
                val releaseProgress = (sampleIndex - (totalSamples - releaseSamples)).toFloat() / releaseSamples
                sustainAmplitude * (1.0f - releaseProgress)
            }

            else -> 0f
        }

    private fun oscillatorSample(
        waveform: WaveformType,
        phase: Double,
        frequency: Float,
    ): Float =
        when (waveform) {
            WaveformType.SINE -> sin(phase).toFloat()
            WaveformType.SQUARE -> bandLimitedSquare(phase, frequency)
            WaveformType.TRIANGLE -> bandLimitedTriangle(phase, frequency)
            WaveformType.SAWTOOTH -> bandLimitedSaw(phase, frequency)
            WaveformType.NOISE -> Random.nextFloat() * 2.0f - 1.0f
        }

    private fun bandLimitedSquare(
        phase: Double,
        frequency: Float,
    ): Float {
        val harmonics = harmonicLimit(frequency, oddOnly = true)
        var sum = 0.0
        var harmonic = 1
        while (harmonic <= harmonics) {
            sum += sin(phase * harmonic) / harmonic
            harmonic += 2
        }
        return (4.0 / PI * sum).toFloat()
    }

    private fun bandLimitedTriangle(
        phase: Double,
        frequency: Float,
    ): Float {
        val harmonics = harmonicLimit(frequency, oddOnly = true)
        var sum = 0.0
        var harmonic = 1
        var signFlip = 1.0
        while (harmonic <= harmonics) {
            sum += signFlip * (sin(phase * harmonic) / (harmonic * harmonic))
            signFlip *= -1.0
            harmonic += 2
        }
        return (8.0 / (PI * PI) * sum).toFloat()
    }

    private fun bandLimitedSaw(
        phase: Double,
        frequency: Float,
    ): Float {
        val harmonics = harmonicLimit(frequency, oddOnly = false)
        var sum = 0.0
        for (harmonic in 1..harmonics) {
            sum += sin(phase * harmonic) / harmonic
        }
        return (-2.0 / PI * sum).toFloat()
    }

    private fun harmonicLimit(
        frequency: Float,
        oddOnly: Boolean,
    ): Int {
        val safeFrequency = max(1.0f, abs(frequency))
        val rawLimit = max(1, (NYQUIST_FREQUENCY / safeFrequency).toInt())
        val limited = minOf(rawLimit, MAX_HARMONICS)
        return if (oddOnly && limited % 2 == 0) limited - 1 else limited
    }

    private fun advancePhase(
        phase: Double,
        frequency: Float,
    ): Double {
        val phaseStep = 2.0 * PI * frequency / SAMPLE_RATE
        val advanced = phase + phaseStep
        return if (advanced >= 2.0 * PI) advanced % (2.0 * PI) else advanced
    }

    private fun finalizePcm(pcmData: FloatArray): FloatArray {
        if (pcmData.isEmpty()) return pcmData

        applyEdgeFade(pcmData)

        val peak = pcmData.maxOf { abs(it) }
        if (peak > TARGET_PEAK && peak > 0f) {
            val gain = TARGET_PEAK / peak
            for (index in pcmData.indices) {
                pcmData[index] *= gain
            }
        }

        for (index in pcmData.indices) {
            pcmData[index] = softClip(pcmData[index])
        }
        return pcmData
    }

    private fun applyEdgeFade(pcmData: FloatArray) {
        val fadeSamples = minOf(EDGE_FADE_SAMPLES, pcmData.size / 2)
        if (fadeSamples <= 0) return

        for (index in 0 until fadeSamples) {
            val fade = index.toFloat() / fadeSamples
            pcmData[index] *= fade
            val tailIndex = pcmData.lastIndex - index
            pcmData[tailIndex] *= fade
        }
    }

    private fun softClip(sample: Float): Float {
        val limited = sample / (1.0f + abs(sample))
        return limited * 1.15f
    }
}
