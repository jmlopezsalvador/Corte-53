import SwiftUI
import AVFoundation

@main
struct iOSApp: App {
    init() {
        // Categoría "playback": la voz suena aunque el interruptor de silencio esté activado.
        try? AVAudioSession.sharedInstance().setCategory(.playback, mode: .spokenAudio)
        try? AVAudioSession.sharedInstance().setActive(true)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
