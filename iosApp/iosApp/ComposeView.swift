import Foundation
import SwiftUI
import shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        ComposeVizViewControllerKt.ComposeVizViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}