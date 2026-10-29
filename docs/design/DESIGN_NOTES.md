# Design notes (from Figma "Code Challenge" > Phone section)

Figma: https://www.figma.com/design/uuhUN9OZYqNZkBxuDq9FWh/Code-Challenge?node-id=11005-2008
Screenshots in this folder were captured from the Figma viewer at 100% zoom.

## Global
- Pure black background (#000000), white primary text, grey secondary text (~#9E9E9E).
- Status bar 12:30, gesture nav bar (white pill) -> edge-to-edge dark theme.
- Typography: sans (Inter/Roboto-like). Screen titles ~28sp bold. List item title 16sp, subtitle 13sp grey.
- Artwork thumbnails 48dp, rounded corners ~8dp.
- Horizontal padding 16dp.

## Splash
- Black to dark-teal radial/linear gradient glow at the top-left. 3D metallic music-note icon centered (~96dp).
- App icon: same note on dark gradient.

## Library - Songs (Home) — two states
1. Title "Songs" + full-width rounded search field (dark grey #1C1C1E-ish, pill radius ~12dp, leading search icon, placeholder "Search").
2. Title "Songs" with a search icon on the right of the top bar (search collapsed) and the list directly below.
- List rows: artwork 48dp | title (white) + artist (grey) | trailing vertical "more" (3 dots) icon.
- Example data: Purple Rain / Prince, Power Of Equality / RHCP, Something / The Beatles, Like A Virgin / Madonna, Get Lucky / Daft Punk feat. Pharrell Williams.

## Library - Player ("Now playing")
- Top bar: back arrow | "Now playing" (bold ~18sp) | overflow (3 dots, opens the action sheet).
- Large square artwork centered (~240dp, radius ~16dp), big vertical space around it.
- Song title left aligned ~28sp bold; artist grey ~16sp.
- Slider (thin track, white progress, round thumb). Elapsed "1:26" left, remaining "-2:54" right, grey 13sp.
- Controls row: large circular play/pause button (dark grey #2C2C2E circle ~64dp, white icon) at the left, then skip-previous, skip-next; repeat icon aligned to the right edge.

## Action sheet (More options)
- Modal bottom sheet, dark grey (#2C2C2E), drag handle on top.
- Centered "Song name" (bold) and "Artist name" (grey).
- One row: album icon + "View album".

## Library - Album
- Top bar: back arrow | "Album Title".
- Centered artwork ~120dp radius ~12dp, then "Album Title" bold ~20sp, "Daft Punk" grey.
- Track list: artwork 48dp | title + artist. No trailing icon.
