/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.2 $
 * $Date: 2007/12/09 17:45:17 $
 */

var     request = null;

/**
 * Make an AJAX style request to a webservice. It is expected that the
 * response will be in the form of XML data.
 */
function httpRequest(reqType, url, asynch, respHandle) {
    if (window.XMLHttpRequest) {
        request = new XMLHttpRequest();
    } else if (window.ActiveXObject) {
        request = new ActiveXObvject("Msxml2.XMLHTTP");
        if (!request) {
            request = new ActiveXObject("Microsoft.XMLHTTP");
        }
    }
    if (request) {
        if (reqType.toLowerCase() != "post") {
            initReq(reqType, url, asynch, respHandle);
        } else {
            var     args = arguments[4];
            if (args != null && args.length > 0) {
                initReq(reqType, url, asynch, respHandle, args);
            }
        }
    } else {
        alert("Ajax support is not available");
    }
}

/**
 * Initialise a request object that is already constructed.
 */
function initReq(reqType, url, bool, respHandle) {
    if (request == null) {
        alert("initReq: No request object");
        return;
    }
    try {
        request.onreadystatechange = respHandle;
        request.open(reqType, url, bool);
        if (reqType.toLowerCase() == "post") {
            request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            request.send(arguments[4]);
        } else {
            request.send(null);
        }
    } catch (e) {
        alert("Cannot connect to server ("+e.message+")");
    }
}
