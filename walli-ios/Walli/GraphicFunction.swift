//
//  ProfileFunction.swift
//  Walli
//
//  Created by Daniele Piergigli on 23/04/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import Charts
import SwiftyJSON
import Foundation

class GraphicFunction {
    
    // For parameters
    var CCommon = CommonFunction()
    var MServer = ServerFunction()
    
    // Set the data in the lineChartView
    func SetLineChart(dataPoints: [String], values: [Double], LineChart: LineChartView) {
        
        var dataEntries: [ChartDataEntry] = []
        
        for i in 0..<dataPoints.count {
            let dataEntry = ChartDataEntry(value: values[i], xIndex: i)
            dataEntries.append(dataEntry)
        }
        
        let lineChartDataSet = LineChartDataSet(yVals: dataEntries, label: "Money Spent")
        lineChartDataSet.drawCirclesEnabled = false
        lineChartDataSet.drawValuesEnabled = true
        lineChartDataSet.valueTextColor = UIColor.whiteColor()
        lineChartDataSet.drawVerticalHighlightIndicatorEnabled = false
        lineChartDataSet.drawHorizontalHighlightIndicatorEnabled = false
        lineChartDataSet.setColor(UIColor.whiteColor())
        let lineChartData = LineChartData(xVals: dataPoints, dataSet: lineChartDataSet)
        LineChart.data = lineChartData
        
    }
    
    // Style of the lineChartView
    func StyleLineChart (LineChart: LineChartView) {
        LineChart.legend.enabled = false
        LineChart.xAxis.drawGridLinesEnabled = false
        LineChart.xAxis.drawAxisLineEnabled = false
        LineChart.xAxis.labelTextColor = UIColor.whiteColor()
        LineChart.leftAxis.drawGridLinesEnabled = false
        LineChart.leftAxis.labelTextColor = UIColor.whiteColor()
        LineChart.rightAxis.drawGridLinesEnabled = false
        LineChart.rightAxis.drawZeroLineEnabled = true
        LineChart.rightAxis.zeroLineColor = UIColor.whiteColor()
        LineChart.rightAxis.zeroLineDashLengths = [5]
        LineChart.rightAxis.drawLabelsEnabled = false
        LineChart.leftAxis.drawAxisLineEnabled = false
        LineChart.rightAxis.drawAxisLineEnabled = false
        LineChart.drawGridBackgroundEnabled = false
        LineChart.drawBordersEnabled = false
        LineChart.descriptionText = ""
        
        LineChart.animate(xAxisDuration: 2.0, easingOption: ChartEasingOption.EaseInCubic)
    }
    
    // Set the data in the pieChartView
    func SetPieChart(dataPoints: [String], values: [Double], PieChart: PieChartView) {
        
        var dataEntries: [ChartDataEntry] = []
        // set color for the center pie chart string
        let mydebt = [ NSForegroundColorAttributeName: CCommon.SetupWalli]
        let mycredit = NSAttributedString(string: "Debt: " + String(format: "-%.2f", values[0]) + "$", attributes: [NSForegroundColorAttributeName : CCommon.OrangeWalli])
        let myString = NSMutableAttributedString(string: "Credit: " + String(format: "+%.2f", values[1]) + "$" + "\n", attributes: mydebt )
        myString.appendAttributedString(mycredit)
        
        for i in 0..<dataPoints.count {
            let dataEntry = ChartDataEntry(value: values[i], xIndex: i)
            dataEntries.append(dataEntry)
        }
        
        let pieChartDataSet = PieChartDataSet(yVals: dataEntries, label: "Money Spent")
        pieChartDataSet.colors = [CCommon.OrangeWalli, CCommon.SetupWalli]
        pieChartDataSet.drawValuesEnabled = false
        let pieChartData = PieChartData(xVals: dataPoints, dataSet: pieChartDataSet)
        PieChart.data = pieChartData
        PieChart.centerAttributedText = myString
    }
    
    // Style of the pieChartView
    func StylePieChart (PieChart: PieChartView) {
        PieChart.legend.enabled = false
        PieChart.holeColor = UIColor.whiteColor()
        PieChart.animate(yAxisDuration: 2.0, easingOption: ChartEasingOption.EaseInCirc)
        PieChart.transparentCircleColor = CCommon.OrangeWalli
        PieChart.descriptionText = ""
        PieChart.drawSliceTextEnabled = false
        PieChart.holeRadiusPercent = 0.95
    }
    
    // Get pie chart data
    func GetPieChart(userid: String, keyuser: String, currency: String, request: String, completionHandler: (JSON, NSError?) -> ())  {
        let stringRequest = ["id": userid, "key": keyuser, "valuta": currency]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if !response.isEmpty {
                completionHandler(response, nil)
            }
            else {
                print("Errore response pie chart \(error)")
                completionHandler(nil, error)
            }
        }
    }
    
    // Get Linear chart data
    func GetLineChart(userid: String, keyuser: String, start: String, end: String, currency: String, request: String, completionHandler: (JSON, NSError?) -> ())  {
        let stringRequest = ["id": userid, "key": keyuser, "date1": start, "date2": end, "valuta": currency]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if !response.isEmpty {
                completionHandler(response, nil)
            }
            else {
                print("Errore response line chart \(error)")
                completionHandler(nil, error)
            }
        }
    }
    
    // set time for linechart values
    func SetTimeBySegment(inout start: String, inout end: String, segment: Int) {
        let endDate = NSDate()
        var AddDay = Int()
        // set data for the secment value you are chosed
        if segment == 0 {
            AddDay = -7
        }
        else if segment == 1 {
            AddDay = -30
        }
        else {
            AddDay = -365
        }
        let startDate = NSCalendar.currentCalendar().dateByAddingUnit(NSCalendarUnit.Day, value: AddDay, toDate: endDate, options: NSCalendarOptions.init(rawValue: 0))!
        let dateFormatter = NSDateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd"
        start = dateFormatter.stringFromDate(startDate)
        end = dateFormatter.stringFromDate(endDate)
    }
}