//
//  PopoverChatController.swift
//  Walli
//
//  Created by Daniele Piergigli on 28/06/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import RealmSwift
import Foundation

// Set prototype for refresh
protocol RefreshExpenseDelegate {
    func loadList()
}

class PopoverExpensesController: UIViewController, UITextViewDelegate {
    
    @IBOutlet weak var MoneyExpenses: UITextField!
    @IBOutlet weak var DescriptionExpenses: UITextView!
    @IBOutlet weak var CounterExpenses: UILabel!
    @IBOutlet weak var ButtonSender: UIButton!
    
    // For the request
    var notifyID = String()
    var descriptionReceive = String()
    // if we want modify the expenses
    var ModifyState = String()
    var DescriptionValue = String()
    var MoneyValue = String()
    var Character = Int()
    var RefreshChatExpenses: RefreshExpenseDelegate?
    
    // MVC
    var EFunc = ExpensesFunction()
    var LFunc = LoginFunction()
    var CFunc = CommonFunction()
    var ChFunc = GroupsChatController()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        DescriptionExpenses.delegate = self
        
        // the keyboard can disappeared when tap on the screen
        self.hideKeyboardWhenTappedAround()
        
        // if prevent a notify effect
        if descriptionReceive != "" {
            DescriptionExpenses.text = descriptionReceive
        }
        // textview style
        DescriptionExpenses.layer.borderColor = UIColor(red: 0, green: 0, blue:0, alpha: 0.1).CGColor
        DescriptionExpenses.layer.borderWidth = 1.0
        DescriptionExpenses.layer.cornerRadius = 5.0
        // set if update
        if ModifyState != "" {
            DescriptionExpenses.text = DescriptionValue
            CounterExpenses.text = String(100 - Character)
            MoneyExpenses.text = MoneyValue
            ButtonSender.setTitle("Update", forState: .Normal)
        }
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func SendExpenses(sender: UIButton) {
        // update the expense
        if ModifyState != "" && DescriptionExpenses.text != DescriptionValue && MoneyExpenses != MoneyValue {
            if DescriptionExpenses.text != "" && MoneyExpenses.text != "" {
                let loginData = LFunc.fetchLoginData()
                // prepare for request groups
                if !loginData.isEmpty {
                    // update request of group expenses
                    EFunc.CheckExpense(loginData[0].id, keyuser: loginData[0].key, expenseID: ModifyState, request: "https://walli.ddns.net:443/isChangeableShop") { json, error in
                        if !json.isEmpty {
                            self.EFunc.UpdateExpense(loginData[0].id, keyuser: loginData[0].key, expenseID: self.ModifyState, description: self.DescriptionExpenses.text!, value: self.MoneyExpenses.text!, request: "https://walli.ddns.net:443/changeShop") { json, error in
                                if error == nil && !json.isEmpty {
                                    let realm = try! Realm()
                                    do {
                                        try realm.write  {
                                            realm.create(ExpensesDB.self, value: ["id_expenses": self.ModifyState, "descr": self.DescriptionExpenses.text!, "value": self.MoneyExpenses.text!], update: true )
                                        }
                                    }
                                    catch let error as NSError  {
                                        print("Could not save \(error), \(error.userInfo)")
                                    }
                                    // expenses view is refreshed
                                    self.RefreshChatExpenses?.loadList()
                                    self.dismissViewControllerAnimated(true, completion: nil)
                                }
                                else {
                                    self.CFunc.errorAlert("Connection fail", controller: self, message: "Check your connection!")
                                }
                            }
                        }
                        else {
                            self.CFunc.errorAlert("Error", controller: self, message: "You can't modify it, because someone has already paid this expense")
                        }
                    }
                }
            }
            else {
                self.CFunc.errorAlert("Error", controller: self, message: "Text field must be filled!")
            }
        }
        // create a new expense
        else {
            if DescriptionExpenses.text != "" && MoneyExpenses.text != "" {
                let loginData = LFunc.fetchLoginData()
                // prepare for request groups
                if !loginData.isEmpty {
                    // create request of expenses
                    EFunc.SendExpenses(loginData[0].id, keyuser: loginData[0].key, groupID: loginData[0].current_group, description: DescriptionExpenses.text!, value: MoneyExpenses.text!, notifyID: notifyID, request: "https://walli.ddns.net:443/insertSpesa") { json, error in
                        if error == nil && !json.isEmpty {
                            self.EFunc.PopulateDatabaseOfExpenses(loginData[0].current_group, ExpensesID: String(json["s_id"].int!), money: Double(self.MoneyExpenses.text!.stringByReplacingOccurrencesOfString(",", withString: "."))!, description: self.DescriptionExpenses.text!, date: json["date"].string!, userID: loginData[0].id, notifyID: self.notifyID)
                            self.RefreshChatExpenses?.loadList()
                            self.dismissViewControllerAnimated(true, completion: nil)
                        }
                        else {
                            self.CFunc.errorAlert("Connection fail", controller: self, message: "Check your connection!")
                        }
                    }
                }
            }
            else {
                self.CFunc.errorAlert("Error", controller: self, message: "Text field must be filled!")
            }
        }
    }
    
    // set max length for label
    func textViewDidChange(textView: UITextView) {
        let maxlength = 100
        let characters = DescriptionExpenses.text.characters.count
        let remainingCharacter = maxlength - characters
        CounterExpenses.text = String(remainingCharacter)
    }
    
    // set max lenght
    func textView(textView: UITextView, shouldChangeTextInRange range: NSRange, replacementText text: String) -> Bool {
        let newText = (textView.text as NSString).stringByReplacingCharactersInRange(range, withString: text)
        let numberOfChars = newText.characters.count
        return numberOfChars <= 100;
    }
    
}
