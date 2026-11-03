package androidx.media3.exoplayer.text;

import androidx.media3.common.Format;
import androidx.media3.common.util.Consumer;
import androidx.media3.extractor.text.CuesWithTiming;
import androidx.media3.extractor.text.SubtitleParser;

/**
 * Compile-compatible stub for the private Media3 fork's subtitle transcript parser factory.
 * No subtitle format is supported; the created parser produces no cues.
 */
public final class SubtitleTranscriptParserFactory implements SubtitleParser.Factory {

    private static final SubtitleParser NO_OP_PARSER = new SubtitleParser() {

        @Override
        public void parse(byte[] data, int offset, int length, SubtitleParser.OutputOptions outputOptions, Consumer<CuesWithTiming> output) {
            // No-op: transcript parsing is not implemented.
        }

        @Override
        public int getCueReplacementBehavior() {
            return 0;
        }
    };

    private final SubtitleTranscript transcript;

    public SubtitleTranscriptParserFactory(SubtitleTranscript transcript) {
        this.transcript = transcript;
    }

    @Override
    public boolean supportsFormat(Format format) {
        return false;
    }

    @Override
    public int getCueReplacementBehavior(Format format) {
        return 0;
    }

    @Override
    public SubtitleParser create(Format format) {
        return NO_OP_PARSER;
    }
}
