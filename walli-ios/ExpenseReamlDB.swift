//
//  ChatRealmDB.swift
//  Walli
//
//  Created by Daniele Piergigli on 28/06/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import Foundation
import RealmSwift

class ExpensesDB: Object {
    
    dynamic var id_expenses = String()
    dynamic var id_group = String()
    dynamic var id_user = String()
    dynamic var nickname = String()
    dynamic var value = String()
    dynamic var paid = Int()
    dynamic var data = String()
    dynamic var descr = String()
    
    // data is only
    override static func primaryKey() -> String? {
        return "id_expenses"
    }
}