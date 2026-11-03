package androidx.media3.common.audio;

import androidx.annotation.Nullable;

/**
 * Compile-compatible stub for the private Media3 fork's audio effect parameters.
 */
public final class AudioEffectParameters {

    public static final AudioEffectParameters DEFAULT = new Builder().build();

    private final int[] equalizerFrequencies;
    private final short[] equalizerLevels;
    private final float stability;
    private final int boostMillibels;
    private final int preampMillibels;
    private final int centerGainMillibels;
    private final float balance;
    private final int channelMode;
    private final boolean loudnessEnabled;

    private AudioEffectParameters(Builder builder) {
        this.equalizerFrequencies = builder.equalizerFrequencies;
        this.equalizerLevels = builder.equalizerLevels;
        this.stability = builder.stability;
        this.boostMillibels = builder.boostMillibels;
        this.preampMillibels = builder.preampMillibels;
        this.centerGainMillibels = builder.centerGainMillibels;
        this.balance = builder.balance;
        this.channelMode = builder.channelMode;
        this.loudnessEnabled = builder.loudnessEnabled;
    }

    public static boolean isCenterGainAvailable(int channelCount) {
        return channelCount == 6 || channelCount == 8;
    }

    public boolean hasBands() {
        return equalizerLevels.length > 0;
    }

    public boolean hasEffect() {
        return hasBands() || stability > 0 || boostMillibels != 0 || preampMillibels != 0
                || centerGainMillibels != 0 || balance != 0 || loudnessEnabled
                || channelMode != AudioChannelMode.AUTO;
    }

    public boolean hasCenterGain(int channelCount) {
        return centerGainMillibels != 0 && isCenterGainAvailable(channelCount);
    }

    public float getCenterGainFactor() {
        return (float) Math.pow(10.0, centerGainMillibels / 20.0);
    }

    public boolean shouldLimitOutput(int channelCount) {
        return hasBands() || boostMillibels != 0 || preampMillibels != 0 || hasCenterGain(channelCount);
    }

    public int[] getEqualizerFrequencies() {
        return equalizerFrequencies.clone();
    }

    public short[] getEqualizerLevelsMillibels() {
        return equalizerLevels.clone();
    }

    public float getStability() {
        return stability;
    }

    public int getBoostMillibels() {
        return boostMillibels;
    }

    public int getPreampMillibels() {
        return preampMillibels;
    }

    public int getCenterGainMillibels() {
        return centerGainMillibels;
    }

    public float getBalance() {
        return balance;
    }

    public int getChannelMode() {
        return channelMode;
    }

    public boolean isLoudnessEnabled() {
        return loudnessEnabled;
    }

    public static final class Builder {

        private int[] equalizerFrequencies = new int[0];
        private short[] equalizerLevels = new short[0];
        private float stability;
        private int boostMillibels;
        private int preampMillibels;
        private int centerGainMillibels;
        private float balance;
        private int channelMode = AudioChannelMode.AUTO;
        private boolean loudnessEnabled;

        public Builder setEqualizerBands(@Nullable int[] frequencies, @Nullable short[] levels) {
            this.equalizerFrequencies = frequencies == null ? new int[0] : frequencies.clone();
            this.equalizerLevels = levels == null ? new short[0] : levels.clone();
            return this;
        }

        public Builder setStability(float stability) {
            this.stability = stability;
            return this;
        }

        public Builder setBoostMillibels(int boostMillibels) {
            this.boostMillibels = boostMillibels;
            return this;
        }

        public Builder setPreampMillibels(int preampMillibels) {
            this.preampMillibels = preampMillibels;
            return this;
        }

        public Builder setCenterGainMillibels(int centerGainMillibels) {
            this.centerGainMillibels = centerGainMillibels;
            return this;
        }

        public Builder setBalance(float balance) {
            this.balance = balance;
            return this;
        }

        public Builder setChannelMode(int channelMode) {
            this.channelMode = AudioChannelMode.clamp(channelMode);
            return this;
        }

        public Builder setLoudnessEnabled(boolean loudnessEnabled) {
            this.loudnessEnabled = loudnessEnabled;
            return this;
        }

        public AudioEffectParameters build() {
            return new AudioEffectParameters(this);
        }
    }

    @Override
    public String toString() {
        return "AudioEffectParameters{bands=" + equalizerLevels.length
                + ", stability=" + stability
                + ", boost=" + boostMillibels
                + ", preamp=" + preampMillibels
                + ", centerGain=" + centerGainMillibels
                + ", balance=" + balance
                + ", channelMode=" + channelMode
                + ", loudness=" + loudnessEnabled
                + '}';
    }
}
