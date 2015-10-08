package org.kurento.tutorial.one2onecall;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.kurento.client.FaceOverlayFilter;
import org.kurento.client.Filter;
import org.kurento.client.MediaPipeline;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OverlayManager {
    
    public static final String HAT_TYPE = "Hat";
    public static final String GLASSES_TYPE = "Glasses";
 
    @Value("${app.server.url}")
    private String serverUrl;
    
    private static final OverlayElement DEFAULT_OVERLAY_ELEMENT =new OverlayElement("/img/mario-wings.png", 1,HAT_TYPE,new OverlayElement.OverlayImageProps(-0.35F, -1.2F, 1.6F, 1.6F));
    private static final Map<Integer,OverlayElement> overlayElements = new HashMap<Integer,OverlayElement>(){{
        put(1,DEFAULT_OVERLAY_ELEMENT);   
        put(2,new OverlayElement("/img/occhiali.png", 2,GLASSES_TYPE,new OverlayElement.OverlayImageProps(0F, 0.3F, 1F, 0.4F)));   
        put(3,new OverlayElement("/img/rainy_cloud.png", 3,HAT_TYPE,new OverlayElement.OverlayImageProps(0.35F, -1.2F, 1.6F, 1.6F)));   
        put(4,new OverlayElement("/img/eyes_angry.png", 4,GLASSES_TYPE,new OverlayElement.OverlayImageProps(0F, 0.15F, 1F, 0.4F)));   
        put(5,new OverlayElement("/img/eyes_female.png", 5,GLASSES_TYPE,new OverlayElement.OverlayImageProps(0F, 0.15F, 1F, 0.4F)));      
    }};
    
    
    public Filter getFilterFromOverlayId(MediaPipeline pipeline,int overlayId){
        FaceOverlayFilter filter = new FaceOverlayFilter.Builder(pipeline).build();
        OverlayElement overlayElement = overlayElements.getOrDefault(overlayId,DEFAULT_OVERLAY_ELEMENT);
        filter.setOverlayedImage(serverUrl+overlayElement.getUrl(), overlayElement.oip.getxOffset(), overlayElement.oip.getyOffset(), overlayElement.oip.getWidth(), overlayElement.oip.getHeight());
        return filter;
    }
    
    public Collection<OverlayElement> getAvailableElements(){
        return overlayElements.values();
    }
}
