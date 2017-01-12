//
//  PayExpensesFunction.swift
//  Walli
//
//  Created by Daniele Piergigli on 05/07/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import SwiftyJSON
import Foundation
import RealmSwift

class PayExpensesFunction {
    
    var MServer = ServerFunction()
    var MFunc = CommonFunction()
    
    private func UpdateDataMembers(item: JSON) {
        let realm = try! Realm()
        let membersDB = MembersDB()
        membersDB.id_user = String(item["u_id"].int!)
        membersDB.name = item["u_nome"].string!
        membersDB.total_money = String(item["credito"].double! - item["debito"].double!)
        membersDB.surname = item["u_cognome"].string!
        membersDB.nickname = item["u_nick"].string!
        do {
            try realm.write  {
                realm.add(membersDB, update: true)
            }
        }
        catch let error as NSError  {
            print("Could not save \(error), \(error.userInfo)")
        }
    }
    
    // prepare for a server get request of users expanses
    func GetPayments(userid: String, keyuser: String, currency: String, request: String, completionHandler: (JSON, NSError?) -> ())  {
        let stringRequest = ["id": userid, "key": keyuser, "valuta": currency]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if response != nil {
                for item in response.arrayValue {
                    self.UpdateDataMembers(item)
                }
                completionHandler(response, nil)
            }
            else {
                print("Errore response get payment \(error)")
                completionHandler(nil, error)
            }
        }
    }
    
    // send payment
    func SendPaid(id: String, keyuser: String, userid: String, request: String, completionHandler: (JSON, NSError?) -> ()) {
        let stringRequest = ["id": id, "key": keyuser, "u_id": userid]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if response != nil {
                completionHandler(response, nil)
            }
            else {
                print("Errore response payment of expenses user total \(error)")
                completionHandler(nil, error)
            }
        }
    }
    
    // send warning
    func SendWarning(id: String, keyuser: String, userid: String, request: String, completionHandler: (JSON, NSError?) -> ()) {
        let stringRequest = ["id": id, "key": keyuser, "u_id": userid]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if response != nil {
                completionHandler(response, nil)
            }
            else {
                print("Errore response send warning \(error)")
                completionHandler(nil, error)
            }
        }
    }
    
    // retrive members
    func FetchMembers() -> Results<MembersDB> {
        let realm = try! Realm()
        return realm.objects(MembersDB)
    }
}
