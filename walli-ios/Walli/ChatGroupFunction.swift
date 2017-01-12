//
//  ChatGroupFunction.swift
//  Walli
//
//  Created by Daniele Piergigli on 26/06/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import SwiftyJSON
import RealmSwift
import Foundation

class ChatGroupFunction {

    var MServer = ServerFunction()
    var MFunc = CommonFunction()
    
    // update member of chat inside its controller
    private func updateDataChat (json: JSON, IDGroup: String, IDUser: String) {
        let realm = try! Realm()
        let chatDB = ExpensesDB()
        chatDB.id_group = IDGroup
        chatDB.id_expenses = String(json["s_id"].int!)
        chatDB.value = String(json["s_valore"].double!)
        chatDB.paid = json["s_pagato"].int!
        chatDB.descr = json["s_desc"].string!
        chatDB.data = MFunc.setTime(json["s_data"].string!)
        chatDB.id_user = String(json["u_id"].int!)
        // if you are the author of the expenses
        if String(json["u_id"].int!) == IDUser {
            chatDB.nickname = "You"
        }
        else {
            chatDB.nickname = json["u_nick"].string!
        }
        do {
            try realm.write  {
                realm.add(chatDB, update: true)
            }
        }
        catch let error as NSError  {
            print("Could not save \(error), \(error.userInfo)")
        }
    }
    
    // update member of notify inside its controller
    private func updateDataNotify (json: JSON, IDGroup: String, IDUser: String) {
        let realm = try! Realm()
        let notifyDB = NotifyDB()
        notifyDB.id_group = IDGroup
        notifyDB.id_note = String(json["n_id"].int!)
        notifyDB.descr = json["n_desc"].string!
        notifyDB.id_user = String(json["u_id"].int!)
        // if you are the author of the notify
        if String(json["u_id"].int!) == IDUser {
            notifyDB.nickname = "You"
        }
        else {
            notifyDB.nickname = json["u_nick"].string!
        }
        do {
            try realm.write  {
                realm.add(notifyDB, update: true)
            }
        }
        catch let error as NSError  {
            print("Could not save \(error), \(error.userInfo)")
        }
    }
    
    // prepare for a server request of chat expanses
    func GetChatGroups (userid: String, keyuser: String, groupID: String, request: String, completionHandler: (NSError?) -> ())  {
        let stringRequest = ["id": userid, "key": keyuser, "g_id": groupID]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if response != nil {
                for item in response.arrayValue {
                    // sync datamodel and database
                    self.updateDataChat(item, IDGroup: groupID, IDUser: userid)
                }
                completionHandler(nil)
            }
            else {
                print("Errore response chat expenses \(error)")
                completionHandler(error)
            }
        }
    }
    
    // prepare for a server request of notify
    func GetNotifyGroups(userid: String, keyuser: String, groupID: String, request: String, completionHandler: (NSError?) -> ())  {
        let stringRequest = ["id": userid, "key": keyuser, "g_id": groupID]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if response != nil {
                for item in response.arrayValue {
                    // sync datamodel and database
                    self.updateDataNotify(item, IDGroup: groupID, IDUser: userid)
                }
                completionHandler(nil)
            }
            else {
                print("Errore response notify chat \(error)")
                completionHandler(error)
            }
        }
    }
    
    // remove a expense from server
    func removeExpenses(userid: String, keyuser: String, expenseID: String, request: String, completionHandler: (JSON, NSError?) -> ())  {
        let stringRequest = ["id": userid, "key": keyuser, "s_id": expenseID]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if !response.isEmpty {
                completionHandler(response, nil)
            }
            else {
                print("Errore response remove expenses \(error)")
                completionHandler(nil, error)
            }
        }
    }
    
    // remove a notify from server
    func removeNotify(userid: String, keyuser: String, notifyID: String, request: String, completionHandler: (JSON, NSError?) -> ())  {
        let stringRequest = ["id": userid, "key": keyuser, "n_id": notifyID]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if !response.isEmpty {
                completionHandler(response, nil)
            }
            else {
                print("Errore response remove notify \(error)")
                completionHandler(nil, error)
            }
        }
    }
    
    // retrive notify
    func FetchNotes(idGroup: String) -> Results<NotifyDB> {
        let realm = try! Realm()
        let notifyData = realm.objects(NotifyDB).filter("id_group == %@", idGroup)
        return notifyData
    }
    
    // used to the segment panel to recovery all the specific value selected
    func SearchInSegment(segment: Int, idGroup: String) -> Results<ExpensesDB> {
        let realm = try! Realm()
        // paid
        if segment == 1 {
            let query = realm.objects(ExpensesDB).filter("paid == 1 AND id_group == %@", idGroup)
            return query
        }
        // to pay
        else if segment == 0 {
            let query = realm.objects(ExpensesDB).filter("paid == 0 AND id_group == %@", idGroup)
            return query
        }
        else {
            let query = realm.objects(ExpensesDB).filter("id_group == %@", idGroup)
            return query
        }
    }
}
