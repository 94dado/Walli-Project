//
//  CommonFunction.swift
//  Walli
//
//  Created by Daniele Piergigli on 20/04/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import Foundation
import RealmSwift
import SystemConfiguration

// extension to menage tapping out of the keybord to hide it
extension UIViewController {
    func hideKeyboardWhenTappedAround() {
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(UIViewController.dismissKeyboard))
        view.addGestureRecognizer(tap)
    }
    
    func dismissKeyboard() {
        view.endEditing(true)
    }
}

class CommonFunction {
    
    // check if is connected to network
    func isConnectedToNetwork() -> Bool {
        var zeroAddress = sockaddr_in()
        zeroAddress.sin_len = UInt8(sizeofValue(zeroAddress))
        zeroAddress.sin_family = sa_family_t(AF_INET)
        // look connection
        let defaultRouteReachability = withUnsafePointer(&zeroAddress) {
            SCNetworkReachabilityCreateWithAddress(nil, UnsafePointer($0))
        }
        var flags = SCNetworkReachabilityFlags()
        if !SCNetworkReachabilityGetFlags(defaultRouteReachability!, &flags) {
            return false
        }
        let isReachable = (flags.rawValue & UInt32(kSCNetworkFlagsReachable)) != 0
        let needsConnection = (flags.rawValue & UInt32(kSCNetworkFlagsConnectionRequired)) != 0
        return (isReachable && !needsConnection)
    }
    
    // regex for email validation
    func validateEmail(enteredEmail:String) -> Bool {
        let emailFormat = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"
        let emailPredicate = NSPredicate(format:"SELF MATCHES %@", emailFormat)
        return emailPredicate.evaluateWithObject(enteredEmail)
    }
    
    // Color constant for the application
    let OrangeWalli = UIColor(red: 1, green: 87/255, blue: 34/255, alpha: 1)
    let BlackWalli = UIColor(red: 33/255, green: 33/255, blue: 33/255, alpha: 1)
    let SetupWalli = UIColor(red: 0/255, green: 122/255, blue: 255/255, alpha: 1)
    
    // create a error alert
    func errorAlert(title: String, controller: UIViewController, message: String) {
        let alertController = UIAlertController(title: title, message: message, preferredStyle:  UIAlertControllerStyle.Alert)
        alertController.addAction(UIAlertAction(title: "Ok", style: UIAlertActionStyle.Default,handler: nil))
        controller.presentViewController(alertController, animated: true, completion: nil)
    }
    
    // set the time of last update (take a look to yesterday value)
    func setTime(timedate: String) -> String {
        let dateFormatter = NSDateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
        let time = dateFormatter.dateFromString(timedate)
        let dateComponents = NSDate()
        let dateComponentsFormatter = NSDateComponentsFormatter()
        dateComponentsFormatter.allowedUnits = [NSCalendarUnit.Day]
        // date of today
        let diffDateComponents = NSCalendar.currentCalendar().components([NSCalendarUnit.Year, NSCalendarUnit.Month, NSCalendarUnit.Day, NSCalendarUnit.Hour, NSCalendarUnit.Minute, NSCalendarUnit.Second], fromDate: time!, toDate: dateComponents, options: NSCalendarOptions.init(rawValue: 0))
        var timeString = String()
        if diffDateComponents.day == 1 {
            timeString = "Yesterday"
        }
        // if difference is less than 24 hours set minutes and hours
        else if diffDateComponents.day == 0 {
            let hours = String(NSCalendar.currentCalendar().component([.Hour], fromDate: time!))
            let minutes = String(NSCalendar.currentCalendar().component([.Minute], fromDate: time!))
            if minutes.characters.count == 1 {
                timeString = hours + ":0" + minutes
            }
            else {
                timeString = hours + ":" + minutes
            }
        }
        // set the date entirely
        else {
            let dateFormatter = NSDateFormatter()
            dateFormatter.dateFormat = "dd/M/yyyy"
            timeString = dateFormatter.stringFromDate(time!)
        }
        return timeString
    }
    
    // set money in currency
    func transformToStringCurrency(string: String) -> String {
        var currency = String()
        if string == "€" {
            currency = "EUR"
        }
        else if string == "$" {
            currency = "USD"
        }
        else if string == "£" {
            currency = "GBP"
        }
        
        return currency
    }
    
    //set currency in money
    func getCurrency(currency: String) -> String {
        var setCurrency = String()
        if currency == "EUR" {
            setCurrency = "€"
        }
        else if currency == "USD" {
            setCurrency = "$"
        }
        else if currency == "GBP" {
            setCurrency = "£"
        }
        return setCurrency
    }
    
    
    // Square all image you pass it with size
    func RBSquareImageTo(image: UIImage, size: CGSize) -> UIImage {
        return RBResizeImage(RBSquareImage(image), targetSize: size)
    }
    
    // Square all image you pass it without size
    func RBSquareImage(image: UIImage) -> UIImage {
        let originalWidth  = image.size.width
        let originalHeight = image.size.height
        
        var edge: CGFloat
        if originalWidth > originalHeight {
            edge = originalHeight
        } else {
            edge = originalWidth
        }
        
        let posX = (originalWidth  - edge) / 2.0
        let posY = (originalHeight - edge) / 2.0
        
        let cropSquare = CGRectMake(posX, posY, edge, edge)
        
        let imageRef = CGImageCreateWithImageInRect(image.CGImage, cropSquare);
        return UIImage(CGImage: imageRef!, scale: UIScreen.mainScreen().scale, orientation: image.imageOrientation)
    }
    
    // resize image like you want
    func RBResizeImage(image: UIImage, targetSize: CGSize) -> UIImage {
        let size = image.size
        
        let widthRatio  = targetSize.width  / image.size.width
        let heightRatio = targetSize.height / image.size.height
        
        // Figure out what our orientation is, and use that to form the rectangle
        var newSize: CGSize
        if(widthRatio > heightRatio) {
            newSize = CGSizeMake(size.width * heightRatio, size.height * heightRatio)
        } else {
            newSize = CGSizeMake(size.width * widthRatio,  size.height * widthRatio)
        }
        
        // This is the rect that we've calculated out and this is what is actually used below
        let rect = CGRectMake(0, 0, newSize.width, newSize.height)
        
        // Actually do the resizing to the rect using the ImageContext stuff
        UIGraphicsBeginImageContextWithOptions(newSize, false, 1.0)
        image.drawInRect(rect)
        let newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        
        return newImage
    }
}