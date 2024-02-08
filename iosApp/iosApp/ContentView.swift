import SwiftUI
import shared
import UIKit

struct ContentView: View {

	var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                NavigationLink(
                    destination: { BubbleChart() },
                    label: { Text("Bubble chart") }
                )
                NavigationLink(
                    destination: { LineChart() },
                    label: { Text("Line chart") }
                )
                NavigationLink(
                    destination: { ComposeView() },
                    label: { Text("Compose") }
                )
            }.navigationTitle("Charts.kt samples")
            BubbleChart()
        }
//        .navigationViewStyle(StackNavigationViewStyle())
	}
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
