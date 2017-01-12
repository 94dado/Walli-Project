//
//  GroupsFunction.swift
//  Walli
//
//  Created by Daniele Piergigli on 21/06/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import SwiftyJSON
import RealmSwift
import Foundation

class GroupsFunction {
    
    var MServer = ServerFunction()
    var MFunc = CommonFunction()
    
    // update group inside its controller
    private func updateDataGroup (json: JSON) {
        let realm = try! Realm()
        let groupDB = GroupDB()
        groupDB.id = String(json["g_id"].int!)
        groupDB.name = json["g_nome"].string!
        groupDB.currency = MFunc.getCurrency(json["g_valuta"].string!)
        groupDB.money = String(json["credito"].double! - json["debito"].double!)
        groupDB.num_notify = json["notifiche"].int!
        if json["g_lastUpdate"].string == nil {
            groupDB.time = "Insert expense"
        }
        else {
            groupDB.time = MFunc.setTime((json["g_lastUpdate"].string!))
        }
        do {
            try realm.write  {
                realm.add(groupDB, update: true)
            }
        }
        catch let error as NSError  {
            print("Could not save \(error), \(error.userInfo)")
        }
    }
    
    // prepare for a server get request of groups
    func GetGroups (userid: String, keyuser: String, request: String, completionHandler: (NSError?) -> ())  {
        let stringRequest = ["id": userid, "key": keyuser]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if response != nil {
                for item in response.arrayValue {
                    // sync datamodel and database
                    self.updateDataGroup(item)
                }
                completionHandler(nil)
            }
            else {
                print("Errore response group \(error)")
                completionHandler(error)
            }
        }
    }
    
    // remove a group from server
    func removeGroups(userid: String, keyuser: String, groupID: String, request: String, completionHandler: (NSError?) -> ())  {
        let stringRequest = ["id": userid, "key": keyuser, "g_id": groupID]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if response["response"] == "ok" {
                completionHandler(nil)
            }
            else {
                print("Errore response remove groups \(error)")
                completionHandler(error)
            }
        }
    }
    
    // get all the element in Group Realm
    func FetchGroup() -> Results<GroupDB> {
        let realm = try! Realm()
        let groupData = realm.objects(GroupDB)
        return groupData
    }
}
