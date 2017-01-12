//
//  LoginController.swift
//  Walli
//
//  Created by Daniele Piergigli on 19/06/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import RealmSwift
import Foundation

class LoginController: UIViewController  {
    
    @IBOutlet weak var UsernameLogin: UITextField!
    @IBOutlet weak var PasswordLogin: UITextField!
    @IBOutlet weak var ImageLogin: UIImageView!
    
    // use for the autentication in server
    var id = String()
    var key = String()

    // MVC
    var LFunc = LoginFunction()
    var CFunc = CommonFunction()
    var IFunc = ImageFunction()
    
    override func viewDidLoad() {
        
        super.viewDidLoad()
        
        // the keyboard can disappeared when tap on the screen
        self.hideKeyboardWhenTappedAround()
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // pass to signup
        if segue.identifier == "showSignup" {
            let VController = segue.destinationViewController as! SignupController
            VController.navigationItem.title = "Sign Up"
        }
        
    }
    
    // On press forgot password button
    @IBAction func ForgotPasswordButton(sender: UIButton) {
        var alertController: UIAlertController?
        alertController = UIAlertController(title: "Forgot Password", message:"Insert your email to obtain a new password", preferredStyle: UIAlertControllerStyle.Alert)
        alertController!.addTextFieldWithConfigurationHandler({
            (textField: UITextField!) in textField.placeholder = "Enter email"
        })
        let action = UIAlertAction(title: "Send", style: UIAlertActionStyle.Default, handler: {
            [weak self](paramAction:UIAlertAction!) in
            if let textFields = alertController!.textFields{
                let theTextFields = textFields as [UITextField]
                let enteredText = theTextFields[0].text
                // insert email
                if self!.CFunc.validateEmail(enteredText!) {
                    self!.LFunc.sendEmailForPassword(enteredText!, request: "https://walli.ddns.net:443/restorePassword") { error in
                        if error {
                            self!.CFunc.errorAlert("Sent email!", controller: self!, message: "We sent you a mail to: \n" + enteredText!)
                        }
                        else {
                            self!.CFunc.errorAlert("Error!", controller: self!, message: "Check your connection!")
                        }
                    }
                }
                else {
                    self!.CFunc.errorAlert("Error!", controller: self!, message: "Email not valid!")
                }
            }
        })
        alertController!.addAction(action)
        alertController!.addAction(UIAlertAction(title: "Back", style: UIAlertActionStyle.Default,handler: nil))
        self.presentViewController(alertController!, animated: true, completion: nil)
    }
    
    // On press login button
    @IBAction func LoginButton(sender: UIButton) {
        if !self.UsernameLogin.text!.isEmpty && !self.PasswordLogin.text!.isEmpty {
            self.navigationItem.title = "Login..."
            // start login after 15 second to allow token to response and in the while rotate the image
            UIView.animateWithDuration(15, animations: {
                self.ImageLogin.transform = CGAffineTransformMakeRotation(CGFloat(M_PI))
            })
            UIView.animateWithDuration(15, animations: {
                self.ImageLogin.transform = CGAffineTransformMakeRotation(CGFloat(M_PI * 2))
            })
            NSTimer.scheduledTimerWithTimeInterval(15, target:self, selector: #selector(LoginController.Login), userInfo: nil, repeats: false)
        }
        else {
            self.CFunc.errorAlert("Error!", controller: self, message: "Text field must be filled!")
        }
    }
    
    // login to the server
    func Login() {
        // get the token
        LFunc.tokenRefreshNotification() { tokenID in
            if tokenID != "" {
                // send value for the login like platform and token for push notification
                self.LFunc.GetLogin(self.UsernameLogin.text!, password: self.PasswordLogin.text!, token: tokenID, plat: "IOS", request: "https://walli.ddns.net:443/login") { json, error in
                    if !json.isEmpty {
                        let realm = try! Realm()
                        let loginData = self.LFunc.fetchLoginData()
                        // if currency is empty filled with EUR value
                        if loginData[0].currency == "" {
                            do {
                                try realm.write  {
                                    realm.create(LoginDB.self, value: ["id": loginData[0].id, "currency": "EUR", "token": tokenID], update: true)
                                }
                            }
                            catch let error as NSError  {
                                print("Could not save \(error), \(error.userInfo)")
                            }
                        }
                        // get the user image
                        self.IFunc.GetImageUser() { result in
                            if result {
                                self.navigationItem.title = "Login"
                                self.dismissViewControllerAnimated(true, completion: nil)
                            }
                            else {
                                self.CFunc.errorAlert("Connection fail", controller: self, message: "Check your connection!")
                            }
                        }
                    }
                    else {
                        self.CFunc.errorAlert("Error!", controller: self, message: "Password or username not valid or check your connection!")
                    }
                }
            }
            else {
                self.CFunc.errorAlert("Error!", controller: self, message: "Check your connection!")
            }
        }
    }
}