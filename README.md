<!--BEGIN_BANNER_IMAGE-->

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="/.github/banner_dark.png">
  <source media="(prefers-color-scheme: light)" srcset="/.github/banner_light.png">
  <img style="width:100%;" alt="The LiveKit icon, the name of the repository and some sample code in the background." src="https://raw.githubusercontent.com/livekit/components-android/main/.github/banner_light.png">
</picture>

<!--END_BANNER_IMAGE-->

# LiveKit Components for Android

<!--BEGIN_DESCRIPTION-->
Use this SDK to add real-time video, audio and data features to your Android app. By connecting to a self- or cloud-hosted <a href="https://livekit.io/">LiveKit</a> server, you can quickly build applications like interactive live streaming or video calls with just a few lines of code.
<!--END_DESCRIPTION-->

# Table of Contents

- [Docs](#docs)
- [Installation](#installation)
- [Usage](#basic-usage)

## Docs

Docs and guides at [https://docs.livekit.io](https://docs.livekit.io)

## Installation

LiveKit Components for Android is available as a Maven package.

```groovy title="build.gradle"
...
dependencies {
    implementation "io.livekit:livekit-android-compose-components:1.1.1"
    // Snapshots of the latest development version are available at:
    // implementation "io.livekit:livekit-android-compose-components:1.1.2-SNAPSHOT"
}
```

You'll also need jitpack as one of your repositories.

```groovy
subprojects {
    repositories {
        google()
        mavenCentral()
        // ...
        maven { url 'https://jitpack.io' }

        // For SNAPSHOT access
        // maven { url 'https://s01.oss.sonatype.org/content/repositories/snapshots/' }
    }
}
```

## Basic Usage

```kotlin
@Composable
fun exampleComposable() {
    // Create and connect to a room.
    RoomScope(
        url = wsURL,
        token = token,
        audio = true,
        video = true,
        connect = true,
    ) {
        // Get all the tracks in the room
        val trackRefs = rememberTracks()

        // Display the video tracks
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(trackRefs.size) { index ->
                VideoTrackView(
                    trackReference = trackRefs[index],
                    modifier = Modifier.fillParentMaxHeight(0.5f)
                )
            }
        }
    }
}
```

## Example App

See our [Meet Example app](https://github.com/livekit-examples/android-components-meet) for a simple teleconferencing app, and [Livestream Example app](https://github.com/livekit-examples/android-livestream) for a 
fully-functional livestreaming app, with more fleshed out usage of the Components SDK.

<!--BEGIN_REPO_NAV-->
<br/><table>
<thead><tr><th colspan="2">LiveKit Ecosystem</th></tr></thead>
<tbody>
<tr><td>Real-time SDKs</td><td><a href="https://github.com/livekit/components-js">React Components</a> · <a href="https://github.com/livekit/client-sdk-js">JavaScript</a> · <a href="https://github.com/livekit/client-sdk-swift">iOS/macOS</a> · <a href="https://github.com/livekit/client-sdk-android">Android</a> · <a href="https://github.com/livekit/client-sdk-flutter">Flutter</a> · <a href="https://github.com/livekit/client-sdk-react-native">React Native</a> · <a href="https://github.com/livekit/client-sdk-rust">Rust</a> · <a href="https://github.com/livekit/client-sdk-python">Python</a> · <a href="https://github.com/livekit/client-sdk-unity-web">Unity (web)</a> · <a href="https://github.com/livekit/client-sdk-unity">Unity (beta)</a></td></tr><tr></tr>
<tr><td>Server APIs</td><td><a href="https://github.com/livekit/server-sdk-js">Node.js</a> · <a href="https://github.com/livekit/server-sdk-go">Golang</a> · <a href="https://github.com/livekit/server-sdk-ruby">Ruby</a> · <a href="https://github.com/livekit/server-sdk-kotlin">Java/Kotlin</a> · <a href="https://github.com/livekit/client-sdk-python">Python</a> · <a href="https://github.com/livekit/client-sdk-rust">Rust</a> · <a href="https://github.com/agence104/livekit-server-sdk-php">PHP (community)</a></td></tr><tr></tr>
<tr><td>Agents Frameworks</td><td><a href="https://github.com/livekit/agents">Python</a> · <a href="https://github.com/livekit/agent-playground">Playground</a></td></tr><tr></tr>
<tr><td>Services</td><td><a href="https://github.com/livekit/livekit">Livekit server</a> · <a href="https://github.com/livekit/egress">Egress</a> · <a href="https://github.com/livekit/ingress">Ingress</a> · <a href="https://github.com/livekit/sip">SIP</a></td></tr><tr></tr>
<tr><td>Resources</td><td><a href="https://docs.livekit.io">Docs</a> · <a href="https://github.com/livekit-examples">Example apps</a> · <a href="https://livekit.io/cloud">Cloud</a> · <a href="https://docs.livekit.io/oss/deployment">Self-hosting</a> · <a href="https://github.com/livekit/livekit-cli">CLI</a></td></tr>
</tbody>
</table>
<!--END_REPO_NAV-->
