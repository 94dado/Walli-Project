//
//  ViewController.swift
//  Walli
//
//  Created by Daniele Piergigli on 16/04/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import RealmSwift
import Foundation

class ChatCells: UITableViewCell {
    
    @IBOutlet weak var ChatNameCell: UILabel!
    @IBOutlet weak var ChatUpdateCell: UILabel!
    @IBOutlet weak var ChatImageCell: UIImageView!
    @IBOutlet weak var ChatMoneyCell: UILabel!
    @IBOutlet weak var ChatDescriptionCell: UITextView!
    @IBOutlet weak var ChatButtonPaid: UIButton!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    
    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
}

class NoteCells: UITableViewCell {
    
    @IBOutlet weak var ChatNoteCell: UITextView!
    @IBOutlet weak var ChatNicknameCell: UILabel!
    @IBOutlet weak var ChatImageNoteCell: UIImageView!
    @IBOutlet weak var ChatNoteButtonPaid: UIButton!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    
    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
}

class GroupsChatController: UIViewController, UITableViewDataSource, UITableViewDelegate, UIPopoverPresentationControllerDelegate, RefreshExpenseDelegate, RefreshNotifyDelegate {
    
    // MVC
    var CFunc = CommonFunction()
    var LFunc = LoginFunction()
    var CGFunc = ChatGroupFunction()
    var IFunc = ImageFunction()
    
    //Define tableView and segment
    @IBOutlet weak var SegmentedChatControl: UISegmentedControl!
    @IBOutlet weak var ChatTableView: UITableView!
    var titelNav = String()
    var currency = String()
    // count umber of row
    var countRows = Int()
    
    override func viewWillAppear(animated: Bool) {
        // finche non ho il database interno
        let loginData = LFunc.fetchLoginData()
        // prepare for request groups
        self.navigationItem.title = "Update..."
        if !loginData.isEmpty {
            // get request of group expenses
            CGFunc.GetChatGroups(loginData[0].id, keyuser: loginData[0].key, groupID: loginData[0].current_group, request: "https://walli.ddns.net:443/getSpeseByGroup") { error in
                if error == nil {
                    // download notify
                    self.CGFunc.GetNotifyGroups(loginData[0].id, keyuser: loginData[0].key, groupID: loginData[0].current_group, request: "https://walli.ddns.net:443/getNotificheByGroup") { error in
                        if error == nil {
                            // execute one time first time will be set to true
                            self.navigationItem.title = self.titelNav
                            self.ChatTableView.reloadData()
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
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    // Function override UITableViewDataSource and allow to print data on screen
    func tableView(tableView:UITableView, numberOfRowsInSection section:Int) -> Int {
        let loginData = LFunc.fetchLoginData()
        let expensesData = CGFunc.SearchInSegment(SegmentedChatControl.selectedSegmentIndex, idGroup: loginData[0].current_group)
        let notifyData = CGFunc.FetchNotes(loginData[0].current_group)
        var count = Int()
        // Set data in cells for all
        if SegmentedChatControl.selectedSegmentIndex == 2 {
            count = expensesData.count + notifyData.count
        }
        // Set data in cells for to pay
        else if SegmentedChatControl.selectedSegmentIndex == 0 {
            for e in expensesData {
                if e.paid == 0 {
                    count += 1
                }
            }
            count += notifyData.count
        }
        // Set data in cells for paid
        else if SegmentedChatControl.selectedSegmentIndex == 1 {
            for e in expensesData {
                if e.paid == 1 {
                    count += 1
                }
            }
            count += notifyData.count
        }
        return count
    }
    
    // reload data with the correct element
    @IBAction func ChangeRangeOfExpenses(sender: UISegmentedControl) {
        ChatTableView.reloadData()
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let loginData = LFunc.fetchLoginData()
        let expensesData = CGFunc.SearchInSegment(SegmentedChatControl.selectedSegmentIndex, idGroup: loginData[0].current_group)
        let notifyData = CGFunc.FetchNotes(loginData[0].current_group)
        if indexPath.row < notifyData.count {
            let NoteCell = self.ChatTableView.dequeueReusableCellWithIdentifier("NoteCells", forIndexPath: indexPath) as! NoteCells
            // image from notify
            let imageData = IFunc.FetchUserImage(notifyData[indexPath.row].id_user)
            // Set style image note
            NoteCell.ChatImageNoteCell.layer.borderWidth = 1
            NoteCell.ChatImageNoteCell.layer.masksToBounds = false
            NoteCell.ChatImageNoteCell.layer.borderColor = CFunc.BlackWalli.CGColor
            NoteCell.ChatImageNoteCell.layer.cornerRadius = NoteCell.ChatImageNoteCell.frame.height/2
            NoteCell.ChatImageNoteCell.clipsToBounds = true
        
            // Style nickname note
            NoteCell.ChatNicknameCell.font = UIFont.boldSystemFontOfSize(15.0)
            
            // set Notecell
            NoteCell.ChatImageNoteCell.image = UIImage(data: imageData[0].image)
            NoteCell.ChatNicknameCell.text = "@" + notifyData[indexPath.row].nickname
            NoteCell.ChatNoteCell.text = notifyData[indexPath.row].descr
            NoteCell.ChatNoteButtonPaid.tag = indexPath.row
            return NoteCell
        }
        else {
            let cell = ChatTableView.dequeueReusableCellWithIdentifier("ChatCells", forIndexPath: indexPath) as! ChatCells
            let imageData = IFunc.FetchUserImage(expensesData[indexPath.row - notifyData.count].id_user)
            // Set style image
            cell.ChatImageCell.layer.borderWidth = 1
            cell.ChatImageCell.layer.masksToBounds = false
            cell.ChatImageCell.layer.borderColor = CFunc.BlackWalli.CGColor
            cell.ChatImageCell.layer.cornerRadius = cell.ChatImageCell.frame.height/2
            cell.ChatImageCell.clipsToBounds = true
        
            // Set style money
            cell.ChatMoneyCell.textColor = UIColor(red: 1, green: 87/255, blue: 34/255, alpha: 1)
            cell.ChatMoneyCell.font = UIFont.boldSystemFontOfSize(16.0)
        
            // Style name
            cell.ChatNameCell.font = UIFont.boldSystemFontOfSize(13.0)
            // Set data in cells for to pay
            if expensesData[indexPath.row - notifyData.count].paid == 0 && SegmentedChatControl.selectedSegmentIndex == 0 {
                cell.ChatImageCell.image = UIImage(data: imageData[0].image)
                if expensesData[indexPath.row - notifyData.count].nickname == "You" {
                    cell.ChatMoneyCell.text = String(format: "%.2f", Double(expensesData[indexPath.row - notifyData.count].value)!) + currency
                }
                else {
                    cell.ChatMoneyCell.text = String(format: "-%.2f", Double(expensesData[indexPath.row - notifyData.count].value)!) + currency
                }
                cell.ChatNameCell.text = "@" + expensesData[indexPath.row - notifyData.count].nickname
                cell.ChatUpdateCell.text = expensesData[indexPath.row - notifyData.count].data
                cell.ChatDescriptionCell.text = expensesData[indexPath.row - notifyData.count].descr
                cell.ChatButtonPaid.tag = indexPath.row - notifyData.count
                cell.ChatButtonPaid.setTitle("To Pay", forState: .Normal)
                cell.ChatButtonPaid.setImage(nil, forState: .Normal)
            }
            // Set data in cells for paid
            else if expensesData[indexPath.row - notifyData.count].paid == 1 && SegmentedChatControl.selectedSegmentIndex == 1 {
                cell.ChatImageCell.image = UIImage(data: imageData[0].image)
                if expensesData[indexPath.row - notifyData.count].nickname == "You" {
                    cell.ChatMoneyCell.text = String(format: "%.2f", Double(expensesData[indexPath.row - notifyData.count].value)!) + currency
                }
                else {
                    cell.ChatMoneyCell.text = String(format: "-%.2f", Double(expensesData[indexPath.row - notifyData.count].value)!) + currency
                }
                cell.ChatNameCell.text = "@" + expensesData[indexPath.row - notifyData.count].nickname
                cell.ChatUpdateCell.text = expensesData[indexPath.row - notifyData.count].data
                cell.ChatDescriptionCell.text = expensesData[indexPath.row - notifyData.count].descr
                cell.ChatButtonPaid.tag = indexPath.row - notifyData.count
                cell.ChatButtonPaid.setTitle("", forState: .Normal)
                cell.ChatButtonPaid.setImage(UIImage(named: "ExpensesPaid"), forState: .Normal)
                cell.ChatButtonPaid.tag = indexPath.row - notifyData.count
            }
            // Set data in cells for all
            else if SegmentedChatControl.selectedSegmentIndex == 2 {
                cell.ChatImageCell.image = UIImage(data: imageData[0].image)
                if expensesData[indexPath.row - notifyData.count].nickname == "You" {
                    cell.ChatMoneyCell.text = String(format: "%.2f", Double(expensesData[indexPath.row - notifyData.count].value)!) + currency
                }
                else {
                    cell.ChatMoneyCell.text = String(format: "-%.2f", Double(expensesData[indexPath.row - notifyData.count].value)!) + currency
                }
                cell.ChatNameCell.text = "@" + expensesData[indexPath.row - notifyData.count].nickname
                cell.ChatUpdateCell.text = expensesData[indexPath.row - notifyData.count].data
                cell.ChatDescriptionCell.text = expensesData[indexPath.row - notifyData.count].descr
                if expensesData[indexPath.row - notifyData.count].paid == 0 {
                    cell.ChatButtonPaid.tag = indexPath.row - notifyData.count
                    cell.ChatButtonPaid.setTitle("To Pay", forState: .Normal)
                    cell.ChatButtonPaid.setImage(nil, forState: .Normal)
                }
                else {
                    cell.ChatButtonPaid.setTitle("", forState: .Normal)
                    cell.ChatButtonPaid.setImage(UIImage(named: "ExpensesPaid"), forState: .Normal)
                    cell.ChatButtonPaid.tag = indexPath.row - notifyData.count
                }
            }
            return cell
        }
    }
    
    // turn back
    @IBAction func BackUserGroup(sender: UIBarButtonItem) {
        self.dismissViewControllerAnimated(true, completion: nil)
    }
    
    // refresh table
    @IBAction func SegmentControl(sender: UISegmentedControl) {
        self.ChatTableView.reloadData()
    }
    
    // search element to swipe or not swipe
    func tableView(tableView: UITableView, canEditRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        let loginData = LFunc.fetchLoginData()
        let expensesData = CGFunc.SearchInSegment(SegmentedChatControl.selectedSegmentIndex, idGroup: loginData[0].current_group)
        let notifyData = CGFunc.FetchNotes(loginData[0].current_group)
        if indexPath.row >= notifyData.count {
            if expensesData[indexPath.row - notifyData.count].paid == 0 {
                return true
            }
            else {
                return false
            }
        }
        else {
            return true
        }
    }
    
    // edit swipe left
    func tableView(tableView: UITableView, editActionsForRowAtIndexPath indexPath: NSIndexPath) -> [UITableViewRowAction]? {
        let Edit = UITableViewRowAction(style: .Default, title: "Edit") { (rowAction:UITableViewRowAction, indexPath:NSIndexPath) -> Void in
            self.EditCell(indexPath)
        }
        Edit.backgroundColor = CFunc.BlackWalli
        
        let Delete = UITableViewRowAction(style: .Default, title: "Delete") { (rowAction:UITableViewRowAction, indexPath:NSIndexPath) -> Void in
            self.RemoveCell(indexPath)
        }
        Delete.backgroundColor = UIColor.redColor()
        
        return [Edit, Delete]
    }
    
    // allow to show swipe left
    func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
        
    }
    
    // show expenses popover
    @IBAction func SendExpanses(sender: UIBarButtonItem) {
        let VController = storyboard?.instantiateViewControllerWithIdentifier("PopoverExpenses") as! PopoverExpensesController
        VController.preferredContentSize = CGSize(width: UIScreen.mainScreen().bounds.width, height: 250)
        VController.modalPresentationStyle = UIModalPresentationStyle.Popover
        VController.RefreshChatExpenses = self
        let popover = VController.popoverPresentationController
        popover?.delegate = self
        popover?.barButtonItem = sender
        self.presentViewController(VController, animated: true, completion: nil)
    }
    
    // show notify popover
    @IBAction func SendNotify(sender: UIBarButtonItem) {
        let VController = storyboard?.instantiateViewControllerWithIdentifier("PopoverNotify") as! PopoverNotifyController
        VController.preferredContentSize = CGSize(width: UIScreen.mainScreen().bounds.width, height: 250)
        VController.modalPresentationStyle = UIModalPresentationStyle.Popover
        VController.RefreshChatNotify = self
        let popover = VController.popoverPresentationController
        popover?.delegate = self
        popover?.barButtonItem = sender
        self.presentViewController(VController, animated: true, completion: nil)
    }
    
    func adaptivePresentationStyleForPresentationController(controller: UIPresentationController) -> UIModalPresentationStyle {
        return .None
    }
    
    // Pay you the notify
    @IBAction func SendReciveNotify(sender: UIButton) {
        var alertController: UIAlertController?
        let realm = try! Realm()
        let notify = realm.objects(NotifyDB)
        // move to the expense popover
        alertController = UIAlertController(title: "Did you buy", message:"\(notify[sender.tag].descr)?", preferredStyle: UIAlertControllerStyle.Alert)
        let action = UIAlertAction(title: "Confirm", style: UIAlertActionStyle.Default, handler: {
            [weak self](paramAction:UIAlertAction!) in
                let VController = self?.storyboard?.instantiateViewControllerWithIdentifier("PopoverExpenses") as! PopoverExpensesController
                VController.preferredContentSize = CGSize(width: UIScreen.mainScreen().bounds.width, height: 250)
                VController.modalPresentationStyle = UIModalPresentationStyle.Popover
                let popover = VController.popoverPresentationController
                VController.RefreshChatExpenses = self
                popover?.delegate = self
                popover?.barButtonItem = self?.navigationItem.rightBarButtonItems![0]
                VController.descriptionReceive = notify[sender.tag].descr
                VController.notifyID = notify[sender.tag].id_note
                self!.presentViewController(VController, animated: true, completion: nil)
            })
        alertController!.addAction(action)
        alertController!.addAction(UIAlertAction(title: "No", style: UIAlertActionStyle.Default,handler: nil))
        self.presentViewController(alertController!, animated: true, completion: nil)
    }
    
    func RemoveCell(indexPath: NSIndexPath) {
        let loginData = LFunc.fetchLoginData()
        let expensesData = CGFunc.SearchInSegment(SegmentedChatControl.selectedSegmentIndex, idGroup: loginData[0].current_group)
        let notifyData = CGFunc.FetchNotes(loginData[0].current_group)
        // remove expense
        if indexPath.row >= notifyData.count {
            if expensesData[indexPath.row - notifyData.count].paid != 1 {
                let expenseDelete = expensesData[indexPath.row - notifyData.count].id_expenses
                let realm = try! Realm()
                let query = realm.objects(ExpensesDB).filter("id_expenses == %@", expenseDelete)
                if query.count == 1 {
                    var alertController: UIAlertController?
                    alertController = UIAlertController(title: "Remove", message:"Are you sure to remove this expense?", preferredStyle: UIAlertControllerStyle.Alert)
                    let action = UIAlertAction(title: "Confirm", style: UIAlertActionStyle.Default, handler: {
                        [weak self](paramAction:UIAlertAction!) in
                        let loginData = self!.LFunc.fetchLoginData()
                        self!.CGFunc.removeExpenses(loginData[0].id, keyuser: loginData[0].key, expenseID: expenseDelete, request: "https://walli.ddns.net:443/deleteShop") { response, error in
                            if error == nil {
                                if response["response"] == "ok" {
                                    do {
                                        try realm.write {
                                            realm.delete(query)
                                            self!.ChatTableView.reloadData()
                                        }
                                    }
                                    catch let error as NSError  {
                                        print("Could not save \(error), \(error.userInfo)")
                                    }
                                }
                                else {
                                    self!.CFunc.errorAlert("Alert", controller: self!, message: "You can't remove a expense if this is already paid from someone!")
                                }
                            }
                            else {
                                self!.CFunc.errorAlert("Connection fail", controller: self!, message: "Check your connection!")
                            }
                        }
                    })
                    alertController!.addAction(action)
                    alertController!.addAction(UIAlertAction(title: "No", style: UIAlertActionStyle.Default,handler: nil))
                    self.presentViewController(alertController!, animated: true, completion: nil)
                }
            }
        }
        // remove notification
        else {
            let notifyDelete = notifyData[indexPath.row].id_note
            let realm = try! Realm()
            let query = realm.objects(NotifyDB).filter("id_note == %@", notifyDelete)
            if query.count == 1 {
                var alertController: UIAlertController?
                alertController = UIAlertController(title: "Remove", message:"Are you sure to remove this notification?", preferredStyle: UIAlertControllerStyle.Alert)
                let action = UIAlertAction(title: "Confirm", style: UIAlertActionStyle.Default, handler: {
                    [weak self](paramAction:UIAlertAction!) in
                    let loginData = self!.LFunc.fetchLoginData()
                    self!.CGFunc.removeNotify(loginData[0].id, keyuser: loginData[0].key, notifyID: notifyDelete, request: "https://walli.ddns.net:443/updateNotify") { response, error in
                        if error == nil {
                            if response["response"] == "ok" {
                                do {
                                    try realm.write {
                                        realm.delete(query)
                                        self!.ChatTableView.reloadData()
                                    }
                                }
                                catch let error as NSError  {
                                    print("Could not save \(error), \(error.userInfo)")
                                }
                            }
                        }
                        else {
                            self!.CFunc.errorAlert("Connection fail", controller: self!, message: "Check your connection!")
                        }
                    }
                })
                alertController!.addAction(action)
                alertController!.addAction(UIAlertAction(title: "No", style: UIAlertActionStyle.Default,handler: nil))
                self.presentViewController(alertController!, animated: true, completion: nil)
            }
        }
    }
    
    // edit cell of file
    func EditCell(indexPath: NSIndexPath) {
        let loginData = LFunc.fetchLoginData()
        let expensesData = CGFunc.SearchInSegment(SegmentedChatControl.selectedSegmentIndex, idGroup: loginData[0].current_group)
        let notifyData = CGFunc.FetchNotes(loginData[0].current_group)
        //edit expense
        if indexPath.row >= notifyData.count {
            if expensesData[indexPath.row - notifyData.count].paid != 1 {
                let expenseDelete = expensesData[indexPath.row - notifyData.count].id_expenses
                let realm = try! Realm()
                let query = realm.objects(ExpensesDB).filter("id_expenses == %@", expenseDelete)
                if query.count == 1 {
                    let VController = storyboard?.instantiateViewControllerWithIdentifier("PopoverExpenses") as! PopoverExpensesController
                    VController.preferredContentSize = CGSize(width: UIScreen.mainScreen().bounds.width, height: 250)
                    VController.modalPresentationStyle = UIModalPresentationStyle.Popover
                    let popover = VController.popoverPresentationController
                    VController.RefreshChatExpenses = self
                    popover?.delegate = self
                    popover?.barButtonItem = self.navigationItem.rightBarButtonItems![0]
                    VController.ModifyState = expenseDelete
                    VController.DescriptionValue = expensesData[indexPath.row - notifyData.count].descr
                    VController.MoneyValue = String(expensesData[indexPath.row - notifyData.count].value)
                    VController.Character = expensesData[indexPath.row - notifyData.count].descr.characters.count
                    self.presentViewController(VController, animated: true, completion: nil)
                }
            }
        }
        // edit notification
        else {
            let notifyDelete = notifyData[indexPath.row].id_note
            let realm = try! Realm()
            let query = realm.objects(NotifyDB).filter("id_note == %@", notifyDelete)
            if query.count == 1 {
                let VController = storyboard?.instantiateViewControllerWithIdentifier("PopoverNotify") as! PopoverNotifyController
                VController.preferredContentSize = CGSize(width: UIScreen.mainScreen().bounds.width, height: 250)
                VController.modalPresentationStyle = UIModalPresentationStyle.Popover
                let popover = VController.popoverPresentationController
                VController.RefreshChatNotify = self
                popover?.delegate = self
                popover?.barButtonItem = self.navigationItem.rightBarButtonItems![0]
                VController.ModifyState = notifyDelete
                VController.DescriptionValue = notifyData[indexPath.row].descr
                VController.Character = notifyData[indexPath.row].descr.characters.count
                self.presentViewController(VController, animated: true, completion: nil)
            }
        }
    }

    // reload list prototype
    func loadList() {
        //load data here
        self.ChatTableView.reloadData()
    }
    
}

