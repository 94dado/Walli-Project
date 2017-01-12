//
//  ServerFunction.swift
//  Walli
//
//  Created by Daniele Piergigli on 21/06/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import Foundation
import Alamofire
import SwiftyJSON

class ServerFunction {    
    
    // solve asyncrounus call POST
    func outPOST(httpHeader: String, stringServer: [String:String]?, completionHandler: (JSON, NSError?) -> ()) {
        return POSTServer(httpHeader, string: stringServer, completionHandler: completionHandler)
    }
    
    // POST request
    private func POSTServer(httpHeader: String, string: [String:String]?, completionHandler: (JSON, NSError?) -> ()) {
        Alamofire.request(.POST, httpHeader, parameters: string, encoding: .URL)
        .validate()
        .responseJSON { response in
            switch response.result {
            case .Success:
                if let value = response.result.value {
                    let json = JSON(value)
                    completionHandler(json, nil)
                }
            case .Failure(let error):
                completionHandler(nil, error)
            }
        }
    }
}