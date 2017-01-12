//
//  LoginRealmDB.swift
//  Walli
//
//  Created by Daniele Piergigli on 22/06/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import Foundation
import RealmSwift

class LoginDB: Object {

    dynamic var id = String()
    dynamic var key = String()
    dynamic var password = String()
    dynamic var name = String()
    dynamic var surname = String()
    dynamic var current_group = String()
    dynamic var nickname = String()
    dynamic var mail = String()
    dynamic var cellPhone = String()
    dynamic var currency = String()
    dynamic var token = String()
    
    override static func primaryKey() -> String? {
        return "id"
    }
}