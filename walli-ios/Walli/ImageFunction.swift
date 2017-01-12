//
//  ImageFunction.swift
//  Walli
//
//  Created by Daniele Piergigli on 07/07/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import SwiftyJSON
import Foundation
import RealmSwift

class ImageFunction {
    
    // server function
    var MServer = ServerFunction()
    var LFunc = LoginFunction()
    var GFunc = GroupsFunction()
    var UFunc = PayExpensesFunction()
    var USFunc = UserGroupFunction()
    
    // send image to server
    func SaveImage(userid: String, keyuser: String, type: String, imageData: NSData, userIDOrGroupId: String, request: String, completionHandler: (JSON, NSError?) -> ())  {
        let imageBase64 = imageData.base64EncodedStringWithOptions(.Encoding64CharacterLineLength)
        let stringRequest = ["id": userid, "key": keyuser, "type": type, "img_id": userIDOrGroupId, "img_ext": "png", "raw_img": imageBase64]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if !response.isEmpty {
                completionHandler(response, nil)
            }
            else {
                print("Errore response save image \(error)")
                completionHandler(nil, error)
            }
        }
    }
    
    // send image to server
    func GetImage(userid: String, keyuser: String, type: String, userIDOrGroupId: String, request: String, completionHandler: (JSON, NSError?) -> ())  {
        let stringRequest = ["id": userid, "key": keyuser, "type": type, "img_id": userIDOrGroupId]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if !response.isEmpty {
                completionHandler(response, nil)
            }
            else {
                print("Errore response get image \(error)")
                completionHandler(nil, error)
            }
        }
    }
    
    // send image to server
    func CheckUpdateImage(userid: String, keyuser: String, type: String, timestamp: String, userIDOrGroupId: String, request: String, completionHandler: (JSON, NSError?) -> ())  {
        let stringRequest = ["id": userid, "key": keyuser, "type": type, "img_id": userIDOrGroupId, "timestamp": timestamp]
        MServer.outPOST(request, stringServer: stringRequest) { response, error in
            if !response.isEmpty {
                completionHandler(response, nil)
            }
            else {
                print("Errore response update image check \(error)")
                completionHandler(nil, error)
            }
        }
    }
    
    // retrive notify
    func FetchGroupImage(idGroup: String) -> Results<ImageGroupsDB> {
        let realm = try! Realm()
        let imageData = realm.objects(ImageGroupsDB).filter("id == %@", idGroup)
        return imageData
    }
    
    // retrive notify
    func FetchUserImage(idUser: String) -> Results<ImageUsersDB> {
        let realm = try! Realm()
        let imageData = realm.objects(ImageUsersDB).filter("id == %@", idUser)
        return imageData
    }
    
    func GetImageGroupData(ID: String, completitionHandler: (Bool) -> ()) {
        let loginData = LFunc.fetchLoginData()
        let imageData = FetchGroupImage(ID)
        if !imageData.isEmpty {
                // if timestamp exist check image if is update
            self.CheckUpdateImage(loginData[0].id, keyuser: loginData[0].key, type: "group", timestamp: imageData[0].timestamp, userIDOrGroupId: ID, request: "https://walli.ddns.net:443/checkImage") { response, error in
                if response["response"].string! == "to-up-date" {
                    // update image
                    self.GetImage(loginData[0].id, keyuser: loginData[0].key, type: "group", userIDOrGroupId: ID, request: "https://walli.ddns.net:443/getImage") { response, error in
                        if error == nil {
                            let imageDecoded:NSData = NSData(base64EncodedString: response["response"].string!, options: NSDataBase64DecodingOptions.IgnoreUnknownCharacters)!
                            do {
                                let realm = try! Realm()
                                try realm.write  {
                                    realm.create(ImageGroupsDB.self, value: ["id": ID , "image": imageDecoded, "timestamp": response["timestamp"].string!], update: true)
                                }
                            }
                            catch let error as NSError  {
                                print("Could not save \(error), \(error.userInfo)")
                            }
                            completitionHandler(true)
                        }
                        else {
                            completitionHandler(false)
                        }
                    }
                }
                else {
                    completitionHandler(true)
                }
            }
        }
        // else update
        else {
            self.GetImage(loginData[0].id, keyuser: loginData[0].key, type: "group", userIDOrGroupId: ID, request: "https://walli.ddns.net:443/getImage") { response, error in
                if error == nil {
                    var imageDecoded = NSData()
                    if response["response"].string! != "default" {
                        imageDecoded = NSData(base64EncodedString: response["response"].string!, options: NSDataBase64DecodingOptions.IgnoreUnknownCharacters)!
                        do {
                            let realm = try! Realm()
                            try realm.write  {
                                realm.create(ImageGroupsDB.self, value: ["id": ID, "image": imageDecoded, "timestamp": response["timestamp"].string!], update: true)
                                completitionHandler(true)
                            }
                        }
                        catch let error as NSError  {
                            print("Could not save \(error), \(error.userInfo)")
                        }
                    }
                    else {
                        // image is not in server so use default
                        completitionHandler(false)
                    }
                }
                else {
                    completitionHandler(false)
                }
            }
        }
    }
    
    // download image of all members
    func GetImageForUserId(ID: String, completitionHandler: (Bool) -> ()) {
        let loginData = LFunc.fetchLoginData()
        let imageData = FetchUserImage(ID)
        if !imageData.isEmpty {
            // if timestamp exist check image if is update
            self.CheckUpdateImage(loginData[0].id, keyuser: loginData[0].key, type: "user", timestamp: imageData[0].timestamp, userIDOrGroupId: ID, request: "https://walli.ddns.net:443/checkImage") { response, error in
                if response["response"].string! == "to-up-date" {
                    // update image
                    self.GetImage(loginData[0].id, keyuser: loginData[0].key, type: "user", userIDOrGroupId: ID, request: "https://walli.ddns.net:443/getImage") { response, error in
                        if error == nil {
                            let imageDecoded:NSData = NSData(base64EncodedString: response["response"].string!, options: NSDataBase64DecodingOptions.IgnoreUnknownCharacters)!
                            do {
                                let realm = try! Realm()
                                try realm.write  {
                                    realm.create(ImageUsersDB.self, value: ["id": ID , "image": imageDecoded, "timestamp": response["timestamp"].string!], update: true)
                                }
                            }
                            catch let error as NSError  {
                                print("Could not save \(error), \(error.userInfo)")
                            }
                            completitionHandler(true)
                        }
                        else {
                            // image is not in server so use default
                            completitionHandler(false)
                        }
                    }
                }
                else {
                    completitionHandler(true)
                }
            }
        }
        // else update
        else {
            self.GetImage(loginData[0].id, keyuser: loginData[0].key, type: "user", userIDOrGroupId: ID, request: "https://walli.ddns.net:443/getImage") { response, error in
                if error == nil {
                    var imageDecoded = NSData()
                    if response["response"].string! != "default" {
                        imageDecoded = NSData(base64EncodedString: response["response"].string!, options: NSDataBase64DecodingOptions.IgnoreUnknownCharacters)!
                        do {
                            let realm = try! Realm()
                            try realm.write  {
                                realm.create(ImageUsersDB.self, value: ["id": ID ,"image": imageDecoded, "timestamp": response["timestamp"].string!], update: true)
                                completitionHandler(true)
                            }
                        }
                        catch let error as NSError  {
                            print("Could not save \(error), \(error.userInfo)")
                        }
                    }
                    else {
                        // image is not in server so use default
                        completitionHandler(false)
                    }
                }
                else {
                    completitionHandler(false)
                }
            }
        }
    }

    // allow to download image from current user logged
    func GetImageUser(completitionHandler: (Bool) -> ()) {
        let user = LFunc.fetchLoginData()
        let loginData = LFunc.fetchLoginData()
        let imageData = FetchUserImage(user[0].id)
        if !imageData.isEmpty {
                // if timestamp exist check image if is update
                self.CheckUpdateImage(loginData[0].id, keyuser: loginData[0].key, type: "user", timestamp: imageData[0].timestamp, userIDOrGroupId: user[0].id, request: "https://walli.ddns.net:443/checkImage") { response, error in
                    if response["response"].string! == "to-up-date" {
                        // update image
                        self.GetImage(loginData[0].id, keyuser: loginData[0].key, type: "user", userIDOrGroupId: user[0].id, request: "https://walli.ddns.net:443/getImage") { response, error in
                            if error == nil {
                                let imageDecoded:NSData = NSData(base64EncodedString: response["response"].string!, options: NSDataBase64DecodingOptions.IgnoreUnknownCharacters)!
                                do {
                                    let realm = try! Realm()
                                    try realm.write  {
                                        realm.create(ImageUsersDB.self, value: ["id": user[0].id , "image": imageDecoded, "timestamp": response["timestamp"].string!], update: true)
                                    }
                                }
                                catch let error as NSError  {
                                    print("Could not save \(error), \(error.userInfo)")
                                }
                                completitionHandler(true)
                            }
                            // image is not in server so use default
                            else {
                                completitionHandler(false)
                            }
                        }
                    }
                    else {
                        completitionHandler(true)
                    }
                }
            }
                // else update
            else {
                self.GetImage(loginData[0].id, keyuser: loginData[0].key, type: "user", userIDOrGroupId: user[0].id, request: "https://walli.ddns.net:443/getImage") { response, error in
                    if error == nil {
                        var imageDecoded = NSData()
                        if response["response"].string! != "default" {
                            imageDecoded = NSData(base64EncodedString: response["response"].string!, options: NSDataBase64DecodingOptions.IgnoreUnknownCharacters)!
                            do {
                                let realm = try! Realm()
                                try realm.write  {
                                    realm.create(ImageUsersDB.self, value: ["id": user[0].id ,"image": imageDecoded, "timestamp": response["timestamp"].string!], update: true)
                                    completitionHandler(true)
                                }
                            }
                            catch let error as NSError  {
                                print("Could not save \(error), \(error.userInfo)")
                            }
                        }
                    }
                    else {
                        completitionHandler(false)
                    }
                }
            }
        }
}