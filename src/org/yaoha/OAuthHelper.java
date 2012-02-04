package org.yaoha;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

public class OAuthHelper {
    
    private OAuthProvider OSMprovider;
    private OAuthConsumer OSMconsumer;

    private String mCallbackUrl = "osm-login://yaoha.org";

    public OAuthHelper(){
        this.OSMconsumer = new CommonsHttpOAuthConsumer("LXhdgmfvvoGRmVCc0EPZajUS8458AXYZ2615f9hs", "ZTfY5iYZ8Lszgy6DtRh0b258qciz4aYm1XnMciDi");
        this.OSMprovider = new CommonsHttpOAuthProvider(
                "http://www.openstreetmap.org/oauth/request_token",
                "http://www.openstreetmap.org/oauth/access_token",
                "http://www.openstreetmap.org/oauth/authorize");
        OSMconsumer.setTokenWithSecret("LXhdgmfvvoGRmVCc0EPZajUS8458AXYZ2615f9hs", "ZTfY5iYZ8Lszgy6DtRh0b258qciz4aYm1XnMciDi");
        this.OSMprovider.setOAuth10a(true);
        //mCallbackUrl = (callbackUrl == null ? OAuth.OUT_OF_BAND : callbackUrl);
    }
    public String getRequestToken() throws OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException {
        String authUrl = OSMprovider.retrieveRequestToken(OSMconsumer, mCallbackUrl);
        return authUrl;
    }
    
    public String[] getAccessToken(String verifier) throws OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException {
        this.OSMprovider.retrieveAccessToken(this.OSMconsumer, verifier);
        return new String[] {OSMconsumer.getToken(), OSMconsumer.getTokenSecret()};
    }
}