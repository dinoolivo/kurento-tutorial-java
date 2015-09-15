
package org.kurento.tutorial.one2onecall.data;

import org.kurento.tutorial.one2onecall.UserCallStatus;

public class User {
    
    private String username;
    
    private UserCallStatus status = UserCallStatus.AVAILABLE;

    public User(String username, UserCallStatus status) {
        this.username = username;
        this.status = status;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserCallStatus getStatus() {
        return status;
    }

    public void setStatus(UserCallStatus status) {
        this.status = status;
    }
    
    
}
