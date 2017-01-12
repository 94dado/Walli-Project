//
//  ProfileController.swift
//  Walli
//
//  Created by Daniele Piergigli on 20/04/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import Charts
import Foundation

class GraphController: UIViewController {
    
    @IBOutlet weak var LineChartProfile: LineChartView!
    @IBOutlet weak var PieChartProfile: PieChartView!
    @IBOutlet weak var SegmentBarLineCHart: UISegmentedControl!
    
    // For MVC
    var CGraph = GraphicFunction()
    var LFunc = LoginFunction()
    var CFunc = CommonFunction()
    
    override func viewWillAppear(animated: Bool) {
        sendRequest()
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Set lineChartView
        CGraph.StyleLineChart(LineChartProfile)
        // Set pieChartView
        CGraph.StylePieChart(PieChartProfile)
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // call to server for chart data
    func sendRequest() {
        let loginData = LFunc.fetchLoginData()
        CGraph.GetPieChart(loginData[0].id, keyuser: loginData[0].key, currency: loginData[0].currency, request: "https://walli.ddns.net:443/getCreditDebtForCharts") { request, error in
            if error == nil {
                let credito = request["credito"].double!
                let debito = request["debito"].double!
                // Create the pie chart
                self.CGraph.SetPieChart(["Credit", "Debt"], values: [debito, credito], PieChart: self.PieChartProfile)
                var date1 = String()
                var date2 = String()
                self.CGraph.SetTimeBySegment(&date1, end: &date2, segment: self.SegmentBarLineCHart.selectedSegmentIndex)
                self.CGraph.GetLineChart(loginData[0].id, keyuser: loginData[0].key, start: date1, end: date2, currency: loginData[0].currency, request: "https://walli.ddns.net:443/getSpeseForCharts") { request, error in
                    if error == nil {
                        var DateLine = [String]()
                        var ValueLine = [Double]()
                        for (key, value) in request {
                            DateLine.append(key)
                            ValueLine.append(value.double!)
                        }
                        // create the line chart
                        self.CGraph.SetLineChart(DateLine, values: ValueLine, LineChart: self.LineChartProfile)
                    }
                    else {
                        self.CFunc.errorAlert("Connection fail", controller: self, message: "Check your connection!")
                    }
                }
            }
            else {
                self.CFunc.errorAlert("Connection fail", controller: self, message: "Check your connection!")
            }
        }
    }
    
    // segment change data time
    @IBAction func GetLineChartInRange(sender: UISegmentedControl) {
        sendRequest()
    }
}
