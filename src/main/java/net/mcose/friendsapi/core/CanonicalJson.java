package net.mcose.friendsapi.core;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stable JSON stringifier used before hashing/signing friends files.
 *
 * org.json is convenient but its object key iteration is not a contract. The
 * backport signs a byte-for-byte representation, so we sort keys recursively
 * and avoid whitespace to make the signature portable across JVMs.
 */
public final class CanonicalJson {
    private CanonicalJson() {
    }

    public static String stringify(Object value) {
        if (value == null || value == JSONObject.NULL) {
            return "null";
        }
        if (value instanceof JSONObject) {
            JSONObject object = (JSONObject) value;
            List<String> keys = new ArrayList<String>();
            java.util.Iterator<String> iterator = object.keys();
            while (iterator.hasNext()) {
                keys.add(iterator.next());
            }
            Collections.sort(keys);
            StringBuilder out = new StringBuilder();
            out.append('{');
            for (int i = 0; i < keys.size(); i++) {
                if (i > 0) {
                    out.append(',');
                }
                String key = keys.get(i);
                out.append(JSONObject.quote(key)).append(':').append(stringify(object.opt(key)));
            }
            out.append('}');
            return out.toString();
        }
        if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            StringBuilder out = new StringBuilder();
            out.append('[');
            for (int i = 0; i < array.length(); i++) {
                if (i > 0) {
                    out.append(',');
                }
                out.append(stringify(array.opt(i)));
            }
            out.append(']');
            return out.toString();
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        return JSONObject.quote(String.valueOf(value));
    }
}
