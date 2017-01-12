//
//  MenuController.swift
//  Walli
//
//  Created by Daniele Piergigli on 20/04/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import RealmSwift
import Foundation

class MenuController: UITableViewController, PickerCurrencyChange, UIPopoverPresentationControllerDelegate {
    
    @IBOutlet weak var ImagePofileMenu: UIImageView!
    @IBOutlet weak var NicknameMenu: UILabel!
    @IBOutlet weak var NameProfileMenu: UILabel!
    @IBOutlet weak var NotificationMenu: UILabel!
    @IBOutlet weak var EmailMenu: UILabel!
    @IBOutlet weak var CellPhoneMenu: UILabel!
    @IBOutlet weak var CurrentCurrency: UILabel!
    @IBOutlet var MenuButtonCollection: [UIButton]!
    
    // For MCV
    var CFunc = CommonFunction()
    var CMenu = MenuFunction()
    var LFunc = LoginFunction()
    var IFunc = ImageFunction()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        let loginData = LFunc.fetchLoginData()
        // Set style image
        ImagePofileMenu.layer.borderWidth = 1
        ImagePofileMenu.layer.masksToBounds = false
        ImagePofileMenu.layer.borderColor = CFunc.BlackWalli.CGColor
        ImagePofileMenu.layer.cornerRadius = ImagePofileMenu.frame.height/2
        ImagePofileMenu.clipsToBounds = true
        
        // collection button style
        for button in MenuButtonCollection {
            button.titleEdgeInsets = UIEdgeInsetsMake(0.0, 10.0, 0.0, 0.0)
        }
        
        //Set currency
        CurrentCurrency.text = loginData[0].currency
        // Set notification open and user name
        NotificationMenu.text = CMenu.NumberOfNotify()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // use for the update view when it came back
    override func viewWillAppear(animated: Bool) {
        let loginData = LFunc.fetchLoginData()
        let imageData = IFunc.FetchUserImage(loginData[0].id)
        NicknameMenu.text = "@" + loginData[0].nickname
        NameProfileMenu.text = "\(loginData[0].name) \(loginData[0].surname)"
        EmailMenu.text = loginData[0].mail
        CellPhoneMenu.text = loginData[0].cellPhone
        // Set and resize image for image view
        if !imageData.isEmpty {
            ImagePofileMenu.image = UIImage(data: imageData[0].image)
        }
        else {
            ImagePofileMenu.image = UIImage(named: "StandardImageUser")
        }
    }
    
    // Logout action 
    @IBAction func LogoutBUtton(sender: UIButton) {
        var alertController: UIAlertController?
        alertController = UIAlertController(title: "Logout", message:"Are you sure to log out?", preferredStyle: UIAlertControllerStyle.Alert)
        let action = UIAlertAction(title: "Confirm", style: UIAlertActionStyle.Default, handler: {
                [weak self](paramAction:UIAlertAction!) in
                let loginData = self!.LFunc.fetchLoginData()
                // prepare for request groups
                if !loginData.isEmpty {
                    // get request of group expenses
                    self!.CMenu.Logout(loginData[0].id, keyuser: loginData[0].key, token: loginData[0].token, request: "https://walli.ddns.net:443/logout") { error in
                        if error == nil {
                            // delete local database
                            let realm = try! Realm()
                            let user = realm.objects(LoginDB)
                            let users = realm.objects(UsersDB)
                            let groups = realm.objects(GroupDB)
                            let expenses = realm.objects(ExpensesDB)
                            let notify = realm.objects(NotifyDB)
                            let members = realm.objects(MembersDB)
                            let imageGroups = realm.objects(ImageGroupsDB)
                            let imageUsers = realm.objects(ImageUsersDB)
                            do {
                                try realm.write {
                                    realm.delete(user)
                                    realm.delete(users)
                                    realm.delete(groups)
                                    realm.delete(expenses)
                                    realm.delete(notify)
                                    realm.delete(members)
                                    realm.delete(imageGroups)
                                    realm.delete(imageUsers)
                                }
                                let storyboardLogin: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
                                let vc: LoginController = storyboardLogin.instantiateViewControllerWithIdentifier("LoginApp") as! LoginController
                                self!.presentViewController(vc, animated: true, completion: nil)
                            }
                            catch let error as NSError! {
                                print("Could not delete \(error), \(error.userInfo)")
                            }
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
    
    // start popover to change currency
    @IBAction func ChangeCurrencyButton(sender: UIButton) {
        let VController = storyboard?.instantiateViewControllerWithIdentifier("PopoverCurrency") as! PopoverChangeCurrency
        VController.preferredContentSize = CGSize(width: UIScreen.mainScreen().bounds.width, height: 150)
        VController.modalPresentationStyle = UIModalPresentationStyle.Popover
        VController.PickerDelegate = self
        VController.popoverPresentationController?.permittedArrowDirections = UIPopoverArrowDirection(rawValue:0)
        VController.popoverPresentationController?.delegate = self
        VController.popoverPresentationController?.sourceView = view
        VController.popoverPresentationController?.sourceRect = CGRectMake(0, UIScreen.mainScreen().bounds.height, 0, 0)
        self.presentViewController(VController, animated: true, completion: nil)
    }
    
    func adaptivePresentationStyleForPresentationController(controller: UIPresentationController) -> UIModalPresentationStyle {
        return .None
    }
    
    // protocol function response to change currency
    func ChangeCurrency(currency: String) {
        if currency != "" {
            CurrentCurrency.text = CFunc.transformToStringCurrency(currency)
        }
    }
}
