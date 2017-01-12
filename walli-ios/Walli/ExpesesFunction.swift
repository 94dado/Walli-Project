//
//  ExpesesFunction.swift
//  Walli
//
//  Created by Daniele Piergigli on 28/06/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import SwiftyJSON
import RealmSwift
import Foundation

class ExpensesFunction {
    
    var MServer  = ServerFunction()
    var CFunc = CommonFunction()
    
    // prepare for a server to send expense
    func SendExpenses(userid: String, keyuser: String, groupID: String, description: String, value: String, notifyID: String, request: String, completionHandler: (JSON, NSError?) -> ())  {
        var stringRequest = [String:String]()
        // is a really expense
        if notifyID == "" {
            stringRequest = ["id": userid, "key": keyuser, "g_id": groupID, "description": description, "value": value.stringByReplacingOccurrencesOfString(",", withString: ".")]
        }
        // was befora a notify
        else {
            stringRequest = ["id": userid, "key": keyuser, "g_id": groupID, "description": description, "value": value.stringByReplacingOccurrencesOfString(",", withString: "."), "n_id": notifyID]
        }
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if response != nil {
                completionHandler(response, error)
            }
            else {
                print("Errore response login \(error)")
                completionHandler(nil, error)
            }
        }
    }
    
    // populate the database of expenses and remove from notify
    func PopulateDatabaseOfExpenses(groupID: String, ExpensesID: String, money: Double, description: String, date: String, userID: String, notifyID: String) {
        let realm = try! Realm()
        let chatDB = ExpensesDB()
        chatDB.id_group = groupID
        chatDB.id_expenses = ExpensesID
        chatDB.nickname = "You"
        chatDB.value = String(money)
        chatDB.paid = 0
        chatDB.descr = description
        chatDB.data = self.CFunc.setTime(date)
        chatDB.id_user = userID
        do {
            try realm.write  {
                realm.add(chatDB)
            }
        }
        catch let error as NSError  {
            print("Could not save \(error), \(error.userInfo)")
        }
        if notifyID != "" {
            let realm = try! Realm()
            let notifyDB = realm.objects(NotifyDB)
            for n in notifyDB {
                let query = realm.objects(NotifyDB).filter("id_note == %@", notifyID)
                if query.count == 1 {
                    do {
                        try realm.write {
                            realm.delete(n)
                        }
                    }
                    catch let error as NSError {
                        print("Could not save \(error), \(error.userInfo)")
                    }
                }
            }
        }
    }
    
    // look if expense is paid from someone
    func CheckExpense(userid: String, keyuser: String, expenseID: String, request: String, completionHandler: (JSON, NSError?) -> ())  {
        let stringRequest = ["id": userid, "key": keyuser, "s_id": expenseID]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if !response.isEmpty {
                completionHandler(response, nil)
            }
            else {
                print("Errore response check expenses \(error)")
                completionHandler(nil, error)
            }
        }
    }
    
    // update a expense from server
    func UpdateExpense(userid: String, keyuser: String, expenseID: String, description: String, value: String, request: String, completionHandler: (JSON, NSError?) -> ())  {
        let stringRequest = ["id": userid, "key": keyuser, "s_id": expenseID, "desc": description, "value": value.stringByReplacingOccurrencesOfString(",", withString: ".")]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if !response.isEmpty {
                completionHandler(response, nil)
            }
            else {
                print("Errore response update expenses \(error)")
                completionHandler(nil, error)
            }
        }
    }
}
