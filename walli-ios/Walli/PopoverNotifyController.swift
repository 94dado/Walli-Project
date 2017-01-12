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
protocol RefreshNotifyDelegate {
    func loadList()
}

class PopoverNotifyController: UIViewController, UITextViewDelegate {
    
    @IBOutlet weak var DescriptionNote: UITextView!
    @IBOutlet weak var CounterNote: UILabel!
    @IBOutlet weak var ButtonSender: UIButton!
    
    // if we want modify the notifications
    var ModifyState = String()
    var DescriptionValue = String()
    var Character = Int()
    var RefreshChatNotify: RefreshNotifyDelegate?
    
    // MVC
    var NFunc = NotifyFunction()
    var LFunc = LoginFunction()
    var CFunc = CommonFunction()
    var ChFunc = GroupsChatController()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        DescriptionNote.delegate = self
        
        // the keyboard can disappeared when tap on the screen
        self.hideKeyboardWhenTappedAround()
        
        // textview style
        DescriptionNote.layer.borderColor = UIColor(red: 0, green: 0, blue:0, alpha: 0.1).CGColor
        DescriptionNote.layer.borderWidth = 1.0
        DescriptionNote.layer.cornerRadius = 5.0
        // set if update
        if ModifyState != "" {
            DescriptionNote.text = DescriptionValue
            CounterNote.text = String(100 - Character)
            ButtonSender.setTitle("Update", forState: .Normal)
        }
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func SendNote(sender: UIButton) {
        // if we want update
        if ModifyState != "" && DescriptionNote.text! != DescriptionValue {
            let loginData = LFunc.fetchLoginData()
            // prepare for request groups
            if !loginData.isEmpty {
                // get request of group expenses
                NFunc.UpdateNotify(loginData[0].id, keyuser: loginData[0].key, notifyID: ModifyState, description: DescriptionNote.text!, request: "https://walli.ddns.net:443/updateNotify") { json, error in
                    if !json.isEmpty {
                        let realm = try! Realm()
                        do {
                            try realm.write  {
                                realm.create(NotifyDB.self, value: ["id_note": self.ModifyState, "descr": self.DescriptionNote.text!], update: true )
                            }
                        }
                        catch let error as NSError  {
                            print("Could not save \(error), \(error.userInfo)")
                        }
                        self.RefreshChatNotify?.loadList()
                        self.dismissViewControllerAnimated(true, completion: nil)
                    }
                    else {
                        self.CFunc.errorAlert("Connection fail", controller: self, message: "Check your connection!")
                    }
                }
            }
        }
        else {
            let loginData = LFunc.fetchLoginData()
            // prepare for request groups
            if !loginData.isEmpty {
                // get request of group expenses
                NFunc.SendNote(loginData[0].id, keyuser: loginData[0].key, groupID: loginData[0].current_group, description: DescriptionNote.text!, request: "https://walli.ddns.net:443/insertNotifica") { json, error in
                    if error == nil && !json.isEmpty {
                        let realm = try! Realm()
                        let noteDB = NotifyDB()
                        noteDB.id_group = loginData[0].current_group
                        noteDB.id_note = String(json["n_id"].int!)
                        noteDB.nickname = "You"
                        noteDB.descr = self.DescriptionNote.text!
                        noteDB.id_user = loginData[0].id
                        do {
                            try realm.write  {
                                realm.add(noteDB)
                            }
                        }
                        catch let error as NSError  {
                            print("Could not save \(error), \(error.userInfo)")
                        }
                        self.RefreshChatNotify?.loadList()
                        self.dismissViewControllerAnimated(true, completion: nil)
                    }
                    else {
                        self.CFunc.errorAlert("Connection fail", controller: self, message: "Check your connection!")
                    }
                }
            }
        }
    }
    
    // set max length for label
    func textViewDidChange(textView: UITextView) {
        let maxlength = 100
        let characters = DescriptionNote.text.characters.count
        let remainingCharacter = maxlength - characters
        CounterNote.text = String(remainingCharacter)
    }
    
    // set max lenght
    func textView(textView: UITextView, shouldChangeTextInRange range: NSRange, replacementText text: String) -> Bool {
        let newText = (textView.text as NSString).stringByReplacingCharactersInRange(range, withString: text)
        let numberOfChars = newText.characters.count
        return numberOfChars <= 100;
    }
}
