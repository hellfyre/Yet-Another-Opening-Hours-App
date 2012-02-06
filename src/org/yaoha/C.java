/*
 *  This file is part of YAOHA.
 *
 *  YAOHA is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YAOHA is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YAOHA.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Copyright 2012 Stefan Hobohm, Lutz Reinhardt, Matthias Uschok
 *
 */

package org.yaoha;


public class C {
	
	public static final String TAG = "Yaoha";

	public static final String CONSUMER_KEY 	= "LXhdgmfvvoGRmVCc0EPZajUS8458AXYZ2615f9hs";
	public static final String CONSUMER_SECRET 	= "ZTfY5iYZ8Lszgy6DtRh0b258qciz4aYm1XnMciDi";

	public static final String SCOPE 			= "http://http://www.openstreetmap.org/";
	public static final String REQUEST_URL 		= "http://www.openstreetmap.org/oauth/request_token";
	public static final String ACCESS_URL 		= "http://www.openstreetmap.org/oauth/access_token";  
	public static final String AUTHORIZE_URL 	= "http://www.openstreetmap.org/oauth/authorize";
	
	//public static final String GET_CONTACTS_FROM_GOOGLE_REQUEST 		= "https://www.google.com/m8/feeds/contacts/default/full?alt=json";
	
	public static final String ENCODING 		= "UTF-8";
	
	public static final String	OAUTH_CALLBACK_SCHEME	= "osm-login";
	public static final String	OAUTH_CALLBACK_HOST		= "yaoha.org";
	public static final String	OAUTH_CALLBACK_URL		= OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;
	public static final String	APP_NAME                = "Yaoha";

}
