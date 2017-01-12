//
//  UserGroupController.swift
//  Walli
//
//  Created by Daniele Piergigli on 26/06/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import Foundation

class UserCells: UICollectionViewCell {
    
    @IBOutlet weak var UserMoneyCell: UILabel!
    @IBOutlet weak var UserImageCell: UIImageView!
    @IBOutlet weak var UserTypeButtonCell: UIButton!
    @IBOutlet weak var UserNameCell: UILabel!
    @IBOutlet weak var ViewContainImageCell: UIView!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    
}

class UserGroupController: UICollectionViewController {
    
    
    @IBOutlet var UserCollectionView: UICollectionView!
    
    // Define variable for row
    var currency = String()
    var titleNav = String()
    var userinthisGroup = [String]()
    
    // For MVC
    var CFunc = CommonFunction()
    var LFunc = LoginFunction()
    var UGFunc = UserGroupFunction()
    var IFunc = ImageFunction()
    var PFunc = PayExpensesFunction()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.UserCollectionView.reloadData()
    }
    
    override func viewWillAppear(animated: Bool) {
        sendRequest()
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // prepare to show expenses chat
        if segue.identifier == "showChatGroup" {
            let destinationNavigationController = segue.destinationViewController as! UINavigationController
            let VController = destinationNavigationController.topViewController as! GroupsChatController
            VController.titelNav = titleNav
            VController.currency = currency
        }
        // prepare to show add user view
        else if segue.identifier == "ShowAddUser" {
            let loginData = LFunc.fetchLoginData()
            let destinationNavigationController = segue.destinationViewController as! UINavigationController
            let VController = destinationNavigationController.topViewController as! AddUserController
            VController.titleGroup = self.titleNav
            VController.currency = currency
            VController.currentGroup = loginData[0].current_group
            VController.userIDGroup = UGFunc.UserInThisGroup(loginData[0].current_group)
        }
    }
    
    // collection view number of cell
    override func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        let loginData = LFunc.fetchLoginData()
        let usersData = UGFunc.FetchUsers(loginData[0].current_group)
        return usersData.count
    }
    
    // resoze collection view to how two user in each row
    func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAtIndexPath indexPath: NSIndexPath) -> CGSize {
        // flow layout have all the important info like spacing, inset of collection view cell, fetch it to find out the attributes specified in xib file
        guard let flowLayout = collectionViewLayout as? UICollectionViewFlowLayout else {
            return CGSize()
        }
        // subtract section left/ right insets mentioned in xib view
        let widthAvailbleForAllItems =  (collectionView.frame.width - flowLayout.sectionInset.left - flowLayout.sectionInset.right)
        // Suppose we have to create nColunmns
        // widthForOneItem achieved by sunbtracting item spacing if any
        let widthForOneItem = widthAvailbleForAllItems / 2 - flowLayout.minimumInteritemSpacing
        // here height is mentioned in xib file or storyboard
        return CGSize(width: CGFloat(widthForOneItem), height: 280)
    }
    
    // show data
    override func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        let cell = self.UserCollectionView.dequeueReusableCellWithReuseIdentifier("CollectionCells", forIndexPath: indexPath) as! UserCells
        let loginData = LFunc.fetchLoginData()
        let usersData = UGFunc.FetchUsers(loginData[0].current_group)
        // Set style image
        cell.UserImageCell.layer.borderWidth = 1
        cell.UserImageCell.layer.masksToBounds = false
        cell.UserImageCell.layer.borderColor = CFunc.BlackWalli.CGColor
        cell.UserImageCell.layer.cornerRadius = cell.UserImageCell.frame.height/2
        cell.UserImageCell.clipsToBounds = true
        // set image
        if CFunc.isConnectedToNetwork() {
            self.IFunc.GetImageForUserId(usersData[indexPath.row].id_user) { result in
                if result {
                    let imageData = self.IFunc.FetchUserImage(usersData[indexPath.row].id_user)
                    cell.UserImageCell.image = UIImage(data: imageData[0].image)
                }
                else {
                    cell.UserImageCell.image = UIImage(named: "StandardImageUser")
                }
            }
        }
        else {
            self.CFunc.errorAlert("Connection fail", controller: self, message: "Check your connection!")
            cell.UserImageCell.image = UIImage(named: "StandardImageGroup")
        }
        // money check
        // if you are a credit
        if Double(usersData[indexPath.row].heritage) > 0 {
            cell.UserMoneyCell.text = String(format: "+%.2f", Double(usersData[indexPath.row].heritage)!) + currency
            // used button to pay
            cell.UserTypeButtonCell.setImage(UIImage(named: "SendAlert"), forState: .Normal)
            cell.UserTypeButtonCell.setTitle("Send a warning", forState: .Normal)
        }
        // if you are ok
        else if Double(usersData[indexPath.row].heritage) == 0 {
            cell.UserMoneyCell.text = "0"
            cell.UserTypeButtonCell.setImage(UIImage(named: "OkIcon"), forState: .Normal)
            cell.UserTypeButtonCell.setTitle("", forState: .Normal)
        }
        // if you are a debt
        else {
            cell.UserMoneyCell.text = String(format: "%.2f", Double(usersData[indexPath.row].heritage)!) + currency
            cell.UserTypeButtonCell.setImage(nil, forState: .Normal)
            cell.UserTypeButtonCell.setTitle("Pay", forState: .Normal)
        }
        cell.UserTypeButtonCell.tag = indexPath.row
        cell.UserNameCell.font = UIFont.boldSystemFontOfSize(16.0)
        cell.UserNameCell.text = "@" + usersData[indexPath.row].nickname
        return cell
    }
    
    @IBAction func BackGroups(sender: UIBarButtonItem) {
        self.dismissViewControllerAnimated(true, completion: nil)
    }
    
    @IBAction func SendPayment(sender: UIButton) {
        if sender.currentTitle == "Pay" {
            var alertController: UIAlertController?
            alertController = UIAlertController(title: "Pay", message:"Are you sure you want to pay this expense?", preferredStyle: UIAlertControllerStyle.Alert)
            let action = UIAlertAction(title: "Yes", style: UIAlertActionStyle.Default, handler: {
                [weak self](paramAction:UIAlertAction!) in
                let loginData = self!.LFunc.fetchLoginData()
                let usersData = self!.UGFunc.FetchUsers(loginData[0].current_group)
                // prepare for request groups
                if !loginData.isEmpty {
                    // get request of group expenses
                    self!.UGFunc.SendPaid(loginData[0].id, keyuser: loginData[0].key, userid: usersData[sender.tag].id_user, idGroup: loginData[0].current_group, request: "https://walli.ddns.net:443/setAsPaid") { response, error in
                        if response["response"] == "ok" {
                            self!.sendRequest()
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
                // prepare for request groups
                if !loginData.isEmpty {
                    let usersData = self!.UGFunc.FetchUsers(loginData[0].current_group)
                    // get request of group expenses
                    self!.PFunc.SendWarning(loginData[0].id, keyuser: loginData[0].key, userid: usersData[sender.tag].id_user, request: "https://walli.ddns.net:443/askForPayment") { response, error in
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
    
    // recive user from server of this group
    func sendRequest () {
        self.navigationItem.title = "Update..."
        let loginData = LFunc.fetchLoginData()
        // prepare for request groups
        if !loginData.isEmpty {
            // get request of group expenses
            UGFunc.GetUserGroups(loginData[0].id, keyuser: loginData[0].key, groupID: loginData[0].current_group, request: "https://walli.ddns.net:443/getCreditDebitByGroup") { error in
                if error == nil {
                    self.navigationItem.title = self.titleNav
                    self.UserCollectionView.reloadData()
                }
                else {
                    self.CFunc.errorAlert("Connection fail", controller: self, message: "Check your connection!")
                }
            }
        }
    }
}
