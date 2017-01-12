//
//  SignupFunction.swift
//  Walli
//
//  Created by Daniele Piergigli on 27/06/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import CryptoSwift
import SwiftyJSON
import Foundation

class SignupFunction {
    
    // server function
    var MServer = ServerFunction()
    
    // calcolate md5 of a string
    private func MD5get (password: String) -> String {
        let digest = password.md5()
        return digest
    }
    
    
    // prepare for send a signup request
    func GetSignup (username: String, mail: String, name: String, surname: String, cell: String, password: String, request: String, completionHandler: (JSON, NSError?) -> ())  {
        let md5 = MD5get(password)
        let stringRequest = ["nick": username, "pwd": md5, "cell": cell, "mail": mail, "surname": surname, "name": name]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if response != nil {
                completionHandler(response, error)
            }
            else {
                print("Errore response signup \(error)")
                completionHandler(nil, error)
            }
        }
    }
}