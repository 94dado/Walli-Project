//
//  SignupController.swift
//  Walli
//
//  Created by Daniele Piergigli on 19/06/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import Foundation

class SignupController: UIViewController  {
        
    @IBOutlet weak var nameSignup: UITextField!
    @IBOutlet weak var surnameSignup: UITextField!
    @IBOutlet weak var nicknameSignup: UITextField!
    @IBOutlet weak var passwordSignup: UITextField!
    @IBOutlet weak var emailSignup: UITextField!
    @IBOutlet weak var reMailSignup: UITextField!
    @IBOutlet weak var cellphoneSignup: UITextField!
    
    // For MVC
    var CFunc = CommonFunction()
    var SFunc = SignupFunction()
    
        override func viewDidLoad() {
            super.viewDidLoad()
            
            // the keyboard can disappeared when tap on the screen
            self.hideKeyboardWhenTappedAround()
        }

    //Allow to signup in the application
    @IBAction func SignupButton(sender: UIButton) {
        if emailSignup.text! == reMailSignup.text! && CFunc.validateEmail(emailSignup.text!) && nameSignup.text! != "" && surnameSignup.text! != "" && passwordSignup.text! != "" && cellphoneSignup.text! != "" && nicknameSignup.text! != "" {
            SFunc.GetSignup(nicknameSignup.text!, mail: emailSignup.text!, name: nameSignup.text!, surname: surnameSignup.text!, cell: cellphoneSignup.text!, password: passwordSignup.text!, request: "https://walli.ddns.net:443/signUp") { response, error in
                if response["response"] == "user_taken" {
                    self.CFunc.errorAlert("Error!", controller: self, message: "Nickname is already used!")
                }
                else if response != nil {
                    self.CFunc.errorAlert("Check email", controller: self, message: "We send you a mail to : \n" + self.emailSignup.text!)
                    self.dismissViewControllerAnimated(true, completion: nil)
                }
                else {
                    self.CFunc.errorAlert("Error!", controller: self, message: "Check your connection!")
                }
            }
        }
        else {
            self.CFunc.errorAlert("Error!", controller: self, message: "Fill out all the form!")
        }
    }
    
    
    @IBAction func BackButtonSignup(sender: UIButton) {
        self.dismissViewControllerAnimated(true, completion: nil)
    }
}