# components-android

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
