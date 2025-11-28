# components-android

## 2.1.1

### Patch Changes

- Update LiveKit Android SDK to 2.23.1 - [#54](https://github.com/livekit/components-android/pull/54) ([@davidliu](https://github.com/davidliu))

## 2.1.0

### Minor Changes

- Change the LocalMedia API to use setEnabled rather than start/stop methods to make clear that they are idempotent. - [#52](https://github.com/livekit/components-android/pull/52) ([@davidliu](https://github.com/davidliu))

### Patch Changes

- Remove Immutable annotation from TrackReference as the contained objects are not immutable - [#52](https://github.com/livekit/components-android/pull/52) ([@davidliu](https://github.com/davidliu))

- Fix rememberLiveKitRoom disabling audio/video if enabled from outside the composable - [#52](https://github.com/livekit/components-android/pull/52) ([@davidliu](https://github.com/davidliu))

- Fix isDeviceEnabled states not properly updating - [#52](https://github.com/livekit/components-android/pull/52) ([@davidliu](https://github.com/davidliu))

## 2.0.0

### Major Changes

- Compose depends on the timing of reads of `State` objects to determine whether it is a dependency for certain - [#50](https://github.com/livekit/components-android/pull/50) ([@davidliu](https://github.com/davidliu))
  use cases, such as when using `derivedStateOf` or `snapshotFlow`. When we pass back state values, these timings
  can be disassociated from their usage, causing Compose to not register the states appropriately and not update
  when the state value changed.

  To address this, we've changed the return values of simple functions like `rememberConnectionState` to return
  `State` objects instead of the values directly. This means that their reads will be more closely aligned with
  their usages and prevent issues with Compose not updating appropriately.

  To migrate, switch to using the `by` delegate syntax when declaring an object to hold the state:

  ```
  val connectionState by rememberConnectionState()
  ```

  In places where we return data objects to hold multiple values (such as `rememberRoomInfo`), we've kept the API
  to return values, as these have been converted to be delegates to the state objects backing them.

### Minor Changes

- Add session and agent APIs - [#50](https://github.com/livekit/components-android/pull/50) ([@davidliu](https://github.com/davidliu))

- Add rememberSpeakingParticipants - [#50](https://github.com/livekit/components-android/pull/50) ([@davidliu](https://github.com/davidliu))

- Change Chat to use datastreams - [#50](https://github.com/livekit/components-android/pull/50) ([@davidliu](https://github.com/davidliu))

### Patch Changes

- Fix RememberParticipantTrackReferences returning a new flow every recomposition - [#50](https://github.com/livekit/components-android/pull/50) ([@davidliu](https://github.com/davidliu))

- rememberLiveKitRoom: Only disconnect Room if it has connected before to manage the connection - [#50](https://github.com/livekit/components-android/pull/50) ([@davidliu](https://github.com/davidliu))

- rememberLiveKitRoom: Don't require local context if room is passed - [#50](https://github.com/livekit/components-android/pull/50) ([@davidliu](https://github.com/davidliu))

## 1.4.0

### Minor Changes

- Add disconnectOnDispose argument to RoomScope and rememberLiveKitRoom - [#40](https://github.com/livekit/components-android/pull/40) ([@davidliu](https://github.com/davidliu))

- Add rendererType parameter for VideoTrackView to allow choosing between Surface and Texture implementations - [#44](https://github.com/livekit/components-android/pull/44) ([@davidliu](https://github.com/davidliu))

- Added AudioVisualizer to allow for visualizations other than BarVisualizer - [#45](https://github.com/livekit/components-android/pull/45) ([@davidliu](https://github.com/davidliu))

### Patch Changes

- Update livekit sdk to 2.16.0 - [#40](https://github.com/livekit/components-android/pull/40) ([@davidliu](https://github.com/davidliu))

- Update noise lib to use a 16KB aligned version - [#45](https://github.com/livekit/components-android/pull/45) ([@davidliu](https://github.com/davidliu))

- Update livekit android sdk to 2.18.3 - [#45](https://github.com/livekit/components-android/pull/45) ([@davidliu](https://github.com/davidliu))

## 1.3.1

### Patch Changes

- Update for use with LiveKit Android SDK 2.14.0 - [#38](https://github.com/livekit/components-android/pull/38) ([@davidliu](https://github.com/davidliu))

## 1.3.0

### Minor Changes

- Add AudioBarVisualizer for audio waveform visualizations - [#32](https://github.com/livekit/components-android/pull/32) ([@davidliu](https://github.com/davidliu))

- Add rememberConnectionState and rememberVoiceAssistant - [#30](https://github.com/livekit/components-android/pull/30) ([@davidliu](https://github.com/davidliu))

### Patch Changes

- Fix local participant sometimes publishing multiple of local tracks when using RoomScope with audio/video = true - [#33](https://github.com/livekit/components-android/pull/33) ([@davidliu](https://github.com/davidliu))

## 1.2.0

### Minor Changes

- Add rememberTranscriptions - [#26](https://github.com/livekit/components-android/pull/26) ([@davidliu](https://github.com/davidliu))

- Update LiveKit Android SDK to 2.8.1 - [`5a79521ed2400867335442b0eac53a8cf20648cf`](https://github.com/livekit/components-android/commit/5a79521ed2400867335442b0eac53a8cf20648cf) ([@davidliu](https://github.com/davidliu))

- Add rememberEventSelector implementations for other types of events (e.g. ParticipantEvent, TrackEvent) - [#26](https://github.com/livekit/components-android/pull/26) ([@davidliu](https://github.com/davidliu))

### Patch Changes

- Fix rememberTrackReferences/ParticipantTrackReferences not updating when track subscribed - [#26](https://github.com/livekit/components-android/pull/26) ([@davidliu](https://github.com/davidliu))

## 1.2.0

### Minor Changes

- Add rememberTranscriptions - [#26](https://github.com/livekit/components-android/pull/26) ([@davidliu](https://github.com/davidliu))

- Update LiveKit Android SDK to 2.8.1 - [`5a79521ed2400867335442b0eac53a8cf20648cf`](https://github.com/livekit/components-android/commit/5a79521ed2400867335442b0eac53a8cf20648cf) ([@davidliu](https://github.com/davidliu))

- Add rememberEventSelector implementations for other types of events (e.g. ParticipantEvent, TrackEvent) - [#26](https://github.com/livekit/components-android/pull/26) ([@davidliu](https://github.com/davidliu))

### Patch Changes

- Fix rememberTrackReferences/ParticipantTrackReferences not updating when track subscribed - [#26](https://github.com/livekit/components-android/pull/26) ([@davidliu](https://github.com/davidliu))
