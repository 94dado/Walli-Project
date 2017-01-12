//
//  UpdateProfileFunction.swift
//  Walli
//
//  Created by Daniele Piergigli on 01/07/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import CryptoSwift
import SwiftyJSON
import Foundation

class UpdateProfileFunction {
    
    // server function
    var MServer = ServerFunction()
    
    // calcolate md5 of a string
    private func MD5get (password: String) -> String {
        let digest = password.md5()
        return digest
    }
    
    
    // prepare for a server get request of key and id of user
    func sendUpdateUserProfile(userid: String, key: String, mail: String, name: String, surname: String, cell: String, password: String, oldPassword: String, request: String, completionHandler: (Bool) -> ())  {
        var stringRequest = [String:String]()
        // update password or not
        if oldPassword != password {
            let md5 = MD5get(password)
            stringRequest = ["id": userid, "key": key, "pwd": md5, "phone": cell, "mail": mail, "surname": surname, "name": name]
        }
        else {
            stringRequest = ["id": userid, "key": key, "phone": cell, "mail": mail, "surname": surname, "name": name]
        }
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if !response.isEmpty {
                completionHandler(true)
            }
            else {
                print("Errore response send update user profile \(error)")
                completionHandler(false)
            }
        }
    }
}
