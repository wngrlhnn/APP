# GIF sources

The app targets 5,000+ real animated GIFs downloaded from Wikimedia Commons during the GitHub Actions build.

The downloader:
- searches multiple animated-GIF topics (reactions, animals, effects, people, nature, gaming and more);
- explicitly excludes country/flag results;
- uses Wikimedia thumbnail URLs;
- recompresses the animations to 128px, at most 16 frames and 96 colors;
- stores the resulting GIFs in the APK so playback is fully offline.

The source metadata is written to `app/src/main/res/drawable-nodpi/manifest.json` during the build.

Because the source collection is fetched at build time, the exact files can change as Wikimedia search results change.