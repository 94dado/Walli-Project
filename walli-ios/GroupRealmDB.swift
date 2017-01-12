//
//  GroupRealmDB.swift
//  Walli
//
//  Created by Daniele Piergigli on 23/06/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import Foundation
import RealmSwift

class GroupDB: Object {
    
    dynamic var id = String()
    dynamic var name = String()
    dynamic var currency = String()
    dynamic var money = String()
    dynamic var time = String()
    dynamic var num_notify = Int()
    
    override static func primaryKey() -> String? {
        return "id"
    }
}