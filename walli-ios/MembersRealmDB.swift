//
//  MembersRealmDB.swift
//  Walli
//
//  Created by Daniele Piergigli on 07/07/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import Foundation
import RealmSwift

// all the members you know and not just users in group
class MembersDB: Object {
    
    dynamic var id_user = String()
    dynamic var nickname = String()
    dynamic var total_money = String() // credit - debt server
    dynamic var name = String()
    dynamic var surname = String()
    dynamic var checked = Bool() // for the add user to know if is in the current group or is checked
    
    // data is only
    override static func primaryKey() -> String? {
        return "id_user"
    }
}
