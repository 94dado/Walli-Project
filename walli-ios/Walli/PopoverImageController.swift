//
//  PopoverImageController.swift
//  Walli
//
//  Created by Daniele Piergigli on 06/07/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import Foundation
import UIKit

protocol PopoverImageDelegate
{
    func PopoverImageResponse(image: UIImage)
}

class PopoverImageController: UIViewController, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    
    var imagePicker = UIImagePickerController()
    var delegateImage: PopoverImageDelegate?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        imagePicker.delegate = self
    }
    
    // get the photo from the camera
    @IBAction func GetCamera(sender: UIButton) {
        imagePicker.sourceType = UIImagePickerControllerSourceType.Camera
        presentViewController(imagePicker, animated: true, completion: nil)
    }
    
    // get the photo from the image library
    @IBAction func GetPhotoLibrary(sender: UIButton) {
        imagePicker.sourceType = UIImagePickerControllerSourceType.PhotoLibrary
        presentViewController(imagePicker, animated: true, completion: nil)
    }
    
    // store the image
    func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : AnyObject]) {
        self.dismissViewControllerAnimated(true, completion: nil)
        delegateImage?.PopoverImageResponse((info[UIImagePickerControllerOriginalImage] as? UIImage)!)
        self.dismissViewControllerAnimated(true, completion: nil)
    }
}