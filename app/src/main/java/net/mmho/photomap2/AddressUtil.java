package net.mmho.photomap2;

import android.content.Context;
import android.location.Address;

public class AddressUtil {
    static public String getTitle(Address address, Context context){
        StringBuilder builder = new StringBuilder();
        final String separator = context.getString(R.string.address_separator);
        // TODO: change address order with Language setting.

        if(address.getAdminArea()!=null){
            builder.append(address.getAdminArea());
            builder.append(separator);
        }
        if(address.getSubAdminArea()!=null){
            builder.append(address.getSubAdminArea());
            builder.append(separator);
        }
        if(address.getLocality()!=null){
            builder.append(address.getLocality());
        }
        if(builder.length()==0){
            builder.append((address.getFeatureName()));
        }

        return new String(builder);
    }

    static private String removePostalCode(String source){
        return source.replaceFirst("〒[0-9¥-]*","");
    }

    static public String getDescription(Address address){
        String description;
        if(address.getMaxAddressLineIndex()>0){
            description = address.getAddressLine(1);
        }
        else{
            description = address.getAddressLine(0);
        }
        return removePostalCode(description);
    }
}
