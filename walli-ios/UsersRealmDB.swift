//
//  UsersRealmDB.swift
//  Walli
//
//  Created by Daniele Piergigli on 30/06/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import Foundation
import RealmSwift

class UsersDB: Object {
    
    dynamic var id_group = String()
    dynamic var id_user = String()
    dynamic var key = String() // use like primary key is equal to "\(UserDB.user_id)-\(UserDB.group_id) = 1-3 for example"
    dynamic var nickname = String()
    dynamic var mail = String()
    dynamic var heritage = String() // credit - debt server
    dynamic var name = String()
    dynamic var surname = String()
    dynamic var cell = String()
    
    // data is only
    override static func primaryKey() -> String? {
        return "key"
    }
}
