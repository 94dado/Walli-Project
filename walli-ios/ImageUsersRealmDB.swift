//
//  ImageUsersRealmDB.swift
//  Walli
//
//  Created by Daniele Piergigli on 07/07/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import Foundation
import RealmSwift

class ImageUsersDB: Object {
    
    dynamic var id = String()
    dynamic var image = NSData()
    dynamic var timestamp = String()
    
    // data is only
    override static func primaryKey() -> String? {
        return "id"
    }
}
