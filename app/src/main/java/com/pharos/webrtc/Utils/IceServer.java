package com.pharos.webrtc.Utils;

public class IceServer {

    public String url;

    public String username;

    public String credential;

    public IceServer() {
    }

    public IceServer(String url, String username, String credential) {
        this.url = url;
        this.username = username;
        this.credential = credential;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    @Override
    public String toString() {
        return "IceServer{" +
                "url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", credential='" + credential + '\'' +
                '}';
    }
}
