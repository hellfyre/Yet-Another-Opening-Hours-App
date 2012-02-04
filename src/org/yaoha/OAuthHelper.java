package org.yaoha;

import android.widget.Toast;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

public class OAuthHelper {
    
    private OAuthProvider OSMprovider;
    private OAuthConsumer OSMconsumer;

    private String mCallbackUrl = "osm-login://yaoha.org";

    public OAuthHelper(){
        this.OSMprovider = YaohaActivity.getProvider();
        this.OSMconsumer = YaohaActivity.getConsumer();
        OSMconsumer.setTokenWithSecret(stringtoUTF8("LXhdgmfvvoGRmVCc0EPZajUS8458AXYZ2615f9hs"), stringtoUTF8("ZTfY5iYZ8Lszgy6DtRh0b258qciz4aYm1XnMciDi"));
        OSMprovider.setOAuth10a(true);
        //mCallbackUrl = (callbackUrl == null ? OAuth.OUT_OF_BAND : callbackUrl);
    }
    public String getRequestToken() throws OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException {
        String authUrl = OSMprovider.retrieveRequestToken(OSMconsumer, mCallbackUrl);
        return authUrl;
    }
    
    public String[] getAccessToken(String verifier) throws OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException {
        OSMprovider.retrieveAccessToken(this.OSMconsumer, stringtoUTF8(verifier));
        return new String[] {OSMconsumer.getToken(), OSMconsumer.getTokenSecret()};
    }
    
    public String stringtoUTF8(String text){
        String s;
        try {
            byte[] b = text.getBytes("UTF-8");
            s = new String(b, "UTF-8");
        } catch (Exception e) {
            String trans = e.getMessage();
            String bla = trans;
            return "";
        }
        return s;
    }
}