# Open 2048

<p align="center">
  <img src="app_icon.svg" width="140" alt="Open 2048 Logo">
</p>

<p align="center">
  <strong>A modern, open-source, and ad-free 2048 experience for Android and Android TV.</strong>
</p>

---

**Open 2048** is a lightweight and polished port of the classic puzzle game. Built with **Jetpack Compose**, it offers a clean "Modern Flat" aesthetic combined with detailed statistics and customizable gameplay.

## Features

- **Ad-Free & Private**: No trackers, no interruptions, and no unnecessary permissions.
- **Minimalist Design**: A shadow-free UI with Light, Dark, and Classic themes.
- **Android TV Support**: Fully optimized for the big screen with D-pad navigation.
- **Multiple Game Modes**:
  - **Classic**: The timeless challenge across multiple board sizes.
  - **Daily Challenge**: A unique, high-difficulty board generated every day.
  - **Blitz**: Timed gameplay modes (2 and 5 minutes).
- **Comprehensive Statistics**: Track your best scores, total wins, fewest moves, and fastest times.
- **Customization**:
  - **Animation Speed**: Choose between instant, fast, normal, or slow transitions.
  - **Controls**: Support for swipe gestures, on-screen buttons, or both.
  - **Haptic Feedback**: Subtle tactile responses for moves and merges.

## Installation

You can download the latest `.apk` from the [Releases](https://github.com/scottmangiapane/open-2048/releases) page.

## Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Architecture**: MVVM with StateFlow
- **Storage**: Jetpack DataStore (Preferences)
- **Animations**: Compose Animation APIs

## Development

1. **Clone the repository**: `git clone https://github.com/scottmangiapane/open-2048.git`
2. **Open in Android Studio**: Use **Android Studio Ladybug** (2024.2.1) or newer.
3. **Build and Run**: Deploy to an emulator or physical device.

### Running Tests

To ensure everything is working correctly, you can run unit tests, instrumentation tests, and coverage reports:

#### Unit Tests (Logic, Models, ViewModels, UI)
- **Via Command Line**:
  ```bash
  ./gradlew :app:testDebugUnitTest
  ```
- **Via Android Studio**: Right-click the `app/src/test` directory and select **"Run 'Tests in 'com.scottmangiapane.open2048''"**.

#### Code Coverage Report
We use **JaCoCo** to measure test coverage. The project enforces a minimum of **85% line coverage** and **85% branch coverage** for core logic (excluding compiler-generated code and UI components).

- **Generate Report**:
  ```bash
  ./gradlew :app:jacocoTestReport
  ```
  After running, the HTML report can be found at `app/build/reports/jacoco/jacocoTestReport/html/index.html`.

- **Verify Coverage**:
  ```bash
  ./gradlew :app:jacocoTestCoverageVerification
  ```
  This task will fail if the coverage falls below the 85% threshold.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
