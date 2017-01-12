//
//  UserGroupFunction.swift
//  Walli
//
//  Created by Daniele Piergigli on 27/06/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import SwiftyJSON
import RealmSwift
import Foundation

class UserGroupFunction {
    
    var MServer = ServerFunction()
    var MFunc = CommonFunction()
    
    // update member of users inside its controller
    private func updateDataUsers (json: JSON, IDGroup: String) {
        let realm = try! Realm()
        let usersDB = UsersDB()
        usersDB.id_group = IDGroup
        usersDB.mail = json["u_mail"].string!
        usersDB.nickname = json["u_nick"].string!
        usersDB.heritage = String(json["credito"].double! - json["debito"].double!)
        usersDB.name = json["u_nome"].string!
        usersDB.surname = json["u_cognome"].string!
        usersDB.cell = json["u_cell"].string!
        usersDB.id_user = String(json["u_id"].int!)
        usersDB.key = "\(String(json["u_id"].int!))-\(IDGroup)"
        do {
            try realm.write  {
                realm.add(usersDB, update: true)
            }
        }
        catch let error as NSError  {
            print("Could not save \(error), \(error.userInfo)")
        }
    }
    
    // prepare for a server get request of users expanses
    func GetUserGroups (userid: String, keyuser: String, groupID: String, request: String, completionHandler: (NSError?) -> ())  {
        let stringRequest = ["id": userid, "key": keyuser, "g_id": groupID]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if response != nil {
                for item in response.arrayValue {
                    // sync datamodel and database
                    self.updateDataUsers(item, IDGroup: groupID)
                }
                completionHandler(nil)
            }
            else {
                print("Errore response user groups \(error)")
                completionHandler(error)
            }
        }
    }
    
    // set the array of chat
    func UserInThisGroup(groupID: String) -> [String]{
        var userInThisGroup = [String]()
        let realm = try! Realm()
        let allUsers = realm.objects(UsersDB).filter("id_group == %@", groupID)
        for us in allUsers {
            userInThisGroup.append(us.id_user)
        }
        return userInThisGroup
    }
    
    // send payment
    func SendPaid(id: String, keyuser: String, userid: String, idGroup: String, request: String, completionHandler: (JSON, NSError?) -> ()) {
        let stringRequest = ["id": id, "key": keyuser, "u_id": userid, "g_id": idGroup]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if response != nil {
                completionHandler(response, nil)
            }
            else {
                print("Errore response send paid user groups \(error)")
                completionHandler(nil, error)
            }
        }
    }
    
    // retrive users realm
    func FetchUsers(idGroup: String) -> Results<UsersDB> {
        let realm = try! Realm()
        let usersData = realm.objects(UsersDB).filter("id_group == %@", idGroup)
        return usersData
    }
}
