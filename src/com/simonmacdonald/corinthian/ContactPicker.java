package com.simonmacdonald.corinthian;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

public class ContactPicker extends Plugin {
    private static final int PICK_CONTACT = 8974;
    private static final String LOG_TAG = "ContactPicker";
    private static final String _ID = "_id";
    private static final String VALUE = "data1";
    private static final String TYPE = "data2";
    public String callbackId;

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action        The action to execute.
     * @param args          JSONArry of arguments for the plugin.
     * @param callbackId    The callback id used when calling back into JavaScript.
     * @return              A PluginResult object with a status and message.
     */
    @Override
    public PluginResult execute(String action, JSONArray args, String callbackId) {
        this.callbackId = callbackId;

        JSONObject options = args.optJSONObject(0);

        if (action.equals("choose")) {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            this.ctx.startActivityForResult((Plugin)this, intent, PICK_CONTACT);
        }
        else {
            return new PluginResult(PluginResult.Status.INVALID_ACTION);
        }
        PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
        r.setKeepCallback(true);
        return r;
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
      switch (reqCode) {
          case PICK_CONTACT: {
              if (resultCode==Activity.RESULT_OK && data!=null && data.getData()!=null) {
                  Uri contactData = data.getData();
                  try {
                      JSONObject contact = createContact(contactData);
                      this.success(new PluginResult(PluginResult.Status.OK, contact), this.callbackId);
                  } catch (JSONException e) {
                      // this should never happen
                  }
              }
              break;
          }
      }
    }

    private JSONObject createContact(Uri contactData) throws JSONException {
        JSONObject contact = new JSONObject();

        Cursor c = this.cordova.getActivity().managedQuery(contactData, null, null, null, null);
        if (c.moveToFirst()) {
//            Log.d(LOG_TAG, "Column count = " + c.getColumnCount());
//            for (int i = 0; i < c.getColumnCount(); i++) {
//                Log.d(LOG_TAG, c.getColumnName(i) + " = " + c.getString(i));
//            }
            String id = c.getString(c.getColumnIndex(ContactsContract.Data._ID));
            contact.put("id", id);
            //contact.put("rawId", c.getString(c.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID)));
            contact.put("displayName", c.getString(c.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)));
            contact.put("name", queryName(id));
            if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                contact.put("phoneNumbers", entryQuery(id, ContactsContract.CommonDataKinds.Phone.CONTENT_URI, ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
            }
            contact.put("emails", entryQuery(id, ContactsContract.CommonDataKinds.Email.CONTENT_URI, ContactsContract.CommonDataKinds.Email.CONTACT_ID));
            contact.put("addresses", queryAddress(id));
            contact.put("organizations", queryOrganization(id));
            contact.put("ims", queryIm(id));
            contact.put("note", queryNote(id));
            contact.put("nickname", queryNickname(id));
            contact.put("urls", queryWebsite(id));
            contact.put("birthday", queryBirthday(id));
            contact.put("photos", queryPhoto(id));

        }
        return contact;
    }

    private JSONObject queryName(String id) throws JSONException {
        JSONObject name = new JSONObject();
        String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] whereParams = new String[]{id,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
        Cursor c = this.cordova.getActivity().getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, where, whereParams, null);
        if (c.moveToFirst()) {
            String familyName = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
            String givenName = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
            String middleName = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));
            String honorificPrefix = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PREFIX));
            String honorificSuffix = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.SUFFIX));

            // Create the formatted name
            StringBuffer formatted = new StringBuffer("");
            if (honorificPrefix != null) { formatted.append(honorificPrefix + " "); }
            if (givenName != null) { formatted.append(givenName + " "); }
            if (middleName != null) { formatted.append(middleName + " "); }
            if (familyName != null) { formatted.append(familyName + " "); }
            if (honorificSuffix != null) { formatted.append(honorificSuffix + " "); }

            name.put("familyName", familyName);
            name.put("givenName", givenName);
            name.put("middleName", middleName);
            name.put("honorificPrefix", honorificPrefix);
            name.put("honorificSuffix", honorificSuffix);
            name.put("formatted", formatted);
        }
        c.close();
        return name;
    }

    private String queryBirthday(String id) {
        String birthday = null;
        String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ? AND "
            + ContactsContract.CommonDataKinds.Event.TYPE + " = ?";
        String[] whereParams = new String[]{id,
                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
                new String("" + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)};
        Cursor c = this.cordova.getActivity().getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, where, whereParams, null);
        if (c.moveToFirst()) {
            birthday = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
        }
        c.close();
        return birthday;
    }

    private String queryNickname(String id) {
        String nickname = null;
        String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] whereParams = new String[]{id,
        ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE};
                Cursor noteCur = this.cordova.getActivity().getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, where, whereParams, null);
        if (noteCur.moveToFirst()) {
            nickname = noteCur.getString(noteCur.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME));
        }
        noteCur.close();
        return nickname;
    }

    private JSONArray queryPhoto(String id) throws JSONException {
        JSONArray photos = new JSONArray();
        String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] whereParams = new String[]{id,
            ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE};
        Cursor cursor = this.cordova.getActivity().getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                null, where, whereParams, null);
        while (cursor.moveToNext()) {
            JSONObject photo = new JSONObject();
            photo.put("id", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo._ID)));
            photo.put("pref", false);
            photo.put("type", "url");
            Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, (new Long(id)));
            Uri photoUri = Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
            photo.put("value", photoUri.toString());
            photos.put(photo);
       }
        cursor.close();
        return photos;
    }

    private JSONArray queryWebsite(String id) throws JSONException {
        JSONArray websites = new JSONArray();
        String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] whereParams = new String[]{id,
            ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE};
        Cursor cursor = this.cordova.getActivity().getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                null, where, whereParams, null);
        while (cursor.moveToNext()) {
            JSONObject website = new JSONObject();
            website.put("id", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Website._ID)));
            website.put("pref", false);
            website.put("value", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.DATA)));
            website.put("type", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.TYPE)));
            websites.put(website);
       }
        cursor.close();
        return websites;
    }

    private String queryNote(String id) {
        String note = null;
        String noteWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] noteWhereParams = new String[]{id,
        ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};
                Cursor noteCur = this.cordova.getActivity().getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, noteWhere, noteWhereParams, null);
        if (noteCur.moveToFirst()) {
            note = noteCur.getString(noteCur.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
        }
        noteCur.close();
        return note;
    }

    private JSONArray queryOrganization(String id) throws JSONException {
        JSONArray orgs = new JSONArray();
        String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] whereParams = new String[]{id,
            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE};
        Cursor c = this.cordova.getActivity().getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                null, where, whereParams, null);
        while (c.moveToNext()) {
            JSONObject organization = new JSONObject();
            organization.put("id", c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Organization._ID)));
            organization.put("pref", false); // Android does not store pref attribute
            organization.put("type", getOrgType(c.getInt(c.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TYPE))));
            organization.put("department", c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DEPARTMENT)));
            organization.put("name", c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY)));
            organization.put("title", c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE)));
            orgs.put(organization);
       }
        c.close();
        return orgs;
    }

    private JSONArray queryAddress(String id) throws JSONException {
        JSONArray addresses = new JSONArray();
        String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] whereParams = new String[]{id,
            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE};
        Cursor c = this.cordova.getActivity().getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                null, where, whereParams, null);
        while (c.moveToNext()) {
            JSONObject address = new JSONObject();
            address.put("id", c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal._ID)));
            address.put("pref", false); // Android does not store pref attribute
                  address.put("type", getAddressType(c.getInt(c.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TYPE))));
            address.put("formatted", c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS)));
            address.put("streetAddress", c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET)));
            address.put("locality", c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)));
            address.put("region", c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION)));
            address.put("postalCode", c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)));
            address.put("country", c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY)));
            addresses.put(address);
       }
        c.close();
        return addresses;
    }

    private JSONArray queryIm(String id) throws JSONException {
        JSONArray ims = new JSONArray();
        String imWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] imWhereParams = new String[]{id,
            ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE};
        Cursor imCur = this.cordova.getActivity().getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                null, imWhere, imWhereParams, null);
        while (imCur.moveToNext()) {
            JSONObject im = new JSONObject();
            im.put("id", imCur.getString(imCur.getColumnIndex(ContactsContract.CommonDataKinds.Im._ID)));
            im.put("pref", false);
            im.put("value", imCur.getString(imCur.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA)));
            im.put("type", imCur.getString(imCur.getColumnIndex(ContactsContract.CommonDataKinds.Im.TYPE)));
            ims.put(im);
       }
        imCur.close();
        return ims;
    }

    private JSONArray entryQuery(String id, Uri contentUri, String contactId) throws JSONException {
        JSONArray entries = new JSONArray();
        Cursor c = this.cordova.getActivity().getContentResolver().query(
            contentUri,
            null,
            contactId +" = ?",
            new String[]{id}, null);

        while (c.moveToNext()) {
            JSONObject entry = new JSONObject();
            entry.put("id", c.getString(c.getColumnIndex(_ID)));
            entry.put("pref", false);
            entry.put("value", c.getString(c.getColumnIndex(VALUE)));
            entry.put("type", getPhoneType(c.getInt(c.getColumnIndex(TYPE))));
            entries.put(entry);
        }
        c.close();

        return entries;
    }

    /**
     * getPhoneType converts an Android phone type into a string
     * @param type
     * @return phone type as string.
     */
    private String getPhoneType(int type) {
      String stringType;
      switch (type) {
      case ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM:
        stringType = "custom";
        break;
      case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
        stringType = "home fax";
        break;
      case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
        stringType = "work fax";
        break;
      case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
        stringType = "home";
        break;
      case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
        stringType = "mobile";
        break;
      case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER:
        stringType = "pager";
        break;
      case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
        stringType = "work";
        break;
      case ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK:
        stringType = "callback";
        break;
      case ContactsContract.CommonDataKinds.Phone.TYPE_CAR:
        stringType = "car";
        break;
      case ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN:
        stringType = "company main";
        break;
      case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX:
        stringType = "other fax";
        break;
      case ContactsContract.CommonDataKinds.Phone.TYPE_RADIO:
        stringType = "radio";
        break;
      case ContactsContract.CommonDataKinds.Phone.TYPE_TELEX:
        stringType = "telex";
        break;
      case ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD:
        stringType = "tty tdd";
        break;
      case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
        stringType = "work mobile";
        break;
      case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER:
        stringType = "work pager";
        break;
      case ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT:
        stringType = "assistant";
        break;
      case ContactsContract.CommonDataKinds.Phone.TYPE_MMS:
        stringType = "mms";
        break;
      case ContactsContract.CommonDataKinds.Phone.TYPE_ISDN:
        stringType = "isdn";
        break;
      case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
      default:
        stringType = "other";
        break;
      }
      return stringType;
    }

    /**
     * getPhoneType converts an Android phone type into a string
     * @param type
     * @return phone type as string.
     */
    private String getAddressType(int type) {
        String stringType;
        switch (type) {
            case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME:
                stringType = "home";
                break;
            case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK:
                stringType = "work";
                break;
            case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER:
            default:
                stringType = "other";
                break;
        }
        return stringType;
    }

    /**
     * getOrgType converts an Android organization type into a string
     * @param type
     * @return organization type as string.
     */
    private String getOrgType(int type) {
        String stringType;
        switch (type) {
            case ContactsContract.CommonDataKinds.Organization.TYPE_CUSTOM:
                stringType = "custom";
                break;
            case ContactsContract.CommonDataKinds.Organization.TYPE_WORK:
                stringType = "work";
                break;
            case ContactsContract.CommonDataKinds.Organization.TYPE_OTHER:
            default:
                stringType = "other";
                break;
        }
        return stringType;
    }
}
