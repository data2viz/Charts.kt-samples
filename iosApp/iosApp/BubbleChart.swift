//
//  BubbleChart.swift
//  iosApp
//
//  Created by Louis CAD on 24/04/2023.
//  Copyright © 2023 Data2Viz. All rights reserved.
//

import UIKit
import SwiftUI
import shared

struct BubbleChart: UIViewRepresentable {
    func makeUIView(context: Context) -> UIView {
        VizViewsKt.createBubbleChartView()
    }
    
    func updateUIView(_ uiView: UIView, context: Context) {}
}
