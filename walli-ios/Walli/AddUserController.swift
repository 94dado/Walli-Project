//
//  AddUserController.swift
//  Walli
//
//  Created by Daniele Piergigli on 01/07/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import RealmSwift
import Foundation

extension Array where Element : Equatable {
    
    // Remove first collection element that is equal to the given `object`:
    mutating func removeObject(object : Generator.Element) {
        if let index = self.indexOf(object) {
            self.removeAtIndex(index)
        }
    }
}

class AddUserCells: UITableViewCell {
    
    @IBOutlet weak var UserNicknameCell: UILabel!
    @IBOutlet weak var UserImageCell: UIImageView!
    @IBOutlet weak var UserNameCell: UILabel!
    @IBOutlet weak var SelectableButtonCell: UIButton!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    
}

class AddUserController: UIViewController, UITableViewDelegate, UITableViewDataSource, UIPickerViewDelegate, UIPickerViewDataSource, UISearchBarDelegate, UITextFieldDelegate, UIPopoverPresentationControllerDelegate, PopoverImageDelegate {
    
    @IBOutlet weak var AddUserTable: UITableView!
    @IBOutlet weak var SearchAddUsers: UISearchBar!
    @IBOutlet weak var GroupAddUserImage: UIImageView!
    @IBOutlet weak var GroupAddUserName: UITextField!
    @IBOutlet weak var AddImageButton: UIButton!
    @IBOutlet weak var CurrencyAddGroup: UITextField!
    
    // Define variable for row
    var currencyData = ["€", "$", "£"]
    var picker = UIPickerView()
    var titleGroup = String()
    var currency = String()
    // Used in segue to know if we are in add user or group
    var currentGroup = String()
    // users already in group
    var userIDGroup = [String]()
    // filter members by hints
    var GetHintsString = String()
    // Save element to delete
    var RemoveIdAtBack = [String]()
    // Default Image
    var DefaultImageData = UIImagePNGRepresentation(UIImage(named: "StandardImageGroup")!)
    
    // For MVC
    var CFunc = CommonFunction()
    var LFunc = LoginFunction()
    var AFunc = AddUserFunction()
    var IFunc = ImageFunction()
    
    override func viewWillAppear(animated: Bool) {
        let loginData = LFunc.fetchLoginData()
        // request all menber for the table view
        self.AFunc.GetUserAllGroups(loginData[0].id, keyuser: loginData[0].key, currency: loginData[0].currency, request: "https://walli.ddns.net:443/getCreditDebit") { result, error in
            if error != nil {
                self.CFunc.errorAlert("Connection fail", controller: self, message: "Check your connection!")
            }
            else {
                self.AFunc.UpdateCell(self.userIDGroup, currentGroup: self.currentGroup, members: self.AFunc.FetchMembers(self.GetHintsString))
                self.AddUserTable.reloadData()
            }
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Image style
        GroupAddUserImage.layer.borderWidth = 1
        GroupAddUserImage.layer.masksToBounds = false
        GroupAddUserImage.layer.borderColor = CFunc.BlackWalli.CGColor
        GroupAddUserImage.layer.cornerRadius = GroupAddUserImage.frame.height/2
        GroupAddUserImage.clipsToBounds = true
        self.navigationItem.title = "Add"
        
        // the keyboard can disappeared when tap on the screen
        self.hideKeyboardWhenTappedAround()
        
        // set picker view in currency textfield
        picker.delegate = self
        picker.dataSource = self
        CurrencyAddGroup.inputView = picker
        // set data from user segue
        let loginData = LFunc.fetchLoginData()
        if currentGroup != "" {
            // if you came from user view
            GroupAddUserName.text = titleGroup
            CurrencyAddGroup.text = currency
            let imageData = IFunc.FetchGroupImage(loginData[0].current_group)
            // if not standard
            if !imageData.isEmpty {
                GroupAddUserImage.image = UIImage(data: imageData[0].image)
            }
            else {
                GroupAddUserImage.image = UIImage(named: "StandardImageGroup")
            }
        }
        else {
            // if you came from grups view
            CurrencyAddGroup.text = CFunc.getCurrency(loginData[0].currency)
            GroupAddUserImage.image = UIImage(named: "StandardImageGroup")
        }
        GroupAddUserName.delegate? = self
    }
    
    // Function override UITableViewDataSource and allow to print data on screen
    func tableView(tableView:UITableView, numberOfRowsInSection section:Int) -> Int {
        let membersData = AFunc.FetchMembers(self.GetHintsString)
        return membersData.count
    }
    
    // insert data in table view
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = self.AddUserTable.dequeueReusableCellWithIdentifier("AddUserCells", forIndexPath: indexPath) as! AddUserCells
        let membersData = AFunc.FetchMembers(self.GetHintsString)
        // Image style
        cell.UserImageCell.layer.borderWidth = 1
        cell.UserImageCell.layer.masksToBounds = false
        cell.UserImageCell.layer.borderColor = CFunc.BlackWalli.CGColor
        cell.UserImageCell.layer.cornerRadius = cell.UserImageCell.frame.height/2
        cell.UserImageCell.clipsToBounds = true
        // set image
        if CFunc.isConnectedToNetwork() {
            self.IFunc.GetImageForUserId(membersData[indexPath.row].id_user) { result in
                if result {
                    let imageData = self.IFunc.FetchUserImage(membersData[indexPath.row].id_user)
                    cell.UserImageCell.image = UIImage(data: imageData[0].image)
                }
                else {
                    cell.UserImageCell.image = UIImage(named: "StandardImageUser")
                }
            }
        }
        else {
            self.CFunc.errorAlert("Connection fail", controller: self, message: "Check your connection!")
        }
        cell.UserNameCell.text = membersData[indexPath.row].name + " " + membersData[indexPath.row].surname
        cell.UserNicknameCell.text = "@" + membersData[indexPath.row].nickname
        // the button is selected
        if membersData[indexPath.row].checked {
            cell.SelectableButtonCell.setBackgroundImage(UIImage(named: "OkIcon")!, forState: .Normal)
        }
        else {
            cell.SelectableButtonCell.setBackgroundImage(UIImage(named: "buttonDeselect")!, forState: .Normal)
        }
        cell.SelectableButtonCell.tag = indexPath.row
        return cell
    }
    
    // update selected button state form selected to unselected
    @IBAction func SelectUserButton(sender: UIButton) {
        let membersDB = AFunc.FetchMembers(self.GetHintsString)
        if sender.currentBackgroundImage == UIImage(named: "OkIcon") {
            if userIDGroup.contains(membersDB[sender.tag].id_user) {
                userIDGroup.removeObject(membersDB[sender.tag].id_user)
                do {
                    let realm = try! Realm()
                    try realm.write {
                        let queryUser = realm.objects(UsersDB).filter("key == %@ AND heritage == %@", "\(membersDB[sender.tag].id_user)-\(currentGroup)", "0.0")
                        // if is not in the group add, check the heritage
                        if queryUser.count == 0 {
                            sender.setBackgroundImage(UIImage(named: "buttonDeselect")!, forState: .Normal)
                        }
                        else {
                            let queryAll = realm.objects(UsersDB).filter("key == %@ AND heritage == %@", "\(membersDB[sender.tag].id_user)-\(currentGroup)", "0.0")
                            if queryAll.count == 0 {
                                self.CFunc.errorAlert("Error", controller: self, message: "You can't remove a member if you didn't recive or pay all the expenses!")
                            }
                            else {
                                sender.setBackgroundImage(UIImage(named: "buttonDeselect")!, forState: .Normal)
                            }
                        }
                    }
                }
                catch let error as NSError  {
                    print("Could not save \(error), \(error.userInfo)")
                }
            }
        }
        // is not in the group so put in
        else {
            sender.setBackgroundImage(UIImage(named: "OkIcon")!, forState: .Normal)
            if !userIDGroup.contains(membersDB[sender.tag].id_user) {
                    userIDGroup.append(membersDB[sender.tag].id_user)
            }
        }
    }
    
    // open popover for image to choose
    @IBAction func ChangeImageAddUsers(sender: UIButton) {
        let VController = storyboard?.instantiateViewControllerWithIdentifier("PopoverImage") as! PopoverImageController
        VController.preferredContentSize = CGSize(width: UIScreen.mainScreen().bounds.width, height: 150)
        VController.modalPresentationStyle = UIModalPresentationStyle.Popover
        VController.delegateImage = self
        VController.popoverPresentationController?.delegate = self
        VController.popoverPresentationController?.permittedArrowDirections = UIPopoverArrowDirection(rawValue:0)
        VController.popoverPresentationController?.sourceView = view
        VController.popoverPresentationController?.sourceRect = CGRectMake(0, UIScreen.mainScreen().bounds.size.height, 0, 0)
        self.presentViewController(VController, animated: true, completion: nil)
    }
    
    func adaptivePresentationStyleForPresentationController(controller: UIPresentationController) -> UIModalPresentationStyle {
        return UIModalPresentationStyle.None
    }
    
    // This four function are used for the currency
    // num of picker show simultaniously
    func numberOfComponentsInPickerView(pickerView: UIPickerView) -> Int {
        return 1
    }
    
    // num of picker
    func pickerView(pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return currencyData.count
    }
    
    // selected picker saved
    func pickerView(pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        CurrencyAddGroup.text = currencyData[row]
    }
    
    // array for insert element in row
    func pickerView(pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        return currencyData[row]
    }
    
    // search bar function to hints
    func searchBarSearchButtonClicked(searchBar: UISearchBar) {
        if (SearchAddUsers.text!.isEmpty) {
            doSearch(searchBar.text!)
        }
        doSearch(searchBar.text!)
    }
    
    // if searchbar is empty show all members
    func searchBarCancelButtonClicked(searchBar: UISearchBar) {
        SearchAddUsers.resignFirstResponder()
        SearchAddUsers.text = ""
        GetHintsString = ""
        doSearch(searchBar.text!)
    }
    
    // call nodejs to request hints and save the charachters in hints string for filter
    func doSearch (searchWords: String) {
        GetHintsString = searchWords
        SearchAddUsers.resignFirstResponder()
        let loginData = LFunc.fetchLoginData()
        dispatch_async(dispatch_get_main_queue()) {
            self.AFunc.getHintSearchbar(loginData[0].id, key: loginData[0].key, name: searchWords, request: "https://walli.ddns.net:443/getHints") { array, result, error in
                if !result.isEmpty {
                    // add array of user and update the cell
                    self.RemoveIdAtBack += array
                    self.AFunc.UpdateCell(self.userIDGroup, currentGroup: self.currentGroup, members: self.AFunc.FetchMembers(self.GetHintsString))
                    self.AddUserTable.reloadData()
                    self.SearchAddUsers.resignFirstResponder()
                }
                else {
                    self.CFunc.errorAlert("Alert", controller: self, message: "No one found with this nickname!")
                }
            }
        }
    }
    
    // update or create the group
    @IBAction func UpdateButton(sender: UIBarButtonItem) {
        if GroupAddUserName.text! != "" && CurrencyAddGroup.text! != "" && !userIDGroup.isEmpty {
            let loginData = LFunc.fetchLoginData()
            AFunc.SendUpdateCreateGroupUsers(loginData[0].id, keyuser: loginData[0].key, g_id: currentGroup, g_name: GroupAddUserName.text!, g_currency: CurrencyAddGroup.text!, arrayUsers: userIDGroup, request: "https://walli.ddns.net:443/updateGroup") { result, error in
                if !result.isEmpty {
                    // update image if this is not the default
                    if self.DefaultImageData != UIImagePNGRepresentation(self.GroupAddUserImage.image!) {
                        let imageResize = self.CFunc.RBSquareImageTo(self.GroupAddUserImage.image!, size: CGSize(width: 512, height: 512))
                        let imageNSData = UIImagePNGRepresentation(imageResize)!
                        self.IFunc.SaveImage(loginData[0].id, keyuser: loginData[0].key, type: "group", imageData: imageNSData, userIDOrGroupId: loginData[0].id, request: "https://walli.ddns.net:443/saveImage") { response, error in
                            if error == nil {
                                // if came from user view update image
                                if result["g_id"].string! == self.currentGroup {
                                    do {
                                        let realm = try! Realm()
                                        try realm.write  {
                                            realm.create(ImageGroupsDB.self, value: ["id": result["g_id"].string! , "image": imageNSData, "timestamp": String(response["response"].int!)], update: true)
                                        }
                                    }
                                    catch let errore as NSError  {
                                        print("Could not save \(errore), \(errore.userInfo)")
                                    }
                                }
                                // else create a new image
                                else {
                                    let imageDB = ImageGroupsDB()
                                    imageDB.id = result["g_id"].string!
                                    imageDB.image = imageNSData
                                    imageDB.timestamp = String(response["response"].int!)
                                    do {
                                        let realm = try! Realm()
                                        try realm.write  {
                                            realm.add(imageDB, update: true)
                                        }
                                    }
                                    catch let errore as NSError  {
                                        print("Could not save \(errore), \(errore.userInfo)")
                                    }
                                }
                            }
                            else {
                                self.CFunc.errorAlert("Error!", controller: self, message: "Check your connection!")
                            }
                        }
                    }
                    // delete member from realm there are not in group
                    if result["g_id"].string! == self.currentGroup {
                        for members in self.AFunc.FetchMembers("") {
                            if !self.userIDGroup.contains(members.id_user) {
                                let realm = try! Realm()
                                do {
                                    try realm.write {
                                        let query = realm.objects(UsersDB).filter("key == %@ AND heritage == %@", "\(members.id_user)-\(self.currentGroup)", "0.0")
                                        if query.count == 1 {
                                            realm.delete(query)
                                        }
                                    }
                                }
                                catch let error as NSError  {
                                    print("Could not save \(error), \(error.userInfo)")
                                }
                            }
                        }
                        // remove user
                        self.AFunc.RemoveMembersNotIncluded(self.RemoveIdAtBack, action: "update")
                        // exit to this view controller and download image for new user
                        self.CFunc.errorAlert("Ok", controller: self, message: "Group updated!")
                    }
                    else {
                        self.CFunc.errorAlert("Ok", controller: self, message: "Group created!")
                    }
                }
                else {
                    self.CFunc.errorAlert("Error", controller: self, message: "Check your connection!")
                }
            }
        }
        else {
            self.CFunc.errorAlert("Error", controller: self, message: "Group's name and currency must be filled and somone must remain in the group")
        }
    }
    
    // came back to previous controller
    @IBAction func BackButton(sender: UIBarButtonItem) {
        AFunc.RemoveMembersNotIncluded(RemoveIdAtBack, action: "back")
        self.dismissViewControllerAnimated(true, completion: nil)
    }
    
    // set max value of character for textfield
    func textField(textField: UITextField, shouldChangeCharactersInRange range: NSRange, replacementString string: String) -> Bool {
        guard let text = textField.text else {
            return true
        }
        let newLength = text.characters.count + string.characters.count - range.length
        return newLength <= 45
    }
    
    //reload image profile from popover
    func PopoverImageResponse(image: UIImage){
        GroupAddUserImage.image = image
    }
}

