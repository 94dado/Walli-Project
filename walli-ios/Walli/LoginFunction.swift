//
//  LoginFunction.swift
//  Walli
//
//  Created by Daniele Piergigli on 20/06/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import CryptoSwift
import SwiftyJSON
import RealmSwift
import Foundation
import Firebase

class LoginFunction {
    
    // server function
    var MServer = ServerFunction()
    
    // send data to core data
    func sendLoginData (json: JSON, username: String, password: String) {
        let loginData = LoginDB()
        loginData.nickname = username
        loginData.password = password
        loginData.id = String(json["u_id"].int!)
        loginData.key = json["key"].string!
        loginData.name = json["u_nome"].string!
        loginData.surname = json["u_cognome"].string!
        loginData.mail = json["u_mail"].string!
        loginData.cellPhone = json["u_cell"].string!
        let realm = try! Realm()
        do {
            try realm.write  {
                realm.add(loginData, update: true)
            }
        }
        catch let error as NSError  {
            print("Could not save \(error), \(error.userInfo)")
        }
    }
    // calcolate md5 of a string
    private func MD5get (password: String) -> String {
        let digest = password.md5()
        return digest
    }
    
    // send mail for obtain password forgotten
    func sendEmailForPassword(mail: String, request: String, completionHandler: (Bool) -> ()) {
        let stringRequest = ["mail": mail]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if response != nil {
                completionHandler(true)
            }
            else {
                print("Errore response set email \(error)")
                completionHandler(false)
            }
        }
    }
    
    // prepare for a server get request of key and id of user
    func GetLogin (username: String, password: String, token: String, plat: String, request: String, completionHandler: (JSON, NSError?) -> ())  {
        let md5 = MD5get(password)
        let stringRequest = ["user": username, "pwd": md5, "token": token, "platform": plat]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if response != nil {
                for item in response.arrayValue {
                    // sync datamodel and database
                    self.sendLoginData(item, username: username, password: password)
                }
                completionHandler(response, nil)
            }
            else {
                print("Errore response login \(error)")
                completionHandler(nil, error)
            }
        }
    }
    // get data form core data
    func fetchLoginData() -> Results<LoginDB> {
        let realm = try! Realm()
        let loginData = realm.objects(LoginDB)
        return loginData
    }
    
    
    // get token to firebase
    private func ReciveToken(completionHandler: (String) -> ()) {
        return POSTFirebase(completionHandler)
    }
    
    private func POSTFirebase(completionHandler: (String) -> ()) {
        completionHandler(FIRInstanceID.instanceID().token()!)
    }
    
    func tokenRefreshNotification(completionHandler: (String) -> ()) {
        ReciveToken() { token in
            print("Token is: \(token)")
            completionHandler(token)
        }

    }
    
}