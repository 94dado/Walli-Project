//
//  PayExpensesController.swift
//  Walli
//
//  Created by Daniele Piergigli on 04/07/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import RealmSwift
import Foundation

class PayCells: UITableViewCell {
    
    @IBOutlet weak var PayImageCell: UIImageView!
    @IBOutlet weak var PayNameCell: UILabel!
    @IBOutlet weak var PayMoneyCell: UILabel!
    @IBOutlet weak var PayButtonPayCell: UIButton!
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    
    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
}

class PayExpensesController: UIViewController, UITableViewDataSource, UITableViewDelegate {
    
    @IBOutlet weak var PayTableView: UITableView!
    
    // For MCV
    var CFunc = CommonFunction()
    var GFunc = GroupsFunction()
    var LFunc = LoginFunction()
    var PFunc = PayExpensesFunction()
    var IFunc = ImageFunction()
    
    override func viewWillAppear(animated: Bool) {
        self.navigationItem.title = "Update..."
        let loginData = LFunc.fetchLoginData()
        // prepare for request memebers
        if !loginData.isEmpty {
            PFunc.GetPayments(loginData[0].id, keyuser: loginData[0].key, currency: loginData[0].currency, request: "https://walli.ddns.net:443/getCreditDebit") { response, error in
                if !response.isEmpty {
                    self.navigationItem.title = "Pay Expenses"
                    self.PayTableView.reloadData()
                }
                else {
                    self.CFunc.errorAlert("Connection fail", controller: self, message: "Check your connection!")
                }
            }
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        // custom height
        PayTableView.estimatedRowHeight = 70
    }
    
    func adaptivePresentationStyleForPresentationController(controller: UIPresentationController) -> UIModalPresentationStyle {
        // Return no adaptive presentation style, use default presentation behaviour
        return .None
    }
    
    // Function override UITableViewDataSource and allow to print data on screen
    func tableView(tableView:UITableView, numberOfRowsInSection section:Int) -> Int {
        let membersData = PFunc.FetchMembers()
        return membersData.count
    }
    
    // fill the cell
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = self.PayTableView.dequeueReusableCellWithIdentifier("payCells", forIndexPath: indexPath) as! PayCells
        let membersData = PFunc.FetchMembers()
        let loginData = LFunc.fetchLoginData()
        // Set style image
        cell.PayImageCell.layer.borderWidth = 1
        cell.PayImageCell.layer.masksToBounds = false
        cell.PayImageCell.layer.borderColor = CFunc.BlackWalli.CGColor
        cell.PayImageCell.layer.cornerRadius = cell.PayImageCell.frame.height/2
        cell.PayImageCell.clipsToBounds = true
        // get image from server
        if CFunc.isConnectedToNetwork() {
            self.IFunc.GetImageForUserId(membersData[indexPath.row].id_user) { result in
                if result {
                    let imageData = self.IFunc.FetchUserImage(membersData[indexPath.row].id_user)
                    cell.PayImageCell.image = UIImage(data: imageData[0].image)
                }
                else {
                    cell.PayImageCell.image = UIImage(named: "StandardImageUser")
                }
            }
        }
        else {
            self.CFunc.errorAlert("Connection fail", controller: self, message: "Check your connection!")
            cell.PayImageCell.image = UIImage(named: "StandardImageGroup")
        }
        // if you are in credit
        if Double(membersData[indexPath.row].total_money) > 0.0 {
            cell.PayMoneyCell.text = String(format: "+%.2f", Double(membersData[indexPath.row].total_money)!) + CFunc.getCurrency(loginData[0].currency)
            cell.PayButtonPayCell.setTitle("", forState: .Normal)
            cell.PayButtonPayCell.setImage(UIImage(named: "SendAlert"), forState: .Normal)
        }
        // if you are ok
        else if Double(membersData[indexPath.row].total_money) == 0.0 {
            cell.PayMoneyCell.text = "0"
            cell.PayButtonPayCell.setImage(UIImage(named: "OkIcon"), forState: .Normal)
            cell.PayButtonPayCell.setTitle("", forState: .Normal)
        }
            // if you are in debt
        else {
            cell.PayMoneyCell.text = String(format: "%.2f", Double(membersData[indexPath.row].total_money)!) + CFunc.getCurrency(loginData[0].currency)
            cell.PayButtonPayCell.setTitle("Pay", forState: .Normal)
            cell.PayButtonPayCell.setImage(nil, forState: .Normal)
        }
        cell.PayNameCell.text = membersData[indexPath.row].name + " " + membersData[indexPath.row].surname
        cell.PayButtonPayCell.tag = indexPath.row
        
        return cell
    }
    
    // Pay all the expenses of the user
    @IBAction func PayAllExpensesUser(sender: UIButton) {
        // if you had to pay
        if sender.currentTitle == "Pay" {
            var alertController: UIAlertController?
            alertController = UIAlertController(title: "Pay", message:"Are you sure you want to pay this expense?", preferredStyle: UIAlertControllerStyle.Alert)
            let action = UIAlertAction(title: "Yes", style: UIAlertActionStyle.Default, handler: {
                [weak self](paramAction:UIAlertAction!) in
                let loginData = self!.LFunc.fetchLoginData()
                // prepare for request members
                if !loginData.isEmpty {
                    let membersData = self!.PFunc.FetchMembers()
                    // send payment of expense
                    self!.PFunc.SendPaid(loginData[0].id, keyuser: loginData[0].key, userid: membersData[sender.tag].id_user, request: "https://walli.ddns.net:443/setAsPaid") { response, error in
                        if response["response"] == "ok" {
                            // update member credito to paid all
                            let realm = try! Realm()
                            do {
                                try realm.write  {
                                    realm.create(MembersDB.self, value: ["id_user": membersData[sender.tag].id_user, "total_money": "0.00"], update: true )
                                }
                            }
                            catch let error as NSError  {
                                print("Could not save \(error), \(error.userInfo)")
                            }
                            self!.PayTableView.reloadData()
                        }
                        else {
                            self!.CFunc.errorAlert("Connection fail", controller: self!, message: "Check your connection!")
                        }
                    }
                }
                })
            alertController!.addAction(action)
            alertController!.addAction(UIAlertAction(title: "No", style: UIAlertActionStyle.Default,handler: nil))
            self.presentViewController(alertController!, animated: true, completion: nil)
        }
        // send a warning to a user
        if sender.currentImage == UIImage(named: "SendAlert") {
            var alertController: UIAlertController?
            alertController = UIAlertController(title: "Send warning", message:"Are you sure you want to send a warning?", preferredStyle: UIAlertControllerStyle.Alert)
            let action = UIAlertAction(title: "Yes", style: UIAlertActionStyle.Default, handler: {
                [weak self](paramAction:UIAlertAction!) in
                let loginData = self!.LFunc.fetchLoginData()
                // prepare for request members
                if !loginData.isEmpty {
                    let membersData = self!.PFunc.FetchMembers()
                    // send a warning
                    self!.PFunc.SendWarning(loginData[0].id, keyuser: loginData[0].key, userid: membersData[sender.tag].id_user, request: "https://walli.ddns.net:443/askForPayment") { response, error in
                        if response["response"] == "sent" {
                            self!.CFunc.errorAlert("Sent!", controller: self!, message: "Warning sent!")
                        }
                        else {
                            self!.CFunc.errorAlert("Error!", controller: self!, message: "You send a warning in the next 24 hours, wait to send another")
                        }
                    }
                }
                })
            alertController!.addAction(action)
            alertController!.addAction(UIAlertAction(title: "No", style: UIAlertActionStyle.Default,handler: nil))
            self.presentViewController(alertController!, animated: true, completion: nil)
        }
    }
}

