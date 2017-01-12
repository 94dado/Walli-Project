//
//  WebViewController.swift
//  Walli
//
//  Created by Daniele Piergigli on 06/07/16.
//  Copyright © 2016 WalliApp. All rights reserved.
//

import UIKit
import Foundation

class WebViewController: UIViewController {
    
    @IBOutlet weak var WebViewPage: UIWebView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // start web view of our site
        let URLPath = NSURL(string: "https://walli.ddns.net")
        let request = NSURLRequest(URL: URLPath!)
        WebViewPage?.loadRequest(request)
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
}