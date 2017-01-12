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

class GroupsCells: UITableViewCell {
    
    @IBOutlet weak var GroupsTitleCell: UILabel!
    @IBOutlet weak var GroupsUpdateCell: UILabel!
    @IBOutlet weak var GroupsImageCell: UIImageView!
    @IBOutlet weak var GroupsMoneyCell: UILabel!
    @IBOutlet weak var GroupsNotifyCell: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    
    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
}

class GroupsController: UIViewController, UITableViewDataSource, UITableViewDelegate, UIPopoverPresentationControllerDelegate {
    
    //Define tableView
    @IBOutlet weak var GroupsTableView: UITableView!

    // For MCV
    var CFunc = CommonFunction()
    var GFunc = GroupsFunction()
    var LFunc = LoginFunction()
    var IFunc = ImageFunction()
    
    override func viewDidAppear(animated: Bool) {
        self.navigationItem.title = "Update..."
        let loginData = LFunc.fetchLoginData()
        // prepare for request groups if we have connection
        if !loginData.isEmpty {
            GFunc.GetGroups(loginData[0].id, keyuser: loginData[0].key, request: "https://walli.ddns.net:443/getGroupsByUser") { error in
                if error == nil {
                    self.navigationItem.title = "Groups"
                    self.GroupsTableView.reloadData()
                }
                else {
                    self.CFunc.errorAlert("Connection fail", controller: self, message: "Check your connection!")
                }
            }
        }
        else {
            // do before login
            let storyboardLogin: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
            let vc: LoginController = storyboardLogin.instantiateViewControllerWithIdentifier("LoginApp") as! LoginController
            self.presentViewController(vc, animated: true, completion: nil)
        }
    }
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // prepare for show user group
        if segue.identifier == "showUserGroup" {
            let groupData = GFunc.FetchGroup()
            if let indexPath = GroupsTableView.indexPathForSelectedRow {
                let destinationNavigationController = segue.destinationViewController as! UINavigationController
                let VController = destinationNavigationController.topViewController as! UserGroupController
                VController.titleNav = groupData[indexPath.row].name
                VController.currency = groupData[indexPath.row].currency
                let loginData = LFunc.fetchLoginData()
                let realm = try! Realm()
                do {
                    try realm.write  {
                        realm.create(LoginDB.self, value: ["id": loginData[0].id, "current_group": groupData[indexPath.row].id], update: true)
                    }
                }
                catch let error as NSError  {
                    print("Could not save \(error), \(error.userInfo)")
                }
            }
        }
    }

    // Function override UITableViewDataSource and allow to print data on screen
    func tableView(tableView:UITableView, numberOfRowsInSection section:Int) -> Int {
        let groupData = GFunc.FetchGroup()
        return groupData.count
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = self.GroupsTableView.dequeueReusableCellWithIdentifier("cell", forIndexPath: indexPath) as! GroupsCells
        let groupData = GFunc.FetchGroup()
        // Set style image
        cell.GroupsImageCell.layer.borderWidth = 1
        cell.GroupsImageCell.layer.masksToBounds = false
        cell.GroupsImageCell.layer.borderColor = CFunc.BlackWalli.CGColor
        cell.GroupsImageCell.layer.cornerRadius = cell.GroupsImageCell.frame.height/2
        cell.GroupsImageCell.clipsToBounds = true
        
        // Set style notification label
        cell.GroupsNotifyCell.layer.cornerRadius = cell.GroupsNotifyCell.frame.height/2
        cell.GroupsNotifyCell.clipsToBounds = true
        
        // Set style money
        cell.GroupsMoneyCell.textColor = UIColor(red: 1, green: 87/255, blue: 34/255, alpha: 1)
        
        // Set style title
        cell.GroupsTitleCell.font = UIFont.boldSystemFontOfSize(17.0)
        
        // recive image
        self.IFunc.GetImageGroupData(groupData[indexPath.row].id) { result in
            if result {
                let imageData = self.IFunc.FetchGroupImage(groupData[indexPath.row].id)
                cell.GroupsImageCell.image = UIImage(data: imageData[0].image)
            }
            else {
                cell.GroupsImageCell.image = UIImage(named: "StandardImageGroup")
            }
        }
        // Set data in cells and resize image for image view
        if Double(groupData[indexPath.row].money) > 0 {
            cell.GroupsMoneyCell.text = String(format: "+%.2f", Double(groupData[indexPath.row].money)!) + groupData[indexPath.row].currency
        }
        else if Double(groupData[indexPath.row].money) == 0 {
            cell.GroupsMoneyCell.text = "0"
        }
        else {
            cell.GroupsMoneyCell.text = String(format: "%.2f", Double(groupData[indexPath.row].money)!) + groupData[indexPath.row].currency
        }
        cell.GroupsTitleCell.text = groupData[indexPath.row].name
        cell.GroupsUpdateCell.text = groupData[indexPath.row].time
        // show or not show the notify cell if there is no value
        if groupData[indexPath.row].num_notify != 0 {
            cell.GroupsNotifyCell.text = String(groupData[indexPath.row].num_notify)
            cell.GroupsNotifyCell.hidden = false
        }
        else {
            cell.GroupsNotifyCell.hidden = true
        }
        return cell
    }
    
    // Delete the groups
    func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
        if editingStyle == .Delete {
            let groupData = GFunc.FetchGroup()
            let groupDelete = groupData[indexPath.row].id
            let realm = try! Realm()
            let query = realm.objects(GroupDB).filter("id == %@ AND money == %@", groupDelete, "0.0")
            if query.count == 1 {
                // remove the group
                var alertController: UIAlertController?
                alertController = UIAlertController(title: "Remove", message:"Are you sure to remove this group?", preferredStyle: UIAlertControllerStyle.Alert)
                let action = UIAlertAction(title: "Confirm", style: UIAlertActionStyle.Default, handler: {
                    [weak self](paramAction:UIAlertAction!) in
                    let loginData = self!.LFunc.fetchLoginData()
                    self!.GFunc.removeGroups(loginData[0].id, keyuser: loginData[0].key, groupID: groupDelete, request: "https://walli.ddns.net:443/deleteGroup") { error in
                        if error == nil {
                            let imageToDelete = realm.objects(ImageGroupsDB).filter("id == %@", groupDelete)
                            do {
                                try realm.write {
                                    realm.delete(query)
                                    realm.delete(imageToDelete)
                                    self!.GroupsTableView.reloadData()
                                }
                            }
                            catch let error as NSError  {
                                print("Could not save \(error), \(error.userInfo)")
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
            else {
                self.CFunc.errorAlert("Error", controller: self, message: "You can't remove a group if thare is still something to be paid or to be recived!")
            }
        }
    }
    
    func adaptivePresentationStyleForPresentationController(controller: UIPresentationController) -> UIModalPresentationStyle {
        // Return no adaptive presentation style, use default presentation behaviour
        return .None
    }
}

