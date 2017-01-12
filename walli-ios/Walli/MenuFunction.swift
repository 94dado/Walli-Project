//
//  MenuFunction.swift
//  Walli
//
//  Created by Daniele Piergigli on 05/05/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import Foundation

class MenuFunction {
    
    var CGroups = GroupsFunction()
    var MServer = ServerFunction()
    
    // Max num of notification not again paid
    func NumberOfNotify () -> String {
        let groupData = CGroups.FetchGroup()
        var NumNotify = 0
        
        for item in groupData {
            NumNotify += item.num_notify
        }
        
        return String(NumNotify)
        
    }
    
    // prepare for a server get request of logout
    func Logout(userid: String, keyuser: String, token: String, request: String, completionHandler: (NSError?) -> ())  {
        let stringRequest = ["id": userid, "key": keyuser, "token": token]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if response != nil {
                completionHandler(error)
            }
            else {
                print("Errore response logout \(error)")
                completionHandler(error)
            }
        }
    }
}
