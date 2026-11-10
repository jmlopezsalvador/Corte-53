import SwiftUI
import Shared

/// Aloja la interfaz de Compose Multiplatform (todo el código de Corte 53 está en Kotlin).
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            // Compose ya respeta las zonas seguras (barra de estado, indicador de inicio) con sus propios
            // márgenes: así el fondo llega hasta los bordes, sin franjas blancas arriba y abajo.
            .ignoresSafeArea(.all)
            .background(Color(red: 0.06, green: 0.24, blue: 0.18))
            // barra de estado con texto claro, para el fondo negro del estilo iPhone
            .preferredColorScheme(.dark)
    }
}
