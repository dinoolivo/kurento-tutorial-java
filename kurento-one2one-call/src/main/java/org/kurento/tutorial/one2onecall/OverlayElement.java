package org.kurento.tutorial.one2onecall;


public class OverlayElement {
    private String url;
    private int id;
    private String type;
    public transient OverlayImageProps oip;

    public OverlayElement(String url, int id, String type,OverlayImageProps oip) {
        this.url = url;
        this.id = id;
        this.type = type;
        this.oip = oip;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    
    public static class OverlayImageProps{
        private float xOffset;
        private float yOffset;
        private float width;
        private float height;

        public OverlayImageProps(float xOffset, float yOffset, float width, float height) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.width = width;
            this.height = height;
        }
        
        public float getxOffset() {
            return xOffset;
        }

        public void setxOffset(float xOffset) {
            this.xOffset = xOffset;
        }

        public float getyOffset() {
            return yOffset;
        }

        public void setyOffset(float yOffset) {
            this.yOffset = yOffset;
        }

        public float getWidth() {
            return width;
        }

        public void setWidth(float width) {
            this.width = width;
        }

        public float getHeight() {
            return height;
        }

        public void setHeight(float height) {
            this.height = height;
        }

    }
}
