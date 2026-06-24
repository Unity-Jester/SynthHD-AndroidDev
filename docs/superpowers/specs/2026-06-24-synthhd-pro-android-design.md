# SynthHD Pro Android Controller Design

Date: 2026-06-24

## Goal

Build the first Android version of a controller for the Windfreak SynthHD Pro V2. This version will use the public Windfreak documentation as the source of truth and will duplicate the Windows app's functional surface, while allowing a cleaner Android-native user experience.

The first milestone is offline only. It will not connect to real hardware yet. It will simulate the device state, responses, validation rules, and status readbacks so the app can be used and tested before USB support is added.

## Sources

- Windfreak SynthHD Pro product page: https://windfreaktech.com/product/microwave-signal-generator-synthhd-pro/
- Windfreak SynthHD software guide and serial programming/API documentation linked from the public Windfreak materials.

## User Experience

The app opens directly into a controller interface, not a landing page. It uses a compact Android-native layout:

- A device header showing offline simulator status.
- Channel A and Channel B selection with a persistent summary of the active channel.
- Always-visible core controls for frequency, power, phase, and RF output.
- Focused tabs for Generator, Sweep, List, Modulation, Trigger, Status, and Extras.
- A persistent readback/status area showing simulated lock, level, reference, temperature, version, serial, and calibration-style values.

The design should improve on the Windows app where Android can do better: fewer crowded panels, stronger grouping, safer inputs, clearer warnings, touch-friendly controls, and faster switching between channels and modes.

## Functional Scope

The app will model a SynthHD Pro V2 with two channels and shared device settings.

Core generator controls:

- Channel A and Channel B selection.
- Frequency from 10 MHz to 24 GHz.
- Output power using the documented SynthHD Pro range, approximately -40 dBm to +18 dBm.
- Phase from 0 to 360 degrees.
- RF output enable and disable.
- Channel lock behavior.
- Reference input and output settings.

Sweep controls:

- Linear frequency sweep settings.
- Sweep start, stop, step, and timing fields.
- Sweep direction and run mode.
- Simulated sweep timing calculations.
- Triggered and continuous sweep behavior in simulated form.

List and hop controls:

- Hop/list table editing.
- Up to 500 points.
- Per-point frequency, power, and dwell-style values where supported by the public command model.
- Add, edit, delete, clear, and reorder rows.
- Simulated execution of list mode.

Modulation controls:

- Pulse modulation settings.
- AM controls.
- FM and chirp controls.
- Modulation enable and disable.
- Simulated behavior for channel-specific modulation readbacks.

Trigger controls:

- Software trigger simulation.
- External trigger mode settings.
- Trigger polarity or edge-style settings where documented.
- Clear feedback showing whether the simulator is idle, armed, running, or complete.

Status and extras:

- Simulated device connection state.
- Version, model, serial, calibration date, and temperature readouts.
- Lock detect and level indicators.
- Save-to-device simulation.
- Reset-to-default simulation.
- Local persistence so the app restores its last simulated state after restart.

## Behavioral Model

The app will behave as if user actions are translated into device commands, even though the first version is offline. That gives the app a stable path to later USB support.

The app state will include:

- Device-level settings and readbacks.
- Channel A state.
- Channel B state.
- Sweep settings per relevant channel.
- List table state.
- Modulation settings.
- Trigger state.
- Local persistence state.

The simulator will validate values before accepting them. It will clamp or reject values according to the intended user experience for each control:

- Numeric fields should show clear errors for invalid values.
- Values outside public SynthHD Pro V2 ranges should not silently apply.
- Settings that are valid but hardware-specific should show warnings rather than pretending real hardware is connected.

The simulator should reproduce useful device quirks from the public command model:

- One selected control channel at a time.
- Separate channel state for frequency, power, phase, and output.
- Shared device-level settings where appropriate.
- Status readbacks that change when simulated modes run.
- Save-to-device behavior that records current simulated settings.

## Architecture

The project will be a native Android app written in Kotlin with Jetpack Compose.

Primary layers:

- Compose UI: screens, tabs, forms, tables, status panels, and user interactions.
- View model/state layer: owns screen state, validation state, and user actions.
- Device controller interface: exposes operations that look like device commands.
- Simulator controller: implements the device controller interface for offline behavior.
- Persistence layer: stores and restores the simulated device state locally.

The device controller interface is the key future-proofing boundary. The initial app will depend on the simulator implementation. A later real USB serial implementation can use the same interface and reuse the UI and most validation logic.

## Main Screens

Generator:

- Active channel picker.
- Frequency, power, phase, RF output, and lock controls.
- Immediate simulated readback.

Sweep:

- Linear sweep setup.
- Start, stop, step, dwell/timing, direction, and mode controls.
- Run, pause/stop, and software trigger controls.

List:

- 500-point-capable editable table.
- Row add/edit/delete.
- Bulk clear.
- Simulated run controls.

Modulation:

- Pulse, AM, FM, and chirp grouped into focused sections.
- Enable/disable controls and relevant numeric fields.
- Warnings for settings that depend on real hardware.

Trigger:

- Trigger source and mode controls.
- Software trigger button.
- Simulated armed/running/complete state.

Status:

- Device, channel, reference, lock, level, temperature, version, serial, and calibration-style readbacks.

Extras:

- Save simulated settings.
- Reset simulated settings.
- Export/import can be deferred unless needed during implementation.

## Error Handling

The app will distinguish between:

- Invalid input: clearly marked in the form and not applied.
- Unsupported offline behavior: shown as an offline simulator limitation.
- Simulated device warning: shown as a non-blocking warning when a setting is valid but may behave differently on hardware.

Since the first version has no real hardware connection, it will not show USB errors. Any hardware connection entry points will be disabled until a later USB milestone.

## Testing

Before reporting the implementation complete, verification should include:

- Unit tests for range validation and simulator state changes.
- Unit tests for sweep/list calculations where practical.
- Persistence test or representative manual check that app state restores.
- Android build verification.
- UI verification of the main screens and important flows.

For visual verification, the app should be launched or inspected with available Android tooling when possible. The initial spec itself is complete when it is written, reviewed for ambiguity, and committed.

## Out of Scope for First Milestone

- Real USB serial communication.
- Bluetooth or network control.
- Exact reproduction of the Windows app layout.
- Firmware update workflows.
- Calibration editing beyond simulated readbacks.
- Cloud sync or account features.

## Finishing Criteria

The first implementation milestone is done when:

- A Kotlin Android project exists and builds.
- The app opens into the SynthHD Pro simulator controller.
- All major functional areas listed in this spec are represented.
- Controls update simulated device state with range validation.
- The app persists and restores simulated state.
- Verification has been run and results are reported.
