/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kurento.tutorial.one2onecall.controllers;

import java.util.Collection;
import org.kurento.tutorial.one2onecall.OverlayElement;
import org.kurento.tutorial.one2onecall.OverlayManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OverlayController {
    
    @Autowired
    private OverlayManager overlayManager;
    
    @RequestMapping("/overlays")
    public Collection<OverlayElement> overlays() {
        return overlayManager.getAvailableElements();
    }
}
