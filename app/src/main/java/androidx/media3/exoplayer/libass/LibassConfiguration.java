package androidx.media3.exoplayer.libass;

import androidx.annotation.Nullable;

/**
 * Compile-compatible stub for the private Media3 fork's libass configuration.
 * Values are retained so callers can build a configuration, but no libass state is created.
 */
public final class LibassConfiguration {

    @Nullable private final String fontConfig;
    @Nullable private final String fontsDirectory;
    @Nullable private final String defaultFontFamily;
    private final int maximumRenderPixels;
    private final int maximumGlyphCount;
    private final int maximumBitmapCacheSizeMb;

    private LibassConfiguration(Builder builder) {
        fontConfig = builder.fontConfig;
        fontsDirectory = builder.fontsDirectory;
        defaultFontFamily = builder.defaultFontFamily;
        maximumRenderPixels = builder.maximumRenderPixels;
        maximumGlyphCount = builder.maximumGlyphCount;
        maximumBitmapCacheSizeMb = builder.maximumBitmapCacheSizeMb;
    }

    public static final class Builder {

        @Nullable private String fontConfig;
        @Nullable private String fontsDirectory;
        @Nullable private String defaultFontFamily;
        private int maximumRenderPixels;
        private int maximumGlyphCount;
        private int maximumBitmapCacheSizeMb;

        public Builder() {
        }

        public Builder setFontConfig(@Nullable String fontConfig) {
            this.fontConfig = fontConfig;
            return this;
        }

        public Builder setFontsDirectory(@Nullable String fontsDirectory) {
            this.fontsDirectory = fontsDirectory;
            return this;
        }

        public Builder setDefaultFontFamily(@Nullable String defaultFontFamily) {
            this.defaultFontFamily = defaultFontFamily;
            return this;
        }

        public Builder setMaximumRenderPixels(int maximumRenderPixels) {
            this.maximumRenderPixels = maximumRenderPixels;
            return this;
        }

        public Builder setMaximumGlyphCount(int maximumGlyphCount) {
            this.maximumGlyphCount = maximumGlyphCount;
            return this;
        }

        public Builder setMaximumBitmapCacheSizeMb(int maximumBitmapCacheSizeMb) {
            this.maximumBitmapCacheSizeMb = maximumBitmapCacheSizeMb;
            return this;
        }

        public LibassConfiguration build() {
            return new LibassConfiguration(this);
        }
    }
}
