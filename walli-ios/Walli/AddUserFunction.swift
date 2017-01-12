//
//  AddUserFunction.swift
//  Walli
//
//  Created by Daniele Piergigli on 02/07/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import RealmSwift
import SwiftyJSON
import Alamofire
import Foundation

class AddUserFunction {
    
    // server function
    var MServer = ServerFunction()
    var CFunc = CommonFunction()
    
    // update realm wit user
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
    
    // update ream with user there are not in member by hint
    private func UpdateDataMembersHints (item: JSON) -> String? {
        let realm = try! Realm()
        let query = realm.objects(MembersDB).filter("id_user == %@", String(item["u_id"].int!))
            if query.count == 0 {
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
                return String(item["u_id"].int!)
            }
            else {
                return nil
            }
    }
    
    // update cell of table view for selected element
    func UpdateCell (key_idArray: [String] , currentGroup: String, members: Results<MembersDB>) {
        let realm = try! Realm()
        for member in realm.objects(MembersDB) {
            if currentGroup == "" {
                if key_idArray.isEmpty {
                    do {
                        try realm.write  {
                            realm.create(MembersDB.self, value: ["id_user": member.id_user, "checked": false], update: true )
                        }
                    }
                    catch let error as NSError  {
                        print("Could not save \(error), \(error.userInfo)")
                    }
                }
                // update member array
                else {
                    // set the view checked or not checked
                    if key_idArray.contains(member.id_user) {
                        do {
                            try realm.write  {
                                realm.create(MembersDB.self, value: ["id_user": member.id_user, "checked": true], update: true )
                            }
                        }
                        catch let error as NSError  {
                            print("Could not save \(error), \(error.userInfo)")
                        }
                    }
                    else {
                        do {
                            try realm.write  {
                                realm.create(MembersDB.self, value: ["id_user": member.id_user, "checked": false], update: true )
                            }
                        }
                        catch let error as NSError  {
                            print("Could not save \(error), \(error.userInfo)")
                        }
                    }
                }
            }
            // if is user update
            else {
                if key_idArray.contains(member.id_user) {
                    do {
                        try realm.write  {
                            realm.create(MembersDB.self, value: ["id_user": member.id_user, "checked": true], update: true )
                        }
                    }
                    catch let error as NSError  {
                        print("Could not save \(error), \(error.userInfo)")
                    }
                }
                else {
                    do {
                        try realm.write  {
                            realm.create(MembersDB.self, value: ["id_user": member.id_user, "checked": false], update: true )
                        }
                    }
                    catch let error as NSError  {
                        print("Could not save \(error), \(error.userInfo)")
                    }
                }
            }
        }
    }
    // prepare for a server get request of key and id of user
    func getHintSearchbar(userid: String, key: String, name: String, request: String, completionHandler: ([String], JSON, NSError?) -> ())  {
        let stringRequest = ["id": userid, "key": key, "text": name]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if !response.isEmpty {
                var arrayOFIDNotUsedYet = [String]()
                for item in response.arrayValue {
                    let id = self.UpdateDataMembersHints(item)
                    if id != nil {
                        arrayOFIDNotUsedYet.append(id!)
                    }
                }
                completionHandler(arrayOFIDNotUsedYet, response, nil)
            }
            else {
                print("Errore response hint \(error)")
                completionHandler([String](), nil, error)
            }
        }
    }
    
    // prepare for a server get request of users
    func GetUserAllGroups(userid: String, keyuser: String, currency: String, request: String, completionHandler: (JSON, NSError?) -> ())  {
        let stringRequest = ["id": userid, "key": keyuser, "valuta": currency]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if response != nil {
                for item in response.arrayValue {
                    self.UpdateDataMembers(item)
                }
                completionHandler(response, nil)
            }
            else {
                print("Errore response lget user of all groups \(error)")
                completionHandler(nil, error)
            }
        }
    }
    
    // prepare for a server update of user and groups
    func SendUpdateCreateGroupUsers(userid: String, keyuser: String, g_id: String, g_name: String, g_currency: String, arrayUsers: [String], request: String, completionHandler: (JSON, NSError?) -> ())  {
        // simbol in value
        let currency = CFunc.transformToStringCurrency(g_currency)
        // Array to json for users
        var stringUserJson = NSString()
        do {
            let dataUser = try NSJSONSerialization.dataWithJSONObject(arrayUsers, options: NSJSONWritingOptions.PrettyPrinted)
            stringUserJson = NSString(data: dataUser, encoding: NSUTF8StringEncoding)!
        }
        catch let error as NSError {
            print ("\(error)")
        }
        var stringRequest = [String:AnyObject]()
        if g_id == "" {
            
            stringRequest = ["id": userid, "key": keyuser, "user_ids": stringUserJson, "g_name": g_name, "valuta": currency]
        }
        else {
            stringRequest = ["id": userid, "key": keyuser, "g_id": g_id, "user_ids": stringUserJson, "g_name": g_name, "valuta": currency]
        }
        outPOST(request, stringServer: stringRequest) { response, error in
            if response != nil {
                completionHandler(response, nil)
            }
            else {
                print("Errore response update or create group \(error)")
                completionHandler(nil, error)
            }
        }
    }
    
    // solce the asyncrounus call POST (not call server for a problem with string)
    private func outPOST(httpHeader: String, stringServer: [String:AnyObject]?, completionHandler: (JSON, NSError?) -> ()) {
        return POSTServer(httpHeader, string: stringServer, completionHandler: completionHandler)
    }
    
    // POST request (not call server for a problem with string)
    private func POSTServer(httpHeader: String, string: [String:AnyObject]?, completionHandler: (JSON, NSError?) -> ()) {
        Alamofire.request(.POST, httpHeader, parameters: string, encoding: .URL)
            .validate()
            .responseJSON { response in
                switch response.result {
                case .Success:
                    if let value = response.result.value {
                        let json = JSON(value)
                        completionHandler(json, nil)
                    }
                case .Failure(let error):
                    completionHandler(nil, error)
                }
        }
    }
    
    
    // retrive members
    func FetchMembers(hints: String) -> Results<MembersDB> {
        let realm = try! Realm()
        if hints != "" {
            return realm.objects(MembersDB).filter("nickname BEGINSWITH[c] '\(hints)'")        }
        else {
            return realm.objects(MembersDB)
        }
    }
    
    // remove members by ints not added
    func RemoveMembersNotIncluded(RemoveIdArray: [String], action: String) {
        // remove all if back because action update is not done
        if action == "back" {
            for member in FetchMembers("") {
                if RemoveIdArray.contains(member.id_user) {
                    do {
                        let realm = try! Realm()
                        try realm.write {
                            realm.delete(member)
                        }
                    }
                    catch let error as NSError  {
                        print("Could not save \(error), \(error.userInfo)")
                    }
                }
            }
        }
        else {
            // remove just the unchecked
            for member in FetchMembers("") {
                if RemoveIdArray.contains(member.id_user) && !member.checked {
                    do {
                        let realm = try! Realm()
                        try realm.write {
                            realm.delete(member)
                        }
                    }
                    catch let error as NSError  {
                        print("Could not save \(error), \(error.userInfo)")
                    }
                }
            }
        }
    }
}
