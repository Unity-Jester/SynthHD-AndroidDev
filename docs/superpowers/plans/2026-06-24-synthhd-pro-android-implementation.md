# SynthHD Pro Android Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a native Kotlin Android app that opens directly into an offline SynthHD Pro V2 simulator controller with the full major control surface represented.

**Architecture:** Create a small Android/Compose app with a pure Kotlin domain and simulator core, then layer local persistence and touch-friendly screens on top. Keep all device behavior behind a controller interface so a future USB controller can replace the simulator without redesigning the UI.

**Tech Stack:** Kotlin, Android SDK 36.1, Android Gradle Plugin 9.2.1 from the local Gradle cache, Kotlin 2.2.10, Jetpack Compose BOM 2026.02.01, Activity Compose 1.8.2, Lifecycle 2.9.4, JUnit 4.13.2, Compose UI tests 1.10.4.

---

## Approved Spec

Use this spec as the source of truth:

- `docs/superpowers/specs/2026-06-24-synthhd-pro-android-design.md`

## File Structure

Create this project structure:

```text
settings.gradle.kts
build.gradle.kts
gradle.properties
app/build.gradle.kts
app/src/main/AndroidManifest.xml
app/src/main/res/values/styles.xml
app/src/main/java/com/windfreak/synthhd/MainActivity.kt
app/src/main/java/com/windfreak/synthhd/domain/SynthConstants.kt
app/src/main/java/com/windfreak/synthhd/domain/SynthModels.kt
app/src/main/java/com/windfreak/synthhd/domain/SynthValidation.kt
app/src/main/java/com/windfreak/synthhd/domain/SweepMath.kt
app/src/main/java/com/windfreak/synthhd/controller/SynthHdController.kt
app/src/main/java/com/windfreak/synthhd/controller/SimulatedSynthHdController.kt
app/src/main/java/com/windfreak/synthhd/persistence/SynthStateStore.kt
app/src/main/java/com/windfreak/synthhd/ui/SynthHdApp.kt
app/src/main/java/com/windfreak/synthhd/ui/SynthHdViewModel.kt
app/src/main/java/com/windfreak/synthhd/ui/components/Controls.kt
app/src/main/java/com/windfreak/synthhd/ui/screens/GeneratorScreen.kt
app/src/main/java/com/windfreak/synthhd/ui/screens/SweepScreen.kt
app/src/main/java/com/windfreak/synthhd/ui/screens/ListScreen.kt
app/src/main/java/com/windfreak/synthhd/ui/screens/ModulationScreen.kt
app/src/main/java/com/windfreak/synthhd/ui/screens/TriggerScreen.kt
app/src/main/java/com/windfreak/synthhd/ui/screens/StatusScreen.kt
app/src/main/java/com/windfreak/synthhd/ui/screens/ExtrasScreen.kt
app/src/test/java/com/windfreak/synthhd/domain/SynthValidationTest.kt
app/src/test/java/com/windfreak/synthhd/domain/SweepMathTest.kt
app/src/test/java/com/windfreak/synthhd/controller/SimulatedSynthHdControllerTest.kt
app/src/androidTest/java/com/windfreak/synthhd/SynthHdAppTest.kt
```

Responsibilities:

- `domain/*`: pure Kotlin device data, limits, validation, and calculations.
- `controller/*`: command-shaped device API and offline simulator implementation.
- `persistence/*`: Android local save/restore for simulated state.
- `ui/*`: Compose app shell, state holder, shared controls, and screens.
- `test/*`: JVM tests for domain and simulator.
- `androidTest/*`: Compose smoke tests for the main Android UI.

## Task 1: Scaffold the Android Project

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/values/styles.xml`
- Create: `app/src/main/java/com/windfreak/synthhd/MainActivity.kt`

- [ ] **Step 1: Verify local Android tooling**

Run:

```bash
ls "$HOME/Library/Android/sdk/platforms/android-36.1"
ls /Applications/Android\ Studio.app
find "$HOME/.gradle/caches/modules-2/files-2.1/com.android.tools.build/gradle" -maxdepth 1 -mindepth 1 -type d | sed 's#.*/##' | sort -V | tail -1
```

Expected: the SDK and Android Studio paths exist, and the cached Android Gradle Plugin version prints `9.2.1`.

- [ ] **Step 2: Create or verify the Gradle wrapper**

If `./gradlew` already exists, run:

```bash
./gradlew --version
```

Expected: Gradle prints its version and exits successfully.

If `./gradlew` does not exist, create the wrapper using Android Studio's Gradle support or an approved Gradle download. After creation, verify these files exist:

```text
gradlew
gradlew.bat
gradle/wrapper/gradle-wrapper.jar
gradle/wrapper/gradle-wrapper.properties
```

Then run:

```bash
chmod +x ./gradlew
./gradlew --version
```

Expected: Gradle prints its version and exits successfully.

- [ ] **Step 3: Create root Gradle files**

Create `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SynthHD Android"
include(":app")
```

Create `build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application") version "9.2.1" apply false
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
}
```

Create `gradle.properties`:

```properties
android.useAndroidX=true
android.nonTransitiveRClass=true
org.gradle.jvmargs=-Xmx3g -Dfile.encoding=UTF-8
kotlin.code.style=official
```

- [ ] **Step 4: Create the app module build file**

Create `app/build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.windfreak.synthhd"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.windfreak.synthhd"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.02.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

- [ ] **Step 5: Create the manifest, theme, and minimal activity**

Create `app/src/main/AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:allowBackup="true"
        android:label="SynthHD Pro"
        android:theme="@style/Theme.SynthHD">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

Create `app/src/main/res/values/styles.xml`:

```xml
<resources>
    <style name="Theme.SynthHD" parent="android:style/Theme.Material.Light.NoActionBar">
        <item name="android:windowNoTitle">true</item>
        <item name="android:windowActionBar">false</item>
        <item name="android:windowLightStatusBar">true</item>
        <item name="android:statusBarColor">@android:color/white</item>
        <item name="android:navigationBarColor">@android:color/white</item>
    </style>
</resources>
```

Create `app/src/main/java/com/windfreak/synthhd/MainActivity.kt`:

```kotlin
package com.windfreak.synthhd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    Text("SynthHD Pro Simulator")
                }
            }
        }
    }
}
```

- [ ] **Step 6: Build the blank Android app**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit**

```bash
git add settings.gradle.kts build.gradle.kts gradle.properties gradlew gradlew.bat gradle/wrapper app
git commit -m "chore: scaffold android app"
```

## Task 2: Add the Device Domain Model

**Files:**
- Create: `app/src/main/java/com/windfreak/synthhd/domain/SynthConstants.kt`
- Create: `app/src/main/java/com/windfreak/synthhd/domain/SynthModels.kt`
- Create: `app/src/main/java/com/windfreak/synthhd/domain/SynthValidation.kt`
- Test: `app/src/test/java/com/windfreak/synthhd/domain/SynthValidationTest.kt`

- [ ] **Step 1: Write failing validation tests**

Create `app/src/test/java/com/windfreak/synthhd/domain/SynthValidationTest.kt`:

```kotlin
package com.windfreak.synthhd.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SynthValidationTest {
    @Test
    fun acceptsDocumentedGeneratorRanges() {
        assertTrue(validateFrequencyMhz(10.0).isValid)
        assertTrue(validateFrequencyMhz(24_000.0).isValid)
        assertTrue(validatePowerDbm(-40.0).isValid)
        assertTrue(validatePowerDbm(18.0).isValid)
        assertTrue(validatePhaseDegrees(0.0).isValid)
        assertTrue(validatePhaseDegrees(360.0).isValid)
    }

    @Test
    fun rejectsOutOfRangeGeneratorValues() {
        assertEquals("Frequency must be between 10 MHz and 24000 MHz.", validateFrequencyMhz(9.9).message)
        assertEquals("Power must be between -40 dBm and 18 dBm.", validatePowerDbm(18.1).message)
        assertEquals("Phase must be between 0 and 360 degrees.", validatePhaseDegrees(-0.1).message)
    }

    @Test
    fun rejectsListTablesAboveFiveHundredRows() {
        assertTrue(validateHopListSize(500).isValid)
        assertEquals("List mode supports up to 500 points.", validateHopListSize(501).message)
    }
}
```

- [ ] **Step 2: Run the failing test**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.windfreak.synthhd.domain.SynthValidationTest"
```

Expected: FAIL because the domain functions are not defined yet.

- [ ] **Step 3: Add constants, models, and validation**

Create `app/src/main/java/com/windfreak/synthhd/domain/SynthConstants.kt`:

```kotlin
package com.windfreak.synthhd.domain

object SynthConstants {
    const val MIN_FREQUENCY_MHZ = 10.0
    const val MAX_FREQUENCY_MHZ = 24_000.0
    const val MIN_POWER_DBM = -40.0
    const val MAX_POWER_DBM = 18.0
    const val MIN_PHASE_DEGREES = 0.0
    const val MAX_PHASE_DEGREES = 360.0
    const val MAX_HOP_POINTS = 500
}
```

Create `app/src/main/java/com/windfreak/synthhd/domain/SynthModels.kt`:

```kotlin
package com.windfreak.synthhd.domain

enum class ChannelId { A, B }
enum class SweepDirection { Up, Down }
enum class RunMode { Idle, Armed, Running, Complete }
enum class TriggerSource { Software, External }
enum class TriggerEdge { Rising, Falling }
enum class ReferenceMode { Internal, External }

data class ValidationResult(val isValid: Boolean, val message: String = "")

data class ChannelState(
    val frequencyMhz: Double = 1_000.0,
    val powerDbm: Double = 0.0,
    val phaseDegrees: Double = 0.0,
    val rfEnabled: Boolean = false,
    val locked: Boolean = false,
)

data class SweepState(
    val startMhz: Double = 1_000.0,
    val stopMhz: Double = 2_000.0,
    val stepMhz: Double = 10.0,
    val dwellMs: Int = 10,
    val direction: SweepDirection = SweepDirection.Up,
    val runMode: RunMode = RunMode.Idle,
)

data class HopPoint(
    val frequencyMhz: Double,
    val powerDbm: Double,
    val dwellMs: Int,
)

data class ModulationState(
    val pulseEnabled: Boolean = false,
    val amEnabled: Boolean = false,
    val fmEnabled: Boolean = false,
    val chirpEnabled: Boolean = false,
    val pulseWidthUs: Double = 10.0,
    val amDepthPercent: Double = 50.0,
    val fmDeviationKhz: Double = 100.0,
)

data class TriggerState(
    val source: TriggerSource = TriggerSource.Software,
    val edge: TriggerEdge = TriggerEdge.Rising,
    val mode: RunMode = RunMode.Idle,
)

data class DeviceStatus(
    val connectedLabel: String = "Offline simulator",
    val model: String = "SynthHD Pro V2",
    val serial: String = "SIM-0001",
    val firmware: String = "sim-0.1",
    val calibrationDate: String = "Simulated",
    val temperatureC: Double = 40.0,
    val referenceMode: ReferenceMode = ReferenceMode.Internal,
    val lockDetect: Boolean = true,
    val levelOk: Boolean = true,
)

data class SynthDeviceState(
    val activeChannel: ChannelId = ChannelId.A,
    val channelA: ChannelState = ChannelState(),
    val channelB: ChannelState = ChannelState(),
    val sweep: SweepState = SweepState(),
    val hopList: List<HopPoint> = emptyList(),
    val modulation: ModulationState = ModulationState(),
    val trigger: TriggerState = TriggerState(),
    val status: DeviceStatus = DeviceStatus(),
    val savedSnapshot: ChannelId? = null,
)
```

Create `app/src/main/java/com/windfreak/synthhd/domain/SynthValidation.kt`:

```kotlin
package com.windfreak.synthhd.domain

fun validateFrequencyMhz(value: Double): ValidationResult =
    if (value in SynthConstants.MIN_FREQUENCY_MHZ..SynthConstants.MAX_FREQUENCY_MHZ) {
        ValidationResult(true)
    } else {
        ValidationResult(false, "Frequency must be between 10 MHz and 24000 MHz.")
    }

fun validatePowerDbm(value: Double): ValidationResult =
    if (value in SynthConstants.MIN_POWER_DBM..SynthConstants.MAX_POWER_DBM) {
        ValidationResult(true)
    } else {
        ValidationResult(false, "Power must be between -40 dBm and 18 dBm.")
    }

fun validatePhaseDegrees(value: Double): ValidationResult =
    if (value in SynthConstants.MIN_PHASE_DEGREES..SynthConstants.MAX_PHASE_DEGREES) {
        ValidationResult(true)
    } else {
        ValidationResult(false, "Phase must be between 0 and 360 degrees.")
    }

fun validatePositiveDwellMs(value: Int): ValidationResult =
    if (value > 0) ValidationResult(true) else ValidationResult(false, "Dwell time must be greater than 0 ms.")

fun validateHopListSize(size: Int): ValidationResult =
    if (size <= SynthConstants.MAX_HOP_POINTS) {
        ValidationResult(true)
    } else {
        ValidationResult(false, "List mode supports up to 500 points.")
    }
```

- [ ] **Step 4: Run the validation tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.windfreak.synthhd.domain.SynthValidationTest"
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/windfreak/synthhd/domain app/src/test/java/com/windfreak/synthhd/domain/SynthValidationTest.kt
git commit -m "feat: add synthhd domain model"
```

## Task 3: Add Sweep and List Calculations

**Files:**
- Create: `app/src/main/java/com/windfreak/synthhd/domain/SweepMath.kt`
- Test: `app/src/test/java/com/windfreak/synthhd/domain/SweepMathTest.kt`

- [ ] **Step 1: Write failing sweep math tests**

Create `app/src/test/java/com/windfreak/synthhd/domain/SweepMathTest.kt`:

```kotlin
package com.windfreak.synthhd.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class SweepMathTest {
    @Test
    fun calculatesInclusiveUpSweepPointCount() {
        val sweep = SweepState(startMhz = 100.0, stopMhz = 110.0, stepMhz = 5.0, dwellMs = 20)

        assertEquals(3, sweepPointCount(sweep))
        assertEquals(60, sweepDurationMs(sweep))
    }

    @Test
    fun calculatesInclusiveDownSweepPointCount() {
        val sweep = SweepState(
            startMhz = 110.0,
            stopMhz = 100.0,
            stepMhz = 5.0,
            dwellMs = 20,
            direction = SweepDirection.Down,
        )

        assertEquals(3, sweepPointCount(sweep))
        assertEquals(listOf(110.0, 105.0, 100.0), generateSweepFrequencies(sweep))
    }

    @Test
    fun rejectsSweepWithZeroStep() {
        val sweep = SweepState(stepMhz = 0.0)

        assertEquals(0, sweepPointCount(sweep))
        assertEquals(emptyList<Double>(), generateSweepFrequencies(sweep))
    }
}
```

- [ ] **Step 2: Run the failing tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.windfreak.synthhd.domain.SweepMathTest"
```

Expected: FAIL because sweep calculation functions are missing.

- [ ] **Step 3: Add sweep math**

Create `app/src/main/java/com/windfreak/synthhd/domain/SweepMath.kt`:

```kotlin
package com.windfreak.synthhd.domain

import kotlin.math.floor

fun sweepPointCount(sweep: SweepState): Int {
    if (sweep.stepMhz <= 0.0 || sweep.dwellMs <= 0) return 0
    val span = when (sweep.direction) {
        SweepDirection.Up -> sweep.stopMhz - sweep.startMhz
        SweepDirection.Down -> sweep.startMhz - sweep.stopMhz
    }
    if (span < 0.0) return 0
    return floor(span / sweep.stepMhz).toInt() + 1
}

fun sweepDurationMs(sweep: SweepState): Int = sweepPointCount(sweep) * sweep.dwellMs

fun generateSweepFrequencies(sweep: SweepState): List<Double> {
    val count = sweepPointCount(sweep)
    if (count == 0) return emptyList()
    return List(count) { index ->
        when (sweep.direction) {
            SweepDirection.Up -> sweep.startMhz + index * sweep.stepMhz
            SweepDirection.Down -> sweep.startMhz - index * sweep.stepMhz
        }
    }
}
```

- [ ] **Step 4: Run domain tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.windfreak.synthhd.domain.*"
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/windfreak/synthhd/domain/SweepMath.kt app/src/test/java/com/windfreak/synthhd/domain/SweepMathTest.kt
git commit -m "feat: add synthhd sweep calculations"
```

## Task 4: Add the Simulator Controller

**Files:**
- Create: `app/src/main/java/com/windfreak/synthhd/controller/SynthHdController.kt`
- Create: `app/src/main/java/com/windfreak/synthhd/controller/SimulatedSynthHdController.kt`
- Test: `app/src/test/java/com/windfreak/synthhd/controller/SimulatedSynthHdControllerTest.kt`

- [ ] **Step 1: Write failing simulator tests**

Create `app/src/test/java/com/windfreak/synthhd/controller/SimulatedSynthHdControllerTest.kt`:

```kotlin
package com.windfreak.synthhd.controller

import com.windfreak.synthhd.domain.ChannelId
import com.windfreak.synthhd.domain.HopPoint
import com.windfreak.synthhd.domain.RunMode
import com.windfreak.synthhd.domain.SweepState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulatedSynthHdControllerTest {
    @Test
    fun updatesOnlyTheActiveChannelGeneratorFields() {
        val controller = SimulatedSynthHdController()

        controller.selectChannel(ChannelId.B)
        controller.setFrequencyMhz(2_450.0)
        controller.setPowerDbm(-3.0)
        controller.setRfEnabled(true)

        val state = controller.state
        assertEquals(ChannelId.B, state.activeChannel)
        assertEquals(1_000.0, state.channelA.frequencyMhz, 0.0)
        assertEquals(2_450.0, state.channelB.frequencyMhz, 0.0)
        assertEquals(-3.0, state.channelB.powerDbm, 0.0)
        assertTrue(state.channelB.rfEnabled)
    }

    @Test
    fun invalidFrequencyDoesNotApply() {
        val controller = SimulatedSynthHdController()

        val result = controller.setFrequencyMhz(1.0)

        assertFalse(result.isValid)
        assertEquals(1_000.0, controller.state.channelA.frequencyMhz, 0.0)
    }

    @Test
    fun listModeCapsAtFiveHundredPoints() {
        val controller = SimulatedSynthHdController()
        repeat(500) { index ->
            controller.addHopPoint(HopPoint(100.0 + index, 0.0, 10))
        }

        val result = controller.addHopPoint(HopPoint(1_000.0, 0.0, 10))

        assertFalse(result.isValid)
        assertEquals(500, controller.state.hopList.size)
    }

    @Test
    fun softwareTriggerRunsAndCompletesArmedSweep() {
        val controller = SimulatedSynthHdController()

        controller.setSweep(SweepState(runMode = RunMode.Armed))
        controller.softwareTrigger()

        assertEquals(RunMode.Complete, controller.state.sweep.runMode)
        assertEquals(RunMode.Complete, controller.state.trigger.mode)
    }
}
```

- [ ] **Step 2: Run the failing simulator tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.windfreak.synthhd.controller.SimulatedSynthHdControllerTest"
```

Expected: FAIL because the controller classes are missing.

- [ ] **Step 3: Add controller interface**

Create `app/src/main/java/com/windfreak/synthhd/controller/SynthHdController.kt`:

```kotlin
package com.windfreak.synthhd.controller

import com.windfreak.synthhd.domain.ChannelId
import com.windfreak.synthhd.domain.HopPoint
import com.windfreak.synthhd.domain.ModulationState
import com.windfreak.synthhd.domain.ReferenceMode
import com.windfreak.synthhd.domain.SweepState
import com.windfreak.synthhd.domain.SynthDeviceState
import com.windfreak.synthhd.domain.TriggerState
import com.windfreak.synthhd.domain.ValidationResult

interface SynthHdController {
    val state: SynthDeviceState

    fun replaceState(state: SynthDeviceState)
    fun selectChannel(channelId: ChannelId)
    fun setFrequencyMhz(value: Double): ValidationResult
    fun setPowerDbm(value: Double): ValidationResult
    fun setPhaseDegrees(value: Double): ValidationResult
    fun setRfEnabled(enabled: Boolean)
    fun setChannelLocked(locked: Boolean)
    fun setReferenceMode(referenceMode: ReferenceMode)
    fun setSweep(sweep: SweepState): ValidationResult
    fun startSweep()
    fun stopSweep()
    fun addHopPoint(point: HopPoint): ValidationResult
    fun removeHopPoint(index: Int)
    fun clearHopList()
    fun setModulation(modulation: ModulationState)
    fun setTrigger(trigger: TriggerState)
    fun softwareTrigger()
    fun saveToDevice()
    fun resetToDefaults()
}
```

- [ ] **Step 4: Add simulator implementation**

Create `app/src/main/java/com/windfreak/synthhd/controller/SimulatedSynthHdController.kt`:

```kotlin
package com.windfreak.synthhd.controller

import com.windfreak.synthhd.domain.ChannelId
import com.windfreak.synthhd.domain.ChannelState
import com.windfreak.synthhd.domain.HopPoint
import com.windfreak.synthhd.domain.ModulationState
import com.windfreak.synthhd.domain.ReferenceMode
import com.windfreak.synthhd.domain.RunMode
import com.windfreak.synthhd.domain.SweepState
import com.windfreak.synthhd.domain.SynthDeviceState
import com.windfreak.synthhd.domain.TriggerState
import com.windfreak.synthhd.domain.ValidationResult
import com.windfreak.synthhd.domain.validateFrequencyMhz
import com.windfreak.synthhd.domain.validateHopListSize
import com.windfreak.synthhd.domain.validatePhaseDegrees
import com.windfreak.synthhd.domain.validatePositiveDwellMs
import com.windfreak.synthhd.domain.validatePowerDbm

class SimulatedSynthHdController(initialState: SynthDeviceState = SynthDeviceState()) : SynthHdController {
    override var state: SynthDeviceState = initialState
        private set

    override fun replaceState(state: SynthDeviceState) {
        this.state = state
    }

    override fun selectChannel(channelId: ChannelId) {
        state = state.copy(activeChannel = channelId)
    }

    override fun setFrequencyMhz(value: Double): ValidationResult =
        applyIfValid(validateFrequencyMhz(value)) { channel -> channel.copy(frequencyMhz = value) }

    override fun setPowerDbm(value: Double): ValidationResult =
        applyIfValid(validatePowerDbm(value)) { channel -> channel.copy(powerDbm = value) }

    override fun setPhaseDegrees(value: Double): ValidationResult =
        applyIfValid(validatePhaseDegrees(value)) { channel -> channel.copy(phaseDegrees = value) }

    override fun setRfEnabled(enabled: Boolean) {
        updateActiveChannel { it.copy(rfEnabled = enabled) }
    }

    override fun setChannelLocked(locked: Boolean) {
        updateActiveChannel { it.copy(locked = locked) }
    }

    override fun setReferenceMode(referenceMode: ReferenceMode) {
        state = state.copy(status = state.status.copy(referenceMode = referenceMode))
    }

    override fun setSweep(sweep: SweepState): ValidationResult {
        val start = validateFrequencyMhz(sweep.startMhz)
        if (!start.isValid) return start
        val stop = validateFrequencyMhz(sweep.stopMhz)
        if (!stop.isValid) return stop
        val dwell = validatePositiveDwellMs(sweep.dwellMs)
        if (!dwell.isValid) return dwell
        if (sweep.stepMhz <= 0.0) return ValidationResult(false, "Sweep step must be greater than 0 MHz.")
        state = state.copy(sweep = sweep)
        return ValidationResult(true)
    }

    override fun startSweep() {
        state = state.copy(sweep = state.sweep.copy(runMode = RunMode.Running))
    }

    override fun stopSweep() {
        state = state.copy(sweep = state.sweep.copy(runMode = RunMode.Idle))
    }

    override fun addHopPoint(point: HopPoint): ValidationResult {
        val size = validateHopListSize(state.hopList.size + 1)
        if (!size.isValid) return size
        val frequency = validateFrequencyMhz(point.frequencyMhz)
        if (!frequency.isValid) return frequency
        val power = validatePowerDbm(point.powerDbm)
        if (!power.isValid) return power
        val dwell = validatePositiveDwellMs(point.dwellMs)
        if (!dwell.isValid) return dwell
        state = state.copy(hopList = state.hopList + point)
        return ValidationResult(true)
    }

    override fun removeHopPoint(index: Int) {
        if (index !in state.hopList.indices) return
        state = state.copy(hopList = state.hopList.toMutableList().also { it.removeAt(index) })
    }

    override fun clearHopList() {
        state = state.copy(hopList = emptyList())
    }

    override fun setModulation(modulation: ModulationState) {
        state = state.copy(modulation = modulation)
    }

    override fun setTrigger(trigger: TriggerState) {
        state = state.copy(trigger = trigger)
    }

    override fun softwareTrigger() {
        state = state.copy(
            sweep = state.sweep.copy(runMode = RunMode.Complete),
            trigger = state.trigger.copy(mode = RunMode.Complete),
        )
    }

    override fun saveToDevice() {
        state = state.copy(savedSnapshot = state.activeChannel)
    }

    override fun resetToDefaults() {
        state = SynthDeviceState()
    }

    private fun applyIfValid(
        result: ValidationResult,
        update: (ChannelState) -> ChannelState,
    ): ValidationResult {
        if (!result.isValid) return result
        updateActiveChannel(update)
        return result
    }

    private fun updateActiveChannel(update: (ChannelState) -> ChannelState) {
        state = when (state.activeChannel) {
            ChannelId.A -> state.copy(channelA = update(state.channelA))
            ChannelId.B -> state.copy(channelB = update(state.channelB))
        }
    }
}
```

- [ ] **Step 5: Run simulator tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.windfreak.synthhd.controller.SimulatedSynthHdControllerTest"
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/windfreak/synthhd/controller app/src/test/java/com/windfreak/synthhd/controller/SimulatedSynthHdControllerTest.kt
git commit -m "feat: add offline synthhd simulator"
```

## Task 5: Add Local Persistence

**Files:**
- Create: `app/src/main/java/com/windfreak/synthhd/persistence/SynthStateStore.kt`
- Modify: `app/src/main/java/com/windfreak/synthhd/domain/SynthModels.kt`

- [ ] **Step 1: Add JSON conversion helpers to the domain**

Move the two `org.json` imports directly below the package declaration in `SynthModels.kt`, then append the conversion functions at the bottom of the file:

```kotlin
import org.json.JSONArray
import org.json.JSONObject

fun SynthDeviceState.toJson(): JSONObject = JSONObject()
    .put("activeChannel", activeChannel.name)
    .put("channelA", channelA.toJson())
    .put("channelB", channelB.toJson())
    .put("sweep", sweep.toJson())
    .put("hopList", JSONArray().also { array -> hopList.forEach { array.put(it.toJson()) } })
    .put("modulation", modulation.toJson())
    .put("trigger", trigger.toJson())
    .put("status", status.toJson())
    .put("savedSnapshot", savedSnapshot?.name ?: "")

fun synthDeviceStateFromJson(json: JSONObject): SynthDeviceState = SynthDeviceState(
    activeChannel = ChannelId.valueOf(json.optString("activeChannel", ChannelId.A.name)),
    channelA = channelStateFromJson(json.optJSONObject("channelA") ?: JSONObject()),
    channelB = channelStateFromJson(json.optJSONObject("channelB") ?: JSONObject()),
    sweep = sweepStateFromJson(json.optJSONObject("sweep") ?: JSONObject()),
    hopList = hopListFromJson(json.optJSONArray("hopList") ?: JSONArray()),
    modulation = modulationStateFromJson(json.optJSONObject("modulation") ?: JSONObject()),
    trigger = triggerStateFromJson(json.optJSONObject("trigger") ?: JSONObject()),
    status = deviceStatusFromJson(json.optJSONObject("status") ?: JSONObject()),
    savedSnapshot = json.optString("savedSnapshot").takeIf { it.isNotBlank() }?.let(ChannelId::valueOf),
)

private fun ChannelState.toJson(): JSONObject = JSONObject()
    .put("frequencyMhz", frequencyMhz)
    .put("powerDbm", powerDbm)
    .put("phaseDegrees", phaseDegrees)
    .put("rfEnabled", rfEnabled)
    .put("locked", locked)

private fun channelStateFromJson(json: JSONObject): ChannelState = ChannelState(
    frequencyMhz = json.optDouble("frequencyMhz", 1_000.0),
    powerDbm = json.optDouble("powerDbm", 0.0),
    phaseDegrees = json.optDouble("phaseDegrees", 0.0),
    rfEnabled = json.optBoolean("rfEnabled", false),
    locked = json.optBoolean("locked", false),
)

private fun SweepState.toJson(): JSONObject = JSONObject()
    .put("startMhz", startMhz)
    .put("stopMhz", stopMhz)
    .put("stepMhz", stepMhz)
    .put("dwellMs", dwellMs)
    .put("direction", direction.name)
    .put("runMode", runMode.name)

private fun sweepStateFromJson(json: JSONObject): SweepState = SweepState(
    startMhz = json.optDouble("startMhz", 1_000.0),
    stopMhz = json.optDouble("stopMhz", 2_000.0),
    stepMhz = json.optDouble("stepMhz", 10.0),
    dwellMs = json.optInt("dwellMs", 10),
    direction = SweepDirection.valueOf(json.optString("direction", SweepDirection.Up.name)),
    runMode = RunMode.valueOf(json.optString("runMode", RunMode.Idle.name)),
)

private fun HopPoint.toJson(): JSONObject = JSONObject()
    .put("frequencyMhz", frequencyMhz)
    .put("powerDbm", powerDbm)
    .put("dwellMs", dwellMs)

private fun hopPointFromJson(json: JSONObject): HopPoint = HopPoint(
    frequencyMhz = json.optDouble("frequencyMhz", 1_000.0),
    powerDbm = json.optDouble("powerDbm", 0.0),
    dwellMs = json.optInt("dwellMs", 10),
)

private fun hopListFromJson(array: JSONArray): List<HopPoint> =
    List(array.length()) { index -> hopPointFromJson(array.optJSONObject(index) ?: JSONObject()) }

private fun ModulationState.toJson(): JSONObject = JSONObject()
    .put("pulseEnabled", pulseEnabled)
    .put("amEnabled", amEnabled)
    .put("fmEnabled", fmEnabled)
    .put("chirpEnabled", chirpEnabled)
    .put("pulseWidthUs", pulseWidthUs)
    .put("amDepthPercent", amDepthPercent)
    .put("fmDeviationKhz", fmDeviationKhz)

private fun modulationStateFromJson(json: JSONObject): ModulationState = ModulationState(
    pulseEnabled = json.optBoolean("pulseEnabled", false),
    amEnabled = json.optBoolean("amEnabled", false),
    fmEnabled = json.optBoolean("fmEnabled", false),
    chirpEnabled = json.optBoolean("chirpEnabled", false),
    pulseWidthUs = json.optDouble("pulseWidthUs", 10.0),
    amDepthPercent = json.optDouble("amDepthPercent", 50.0),
    fmDeviationKhz = json.optDouble("fmDeviationKhz", 100.0),
)

private fun TriggerState.toJson(): JSONObject = JSONObject()
    .put("source", source.name)
    .put("edge", edge.name)
    .put("mode", mode.name)

private fun triggerStateFromJson(json: JSONObject): TriggerState = TriggerState(
    source = TriggerSource.valueOf(json.optString("source", TriggerSource.Software.name)),
    edge = TriggerEdge.valueOf(json.optString("edge", TriggerEdge.Rising.name)),
    mode = RunMode.valueOf(json.optString("mode", RunMode.Idle.name)),
)

private fun DeviceStatus.toJson(): JSONObject = JSONObject()
    .put("connectedLabel", connectedLabel)
    .put("model", model)
    .put("serial", serial)
    .put("firmware", firmware)
    .put("calibrationDate", calibrationDate)
    .put("temperatureC", temperatureC)
    .put("referenceMode", referenceMode.name)
    .put("lockDetect", lockDetect)
    .put("levelOk", levelOk)

private fun deviceStatusFromJson(json: JSONObject): DeviceStatus = DeviceStatus(
    connectedLabel = json.optString("connectedLabel", "Offline simulator"),
    model = json.optString("model", "SynthHD Pro V2"),
    serial = json.optString("serial", "SIM-0001"),
    firmware = json.optString("firmware", "sim-0.1"),
    calibrationDate = json.optString("calibrationDate", "Simulated"),
    temperatureC = json.optDouble("temperatureC", 40.0),
    referenceMode = ReferenceMode.valueOf(json.optString("referenceMode", ReferenceMode.Internal.name)),
    lockDetect = json.optBoolean("lockDetect", true),
    levelOk = json.optBoolean("levelOk", true),
)
```

- [ ] **Step 2: Create the Android store**

Create `app/src/main/java/com/windfreak/synthhd/persistence/SynthStateStore.kt`:

```kotlin
package com.windfreak.synthhd.persistence

import android.content.Context
import com.windfreak.synthhd.domain.SynthDeviceState
import com.windfreak.synthhd.domain.synthDeviceStateFromJson
import com.windfreak.synthhd.domain.toJson
import org.json.JSONObject

class SynthStateStore(context: Context) {
    private val preferences = context.getSharedPreferences("synthhd-state", Context.MODE_PRIVATE)

    fun load(): SynthDeviceState {
        val raw = preferences.getString(KEY_STATE, null) ?: return SynthDeviceState()
        return runCatching { synthDeviceStateFromJson(JSONObject(raw)) }.getOrDefault(SynthDeviceState())
    }

    fun save(state: SynthDeviceState) {
        preferences.edit().putString(KEY_STATE, state.toJson().toString()).apply()
    }

    companion object {
        private const val KEY_STATE = "state"
    }
}
```

- [ ] **Step 3: Build after persistence changes**

Run:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/windfreak/synthhd/domain/SynthModels.kt app/src/main/java/com/windfreak/synthhd/persistence/SynthStateStore.kt
git commit -m "feat: persist simulated synthhd state"
```

## Task 6: Add View Model State and App Shell

**Files:**
- Create: `app/src/main/java/com/windfreak/synthhd/ui/SynthHdViewModel.kt`
- Create: `app/src/main/java/com/windfreak/synthhd/ui/SynthHdApp.kt`
- Modify: `app/src/main/java/com/windfreak/synthhd/MainActivity.kt`

- [ ] **Step 1: Create the view model**

Create `app/src/main/java/com/windfreak/synthhd/ui/SynthHdViewModel.kt`:

```kotlin
package com.windfreak.synthhd.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.windfreak.synthhd.controller.SimulatedSynthHdController
import com.windfreak.synthhd.controller.SynthHdController
import com.windfreak.synthhd.domain.ChannelId
import com.windfreak.synthhd.domain.HopPoint
import com.windfreak.synthhd.domain.ModulationState
import com.windfreak.synthhd.domain.ReferenceMode
import com.windfreak.synthhd.domain.SweepState
import com.windfreak.synthhd.domain.SynthDeviceState
import com.windfreak.synthhd.domain.TriggerState
import com.windfreak.synthhd.domain.ValidationResult
import com.windfreak.synthhd.persistence.SynthStateStore

class SynthHdViewModel(
    private val store: SynthStateStore,
    private val controller: SynthHdController = SimulatedSynthHdController(store.load()),
) : ViewModel() {
    var state by mutableStateOf(controller.state)
        private set

    var message by mutableStateOf("Offline simulator ready")
        private set

    fun selectChannel(channelId: ChannelId) = applyChange { controller.selectChannel(channelId) }
    fun setFrequencyMhz(value: Double) = applyValidation(controller.setFrequencyMhz(value))
    fun setPowerDbm(value: Double) = applyValidation(controller.setPowerDbm(value))
    fun setPhaseDegrees(value: Double) = applyValidation(controller.setPhaseDegrees(value))
    fun setRfEnabled(enabled: Boolean) = applyChange { controller.setRfEnabled(enabled) }
    fun setChannelLocked(locked: Boolean) = applyChange { controller.setChannelLocked(locked) }
    fun setReferenceMode(referenceMode: ReferenceMode) = applyChange { controller.setReferenceMode(referenceMode) }
    fun setSweep(sweep: SweepState) = applyValidation(controller.setSweep(sweep))
    fun startSweep() = applyChange { controller.startSweep() }
    fun stopSweep() = applyChange { controller.stopSweep() }
    fun addHopPoint(point: HopPoint) = applyValidation(controller.addHopPoint(point))
    fun removeHopPoint(index: Int) = applyChange { controller.removeHopPoint(index) }
    fun clearHopList() = applyChange { controller.clearHopList() }
    fun setModulation(modulation: ModulationState) = applyChange { controller.setModulation(modulation) }
    fun setTrigger(trigger: TriggerState) = applyChange { controller.setTrigger(trigger) }
    fun softwareTrigger() = applyChange { controller.softwareTrigger() }
    fun saveToDevice() = applyChange("Simulated settings saved") { controller.saveToDevice() }
    fun resetToDefaults() = applyChange("Simulator reset") { controller.resetToDefaults() }

    fun replaceState(newState: SynthDeviceState) = applyChange { controller.replaceState(newState) }

    private fun applyValidation(result: ValidationResult) {
        if (result.isValid) {
            sync("Updated")
        } else {
            message = result.message
        }
    }

    private fun applyChange(nextMessage: String = "Updated", change: () -> Unit) {
        change()
        sync(nextMessage)
    }

    private fun sync(nextMessage: String) {
        state = controller.state
        store.save(state)
        message = nextMessage
    }
}
```

- [ ] **Step 2: Create the top-level app shell**

Create `app/src/main/java/com/windfreak/synthhd/ui/SynthHdApp.kt`:

```kotlin
package com.windfreak.synthhd.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.windfreak.synthhd.domain.ChannelId

private val tabLabels = listOf("Generator", "Sweep", "List", "Mod", "Trigger", "Status", "Extras")

@Composable
fun SynthHdApp(viewModel: SynthHdViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val state = viewModel.state

    MaterialTheme {
        Scaffold(
            topBar = {
                Column(Modifier.padding(16.dp)) {
                    Text("SynthHD Pro Simulator", style = MaterialTheme.typography.titleLarge)
                    Text(state.status.connectedLabel, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Row {
                        AssistChip(
                            onClick = { viewModel.selectChannel(ChannelId.A) },
                            label = { Text("Channel A ${if (state.activeChannel == ChannelId.A) "active" else ""}") },
                        )
                        Spacer(Modifier.width(8.dp))
                        AssistChip(
                            onClick = { viewModel.selectChannel(ChannelId.B) },
                            label = { Text("Channel B ${if (state.activeChannel == ChannelId.B) "active" else ""}") },
                        )
                    }
                }
            },
            bottomBar = {
                Surface(tonalElevation = 3.dp) {
                    Text(
                        text = "Readback: Temp ${state.status.temperatureC} C | Ref ${state.status.referenceMode} | Level ${if (state.status.levelOk) "OK" else "WARN"} | ${viewModel.message}",
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                    )
                }
            },
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                TabRow(selectedTabIndex = selectedTab) {
                    tabLabels.forEachIndexed { index, label ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(label) },
                        )
                    }
                }
                Text(
                    text = tabLabels[selectedTab],
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }
    }
}
```

- [ ] **Step 3: Wire the activity to the store and view model**

Replace `MainActivity.kt` with:

```kotlin
package com.windfreak.synthhd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.windfreak.synthhd.persistence.SynthStateStore
import com.windfreak.synthhd.ui.SynthHdApp
import com.windfreak.synthhd.ui.SynthHdViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = SynthStateStore(applicationContext)
        setContent {
            val viewModel = remember { SynthHdViewModel(store) }
            SynthHdApp(viewModel)
        }
    }
}
```

- [ ] **Step 4: Build the app shell**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/windfreak/synthhd/MainActivity.kt app/src/main/java/com/windfreak/synthhd/ui
git commit -m "feat: add compose simulator shell"
```

## Task 7: Build Shared Compose Controls

**Files:**
- Create: `app/src/main/java/com/windfreak/synthhd/ui/components/Controls.kt`

- [ ] **Step 1: Create reusable controls**

Create `app/src/main/java/com/windfreak/synthhd/ui/components/Controls.kt`:

```kotlin
package com.windfreak.synthhd.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun Section(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        colors = CardDefaults.cardColors(),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun NumberField(
    label: String,
    value: Double,
    suffix: String,
    onApply: (Double) -> Unit,
) {
    val text = remember(value) { mutableStateOf(value.toString()) }
    Row(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = text.value,
            onValueChange = { text.value = it },
            label = { Text(label) },
            suffix = { Text(suffix) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Button(onClick = { text.value.toDoubleOrNull()?.let(onApply) }) {
            Text("Apply")
        }
    }
}

@Composable
fun IntField(
    label: String,
    value: Int,
    suffix: String,
    onApply: (Int) -> Unit,
) {
    val text = remember(value) { mutableStateOf(value.toString()) }
    Row(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = text.value,
            onValueChange = { text.value = it },
            label = { Text(label) },
            suffix = { Text(suffix) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Button(onClick = { text.value.toIntOrNull()?.let(onApply) }) {
            Text("Apply")
        }
    }
}

@Composable
fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
```

- [ ] **Step 2: Build after adding controls**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/windfreak/synthhd/ui/components/Controls.kt
git commit -m "feat: add shared synthhd compose controls"
```

## Task 8: Build Generator, Sweep, and List Screens

**Files:**
- Create: `app/src/main/java/com/windfreak/synthhd/ui/screens/GeneratorScreen.kt`
- Create: `app/src/main/java/com/windfreak/synthhd/ui/screens/SweepScreen.kt`
- Create: `app/src/main/java/com/windfreak/synthhd/ui/screens/ListScreen.kt`
- Modify: `app/src/main/java/com/windfreak/synthhd/ui/SynthHdApp.kt`

- [ ] **Step 1: Create `GeneratorScreen.kt`**

```kotlin
package com.windfreak.synthhd.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.windfreak.synthhd.domain.ChannelId
import com.windfreak.synthhd.domain.ReferenceMode
import com.windfreak.synthhd.domain.SynthDeviceState
import com.windfreak.synthhd.ui.components.NumberField
import com.windfreak.synthhd.ui.components.Section
import com.windfreak.synthhd.ui.components.ToggleRow

@Composable
fun GeneratorScreen(
    state: SynthDeviceState,
    onFrequency: (Double) -> Unit,
    onPower: (Double) -> Unit,
    onPhase: (Double) -> Unit,
    onRf: (Boolean) -> Unit,
    onLock: (Boolean) -> Unit,
    onReference: (ReferenceMode) -> Unit,
) {
    val channel = if (state.activeChannel == ChannelId.A) state.channelA else state.channelB
    Column {
        Section("Generator ${state.activeChannel}") {
            NumberField("Frequency", channel.frequencyMhz, "MHz", onFrequency)
            NumberField("Power", channel.powerDbm, "dBm", onPower)
            NumberField("Phase", channel.phaseDegrees, "deg", onPhase)
            ToggleRow("RF Output", channel.rfEnabled, onRf)
            ToggleRow("Channel Lock", channel.locked, onLock)
            Row {
                Button(onClick = { onReference(ReferenceMode.Internal) }) { Text("Internal Ref") }
                Button(onClick = { onReference(ReferenceMode.External) }) { Text("External Ref") }
            }
            Text("Reference: ${state.status.referenceMode}")
            Text("Frequency range: 10 MHz to 24000 MHz")
        }
    }
}
```

- [ ] **Step 2: Create `SweepScreen.kt`**

```kotlin
package com.windfreak.synthhd.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.windfreak.synthhd.domain.SweepState
import com.windfreak.synthhd.domain.sweepDurationMs
import com.windfreak.synthhd.domain.sweepPointCount
import com.windfreak.synthhd.ui.components.IntField
import com.windfreak.synthhd.ui.components.NumberField
import com.windfreak.synthhd.ui.components.Section

@Composable
fun SweepScreen(
    sweep: SweepState,
    onSweep: (SweepState) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    Column {
        Section("Linear Sweep") {
            NumberField("Start", sweep.startMhz, "MHz") { onSweep(sweep.copy(startMhz = it)) }
            NumberField("Stop", sweep.stopMhz, "MHz") { onSweep(sweep.copy(stopMhz = it)) }
            NumberField("Step", sweep.stepMhz, "MHz") { onSweep(sweep.copy(stepMhz = it)) }
            IntField("Dwell", sweep.dwellMs, "ms") { onSweep(sweep.copy(dwellMs = it)) }
            Text("Points: ${sweepPointCount(sweep)}")
            Text("Duration: ${sweepDurationMs(sweep)} ms")
            Text("State: ${sweep.runMode}")
            Row {
                Button(onClick = onStart) { Text("Run") }
                Button(onClick = onStop) { Text("Stop") }
            }
        }
    }
}
```

- [ ] **Step 3: Create `ListScreen.kt`**

```kotlin
package com.windfreak.synthhd.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.windfreak.synthhd.domain.HopPoint
import com.windfreak.synthhd.domain.SynthDeviceState
import com.windfreak.synthhd.ui.components.Section

@Composable
fun ListScreen(
    state: SynthDeviceState,
    onAdd: (HopPoint) -> Unit,
    onRemove: (Int) -> Unit,
    onClear: () -> Unit,
) {
    Section("List / Hop Table") {
        Text("${state.hopList.size} / 500 points")
        Row {
            Button(onClick = { onAdd(HopPoint(1_000.0 + state.hopList.size, 0.0, 10)) }) {
                Text("Add Point")
            }
            Button(onClick = onClear) { Text("Clear") }
        }
        Column {
            state.hopList.take(20).forEachIndexed { index, point ->
                Row {
                    Text("${index + 1}. ${point.frequencyMhz} MHz, ${point.powerDbm} dBm, ${point.dwellMs} ms")
                    Button(onClick = { onRemove(index) }) { Text("Delete") }
                }
            }
            if (state.hopList.size > 20) Text("Showing first 20 points")
        }
    }
}
```

- [ ] **Step 4: Wire these screens into `SynthHdApp.kt`**

Replace the body area after the `TabRow` in `SynthHdApp.kt` with:

```kotlin
when (selectedTab) {
    0 -> GeneratorScreen(
        state = state,
        onFrequency = viewModel::setFrequencyMhz,
        onPower = viewModel::setPowerDbm,
        onPhase = viewModel::setPhaseDegrees,
        onRf = viewModel::setRfEnabled,
        onLock = viewModel::setChannelLocked,
        onReference = viewModel::setReferenceMode,
    )
    1 -> SweepScreen(
        sweep = state.sweep,
        onSweep = viewModel::setSweep,
        onStart = viewModel::startSweep,
        onStop = viewModel::stopSweep,
    )
    2 -> ListScreen(
        state = state,
        onAdd = viewModel::addHopPoint,
        onRemove = viewModel::removeHopPoint,
        onClear = viewModel::clearHopList,
    )
    else -> Text(
        text = tabLabels[selectedTab],
        modifier = Modifier.padding(16.dp),
        style = MaterialTheme.typography.headlineSmall,
    )
}
```

Add imports:

```kotlin
import com.windfreak.synthhd.ui.screens.GeneratorScreen
import com.windfreak.synthhd.ui.screens.ListScreen
import com.windfreak.synthhd.ui.screens.SweepScreen
```

- [ ] **Step 5: Build**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/windfreak/synthhd/ui/SynthHdApp.kt app/src/main/java/com/windfreak/synthhd/ui/screens/GeneratorScreen.kt app/src/main/java/com/windfreak/synthhd/ui/screens/SweepScreen.kt app/src/main/java/com/windfreak/synthhd/ui/screens/ListScreen.kt
git commit -m "feat: add generator sweep and list screens"
```

## Task 9: Build Modulation, Trigger, Status, and Extras Screens

**Files:**
- Create: `app/src/main/java/com/windfreak/synthhd/ui/screens/ModulationScreen.kt`
- Create: `app/src/main/java/com/windfreak/synthhd/ui/screens/TriggerScreen.kt`
- Create: `app/src/main/java/com/windfreak/synthhd/ui/screens/StatusScreen.kt`
- Create: `app/src/main/java/com/windfreak/synthhd/ui/screens/ExtrasScreen.kt`
- Modify: `app/src/main/java/com/windfreak/synthhd/ui/SynthHdApp.kt`

- [ ] **Step 1: Create modulation screen**

Create `ModulationScreen.kt` with toggles for pulse, AM, FM, and chirp:

```kotlin
package com.windfreak.synthhd.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.windfreak.synthhd.domain.ModulationState
import com.windfreak.synthhd.ui.components.NumberField
import com.windfreak.synthhd.ui.components.Section
import com.windfreak.synthhd.ui.components.ToggleRow

@Composable
fun ModulationScreen(modulation: ModulationState, onModulation: (ModulationState) -> Unit) {
    Column {
        Section("Pulse") {
            ToggleRow("Pulse modulation", modulation.pulseEnabled) { onModulation(modulation.copy(pulseEnabled = it)) }
            NumberField("Pulse width", modulation.pulseWidthUs, "us") { onModulation(modulation.copy(pulseWidthUs = it)) }
        }
        Section("AM / FM / Chirp") {
            ToggleRow("AM", modulation.amEnabled) { onModulation(modulation.copy(amEnabled = it)) }
            NumberField("AM depth", modulation.amDepthPercent, "%") { onModulation(modulation.copy(amDepthPercent = it)) }
            ToggleRow("FM", modulation.fmEnabled) { onModulation(modulation.copy(fmEnabled = it)) }
            NumberField("FM deviation", modulation.fmDeviationKhz, "kHz") { onModulation(modulation.copy(fmDeviationKhz = it)) }
            ToggleRow("Chirp", modulation.chirpEnabled) { onModulation(modulation.copy(chirpEnabled = it)) }
        }
    }
}
```

- [ ] **Step 2: Create trigger screen**

Create `TriggerScreen.kt`:

```kotlin
package com.windfreak.synthhd.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.windfreak.synthhd.domain.RunMode
import com.windfreak.synthhd.domain.TriggerEdge
import com.windfreak.synthhd.domain.TriggerSource
import com.windfreak.synthhd.domain.TriggerState
import com.windfreak.synthhd.ui.components.Section

@Composable
fun TriggerScreen(
    trigger: TriggerState,
    onTrigger: (TriggerState) -> Unit,
    onSoftwareTrigger: () -> Unit,
) {
    Column {
        Section("Trigger") {
            Text("Source: ${trigger.source}")
            Text("Edge: ${trigger.edge}")
            Text("State: ${trigger.mode}")
            Button(onClick = { onTrigger(trigger.copy(source = TriggerSource.Software)) }) { Text("Software Source") }
            Button(onClick = { onTrigger(trigger.copy(source = TriggerSource.External)) }) { Text("External Source") }
            Button(onClick = { onTrigger(trigger.copy(edge = TriggerEdge.Rising)) }) { Text("Rising Edge") }
            Button(onClick = { onTrigger(trigger.copy(edge = TriggerEdge.Falling)) }) { Text("Falling Edge") }
            Button(onClick = { onTrigger(trigger.copy(mode = RunMode.Armed)) }) { Text("Arm") }
            Button(onClick = onSoftwareTrigger) { Text("Software Trigger") }
        }
    }
}
```

- [ ] **Step 3: Create status and extras screens**

Create `StatusScreen.kt`:

```kotlin
package com.windfreak.synthhd.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.windfreak.synthhd.domain.DeviceStatus
import com.windfreak.synthhd.ui.components.Section

@Composable
fun StatusScreen(status: DeviceStatus) {
    Column {
        Section("Device Status") {
            Text("Connection: ${status.connectedLabel}")
            Text("Model: ${status.model}")
            Text("Serial: ${status.serial}")
            Text("Firmware: ${status.firmware}")
            Text("Calibration: ${status.calibrationDate}")
            Text("Temperature: ${status.temperatureC} C")
            Text("Reference: ${status.referenceMode}")
            Text("Lock detect: ${if (status.lockDetect) "Locked" else "Unlocked"}")
            Text("Level: ${if (status.levelOk) "OK" else "Warning"}")
        }
    }
}
```

Create `ExtrasScreen.kt`:

```kotlin
package com.windfreak.synthhd.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.windfreak.synthhd.domain.SynthDeviceState
import com.windfreak.synthhd.ui.components.Section

@Composable
fun ExtrasScreen(state: SynthDeviceState, onSave: () -> Unit, onReset: () -> Unit) {
    Column {
        Section("Simulator Actions") {
            Text("Save-to-device records the current simulated state locally.")
            Text("Last saved channel: ${state.savedSnapshot ?: "None"}")
            Button(onClick = onSave) { Text("Save Simulated Settings") }
            Button(onClick = onReset) { Text("Reset Simulator") }
        }
    }
}
```

- [ ] **Step 4: Wire the remaining screens into `SynthHdApp.kt`**

Add imports:

```kotlin
import com.windfreak.synthhd.ui.screens.ExtrasScreen
import com.windfreak.synthhd.ui.screens.ModulationScreen
import com.windfreak.synthhd.ui.screens.StatusScreen
import com.windfreak.synthhd.ui.screens.TriggerScreen
```

Extend the `when (selectedTab)` body:

```kotlin
3 -> ModulationScreen(state.modulation, viewModel::setModulation)
4 -> TriggerScreen(state.trigger, viewModel::setTrigger, viewModel::softwareTrigger)
5 -> StatusScreen(state.status)
6 -> ExtrasScreen(state, viewModel::saveToDevice, viewModel::resetToDefaults)
```

- [ ] **Step 5: Build**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/windfreak/synthhd/ui/SynthHdApp.kt app/src/main/java/com/windfreak/synthhd/ui/screens
git commit -m "feat: add modulation trigger status and extras screens"
```

## Task 10: Add Android UI Smoke Tests

**Files:**
- Test: `app/src/androidTest/java/com/windfreak/synthhd/SynthHdAppTest.kt`

- [ ] **Step 1: Write Compose smoke tests**

Create `app/src/androidTest/java/com/windfreak/synthhd/SynthHdAppTest.kt`:

```kotlin
package com.windfreak.synthhd

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class SynthHdAppTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun opensIntoSimulatorController() {
        compose.onNodeWithText("SynthHD Pro Simulator").assertIsDisplayed()
        compose.onNodeWithText("Offline simulator").assertIsDisplayed()
        compose.onNodeWithText("Generator").assertIsDisplayed()
    }

    @Test
    fun navigatesAcrossMajorScreens() {
        listOf("Sweep", "List", "Mod", "Trigger", "Status", "Extras").forEach { label ->
            compose.onNodeWithText(label).performClick()
            compose.onNodeWithText(label).assertIsDisplayed()
        }
    }
}
```

- [ ] **Step 2: Run JVM tests**

Run:

```bash
./gradlew :app:testDebugUnitTest
```

Expected: PASS.

- [ ] **Step 3: Run Android UI tests when an emulator or device is available**

Run:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Expected with a running emulator/device: PASS.

Expected without a running emulator/device: Gradle reports no connected devices. If no device is available, record that UI tests were added but not executed and verify the UI manually in Android Studio's emulator before claiming the implementation is complete.

- [ ] **Step 4: Commit**

```bash
git add app/src/androidTest/java/com/windfreak/synthhd/SynthHdAppTest.kt
git commit -m "test: add synthhd app smoke tests"
```

## Task 11: Final Verification and Polish

**Files:**
- Modify only files needed to fix issues found during verification.

- [ ] **Step 1: Run full local verification**

Run:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Run Android UI verification**

If an emulator or Android device is available, run:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Expected: `BUILD SUCCESSFUL`.

If no emulator or Android device is available, open the app in Android Studio and manually verify:

```text
The app opens to SynthHD Pro Simulator.
Channel A and Channel B can be selected.
Generator values can be edited and invalid values show messages.
Sweep screen calculates point count and duration.
List screen can add, delete, and clear points.
Modulation toggles change state.
Trigger screen can arm and software-trigger.
Status screen shows simulator readbacks.
Extras screen saves and resets simulated settings.
Closing and reopening restores the latest simulated state.
```

- [ ] **Step 3: Fix any verification failures**

For each failure, make the smallest code change that explains the failure, then rerun:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Confirm git status**

Run:

```bash
git status --short
```

Expected: no uncommitted app changes, or only deliberate final polish changes ready to commit.

- [ ] **Step 5: Commit final polish if needed**

If Step 3 changed files:

```bash
git add app
git commit -m "fix: polish synthhd simulator verification"
```

## Self-Review Checklist

Spec coverage:

- Offline simulator first: Tasks 2 through 11.
- Kotlin Android app: Task 1.
- Compose controller UI: Tasks 6 through 9.
- Two channels and core generator controls: Tasks 2, 4, and 8.
- Sweep and list mode: Tasks 3, 4, and 8.
- Modulation, trigger, status, extras: Tasks 4 and 9.
- Local persistence: Task 5 and Task 11 manual verification.
- Range validation: Task 2 and Task 4.
- Test and build verification: Tasks 2, 3, 4, 10, and 11.
- Future hardware-ready boundary: Task 4 controller interface.

Plan quality checks:

- Every task names exact files.
- Every test task includes command and expected result.
- The simulator is useful without USB hardware.
- The plan does not require copying the Windows UI.
- The plan keeps future USB work out of this milestone.
