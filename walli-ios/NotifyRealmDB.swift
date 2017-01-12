//
//  NotifyDB.swift
//  Walli
//
//  Created by Daniele Piergigli on 29/06/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import Foundation
import RealmSwift

class NotifyDB: Object {
    
    dynamic var id_note = String()
    dynamic var id_group = String()
    dynamic var id_user = String()
    dynamic var nickname = String()
    dynamic var descr = String()
    
    // data is only
    override static func primaryKey() -> String? {
        return "id_note"
    }
}
