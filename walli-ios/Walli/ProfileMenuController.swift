//
//  ProfileMenuController.swift
//  Walli
//
//  Created by Daniele Piergigli on 30/06/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import RealmSwift
import Foundation

class ProfileMenuController: UIViewController, UIPopoverPresentationControllerDelegate, PopoverImageDelegate {
    
    @IBOutlet weak var ProfileNameMenu: UITextField!
    @IBOutlet weak var ProfileSurnameMenu: UITextField!
    @IBOutlet weak var ProfilePasswordMenu: UITextField!
    @IBOutlet weak var ProfileEmailMenu: UITextField!
    @IBOutlet weak var ProfileImageMenu: UIImageView!
    @IBOutlet weak var ProfileCellPhoneMenu: UITextField!
    
    // For MVC
    var CFunc = CommonFunction()
    var LFunc = LoginFunction()
    var UFunc = UpdateProfileFunction()
    var IFunc = ImageFunction()
    
    // contain old password if necessary
    var oldPassword = String()
    var currentImageBase64 = String()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationItem.title = "Update Profile"
        let loginData = LFunc.fetchLoginData()
        let imageData = IFunc.FetchUserImage(loginData[0].id)
        let strBase64 = imageData[0].image.base64EncodedStringWithOptions(.Encoding64CharacterLineLength)
        currentImageBase64 = strBase64
        
        // the keyboard can disappeared when tap on the screen
        self.hideKeyboardWhenTappedAround()
        
        // set user data
        ProfileNameMenu.text = loginData[0].name
        ProfileSurnameMenu.text = loginData[0].surname
        ProfileEmailMenu.text = loginData[0].mail
        ProfileCellPhoneMenu.text = loginData[0].cellPhone
        ProfilePasswordMenu.text = loginData[0].password
        oldPassword = loginData[0].password
        
        // set image style
        ProfileImageMenu.layer.borderWidth = 1
        ProfileImageMenu.layer.masksToBounds = false
        ProfileImageMenu.layer.borderColor = CFunc.BlackWalli.CGColor
        ProfileImageMenu.layer.cornerRadius = ProfileImageMenu.frame.height/2
        ProfileImageMenu.clipsToBounds = true
        ProfileImageMenu.image = UIImage(data: imageData[0].image)
    }
    
    // present image popover menage
    @IBAction func PopoverImagePresentation(sender: UIButton) {
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
    
    @IBAction func UpdateUserProfile(sender: UIButton) {
        let loginData = LFunc.fetchLoginData()
        let imageData = IFunc.FetchUserImage(loginData[0].id)
        let strBase64:String = imageData[0].image.base64EncodedStringWithOptions(.Encoding64CharacterLineLength)
        if ProfileNameMenu.text == loginData[0].name && ProfileSurnameMenu.text == loginData[0].surname && ProfileEmailMenu.text == loginData[0].mail && ProfileCellPhoneMenu.text! == loginData[0].cellPhone && ProfilePasswordMenu.text! == loginData[0].password && strBase64 == currentImageBase64 {
            // not update
            self.CFunc.errorAlert("Alert", controller: self, message: "No changes!")
        }
        else {
            var alertController: UIAlertController?
            if oldPassword == ProfilePasswordMenu.text! {
                alertController = UIAlertController(title: "Forgot Password", message:"Insert your password:", preferredStyle: UIAlertControllerStyle.Alert)
            }
            else {
                alertController = UIAlertController(title: "Forgot Password", message:"Insert your old password:", preferredStyle: UIAlertControllerStyle.Alert)
            }
            alertController!.addTextFieldWithConfigurationHandler({
            (textField: UITextField!) in
                textField.placeholder = "Password"
                textField.secureTextEntry = true
            })
            let action = UIAlertAction(title: "Update", style: UIAlertActionStyle.Default, handler: {
                [weak self](paramAction:UIAlertAction!) in
                if let textFields = alertController!.textFields{
                    let theTextFields = textFields as [UITextField]
                    let enteredText = theTextFields[0].text
                    // insert old password for security
                    if self!.oldPassword == enteredText && self!.CFunc.validateEmail(self!.ProfileEmailMenu.text!){
                        self!.navigationItem.title = "Update..."
                        self!.UFunc.sendUpdateUserProfile(loginData[0].id, key: loginData[0].key, mail: self!.ProfileEmailMenu.text!, name: self!.ProfileNameMenu.text!, surname: self!.ProfileSurnameMenu.text!, cell: self!.ProfileCellPhoneMenu.text!, password: self!.ProfilePasswordMenu.text!, oldPassword: self!.oldPassword, request: "https://walli.ddns.net:443/updateProfileData") { error in
                            if error {
                                let realm = try! Realm()
                                let login = LoginDB()
                                login.id = loginData[0].id
                                login.mail = self!.ProfileEmailMenu.text!
                                login.name = self!.ProfileNameMenu.text!
                                login.surname = self!.ProfileSurnameMenu.text!
                                login.key = loginData[0].key
                                login.nickname = loginData[0].nickname
                                login.current_group = loginData[0].current_group
                                login.cellPhone = self!.ProfileCellPhoneMenu.text!
                                login.password = self!.ProfilePasswordMenu.text!
                                do {
                                    try realm.write  {
                                        realm.add(login, update: true)
                                    }
                                }
                                catch let error as NSError  {
                                    print("Could not save \(error), \(error.userInfo)")
                                }
                                // update image
                                let imageResize = self!.CFunc.RBSquareImageTo(self!.ProfileImageMenu.image!, size: CGSize(width: 512, height: 512))
                                let imageNSData = UIImagePNGRepresentation(imageResize)!
                                self!.IFunc.SaveImage(loginData[0].id, keyuser: loginData[0].key, type: "user", imageData: imageNSData, userIDOrGroupId: loginData[0].id, request: "https://walli.ddns.net:443/saveImage") { response, error in
                                    if error == nil {
                                        do {
                                            let realm = try! Realm()
                                            try realm.write  {
                                                realm.create(ImageUsersDB.self, value: ["id": loginData[0].id , "image": imageNSData, "timestamp": String(response["response"].int!)], update: true)
                                            }
                                        }
                                        catch let error as NSError  {
                                            print("Could not save \(error), \(error.userInfo)")
                                        }
                                        self!.CFunc.errorAlert("Update!", controller: self!, message: "")
                                        self!.navigationItem.title = "Update Profile"
                                    }
                                    else {
                                        self!.CFunc.errorAlert("Error!", controller: self!, message: "Check your connection!")
                                    }
                                }
                            }
                            else {
                                self!.CFunc.errorAlert("Error!", controller: self!, message: "Check your connection!")
                            }
                        }
                    }
                }
            })
        alertController!.addAction(action)
        alertController!.addAction(UIAlertAction(title: "Back", style: UIAlertActionStyle.Default,handler: nil))
        self.presentViewController(alertController!, animated: true, completion: nil)
        }
    }
    
    //reload image profile from popover
    func PopoverImageResponse(image: UIImage){
        ProfileImageMenu.image = image
        let imageNSData:NSData = UIImagePNGRepresentation(image)!
        let strBase64:String = imageNSData.base64EncodedStringWithOptions(.Encoding64CharacterLineLength)
        currentImageBase64 = strBase64
    }
}
