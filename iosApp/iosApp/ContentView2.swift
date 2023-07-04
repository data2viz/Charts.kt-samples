import SwiftUI
import shared
import UIKit

struct ContentView2: View {

	var body: some View {
        VStack {
            Text("Charts.kt samples")
            HStack {
                Spacer(minLength: 20)
                Text("Looool")
                VizView()
            }
            VizView().frame(width: 200, height: 200)
        }
	}
}

struct ContentView2_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
