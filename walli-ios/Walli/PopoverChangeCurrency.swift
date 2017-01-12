//
//  PopoverChangeCurrency.swift
//  Walli
//
//  Created by Daniele Piergigli on 07/07/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import Foundation

protocol PickerCurrencyChange {
    func ChangeCurrency(currency: String)
}

class PopoverChangeCurrency: UIViewController, UIPickerViewDelegate {
    
    @IBOutlet weak var PickerCurrency: UIPickerView!
    var PickerDelegate: PickerCurrencyChange?
    var currency = ["", "€", "$", "£"]
    var SelectedCurrency = String()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        PickerCurrency.delegate = self
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // This four function are used for the currency
    // just one picker must be set
    func numberOfComponentsInPickerView(pickerView: UIPickerView) -> Int {
        return 1
    }
    
    // num of picker
    func pickerView(pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return currency.count
    }
    
    // picker for each row
    func pickerView(pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        return currency[row]
    }
    // set picker
    func pickerView(pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        SelectedCurrency = currency[row]
    }
    
    
    // dismiss viewand pass data
    @IBAction func GetCurrencyBack(sender: UIButton) {
        self.dismissViewControllerAnimated(true, completion: nil)
        PickerDelegate?.ChangeCurrency(SelectedCurrency)
    }
}
