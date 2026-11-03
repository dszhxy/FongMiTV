package androidx.media3.common.audio;

import java.nio.ByteBuffer;

/**
 * Compile-compatible stub for the private Media3 fork's audio effect processor.
 * Behaves as an inactive pass-through processor.
 */
public final class AudioEffectProcessor implements AudioProcessor {

    private boolean ended = true;

    public void setParameters(AudioEffectParameters parameters, boolean softwareEqualizer) {
        // No-op: the compatibility layer does not process audio.
    }

    public void resetParameters() {
        // No-op: the compatibility layer does not process audio.
    }

    @Override
    public AudioFormat configure(AudioFormat inputAudioFormat) {
        return inputAudioFormat;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void queueInput(ByteBuffer inputBuffer) {
        inputBuffer.position(inputBuffer.limit());
    }

    @Override
    public void queueEndOfStream() {
        ended = true;
    }

    @Override
    public ByteBuffer getOutput() {
        return EMPTY_BUFFER;
    }

    @Override
    public boolean isEnded() {
        return ended;
    }

    @Override
    public void flush() {
        ended = false;
    }

    @Override
    public void reset() {
        ended = true;
    }

    @Override
    public String toString() {
        return "AudioEffectProcessor(stub)";
    }
}
