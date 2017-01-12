//
//  NotifyFunction.swift
//  Walli
//
//  Created by Daniele Piergigli on 29/06/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import SwiftyJSON
import Foundation

class NotifyFunction {
    
    var MServer  = ServerFunction()
    
    // prepare for a server to send notify
    func SendNote(userid: String, keyuser: String, groupID: String, description: String, request: String, completionHandler: (JSON, NSError?) -> ())  {
        let stringRequest = ["id": userid, "key": keyuser, "g_id": groupID, "description": description]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if response != nil {
                completionHandler(response, error)
            }
            else {
                print("Errore response send notification \(error)")
                completionHandler(nil, error)
            }
        }
    }
    
    // update a notify from server
    func UpdateNotify(userid: String, keyuser: String, notifyID: String, description: String, request: String, completionHandler: (JSON, NSError?) -> ())  {
        let stringRequest = ["id": userid, "key": keyuser, "n_id": notifyID, "desc": description]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if !response.isEmpty {
                completionHandler(response, nil)
            }
            else {
                print("Errore response update notification \(error)")
                completionHandler(nil, error)
            }
        }
    }
}
