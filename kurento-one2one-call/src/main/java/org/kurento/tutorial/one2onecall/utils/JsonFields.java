package org.kurento.tutorial.one2onecall.utils;

public class JsonFields {

    public static class User {

        public static final String USERNAME = "username";
        public static final String STATUS = "status";

    }

    public static class Call {

        public static final String FROM = "from";
        public static final String TO = "to";
        public static final String SDP_OFFER = "sdpOffer";
        public static final String SDP_ANSWER = "sdpAnswer";
        public static final String CALL_RESPONSE = "callResponse";
        public static final String START_COMMUNICATION = "startCommunication";
        public static final String STOP_COMMUNICATION = "stopCommunication";
        public static final String OVERLAY_ID = "overlayId";

    }

    public static class Ice {

        public static final String CANDIDATE_METHOD = "iceCandidate";
        public static final String CANDIDATE = "candidate";
        public static final String SDP_MID = "sdpMid";
        public static final String SDP_MLINE_INDEX = "sdpMLineIndex";                          
    }
    
    public static final String RESPONSE = "response";
    

}
